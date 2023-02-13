package part2;

import com.opencsv.CSVWriter;
import io.swagger.client.ApiException;
import io.swagger.client.api.SwipeApi;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import part2.Producer;
import part2.SwipeData;

public class Main {

  private static final Integer NUM_TOTAL_SWIPE_REQUESTS = 500000;
  private static final Integer NUM_TOTAL_CONSUMER_THREADS = 50;
  private static final Integer NUM_CONSUMERS_EACH_THREAD = 10000;

  private static final Integer NUM_TOTAL_PRODUCER_THREADS = 50;
  private static final Integer NUM_PRODUCERS_EACH_THREAD = 10000;
  private static final String BASE_PATH_LOCAL = "http://localhost:8080/assignment1_server_war_exploded/";
  private static final String BASE_PATH_REMOTE = "http://35.91.54.121:8080/assignment1_server_war/";
  private static final String BASE_PATH_REMOTE_SPRING = "http://54.190.56.78:8082";
  private static final String BASE_PATH_REMOTE_SPRING_LOCAL = "http://localhost:8082";
  private static AtomicInteger successful_requests;
  private static AtomicInteger unsuccessful_requests;
  private static BlockingQueue<SwipeData> queue;
  private static CountDownLatch countDownLatch;
  private static Record record;

  public static void main(String[] args) throws ApiException, InterruptedException {
    //Initialize number of successful/unsucessful requests
    //Initialize blocking queue
    successful_requests = new AtomicInteger(0);
    unsuccessful_requests = new AtomicInteger(0);

    queue = new LinkedBlockingQueue<>();

    long startTime = System.currentTimeMillis();
    // producer
    for (int i = 0; i < NUM_TOTAL_PRODUCER_THREADS; i++) {
      new Thread(new Producer(queue, NUM_PRODUCERS_EACH_THREAD)).start();
    }

    countDownLatch = new CountDownLatch(NUM_TOTAL_CONSUMER_THREADS);

    //consumer
    record = new Record();
    for (int i = 0; i < NUM_TOTAL_CONSUMER_THREADS; i++) {
      SwipeApi swipeApi = new SwipeApi();
      swipeApi.getApiClient().setBasePath(BASE_PATH_REMOTE_SPRING);

      new Thread(new ConsumerPart2(queue, NUM_CONSUMERS_EACH_THREAD, successful_requests,unsuccessful_requests, countDownLatch, swipeApi, record)).start();
    }

    countDownLatch.await();
    long endTime = System.currentTimeMillis();

    new CSVGenerator().generateCSV("csv/swipe_record"+NUM_TOTAL_SWIPE_REQUESTS+"requests.csv", record);

    long wallTime = (endTime - startTime);
    double wallTimeSeconds = wallTime / (double)1000;
    System.out.println(TimeUnit.MILLISECONDS.toSeconds(wallTime) + "seconds");

    //print all information
    System.out.println("--------------TWINDER SWIPE POST REQUESTS--------------");
    System.out.println("Total Number of Requests: " + NUM_TOTAL_SWIPE_REQUESTS);
    System.out.println("Number of Successful Requests Sent: " + successful_requests.get());
    System.out.println("Number of Unsuccessful Requests Sent: " + unsuccessful_requests.get());
    System.out.println("Total Wall Time: " + wallTime + " milliseconds");
    System.out.println("Throughput: " + String.format("%.2f", ((double)NUM_TOTAL_SWIPE_REQUESTS / (double)wallTimeSeconds))  + " requests per seconds");
    System.out.println("Number of Threads: "+ NUM_TOTAL_CONSUMER_THREADS );
    System.out.println("Little's Law Expected Throughput: " +  String.format("%.2f",(double)Math.min(NUM_TOTAL_PRODUCER_THREADS, NUM_TOTAL_CONSUMER_THREADS )/ 0.0339 ));
    System.out.println("------------------------------------------------------");
    DataGenerator dataGenerator = new DataGenerator(record);
    dataGenerator.generateData(wallTimeSeconds, (double) NUM_TOTAL_SWIPE_REQUESTS);
    System.out.println("------------------------------------------------------");

    double latency = TimeUnit.MILLISECONDS.toSeconds(wallTime) / (double)NUM_TOTAL_SWIPE_REQUESTS;
    double littleLaw = (double)NUM_TOTAL_CONSUMER_THREADS / latency;
    System.out.println("Number of Producer: "+ NUM_TOTAL_PRODUCER_THREADS );
    System.out.println("Number of Consumer: " + NUM_TOTAL_CONSUMER_THREADS);
    System.out.println("Average Latency: " + 0.0343);
    System.out.println("Little Law Throughout: " + (double)Math.min(NUM_TOTAL_PRODUCER_THREADS, NUM_TOTAL_CONSUMER_THREADS )/ 0.0343 );
    System.out.println("-----------------------------------------------");

  }

}
