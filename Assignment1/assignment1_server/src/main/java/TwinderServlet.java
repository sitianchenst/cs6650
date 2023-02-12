import com.google.gson.Gson;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import models.ResponseMessage;
import models.SwipeDetails;
import org.json.JSONObject;

@WebServlet(name = "TwinderServlet", value = "/TwinderServlet")
public class TwinderServlet extends HttpServlet {

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

    JSONObject jsonResponse = new JSONObject();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);//404
      ResponseMessage responseMessage404 = new ResponseMessage("Missing paramterers");
      out.print(new Gson().toJson(responseMessage404));
      out.flush();
      return;
    }

    if (isValidPostUrl(urlPath) && isValidPostBody(request)) {
      //      jsonResponse.put("message", "missing paramterers");
      response.setStatus(HttpServletResponse.SC_CREATED);//201

      ResponseMessage responseMessage201 = new ResponseMessage("Write successful");
      out.print(new Gson().toJson(responseMessage201));
    } else {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//400
      ResponseMessage responseMessage400 = new ResponseMessage("Invalid inputs");
      out.print(new Gson().toJson(responseMessage400));
    }
    out.flush();
    out.close();
  }


  // uralPath = /swipe/{leftorright}/
  private boolean isValidPostUrl(String urlPath) {
    String regex_left = "\\/left\\/";
    String regex_right ="\\/right\\/";
    boolean t1 = urlPath.matches(regex_left);
    boolean t2 = urlPath.matches(regex_right);
    return urlPath.matches(regex_left) || urlPath.matches(regex_right);
  }

  private boolean isValidPostBody(HttpServletRequest request) {
    StringBuilder sb = new StringBuilder();
    Gson gson = new Gson();
    String s = null;

    try {
      while ((s = request.getReader().readLine()) != null) {
        sb.append(s);
      }
      if (sb.toString() == null) return false;

      SwipeDetails swipe = (SwipeDetails) gson.fromJson(sb.toString(), SwipeDetails.class);
      if (swipe == null || swipe.getSwipee() == null || swipe.getSwiper() == null || swipe.getComment() == null){
        return false;
      }

      // TODO: check swipee and swiper are number?

    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }


}
