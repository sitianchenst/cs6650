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

    private static final int NUM_CHANNALS = 30;
    private static final String RMQ_LOCAL_HOST = "localhost";
    private static final String RMQ_REMOTE_HOST = "52.35.118.136";
    private RMQChannelPool rmqChannelPool;

    @Override
    public void init() throws ServletException{
        super.init();
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("52.35.118.136");

        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("password");
        connectionFactory.setPort(5672);

//        final Connection connection;
        try {
            final Connection connection = connectionFactory.newConnection();
            RMQChannelFactory rmqChannelFactory = new RMQChannelFactory(connection);
            rmqChannelPool = new RMQChannelPool(NUM_CHANNALS, rmqChannelFactory);
            System.out.println("init");
        } catch (IOException | TimeoutException e) {
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
            //      jsonResponse.put("message", "missing paramterers");
            SwipeData swipeData = generateMessage(urlPath, postBody, gson);
//            System.out.println(gson.toJson(swipeData));
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
//        boolean t1 = urlPath.matches(regex_left);
//        boolean t2 = urlPath.matches(regex_right);
        return urlPath.matches(regex_left) || urlPath.matches(regex_right);
    }

    private boolean isValidPostBody(String postBody, Gson gson) {
//        StringBuilder sb = new StringBuilder();
//        Gson gson = new Gson();
//        String s = null;


//            while ((s = request.getReader().readLine()) != null) {
//                sb.append(s);
//            }
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

    //Question: JSONObject or String?
    private boolean sendMessageToQueue(String message) {
        try {
            Channel channel = rmqChannelPool.borrowObject();
            //set up exchange or queue
//            channel.exchangeDeclare("exchange", "fanout");
            channel.basicPublish("exchange", "", null, message.getBytes());
//            channel.queueDeclare("QueueName",false, false, false, null);
            //MessageProperties or not?? MessageProperties.PERSISTENT_TEXT_PLAIN
//            channel.basicPublish("","QueueName", null, message.getBytes(StandardCharsets.UTF_8));

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
