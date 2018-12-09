package service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageProcessor  {

	
	private final ExecutorService executorService = Executors.newCachedThreadPool();
	private final BlockingQueue<Message> blockingQueue;
	private final int numberOfThread ;
	private volatile boolean stopThread = false; 
	
	@Autowired 
	public MessageProcessor(int numberOfThread, int capacity) {
		this.blockingQueue = new LinkedBlockingQueue<>(capacity);
		this.numberOfThread = numberOfThread;
		init();
	}
	
	public void init() {
		
		for (int i = 0; i < numberOfThread; i++) {
			
				executorService.submit(() -> {
					Message message = null;
					int localCounter =0;
					while (!stopThread) {
					try {
					    message = blockingQueue.take();
					    process(message); // some processing then persist in database
					    
					} catch (InterruptedException e) {
						e.printStackTrace();
					}finally {
						synchronized (this) {
							message.acknowledge();	
						}
					}
					}
				});
		}
	}

	public void putOnQueue(Message message) {
		try {
			blockingQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@PreDestroy
	public void terminateJob() {
		stopThread = true;
		executorService.shutdown();  // not working after stopping application threads are still running
		//executorService.shutdownNow();  work but losing data
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
