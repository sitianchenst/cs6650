package com.server.spring_server;

import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SwipeController {

  @PostMapping(value = "/swipe/{leftOrRight}")
  public ResponseEntity<String> createSwipe(HttpServletRequest request, @PathVariable String leftOrRight) {

    String urlPath = request.getRequestURL().toString();

    if (urlPath == null || urlPath.isEmpty()) {
      return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).body("Missing parameters");//404
    }

    if (!isValidPostUrl(leftOrRight) || !isValidPostBody(request)) {
      return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body("Invalid inputs");//400
    }


    return ResponseEntity.status(HttpServletResponse.SC_CREATED).body("Write successful");//201

  }

  // uralPath = /swipe/{leftorright}/
  private boolean isValidPostUrl(String leftOrRight) {
    return leftOrRight.equals("left") || leftOrRight.equals("right");
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

    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }




}
