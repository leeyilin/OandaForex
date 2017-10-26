package com.eastmoney.vss.oanda.message;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import com.oanda.fxtrade.api.FXTick;


public class OandaMessageProcesser {
  private static final int MAX_QUEUE_COUNT = Short.MAX_VALUE * 1000;

  private BlockingQueue<FXTick[]> responsesQueue = new ArrayBlockingQueue<>(MAX_QUEUE_COUNT);

  private final static OandaMessageProcesser oandaMessageProcesser = new OandaMessageProcesser();

  public static OandaMessageProcesser getInstance() {
    return oandaMessageProcesser;
  }

  public boolean addMessage(FXTick[] messages) {
    return responsesQueue.offer(messages);
  }

  public FXTick[] getMessage() {
    try {
      return responsesQueue.poll(10 * 1000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      return null;
    }
  }

  public int getMessagesCount() {
    return responsesQueue.size();
  }
}
