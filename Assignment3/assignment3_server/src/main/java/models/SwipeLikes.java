package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@ToString
public class SwipeLikes {

    public SwipeLikes(String userId) {
        this.userId = userId;
        this.likes = "0";
        this.dislikes = "0";
    }

    @PartitionKey
    String userId;
    String likes;
    String dislikes;

}
