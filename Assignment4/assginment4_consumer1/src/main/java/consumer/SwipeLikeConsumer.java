package consumer;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import models.SwipeData;

import models.SwipeLikes;
import service.DynamoService;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class SwipeLikeConsumer implements Runnable {

    private static final String QUEUE = "swipe_queue";
    private Connection connection;

//    private static final String DYNAMODB_TABLE_NAME = "SWIPE_LIKE";
    private static final String DYNAMODB_TABLE_NAME_NEW  = "SWIPE_LIKE_NEW";
    public SwipeLikeConsumer(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            final Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE, true, false, false, null);
            channel.queueBind(QUEUE, "exchange", "");
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            channel.basicQos(1);
            Gson gson = new Gson();
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                SwipeData swipeData = (SwipeData) gson.fromJson(message, SwipeData.class);
                String swiper = swipeData.getSwipeDetails().getSwiper();

                DynamoDbClient ddb = DynamoService.getDynamoClient();
                DynamoService<SwipeLikes> service = new DynamoService<>();
                try {
                    SwipeLikes swipeLikes = new SwipeLikes(swiper);
                    service.getDynamoDBItem(ddb, DYNAMODB_TABLE_NAME_NEW, swipeLikes);

//                    int like = Integer.parseInt(swipeLikes.getLikes());
//                    int dislike = Integer.parseInt(swipeLikes.getDislikes());
                    int like = swipeLikes.getLikes();
                    int dislike = swipeLikes.getDislikes();

                    if (swipeData.getLeftOrRight().equals("right")) {
                        like += 1;
                        swipeLikes.setLikes(like);
//                        service.updateTableItem(ddb, DYNAMODB_TABLE_NAME, "USER_ID_KEY",swipeLikes.getUserId(),"like", String.valueOf(like));
                    } else if (swipeData.getLeftOrRight().equals("left")) {
                        dislike += 1;
                        swipeLikes.setDislikes(dislike);
//                        service.updateTableItem(ddb, DYNAMODB_TABLE_NAME, "USER_ID_KEY",swipeLikes.getUserId(),"dislike", String.valueOf(dislike));
                    }
                    service.putItemInTable(ddb, DYNAMODB_TABLE_NAME_NEW, swipeLikes);




                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//                System.out.println(swiper + " : " + swipeMap.get(swiper)[0] + " " + swipeMap.get(swiper)[1]);
            };
            channel.basicConsume(QUEUE, false, deliverCallback, consumerTag -> {
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
