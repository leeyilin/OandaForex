package client.forex.input;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


public class DataPool<T> {
  private static final int POOL_SIZE = 1000000;

  private BlockingQueue<T> dataQueue;

  public DataPool() {
    this.dataQueue = new ArrayBlockingQueue<>(DataPool.POOL_SIZE);
  }

  public boolean add(T data) {
    return this.dataQueue.offer(data);
  }

  public T get() {
    try {
      return this.dataQueue.poll(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
      return null;
    }
  }

  public int size() {
    return this.dataQueue.size();
  }
}
