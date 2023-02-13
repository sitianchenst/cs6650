package part2;

import io.swagger.client.ApiException;
import io.swagger.client.api.SwipeApi;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.httpclient.HttpStatus;
import part2.SwipeData;

public class ConsumerPart2 implements Runnable{
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
  private Record record;

  public ConsumerPart2(BlockingQueue<SwipeData> bq, int number_req_each_thread,
      AtomicInteger successful_requests,
      AtomicInteger unsuccessful_requests, CountDownLatch countDownLatch,
      SwipeApi swipeApi, Record record) {
    this.bq = bq;
    this.number_req_each_thread = number_req_each_thread;
    this.successful_requests = successful_requests;
    this.unsuccessful_requests = unsuccessful_requests;
    this.countDownLatch = countDownLatch;
    this.swipeApi = swipeApi;
    this.record = record;
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
        long startPost = System.currentTimeMillis();
        int statusCode = swipeApi.swipeWithHttpInfo(swipeData.getSwipeDetails(), swipeData.getLeftOrRight()).getStatusCode();
        if (statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_OK) {
          long endPost = System.currentTimeMillis();

          record.getRecordList().add(new String[]{String.valueOf(startPost), "POST", String.valueOf(endPost - startPost), String.valueOf(statusCode)});
          record.getLatencyList().add(endPost - startPost);
          sucess++;
          return;
        }
        else if ((statusCode / 400) == 1 ||  (statusCode / 500) == 1){
          long endPost = System.currentTimeMillis();
          record.getRecordList().add(new String[]{String.valueOf(startPost), "POST", String.valueOf(endPost - startPost), String.valueOf(statusCode)});
          record.getLatencyList().add(endPost - startPost);
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
