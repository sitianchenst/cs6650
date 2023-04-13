package models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
@ToString
public class SwipeMatches {

    @PartitionKey
    private String userId;
    private Set<String> matchSet;

    public SwipeMatches(String userId) {
        this.userId = userId;
        matchSet = new TreeSet<>();
    }
}
