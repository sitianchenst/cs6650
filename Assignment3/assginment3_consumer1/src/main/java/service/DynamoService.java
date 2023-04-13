package service;

import models.PartitionKey;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DynamoService<T> {

    public static DynamoDbClient getDynamoClient() {
        AwsCredentialsProvider credentialsProvider = SystemPropertyCredentialsProvider.create();
        System.setProperty("aws.accessKeyId", "ASIASFMNCJKH2N4XCO2M");
        System.setProperty("aws.secretAccessKey", "7hBvF5UTYorOVTR+JTNTip13vyNv95WwQENhAtRM");
        System.setProperty("aws.sessionToken", "FwoGZXIvYXdzEPT//////////wEaDM+kdWmJki8cS+HZySLMAalq+mzxuSdQGgQ8lGZlNFUJVcBu629ZGXAdmtPv9GXHWrE7TQPpTURde86sHEGucOqd8h6Ot4tRS9YbozbfGVwYy3gta5JCEzQ4B7W6uI/fbeyX0pciA8T8kqRG7Zy/rEoAFr6xrfEopI1nVH+i+e1LssGMNSDfq363cgJyHZaD6Dj6u4H20MsTqRKW7K2/q/1J0UTkTSQAo8gq130yMIPUozSd+1C+tOe0ymVtds7Kk+ihiBtJ9gqgclszek+0q8pVKvYrn3kBuhweSiiE2N2hBjIt/j07HuFARMsosQf5jmxNPzrj8oYuDxrXbT5fngQsYSmLc18GNbnk/Jd9lhH2");
        Region region = Region.US_WEST_2;
        DynamoDbClient ddb = DynamoDbClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
        return ddb;
    }

    public void putItemInTable(DynamoDbClient ddb, String tableName, T data) throws IllegalAccessException {
        Map<String, AttributeValue> itemValues = new HashMap<>();
        Field[] declaredFields = data.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            String key = null;
            String value = null;
            Annotation[] annotations = field.getDeclaredAnnotations();
            if (annotations != null && annotations.length > 0) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof PartitionKey) {
                        key = ((PartitionKey) annotations[0]).value();
                    } else {
                        key = field.getName();
                    }
                }
            } else {
                key = field.getName();
            }
            value = String.valueOf(field.get(data));
            itemValues.put(key, AttributeValue.builder().s(value).build());
        }
        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemValues)
                .build();
        try {
            PutItemResponse response = ddb.putItem(request);
            System.out.println(tableName + " was successfully updated. The request id is " + response.responseMetadata().requestId());
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The Amazon DynamoDB table %s can't be found.", tableName);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
    }

    public T getDynamoDBItem(DynamoDbClient ddb, String tableName, T data) throws IllegalAccessException {
        Field[] declaredFields = data.getClass().getDeclaredFields();
        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        String key = null;
        String keyVal = null;
        for (Field field : declaredFields) {
            field.setAccessible(true);
            PartitionKey partitionKeyAnnotation = field.getAnnotation(PartitionKey.class);
            if (partitionKeyAnnotation != null) {
                key = partitionKeyAnnotation.value();
                keyVal = String.valueOf(field.get(data));
                keyToGet.put(key, AttributeValue.builder()
                        .s(keyVal)
                        .build());
            }
        }
        GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(tableName)
                .build();
        try {
            Map<String, AttributeValue> map = ddb.getItem(request).item();
            if (map != null) {
                for (Field field : declaredFields) {
                    field.setAccessible(true);
                    AttributeValue attributeValue = map.get(field.getName());
                    if (attributeValue == null) continue;
                    if (field.getType().equals(String.class)) {
                        field.set(data, attributeValue.s().toString());
                        continue;
                    }
                }
            }
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
        return data;
    }
}
