package models;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PartitionKey {

    String value() default "USER_ID_KEY";
}
