package part2;

import io.swagger.client.model.SwipeDetails;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class Producer implements Runnable{

  private final String[] leftOrRight = new String[]{"left", "right"};
  private final int COMMENT_SIZE = 256;
  private final int LEFT_LIMIT = 48;
  private final int RIGHT_LIMIT = 122;

  private BlockingQueue<SwipeData> bq;
  private int number_req_each_thread;

  public Producer(BlockingQueue<SwipeData> bq, int number_req_each_thread) {
    this.bq = bq;
    this.number_req_each_thread = number_req_each_thread;
  }

  @Override
  public void run() {
    for (int i = 0; i < number_req_each_thread; i++) {
      try {
        bq.put(createSwipeData());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private SwipeData createSwipeData() {
    SwipeDetails swipeDetails = new SwipeDetails();
//    String swiper = createRandomNumber(1,5000).toString();
    swipeDetails.setSwiper(createRandomNumber(1,5000).toString());
//    String swipee = createRandomNumber(1,1000000).toString();
    swipeDetails.setSwipee(createRandomNumber(1,1000000).toString());
//    String comment = createRandomComment(LEFT_LIMIT, RIGHT_LIMIT, COMMENT_SIZE);
    swipeDetails.setComment(createRandomComment(LEFT_LIMIT, RIGHT_LIMIT, COMMENT_SIZE));
//    String leftOrRight = createRandomLeftOrRight();
    SwipeData swipeData = new SwipeData(swipeDetails, createRandomLeftOrRight());
    return swipeData;
  }

  private String createRandomComment(int left, int right, int size) {
//    Random random = new Random();
    StringBuilder buffer = new StringBuilder(size);
    for (int i = 0; i < ThreadLocalRandom.current().nextInt(COMMENT_SIZE); i++) {
//      int randomLimitedInt = left + (int)
//          (random.nextFloat() * (right - left + 1));
      buffer.append((char) (left + ThreadLocalRandom.current().nextFloat()*(right - left + 1)));
    }
    return buffer.toString();
  }

  private String createRandomLeftOrRight() {
    int idx = ThreadLocalRandom.current().nextInt(2);
    return  leftOrRight[idx];
  }

  private Integer createRandomNumber(int origin, int bound) {
    return ThreadLocalRandom.current().nextInt(origin, bound+1);
  }


}
