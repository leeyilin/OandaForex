package com.eastmoney.vss.oanda.output;


import com.eastmoney.vss.oanda.util.DateUtil;
import server.forex.util.datastruct.MessageQueue;
import server.forex.protocol.MDMessage;
import server.forex.protocol.MDMessageContainer;
import com.oanda.fxtrade.api.FXTick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by keyu on 2016/1/9.
 */
public class MdsmqSender {
  private static Logger LOG = LoggerFactory.getLogger(MdsmqSender.class);

  private MessageQueue<MDMessageContainer> mdMessageQueue;

  private static class SingletonHolder {
    final static MdsmqSender instance = new MdsmqSender();
  }

  public static MdsmqSender getInstance() {
    return SingletonHolder.instance;
  }

  public void setMessageQueue(MessageQueue mdMessageQueue) {
    this.mdMessageQueue = mdMessageQueue;
  }

  public void processRateRecord(FXTick[] recordArray) {
    try {
      MDMessageContainer container = new MDMessageContainer(3701, recordArray.length);
      for (FXTick fxTick : recordArray) {
        try {
          MDMessage mdMessage = new MDMessage(fxTick.getPair().getPair());
          mdMessage.setMsgType(3701);
          handleTimeStamp(mdMessage, fxTick.getTimestamp());
          mdMessage.setDouble(6, fxTick.getMean());
          mdMessage.setDouble(10, fxTick.getBid()); //bid aka buy.
          mdMessage.setDouble(12, fxTick.getAsk()); //ask aka sell.
          container.add(mdMessage);

          MDMessage inverseMDMessage = new MDMessage(getInversePair(fxTick.getPair().getPair()));
          inverseMDMessage.setMsgType(3701);
          handleTimeStamp(inverseMDMessage, fxTick.getTimestamp());
          inverseMDMessage.setDouble(6, fxTick.getInverse().getMean());
          inverseMDMessage.setDouble(10, fxTick.getInverse().getBid()); //bid aka buy.
          inverseMDMessage.setDouble(12, fxTick.getInverse().getAsk()); //ask aka sell.
          container.add(inverseMDMessage);
        } catch (Exception e) {
          e.printStackTrace();
          LOG.info(e.getMessage(), e);
        }
      }
      this.mdMessageQueue.addOutMDMessage(container);
    } catch (Exception e) {
      LOG.error(e.getMessage());
    }
  }

  private String getInversePair(String pair) {
    String[] pairArray = pair.split("/");
    return pairArray[1] + "/" + pairArray[0];
  }

  private void handleTimeStamp(MDMessage mdMessage, long timestamp) {
    String dateTime = DateUtil.dateToStr(timestamp * 1000, "yyyyMMdd-HHmmss");
    String[] dateTimeArray = dateTime.split("-");
    mdMessage.setInt(1, Integer.parseInt(dateTimeArray[0]));
    mdMessage.setInt(2, Integer.parseInt(dateTimeArray[1]));
  }
}
