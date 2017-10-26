package com.eastmoney.vss.oanda.input;

import com.eastmoney.vss.oanda.message.OandaMessageProcesser;
import com.eastmoney.vss.oanda.util.DateUtil;
import com.oanda.fxtrade.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by keyu on 2016/1/11.
 */
public class RateTicker extends FXRateEvent {
  private static Logger LOG = LoggerFactory.getLogger(RateTicker.class);

  RateTicker() {
  }

  public void handle(FXEventInfo EI, FXEventManager EM) {
    ReadTimeoutHandler.lastCheckTime = System.currentTimeMillis();
//        ReadTimeoutHandler.reconnectTime=0;
    FXRateEventInfo REI = (FXRateEventInfo) EI;
//        record to file
//        fileRecorder.write(tickToString(REI.getTick()).getBytes("utf-8"));
//        RateMessage rateMessage=new RateMessage();
//        rateMessage.setPair(new EMFXPair());
    OandaMessageProcesser.getInstance().addMessage(new FXTick[]{REI.getTick()});
  }

  public String tickToString(FXTick tick) {
    String readableTime = null;
    try {
      readableTime = DateUtil.dateToStr(tick.getTimestamp() * 1000, "yyyyMMdd: HHmmss");
    } catch (Exception e) {
      e.printStackTrace();
    }
    String forexFields = "Time\tPair\tAsk\tBid\tMean\tisHalted\t\n";
    String forexValues = String.format("\t\t%s\t%s\t%.4f\t%.4f\t%.4f\t%s", readableTime, tick.getPair().getPair(),
        tick.getAsk(), tick.getBid(), tick.getMean(), tick.getPair().isHalted());
    return forexFields + forexValues;
  }
}
