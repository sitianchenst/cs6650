import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import models.SwipeData;

public class Consumer2 implements Runnable{
  private static final String QUEUE_NAME = "match_queue";
  private int number_req_each_thread;
  private Connection connection;
  private ConcurrentHashMap<String, List<String>> matchMap;

  public Consumer2(int number_req_each_thread, Connection connection,
      ConcurrentHashMap<String, List<String>> matchMap) {
    this.number_req_each_thread = number_req_each_thread;
    this.connection = connection;
    this.matchMap = matchMap;
  }

  @Override
  public void run() {
    for (int i = 0; i < number_req_each_thread; i++) {
      try {
        final Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueBind(QUEUE_NAME,"exchange","");
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // accept only 1 unacknowledged message
        channel.basicQos(1);

        Gson gson = new Gson();//pass gson from main?
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");

          SwipeData swipeData = (SwipeData) gson.fromJson(message, SwipeData.class);
          String swiper = swipeData.getSwipeDetails().getSwiper();
          List<String> list = matchMap.getOrDefault(swiper, new LinkedList<>());
          if (swipeData.getLeftOrRight().equals("right")) {
            if (list.size() == 100) {
              list.remove(0);
            }
//            if (!list.contains(swipeData.getSwipeDetails().getSwipee())) {
//              list.add(swipeData.getSwipeDetails().getSwipee());
//            }
            list.add(swipeData.getSwipeDetails().getSwipee());
          }
          matchMap.put(swiper, list);

          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//          System.out.println(" [x] Received '" + message + "'");
          System.out.println(matchMap.get(swiper));

        };

        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });

      } catch (IOException e) {
        e.printStackTrace();
      }


    }
  }
}
