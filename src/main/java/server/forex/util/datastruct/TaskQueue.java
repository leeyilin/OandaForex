package server.forex.util.datastruct;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class TaskQueue<T> {
  public static Logger LOG = LoggerFactory.getLogger(TaskQueue.class);
  private static final int MAX_SIZE = 1000000;

  private ExecutorService executorService;
  private final LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();
  private final List<Thread> processThreadList = new ArrayList<>();
  private int taskQueueCount = 1;
  private volatile AtomicBoolean run = new AtomicBoolean(false);
  private String queueName = null;
  private int sleepSecond = 0;

  protected TaskQueue() {
  }

  protected TaskQueue(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public String getName() {
    return this.queueName;
  }

  protected void setName(String queueName) {
    this.queueName = queueName;
  }

  public int getTaskCount() {
    return this.taskQueueCount;
  }

  public void setTaskCount(int taskQueueCount) {
    this.taskQueueCount = taskQueueCount;
  }

  public boolean isRunning() {
    return this.run.get();
  }

  public int getSleepSecond() {
    return this.sleepSecond;
  }

  public void setSleepSecond(int sleepSecond) {
    this.sleepSecond = sleepSecond;
  }

  public int size() {
    return this.queue.size();
  }

  public List<Thread> getProcessList() {
    return this.processThreadList;
  }

  private synchronized void start(boolean toStartTask) {
    if (!this.isRunning()) {
      this.run.set(true);
      if (toStartTask) {
        if (this.executorService == null) {
          for (int i = 1; i <= this.taskQueueCount; ++i) {
            Thread t = new Thread(new TaskQueue.ProcessThread());
            t.setDaemon(true);
            this.processThreadList.add(t);
            t.start();
          }
        } else {
          for (int i = 1; i <= this.taskQueueCount; ++i) {
            Thread t = new Thread(new TaskQueue.ProcessThread());
            t.setDaemon(true);
            this.processThreadList.add(t);
            this.executorService.execute(t);
          }
        }
      }
    }
  }

  public synchronized void start() {
    this.start(true);
  }

  public void stop() {
    this.queue.clear();
    this.run.set(false);
  }

  public void put(T t) {
    try {
      if (this.isRunning()) {
        if (this.queue.size() <= TaskQueue.MAX_SIZE) {
          this.queue.put(t);
        } else {
          LOG.info("queue size: " + this.queue.size() + " full!");
        }
      } else {
        throw new Exception("queue: " + this.queueName + " not started!");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void put(List<T> ts) {
    try {
      if (this.run.get()) {
        for (T obj : ts)
          this.queue.put(obj);
      } else {
        throw new Exception("queue:" + this.queueName + " not started!");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected abstract int doTask(T obj);

  private class ProcessThread implements Runnable {
    private ProcessThread() {
    }

    public void run() {
      while (TaskQueue.this.run.get()) {
        try {
          T t = TaskQueue.this.queue.poll(10L, TimeUnit.SECONDS);
          if (t != null) {
            TaskQueue.this.doTask(t);
            TimeUnit.SECONDS.sleep((long) TaskQueue.this.sleepSecond);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      TaskQueue.LOG.info("processThread" + Thread.currentThread().getId() + " quit!");
    }
  }
}