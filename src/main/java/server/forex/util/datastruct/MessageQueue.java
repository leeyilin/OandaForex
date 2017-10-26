package server.forex.util.datastruct;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MessageQueue<T> {
  private static final int MAX_QUEUE_COUNT = 20970880;

  private BlockingQueue<T> outMessageQueue = new ArrayBlockingQueue<>(MessageQueue.MAX_QUEUE_COUNT);

  public MessageQueue() {
  }

  public boolean addOutMDMessage(T t) {
    return this.outMessageQueue.offer(t);
  }

  public T getOutMDMessage() {
    return this.outMessageQueue.poll();
  }

  public int getOutMDMessagesCount() {
    return this.outMessageQueue.size();
  }
}

