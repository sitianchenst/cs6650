package part2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Record {

  private List<String[]> recordList;
  private List<Long> latencyList;


  public Record() {
    this.recordList = Collections.synchronizedList(new ArrayList<>());
    this.latencyList = Collections.synchronizedList(new ArrayList<>());
  }

  public List<String[]> getRecordList() {
    return recordList;
  }

  public void setRecordList(List<String[]> recordList) {
    this.recordList = recordList;
  }

  public List<Long> getLatencyList() {
    return latencyList;
  }

  public void setLatencyList(List<Long> latencyList) {
    this.latencyList = latencyList;
  }

}
