package consumer;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import models.SwipeData;
import models.SwipeMatches;
import service.DynamoService;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class SwipeMatchConsumer implements Runnable {
    private static final String QUEUE_NAME = "match_queue";
    private Connection connection;

    private static final String DYNAMODB_TABLE_NAME = "SWIPE_MATCH";

    public SwipeMatchConsumer(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {

        try {
            final Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.queueBind(QUEUE_NAME, "exchange", "");
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            channel.basicQos(1);
            Gson gson = new Gson();//pass gson from main?
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                SwipeData swipeData = (SwipeData) gson.fromJson(message, SwipeData.class);
                String swiper = swipeData.getSwipeDetails().getSwiper();
                DynamoDbClient ddb = DynamoService.getDynamoClient();
                DynamoService<SwipeMatches> service = new DynamoService<>();
                SwipeMatches swipeMatches = new SwipeMatches(swiper);
                try {
                    service.getDynamoDBItem(ddb, DYNAMODB_TABLE_NAME, swipeMatches);
                    TreeSet<String> set = new TreeSet<>(swipeMatches.getMatchSet());
                    if (swipeData.getLeftOrRight().equals("right")) {
                        if (set.size() == 100) {
                            set.remove(0);
                        }
                        set.add(swipeData.getSwipeDetails().getSwipee());
                    }
                    swipeMatches.setMatchSet(set);
                    service.putItemInTable(ddb, DYNAMODB_TABLE_NAME, swipeMatches);
                    System.out.println(String.format("%s table update successfully. The matches for userId: %s is %s", DYNAMODB_TABLE_NAME, swiper, set));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };

            channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
            });

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
