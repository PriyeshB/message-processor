package service;

import java.util.concurrent.BlockingQueue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;


public class ConsumerMessageListener implements MessageListener {

	@Autowired
	private MessageProcessor messageProcessor;

	public void onMessage(Message message) {
		
		messageProcessor.putOnQueue(message);
	}  


}


