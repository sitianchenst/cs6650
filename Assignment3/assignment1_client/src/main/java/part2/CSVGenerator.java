package part2;

import java.io.FileWriter;
import com.opencsv.CSVWriter;
import java.io.IOException;

public class CSVGenerator {
  public void generateCSV(String filePath, Record record) {
    CSVWriter csvWriter = null;
    try {
      csvWriter = new CSVWriter(new FileWriter(filePath));
      String header[] = {"Start Time", "Request Type", "Latency", "Response Code"};
      csvWriter.writeNext(header);
      csvWriter.writeAll(record.getRecordList());
      csvWriter.flush();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (csvWriter != null) {
        try {
          csvWriter.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

}
