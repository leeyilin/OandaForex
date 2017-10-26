package com.eastmoney.vss.oanda.message;


import com.eastmoney.vss.oanda.output.MdsmqSender;
import com.oanda.fxtrade.api.FXTick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OandaMessageHandler implements Runnable {
  private Logger logger = LoggerFactory.getLogger(OandaMessageHandler.class);

  private boolean _isRunning = true;

  public void setStop() {
    _isRunning = false;
  }

  public OandaMessageHandler() {
  }

  @Override
  public void run() {
    while (_isRunning) {
      try {
        if (!process()) {
          Thread.sleep(10);
        }
      } catch (InterruptedException e) {
        logger.error("m101:" + e.toString());
        e.printStackTrace();
      }
    }
  }

  private boolean process() {
    if (OandaMessageProcesser.getInstance().getMessagesCount() > 0) {
      FXTick[] messages = OandaMessageProcesser.getInstance().getMessage();
      while (messages != null) {
        MdsmqSender.getInstance().processRateRecord(messages);
        messages = OandaMessageProcesser.getInstance().getMessage();
      }
      return true;
    } else {
      return false;
    }
  }
}

