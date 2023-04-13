package part2;

import java.util.Collections;

public class DataGenerator {
  private Record record;

  public DataGenerator(Record record) {
    this.record = record;
    Collections.sort(record.getLatencyList());
  }

  private long calculateMeanResponseTime() {
    int size = record.getLatencyList().size();
    if (size == 0) return 0;
    long totalResponseTime = 0;
    for (long latency: record.getLatencyList()) {
      totalResponseTime += latency;
    }
    return totalResponseTime / size;
  }

  private long calculateMedianResponseTime() {
    int size = record.getLatencyList().size();
    if (size % 2 == 0) {
      long mr = record.getLatencyList().get(size / 2);
      long ml = record.getLatencyList().get((size / 2) - 1);
      return (ml + mr) / 2;
    }
    return record.getLatencyList().get(size / 2);
  }

  private double calculateThroughput(double wallTimeSeconds, double total_requests) {
    double throughput = (double)total_requests / wallTimeSeconds;
    return throughput;
  }

  private long calculateP99() {
    int size = record.getLatencyList().size();
    int idx = (int) (size * 0.99);
    return record.getLatencyList().get(idx);
  }

  private long calculateMinTime() {
    if (record.getLatencyList().size() == 0) return 0;
    return record.getLatencyList().get(0);
  }

  private long calculateMaxTime() {
    int size = record.getLatencyList().size();
    if (size == 0) return 0;
    return record.getLatencyList().get(size - 1);
  }

  public void generateDataPost(double wallTimeSeconds, double total_requests) {
    System.out.println("POST Mean Response Time: " + calculateMeanResponseTime() + " milliseconds");
    System.out.println("POST Median Response Time: " + calculateMedianResponseTime() + " milliseconds");
//    System.out.println("Throughput: " + String.format("%.2f", calculateThroughput(wallTimeSeconds, total_requests)) + " requests per seconds");
    System.out.println("POST 99th Percentile Response Time: " + calculateP99() + " milliseconds");
    System.out.println("POST Minimum Response Time: " + calculateMinTime() + " milliseconds");
    System.out.println("POST Maximum Response Time: " + calculateMaxTime() + " milliseconds");
  }

  public void generateDataGet() {
    System.out.println("GET Minimum Response Time: " + calculateMinTime() + " milliseconds");
    System.out.println("GET Mean Response Time: " + calculateMeanResponseTime() + " milliseconds");
    System.out.println("GET Maximum Response Time: " + calculateMaxTime() + " milliseconds");
  }


}
