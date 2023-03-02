import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CountDownLatch;


public class Main1 {
  private static final Integer NUM_TOTAL_CONSUMER_THREADS = 50;
  private static final Integer NUM_CONSUMERS_EACH_THREAD = 1;
  private static ConcurrentHashMap<String, int[]> swipeMap;
  private static final String RMQ_LOCAL_HOST = "localhost";
  private static final String RMQ_REMOTE_HOST = "44.242.45.40";

  public static void main(String[] args) throws InterruptedException, IOException, TimeoutException {

    swipeMap = new ConcurrentHashMap<>();//key: swiper id, value: int[]{likes, dislikes}

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(RMQ_REMOTE_HOST);

    factory.setUsername("admin");
    factory.setPassword("password");
    factory.setPort(5672);


    Connection connection = factory.newConnection();

    for (int i = 0; i<NUM_TOTAL_CONSUMER_THREADS; i++) {
      new Thread(new Consumer1(NUM_CONSUMERS_EACH_THREAD, connection, swipeMap)).start();
    }

    System.out.println("---------------------------------");
//    for (Map.Entry<String, int[]> entry : swipeMap.entrySet()) {
//      System.out.println(entry.getKey() + ":" + entry.getValue()[0] + " " + entry.getValue()[1]);
//    }


//    Channel channel = connection.createChannel();
//    channel.queueDeclare("c1", false, false, false, null);
//    channel.queueBind("c1", "exchange", "");
//
//
//
//    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
//
//    // accept only 1 unacknowledged message
//    channel.basicQos(1);
//
//    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//      String message = new String(delivery.getBody(), "UTF-8");
//      channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//      System.out.println(" [x] Received '" + message + "'");
//    };
//    channel.basicConsume("c1", false, deliverCallback, consumerTag -> { });
  }

}
