package service;

import models.PartitionKey;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DynamoService<T> {

    public static DynamoDbClient getDynamoClient() {
        AwsCredentialsProvider credentialsProvider = SystemPropertyCredentialsProvider.create();
        System.setProperty("aws.accessKeyId", "ASIASFMNCJKH2GOT445U");
        System.setProperty("aws.secretAccessKey", "oq3sfezXo7qeYuqdQahEw+CnI1Xl2uUpe4nd0TbR");
        System.setProperty("aws.sessionToken", "FwoGZXIvYXdzEDgaDGxPDWegsZp3Obs5ESLMAfp2nM6zTPRFXwtxwyRog29Xrr7aa0RQgReLpoEuS+qyGRULrNBReIfku8qQTyNl7VW3W80dAsmD4xSU/7F2IjS8ABvxdNpAHNrRxf0x1DYvSBSuTRFa7+aLfm7TGJy9q+Q370IyPjb5qiiSgWWSpJy+uxkS+97IlCXpSB8+AUQenqTLdVfZeOYenKgxfQ46PfrbODPCxAs9bS40S9DD5CiOl8cO998HAtX4US8RjkmtpszWpQsghCvug5N43MlcCPG55g63Br3DYS9QBCih5KSiBjIt93M71vP2oNDIcfRHIXfQ9dJIqYaLVs1425K099hbuPEWS5+TKgZCgGuN8Vcv");
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
                    value = String.valueOf(field.get(data));
                    itemValues.put(key, AttributeValue.builder().s(value).build());
                }
            } else {
                key = field.getName();
                value = String.valueOf(field.get(data));
                itemValues.put(key, AttributeValue.builder().n(value).build());
            }

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
                    if (field.getType().equals(Integer.class)) {
                        field.set(data, Integer.parseInt(attributeValue.n()));
                        continue;
                    }
                }
            }
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
        return data;
    }

    public static void updateTableItem(DynamoDbClient ddb,
                                       String tableName,
                                       String key,
                                       String keyVal,
                                       String name,
                                       String updateVal){

        HashMap<String,AttributeValue> itemKey = new HashMap<>();
        itemKey.put(key, AttributeValue.builder()
                .s(keyVal)
                .build());

        HashMap<String,AttributeValueUpdate> updatedValues = new HashMap<>();
        updatedValues.put(name, AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(updateVal).build())
                .action(AttributeAction.PUT)
                .build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(itemKey)
                .attributeUpdates(updatedValues)
                .build();

        try {
            ddb.updateItem(request);
        } catch (ResourceNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("The Amazon DynamoDB table was updated!");
    }






}
