package models;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SwipeData {

    private SwipeDetails swipeDetails;
    private String leftOrRight;

}

