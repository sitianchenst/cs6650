package part1;

import io.swagger.client.ApiException;
import io.swagger.client.api.SwipeApi;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import part2.GetThread;

public class Main {

  private static final Integer NUM_TOTAL_SWIPE_REQUESTS = 500000;
  private static final Integer NUM_TOTAL_CONSUMER_THREADS = 1;
  private static final Integer NUM_CONSUMERS_EACH_THREAD = 100;

  private static final Integer NUM_TOTAL_PRODUCER_THREADS = 1;
  private static final Integer NUM_PRODUCERS_EACH_THREAD = 100;
  private static final String BASE_PATH_LOCAL = "http://localhost:8080/assignment2_server_war_exploded/";
  private static final String BASE_PATH_REMOTE = "http://54.190.56.78:8080/assignment1_server_war/";
  private static final String BASE_PATH_REMOTE_SPRING = "http://54.190.56.78:8082";
  private static final String BASE_PATH_REMOTE_SPRING_LOCAL = "http://localhost:8082";
  private static AtomicInteger successful_requests;
  private static AtomicInteger unsuccessful_requests;
  private static BlockingQueue<SwipeData> queue;
  private static CountDownLatch countDownLatch;

  public static void main(String[] args) throws ApiException, InterruptedException {

    //Initialize number of successful/unsucessful requests
    //Initialize blocking queue
    successful_requests = new AtomicInteger(0);
    unsuccessful_requests = new AtomicInteger(0);

    queue = new LinkedBlockingQueue<>();
    long startTime = System.currentTimeMillis();
//    producer
    for (int i = 0; i < NUM_TOTAL_PRODUCER_THREADS; i++) {
      new Thread(new Producer(queue, NUM_PRODUCERS_EACH_THREAD)).start();
    }

    countDownLatch = new CountDownLatch(NUM_TOTAL_CONSUMER_THREADS);

    //consumer
    for (int i = 0; i < NUM_TOTAL_CONSUMER_THREADS; i++) {
      SwipeApi swipeApi = new SwipeApi();
      swipeApi.getApiClient().setBasePath(BASE_PATH_LOCAL);
      new Thread(new Consumer(queue, NUM_CONSUMERS_EACH_THREAD, successful_requests,unsuccessful_requests, countDownLatch,  swipeApi)).start();
    }

    countDownLatch.await();
    long endTime = System.currentTimeMillis();
    long wallTime = (endTime - startTime);
    double wallTimeSeconds = wallTime / (double)1000;
    //print all information
    System.out.println("--------------TWINDER SWIPE POST REQUESTS--------------");
//    System.out.println("-------------------SPRING BOOT SERVER------------------");
    System.out.println("Total Number of Requests: " + NUM_TOTAL_SWIPE_REQUESTS);
    System.out.println("Number of Successful Requests Sent: " + successful_requests.get());
    System.out.println("Number of Unsuccessful Requests Sent: " + unsuccessful_requests.get());
    System.out.println("Total Wall Time: " + wallTime + " ms");
    System.out.println("Total Throughput in Requests per Second: " + String.format("%.2f",(double)NUM_TOTAL_SWIPE_REQUESTS / (double)wallTimeSeconds ));
    System.out.println("Number of Threads: "+ NUM_TOTAL_CONSUMER_THREADS );
    System.out.println("Little Law Expected Throughput: " + String.format("%.2f",(double)Math.min(NUM_TOTAL_PRODUCER_THREADS, NUM_TOTAL_CONSUMER_THREADS )/ 0.0338));
    System.out.println("------------------------------------------------------");
    System.out.println("Number of Threads: "+ NUM_TOTAL_CONSUMER_THREADS );
    System.out.println("Little's Law Expected Throughput: " +  String.format("%.2f",(double)Math.min(NUM_TOTAL_PRODUCER_THREADS, NUM_TOTAL_CONSUMER_THREADS )/ 0.0343 ));
    System.out.println("------------------------------------------------------");

  }


}
