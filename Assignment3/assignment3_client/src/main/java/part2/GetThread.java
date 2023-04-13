package part2;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.MatchesApi;
import io.swagger.client.api.StatsApi;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.Matches;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.httpclient.HttpStatus;

public class GetThread implements Runnable{
  private final int RETRY_NUM = 1;
  private String basePath;
  private AtomicInteger successful_requests_get;
  private AtomicInteger unsuccessful_requests_get;
  private int sucess;
  private int unsucess;
  private CountDownLatch countDownLatch;
  private MatchesApi matchesApi;
  private StatsApi statsApi;
  private Record record;

  public GetThread(String basePath, AtomicInteger successful_requests,
      AtomicInteger unsuccessful_requests, Record record, MatchesApi matchesApi, StatsApi statsApi) {
    this.basePath = basePath;
    this.successful_requests_get = successful_requests;
    this.unsuccessful_requests_get = unsuccessful_requests;
    this.record = record;
    this.matchesApi = matchesApi;
    this.statsApi = statsApi;
  }

  @Override
  public void run() {
    int selection = ThreadLocalRandom.current().nextInt(2);
    String swiperId = String.valueOf(ThreadLocalRandom.current().nextInt(1,50001));
    if (selection == 0) {
      try {
        getMatch(swiperId);
      } catch (ApiException e) {
        e.printStackTrace();
      }
    } else {
      try {
        getStats(swiperId);
      } catch (ApiException e) {
        e.printStackTrace();
      }

    }

    successful_requests_get.getAndAdd(sucess);
    unsuccessful_requests_get.getAndAdd(unsucess);

  }

  private void getMatch(String swiperId) throws ApiException {
    long startMatch = System.currentTimeMillis();
    try {
      int statusCode = matchesApi.matchesWithHttpInfo(swiperId).getStatusCode();
      if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
        long endMatch = System.currentTimeMillis();
        record.getLatencyList().add(endMatch - startMatch);
//        successful_requests_get.incrementAndGet();
        System.out.println("Match Success");
        System.out.println(statusCode);
        return;
      }

    } catch (ApiException e) {
      int statusCode = e.getCode();
      if (statusCode == 404 || statusCode == 400) {
        long endMatch = System.currentTimeMillis();
        record.getLatencyList().add(endMatch - startMatch);
        System.out.println(statusCode);
      }
//      unsuccessful_requests_get.incrementAndGet();
      System.out.println("Fail to Get Match");
      e.printStackTrace();
    }

  }

  private void getStats(String swiperId) throws ApiException{
    long startStats = System.currentTimeMillis();
    try {
      int statusCode = statsApi.matchStatsWithHttpInfo(swiperId).getStatusCode();
      if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
        long endStats = System.currentTimeMillis();
        record.getLatencyList().add(endStats - startStats);
//        successful_requests_get.incrementAndGet();
        System.out.println("Stats Success");
        System.out.println(statusCode);
        return;
      }
    } catch (ApiException e) {
      int statusCode = e.getCode();
      if (statusCode == 404 || statusCode == 400) {
        long endStats = System.currentTimeMillis();
        record.getLatencyList().add(endStats - startStats);
        System.out.println(statusCode);
        System.out.println(endStats - startStats);
      }
//      unsuccessful_requests_get.incrementAndGet();
      System.out.println("Fail to Get Stats");
      e.printStackTrace();
    }

  }





//  private void getMatch(String swiperId) throws ApiException {
//    int retry = 0;
//    while (retry < RETRY_NUM) {
//      long startMatch = System.currentTimeMillis();
//      try {
//        ApiResponse<Matches> response =  matchesApi.matchesWithHttpInfo(swiperId);
//        int statusCode = matchesApi.matchesWithHttpInfo(swiperId).getStatusCode();
//        if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
//          long endMatch = System.currentTimeMillis();
////          record.getRecordList().add(
////              new String[]{String.valueOf(startMatch), "GET", String.valueOf(endMatch - startMatch),
////                  String.valueOf(statusCode)});
//          record.getLatencyList().add(endMatch - startMatch);
//          sucess++;
//          System.out.println("Match Success");
//          System.out.println(statusCode);
//          return;
//        }
//
////        else if (statusCode == 404 || statusCode == 400) {
////          long endMatch = System.currentTimeMillis();
//////          record.getRecordList().add(new String[]{String.valueOf(startMatch), "POST", String.valueOf(endMatch - startMatch), String.valueOf(statusCode)});
////          record.getLatencyList().add(endMatch - startMatch);
////          retry++;
////          System.out.println(statusCode);
////        }
//      } catch (ApiException e) {
//        System.out.println("Fail to Get Match");
//        e.printStackTrace();
//        int statusCode = e.getCode();
//        if (statusCode == 404 || statusCode == 400) {
//          long endMatch = System.currentTimeMillis();
////          record.getRecordList().add(new String[]{String.valueOf(startMatch), "POST", String.valueOf(endMatch - startMatch), String.valueOf(statusCode)});
//          record.getLatencyList().add(endMatch - startMatch);
//          System.out.println(statusCode);
//          System.out.println(endMatch - startMatch);
//        }
//        retry++;
//      }
//    }
//    unsucess++;
//  }
//
//  private void getStats(String swiperId) throws ApiException{
//    int retry = 0;
//    while (retry < RETRY_NUM) {
//      long startStats = System.currentTimeMillis();
//      try {
//        int statusCode = statsApi.matchStatsWithHttpInfo(swiperId).getStatusCode();
//        if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
//          long endStats = System.currentTimeMillis();
////          record.getRecordList().add(
////              new String[]{String.valueOf(startStats), "GET", String.valueOf(endStats - startStats),
////                  String.valueOf(statusCode)});
//          record.getLatencyList().add(endStats - startStats);
//          sucess++;
//          System.out.println("Stats Success");
//          System.out.println(statusCode);
//          return;
//        }
////        else if (statusCode == 404 || statusCode == 400) {
////          long endStats = System.currentTimeMillis();
////          record.getRecordList().add(new String[]{String.valueOf(startStats), "POST", String.valueOf(endStats - startStats), String.valueOf(statusCode)});
////          record.getLatencyList().add(endStats - startStats);
////          retry++;
////          System.out.println(statusCode);
////        }
//      } catch (ApiException e) {
//        System.out.println("Fail to Get Stats");
//        e.printStackTrace();
//        int statusCode = e.getCode();
//        if (statusCode == 404 || statusCode == 400) {
//          long endStats = System.currentTimeMillis();
//          record.getRecordList().add(new String[]{String.valueOf(startStats), "POST", String.valueOf(endStats - startStats), String.valueOf(statusCode)});
//          record.getLatencyList().add(endStats - startStats);
//          System.out.println(statusCode);
//          System.out.println(endStats - startStats);
//        }
//        retry++;
//      }
//    }
//    unsucess++;
//  }
}
