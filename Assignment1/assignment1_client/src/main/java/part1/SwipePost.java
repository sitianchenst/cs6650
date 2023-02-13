package part1;

import io.swagger.client.ApiException;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.httpclient.HttpStatus;

public class SwipePost implements Runnable{

  private final String[] leftOrRight = new String[]{"left", "right"};
  private final int COMMENT_SIZE = 256;
  private final int LEFT_LIMIT = 48;
  private final int RIGHT_LIMIT = 122;

//  private BlockingQueue<SwipeData> bq;
  private int number_req_each_thread;

  private final int RETRY_NUM = 5;
//  private BlockingQueue<SwipeData> bq;
//  private int number_req_each_thread;
  private AtomicInteger successful_requests;
  private int sucess;
  private int unsucess;
  private AtomicInteger unsuccessful_requests;
  private CountDownLatch countDownLatch;
  private SwipeApi swipeApi;

  public SwipePost(int number_req_each_thread,
      AtomicInteger successful_requests,
      AtomicInteger unsuccessful_requests, CountDownLatch countDownLatch,
      SwipeApi swipeApi) {
    this.number_req_each_thread = number_req_each_thread;
    this.successful_requests = successful_requests;
    this.unsuccessful_requests = unsuccessful_requests;
    this.countDownLatch = countDownLatch;
    this.swipeApi = swipeApi;
  }


  @Override
  public void run() {
    for (int i = 0; i < number_req_each_thread; i++) {
      try {
        swipePost(swipeApi,createSwipeData());
      } catch (ApiException e) {
        e.printStackTrace();
      }
    }

    successful_requests.getAndAdd(sucess);
    unsuccessful_requests.getAndAdd(unsucess);
    countDownLatch.countDown();

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
    StringBuilder buffer = new StringBuilder(size);
    for (int i = 0; i < ThreadLocalRandom.current().nextInt(COMMENT_SIZE); i++) {
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



  private void swipePost(SwipeApi swipeApi, SwipeData swipeData) throws ApiException {
    int retry = 0;
    while (retry < RETRY_NUM) {
      try {

        int statusCode = swipeApi.swipeWithHttpInfo(swipeData.getSwipeDetails(), swipeData.getLeftOrRight()).getStatusCode();
        if (statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_OK) {
//          successful_requests.getAndIncrement();
          sucess++;
          return;
        }
        else if ((statusCode / 400) == 1 ||  (statusCode / 500) == 1){
          System.out.println("Do retry");
          retry++;
        }
      } catch (ApiException e) {
        System.out.println("Fail to send Swipe Post");
        e.printStackTrace();
        retry++;
      }
    }
//    unsuccessful_requests.getAndIncrement();
    unsucess++;
  }
}


