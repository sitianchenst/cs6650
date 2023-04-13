package part1;

import io.swagger.client.model.SwipeDetails;

public class SwipeData {
  private SwipeDetails swipeDetails;
  private String leftOrRight;


  public SwipeData(SwipeDetails swipeDetails, String leftOrRight) {
    this.swipeDetails = swipeDetails;
    this.leftOrRight = leftOrRight;
  }

  public SwipeDetails getSwipeDetails() {
    return swipeDetails;
  }

  public void setSwipeDetails(SwipeDetails swipeDetails) {
    this.swipeDetails = swipeDetails;
  }

  public String getLeftOrRight() {
    return leftOrRight;
  }

  public void setLeftOrRight(String leftOrRight) {
    this.leftOrRight = leftOrRight;
  }
}
