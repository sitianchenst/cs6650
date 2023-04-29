import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import consumer.SwipeLikeConsumer;
import models.SwipeLikes;

import service.DynamoService;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CountDownLatch;

public class Main1 {

    private static final Integer NUM_TOTAL_CONSUMER_THREADS = 100;
    //  private static final Integer NUM_CONSUMERS_EACH_THREAD = 1;
    private static final String RMQ_LOCAL_HOST = "localhost";
    private static final String RMQ_REMOTE_HOST = "34.223.230.144";

    private static final Integer RMQ_PORT = 5672;
    private static final String USER_NAME = "admin";
    private static final String PASSWORD = "password";

    public static void main(String[] args) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RMQ_REMOTE_HOST);
        factory.setUsername(USER_NAME);
        factory.setPassword(PASSWORD);
        factory.setPort(RMQ_PORT);
        Connection connection = factory.newConnection();

        for (int i = 0; i < NUM_TOTAL_CONSUMER_THREADS; i++) {
            new Thread(new SwipeLikeConsumer(connection)).start();
        }
    }

}
