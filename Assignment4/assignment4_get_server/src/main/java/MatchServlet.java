import com.google.gson.Gson;

import dto.Matches;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

import exception.UserNotFoundException;
import models.ResponseMsg;
import models.SwipeMatches;

import service.DynamoService;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@WebServlet(name = "MatchServlet", value = "/matches")
public class MatchServlet extends HttpServlet {

    private static final String DYNAMODB_TABLE_NAME = "SWIPE_MATCH_QUERY";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String urlPath = request.getPathInfo();
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        // check we have a URL!
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
            DynamoService<SwipeMatches> service = new DynamoService<>();
            SwipeMatches swipeMatches = new SwipeMatches(swiper);
            try {
//                service.getDynamoDBItemMatch(ddb, DYNAMODB_TABLE_NAME, swipeMatches);
                service.getDynamoDBItem(ddb, DYNAMODB_TABLE_NAME, swipeMatches);
                Matches matches = new Matches(new ArrayList<>(swipeMatches.getMatchSet()));
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(matches));
            } catch (UserNotFoundException | IllegalAccessException e) {
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
