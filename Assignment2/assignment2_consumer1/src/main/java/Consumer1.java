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
  private int number_req_each_thread;
  private Connection connection;
  private ConcurrentHashMap<String, int[]> swipeMap;

  public Consumer1(int number_req_each_thread, Connection connection,
      ConcurrentHashMap<String, int[]> swipeMap) {
    this.number_req_each_thread = number_req_each_thread;
    this.connection = connection;
    this.swipeMap = swipeMap;
  }


  @Override
  public void run() {
    for (int i = 0; i < number_req_each_thread; i++) {
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
//            swipeMap.getOrDefault(swiper, new int[]{0,0})[LIKES_INDEX]++;
            swipeMap.get(swiper)[LIKES_INDEX]++;
          } else if (swipeData.getLeftOrRight().equals("left")) {
//            swipeMap.getOrDefault(swiper, new int[]{0,0})[DISLIKES_INDEX]++;
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
}
