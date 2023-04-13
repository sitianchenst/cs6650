import com.google.gson.Gson;

import dto.MatchStats;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

import exception.UserNotFoundException;
import models.ResponseMsg;
import models.SwipeLikes;
import service.DynamoService;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@WebServlet(name = "StatsServlet", value = "/stats")
public class StatsServlet extends HttpServlet {

    private static final String DYNAMODB_TABLE_NAME = "SWIPE_LIKE";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String urlPath = request.getPathInfo();
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);//404
            ResponseMsg responseMessage404 = new ResponseMsg("Missing paramterers");
            out.print(gson.toJson(responseMessage404));
            out.flush();
            return;
        }

        String[] urlParts = urlPath.split("/");
        if (isUrlValid(urlParts)) {

            String swiper = urlParts[1];
            DynamoDbClient ddb = DynamoService.getDynamoClient();
            DynamoService<SwipeLikes> service = new DynamoService<>();
            try {
                SwipeLikes swipeLikes = new SwipeLikes(swiper);
                service.getDynamoDBItem(ddb, DYNAMODB_TABLE_NAME, swipeLikes);
                MatchStats matchStats = new MatchStats(Integer.valueOf(swipeLikes.getLikes()), Integer.valueOf(swipeLikes.getDislikes()));
                response.setStatus(HttpServletResponse.SC_OK);//201
                out.print(gson.toJson(matchStats));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (UserNotFoundException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);//404
                ResponseMsg responseMessage404 = new ResponseMsg("User Not Found");
                out.print(gson.toJson(responseMessage404));
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//400
            ResponseMsg responseMessage400 = new ResponseMsg("Invalid inputs");
            out.print(gson.toJson(responseMessage400));
        }
        out.flush();
        out.close();
    }

    private boolean isUrlValid(String[] urlParts) {
        // urlPath  = "/123"
        // urlParts = [, 123]
        if (urlParts.length != 2) return false;

        int swiperId = Integer.parseInt(urlParts[1]);
        if (swiperId < 1 || swiperId > 1000000) {
            return false;
        }
        return true;
    }

}
