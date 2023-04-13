import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import models.ResponseMessage;
import models.SwipeData;
import models.SwipeDetails;
import org.json.JSONObject;
import rmqpool.RMQChannelFactory;
import rmqpool.RMQChannelPool;

@WebServlet(name = "TwinderServlet", value = "/TwinderServlet")
public class TwinderServlet extends HttpServlet {

    private static final int NUM_CHANNALS = 50;
    private static final String RMQ_LOCAL_HOST = "localhost";
    private static final String RMQ_REMOTE_HOST = "52.10.201.101";//public 44.242.45.40
    private static RMQChannelPool rmqChannelPool;

    @Override
    public void init() throws ServletException{
        super.init();
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(RMQ_REMOTE_HOST);
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("password");
        connectionFactory.setPort(5672);

//        final Connection connection;
        try {
            final Connection connection = connectionFactory.newConnection();
            RMQChannelFactory rmqChannelFactory = new RMQChannelFactory(connection);
            rmqChannelPool = new RMQChannelPool(NUM_CHANNALS, rmqChannelFactory);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

        try {
            declareExchange("exchange", "fanout");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String urlPath = request.getPathInfo();
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);//404
            ResponseMessage responseMessage404 = new ResponseMessage("Missing paramterers");
            out.print(gson.toJson(responseMessage404));
            out.flush();
            return;
        }

        String postBody = getPostBody(request);
        if (isValidPostUrl(urlPath) && isValidPostBody(postBody, gson)) {
            SwipeData swipeData = generateMessage(urlPath, postBody, gson);
            sendMessageToQueue(gson.toJson(swipeData));

            response.setStatus(HttpServletResponse.SC_CREATED);//201
            ResponseMessage responseMessage201 = new ResponseMessage("Write successful");
            out.print(gson.toJson(responseMessage201));
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//400
            ResponseMessage responseMessage400 = new ResponseMessage("Invalid inputs");
            out.print(gson.toJson(responseMessage400));
        }
        out.flush();
        out.close();
    }

    private String getPostBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        String s = null;

        try {
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }


    // uralPath = /{leftorright}/
    private boolean isValidPostUrl(String urlPath) {
        String regex_left = "\\/left\\/";
        String regex_right ="\\/right\\/";
        return urlPath.matches(regex_left) || urlPath.matches(regex_right);
    }

    private boolean isValidPostBody(String postBody, Gson gson) {
        if (postBody == null) return false;

        SwipeDetails swipe = (SwipeDetails) gson.fromJson(postBody, SwipeDetails.class);
        if (swipe == null || swipe.getSwipee() == null || swipe.getSwiper() == null || swipe.getComment() == null){
            return false;
        }

        int swipee = Integer.parseInt(swipe.getSwipee());
        int swiper = Integer.parseInt(swipe.getSwiper());

        if (swipee < 1 || swipee > 1000000 || swiper < 1 || swiper > 5000 || swipe.getComment().length() > 256) {
            return false;
        }

        return true;
    }

    private SwipeData generateMessage(String urlPath, String postBody, Gson gson) {
        SwipeDetails swipe = (SwipeDetails) gson.fromJson(postBody, SwipeDetails.class);
        String[] urlParts = urlPath.split("/");
        SwipeData swipeData = new SwipeData(swipe, urlParts[1]);
        return swipeData;
    }

    private void declareExchange(String EXCHANGE_NAME, String EXCHANGE_TYPE) throws Exception {
        Channel channel = rmqChannelPool.borrowObject();
        channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE, true);
        rmqChannelPool.returnObject(channel);
    }

    private boolean sendMessageToQueue(String message) {
        try {
            Channel channel = rmqChannelPool.borrowObject();
            channel.basicPublish("exchange", "persistent_db", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
            // return channel to the pool
            rmqChannelPool.returnObject(channel);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(TwinderServlet.class.getName()).log(Level.INFO, null, e);
            return false;
        }
    }
}
