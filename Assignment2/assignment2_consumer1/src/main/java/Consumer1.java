import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import models.SwipeData;
import models.SwipeDetails;

public class Consumer1 implements Runnable{

  private static final String QUEUE = "swipe_queue";
  private static final int LIKES_INDEX = 0;
  private static final int DISLIKES_INDEX = 1;
  private Connection connection;
  private ConcurrentHashMap<String, int[]> swipeMap;

  public Consumer1(Connection connection,
      ConcurrentHashMap<String, int[]> swipeMap) {

    this.connection = connection;
    this.swipeMap = swipeMap;
  }


  @Override
  public void run() {
    try {
      final Channel channel = connection.createChannel();
      channel.queueDeclare(QUEUE, true, false, false, null);//second true or false??
      channel.queueBind(QUEUE, "exchange", "");
      System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

      // accept only 1 unacknowledged message
      channel.basicQos(1);

      Gson gson = new Gson();//pass gson from main?
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");

        SwipeData swipeData = (SwipeData) gson.fromJson(message, SwipeData.class);
        String swiper = swipeData.getSwipeDetails().getSwiper();

        swipeMap.putIfAbsent(swiper, new int[] {0,0});
        if (swipeData.getLeftOrRight().equals("right")) {
          swipeMap.get(swiper)[LIKES_INDEX]++;
        } else if (swipeData.getLeftOrRight().equals("left")) {
          swipeMap.get(swiper)[DISLIKES_INDEX]++;
        }

        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//          System.out.println(" [x] Received '" + message + "'");
        System.out.println(swiper + " : " + swipeMap.get(swiper)[0] + " " + swipeMap.get(swiper)[1]);
      };

      channel.basicConsume(QUEUE, false, deliverCallback, consumerTag -> { });

    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
