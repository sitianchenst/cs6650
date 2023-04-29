package part2;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.MatchesApi;
import io.swagger.client.api.StatsApi;
import io.swagger.client.api.SwipeApi;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

  private static final Integer NUM_TOTAL_SWIPE_REQUESTS =1000;
  private static final Integer NUM_TOTAL_CONSUMER_THREADS = 50;
  private static final Integer NUM_CONSUMERS_EACH_THREAD = 20;

  private static final Integer NUM_TOTAL_PRODUCER_THREADS = 50;
  private static final Integer NUM_PRODUCERS_EACH_THREAD = 20;
  private static final String POST_BASE_PATH_LOCAL = "http://localhost:8080/assignment1_server_war_exploded/";
  private static final String POST_BASE_PATH_REMOTE = "http://54.191.139.21:8080/assignment2_server_war/";
  private static final String GET_BASE_PATH_LOCAL = "http://localhost:8080/assignment3_server_war_exploded/";
  private static final String GET_BASE_PATH_REMOTE = "http://44.229.4.157:8080/assignment3_server_war/";
  private static final String GET_BASE_PATH_LB = "http://a3-get-lb-1620590431.us-west-2.elb.amazonaws.com:8080/assignment3_server_war/";
  private static final String BASE_PATH_REMOTE_SPRING = "http://54.190.56.78:8082";
  private static final String BASE_PATH_REMOTE_SPRING_LOCAL = "http://localhost:8082";
  private static final String BASE_PATH_POST_LB = "http://a3-lb-804022318.us-west-2.elb.amazonaws.com/assignment2_server_war/";
  private static AtomicInteger successful_requests_post;
  private static AtomicInteger unsuccessful_requests_post;
  private static AtomicInteger successful_requests_get;
  private static AtomicInteger unsuccessful_requests_get;
  private static BlockingQueue<SwipeData> queue;

  private static CountDownLatch countDownLatch;
  private static Record recordPost;
  private static Record recordGet;

  public static void main(String[] args) throws ApiException, InterruptedException {
    //Initialize number of successful/unsucessful requests
    //Initialize blocking queue
    successful_requests_post = new AtomicInteger(0);
    unsuccessful_requests_post = new AtomicInteger(0);
    successful_requests_get = new AtomicInteger(0);
    unsuccessful_requests_get = new AtomicInteger(0);
    queue = new LinkedBlockingQueue<>();
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    long startTime = System.currentTimeMillis();

    // producer
    System.out.println("--------------POST PRODUCER STARS--------------");
    for (int i = 0; i < NUM_TOTAL_PRODUCER_THREADS; i++) {
      new Thread(new Producer(queue, NUM_PRODUCERS_EACH_THREAD)).start();
    }

    countDownLatch = new CountDownLatch(NUM_TOTAL_CONSUMER_THREADS);

    //consumer
    System.out.println("--------------POST CONSUMER STARS--------------");
    recordPost = new Record();
    for (int i = 0; i < NUM_TOTAL_CONSUMER_THREADS; i++) {
      SwipeApi swipeApi = new SwipeApi(new ApiClient());
      swipeApi.getApiClient().setBasePath(POST_BASE_PATH_REMOTE);

      new Thread(new ConsumerPart2(queue, NUM_CONSUMERS_EACH_THREAD, successful_requests_post,
          unsuccessful_requests_post, countDownLatch, swipeApi, recordPost)).start();
    }

    System.out.println("--------------GET REQUESTS STARS--------------");
    //Get Requests
    ApiClient apiClient = new ApiClient();
    MatchesApi matchesApi = new MatchesApi(apiClient);
    matchesApi.getApiClient().setBasePath(GET_BASE_PATH_LOCAL);
    StatsApi statsApi = new StatsApi(apiClient);
    statsApi.getApiClient().setBasePath(GET_BASE_PATH_LOCAL);
    recordGet = new Record();
    executorService.scheduleAtFixedRate(new GetThread(GET_BASE_PATH_LOCAL, successful_requests_get, unsuccessful_requests_get, recordGet, matchesApi, statsApi),0, 200, TimeUnit.MILLISECONDS);

//    Thread t = new Thread(new GetThread(GET_BASE_PATH_LOCAL, successful_requests_get, unsuccessful_requests_get, recordGet, matchesApi, statsApi));
//    t.start();
//    t.join();


    countDownLatch.await();
//    System.out.println("--------------POST REQUESTS FINISHED--------------");
//    executorService.shutdownNow();
//    System.out.println("--------------GET REQUESTS FINISHED--------------");
//
    long endTime = System.currentTimeMillis();
    long wallTime = (endTime - startTime);
    double wallTimeSeconds = wallTime / (double)1000;
    System.out.println("Number of Producer Threads: "+ NUM_TOTAL_PRODUCER_THREADS );
    System.out.println("Number of Consumer Threads: "+ NUM_TOTAL_CONSUMER_THREADS );
    System.out.println(TimeUnit.MILLISECONDS.toSeconds(wallTime) + "seconds");
    System.out.println("------------------------------------------------------");
//    print all information
    System.out.println("--------------TWINDER SWIPE GET REQUESTS--------------");
//    System.out.println("Number of Successful GET Requests Sent: " + successful_requests_get.get());
//    System.out.println("Number of Unsuccessful GET Requests Sent: " + unsuccessful_requests_get.get());
    DataGenerator dataGeneratorGet = new DataGenerator(recordGet);
    dataGeneratorGet.generateDataGet();
    System.out.println("------------------------------------------------------");
    System.out.println("--------------TWINDER SWIPE POST REQUESTS--------------");
    System.out.println("Total Number of Requests: " + NUM_TOTAL_SWIPE_REQUESTS);
    System.out.println("Number of Successful Requests Sent: " + successful_requests_post.get());
    System.out.println("Number of Unsuccessful Requests Sent: " + unsuccessful_requests_post.get());
    System.out.println("Total Wall Time: " + wallTime + " milliseconds");
    System.out.println("Throughput: " + String.format("%.2f", ((double)NUM_TOTAL_SWIPE_REQUESTS / (double)wallTimeSeconds))  + " requests per seconds");
//    System.out.println("Number of Threads: "+ NUM_TOTAL_CONSUMER_THREADS );
//    System.out.println("------------------------------------------------------");
    DataGenerator dataGeneratorPost = new DataGenerator(recordPost);
    dataGeneratorPost.generateDataPost(wallTimeSeconds, (double) NUM_TOTAL_SWIPE_REQUESTS);
    System.out.println("------------------------------------------------------");

  }

}
