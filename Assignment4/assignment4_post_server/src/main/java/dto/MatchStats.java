package dto;

import java.util.List;
import jdk.jfr.StackTrace;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class MatchStats {
  private Integer numLlikes;
  private Integer numDislikes;
}
