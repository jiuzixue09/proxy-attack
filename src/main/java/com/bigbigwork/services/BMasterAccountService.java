package com.bigbigwork.services;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BMasterAccountService {
    private final static String QUEUE_NAME = "direct.bmaster.account.queue";
    private final static String EXCHANGE_NAME = "direct.python.pinterest.account.exchange";
    private final static String VIRTUAL_HOST = "/neptune";
    private final static String HOST = "127.0.0.1";
    private final static int PORT = 5672;
    private final static String USERNAME = "neptune";
    private final static String PASSWORD = "neptune123.jhk";

    private static String account;

    public synchronized static String getAccount() throws InterruptedException {
        if(Objects.isNull(account) || account.isEmpty()){
            Thread thread = new Thread(() -> {
                try {
                    init();
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
        for (int i = 0; i < 10; i++) {
            if(Objects.nonNull(account) && account.length() > 1){
                return account;
            }
            TimeUnit.SECONDS.sleep(2);
        }

        return null;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println(BMasterAccountService.getAccount());
    }

    private static void init() throws IOException, TimeoutException {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置RabbitMQ地址

        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);
        factory.setVirtualHost(VIRTUAL_HOST);
        //创建一个新的连接
        Connection connection = factory.newConnection();
        //创建一个通道
        Channel channel = connection.createChannel();
        //声明要关注的队列
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.basicQos(1); //每次只取一条记录

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT,true);
        System.out.println("Customer Waiting Received messages");
        //DefaultConsumer类实现了Consumer接口，通过传入一个频道，
        // 告诉服务器我们需要那个频道的消息，如果频道中有消息，就会执行回调函数handleDelivery
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("Customer Received '" + message + "'");
                account = message;

                //channel.basicAck(envelope.getDeliveryTag(), false); 手动应答
            }

        };
        //自动回复队列应答 -- RabbitMQ中的消息确认机制
        channel.basicConsume(QUEUE_NAME, false, consumer);
    }
}
