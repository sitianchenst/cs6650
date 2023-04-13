import com.google.gson.Gson;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "MatchServlet", value = "/MatchServlet")
public class MatchServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");
    String urlPath = request.getPathInfo();
    PrintWriter out = response.getWriter();
    Gson gson = new Gson();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing paramterers");
      return;
    }

    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);//404
      ResponseMessage responseMessage404 = new ResponseMessage("Missing paramterers");
      out.print(gson.toJson(responseMessage404));
      out.flush();
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      response.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts`
      response.getWriter().write("It works!");
    }


  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

  }

  private boolean isUrlValid(String[] urlPath) {
      // TODO: validate the request url path according to the API spec
      // urlPath  = "/1/seasons/2019/day/1/skier/123"
      // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
      return true;
  }
}
