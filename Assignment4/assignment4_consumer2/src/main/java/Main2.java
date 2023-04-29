import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import consumer.SwipeMatchConsumer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class Main2 {
    private static final Integer NUM_TOTAL_CONSUMER_THREADS = 100;
    private static final Integer NUM_CONSUMERS_EACH_THREAD = 1;
    private static CountDownLatch countDownLatch;//??
    private static ConcurrentHashMap<String, List<String>> matchMap;//key:swiper, value: list of match user
    private static final String RMQ_LOCAL_HOST = "localhost";
    private static final String RMQ_REMOTE_HOST = "34.223.230.144";

    public static void main(String[] args) throws IOException, TimeoutException {
        matchMap = new ConcurrentHashMap<>();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RMQ_REMOTE_HOST);
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("password");
        Connection connection = factory.newConnection();

        for (int i = 0; i < NUM_TOTAL_CONSUMER_THREADS; i++) {
            new Thread(new SwipeMatchConsumer(connection)).start();
        }

        System.out.println("consumer.Consumer2 Finish");

    }

}
