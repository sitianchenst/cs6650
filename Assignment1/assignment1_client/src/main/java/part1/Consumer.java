package part1;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.httpclient.HttpStatus;

public class Consumer implements Runnable{
  private final int RETRY_NUM = 5;
  private BlockingQueue<SwipeData> bq;
//  private String basePath;
  private int number_req_each_thread;
  private AtomicInteger successful_requests;
  private int sucess;
  private int unsucess;
  private AtomicInteger unsuccessful_requests;
  private CountDownLatch countDownLatch;
  private SwipeApi swipeApi;

  public Consumer(BlockingQueue<SwipeData> bq, int number_req_each_thread,
      AtomicInteger successful_requests,
      AtomicInteger unsuccessful_requests, CountDownLatch countDownLatch,
      SwipeApi swipeApi) {
    this.bq = bq;
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
        swipePost(swipeApi, bq.take());
      } catch (InterruptedException | ApiException e) {
        e.printStackTrace();
      }
    }
    successful_requests.getAndAdd(sucess);
    unsuccessful_requests.getAndAdd(unsucess);
    countDownLatch.countDown();

  }

  private void swipePost(SwipeApi swipeApi, SwipeData swipeData) throws ApiException {
    int retry = 0;
    while (retry < RETRY_NUM) {
      try {

        int statusCode = swipeApi.swipeWithHttpInfo(swipeData.getSwipeDetails(), swipeData.getLeftOrRight()).getStatusCode();
        if (statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_OK) {
          sucess++;
          return;
        }
        else if ((statusCode / 400) == 1 ||  (statusCode / 500) == 1){
          retry++;
        }
      } catch (ApiException e) {
        System.out.println("Fail to send Swipe Post");
        e.printStackTrace();
        retry++;
      }
    }
    unsucess++;
  }





}
