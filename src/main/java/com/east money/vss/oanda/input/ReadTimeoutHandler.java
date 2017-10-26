package com.eastmoney.vss.oanda.input;

import java.util.concurrent.TimeUnit;

import com.eastmoney.vss.oanda.OandaVssStartup;
import com.eastmoney.vss.oanda.common.OandaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by keyu on 2016/1/20.
 */
public class ReadTimeoutHandler implements Runnable {
  private static Logger LOG = LoggerFactory.getLogger(ReadTimeoutHandler.class);
  static long lastCheckTime = 0;
  static long reconnectTime = 0;

  @Override
  public void run() {
    try {
      LOG.info("ReadTimeoutHandler is set up.....");
      while (OandaReceiver.fxclient == null || !OandaReceiver.fxclient.isLoggedIn()) {
        TimeUnit.SECONDS.sleep(OandaConfig.getInstance().getTimeOut());
      }
      while (true) {
        long curTime = System.currentTimeMillis();
        if (((curTime - lastCheckTime) > OandaConfig.getInstance().getTimeOut() * 1000)
            || (OandaReceiver.fxclient == null || !OandaReceiver.fxclient.isLoggedIn())) {
          ++reconnectTime;
          if (OandaReceiver.fxclient == null) {
            LOG.info("OandaReceiver.fxclient == null, oandaReceiver will be restarted.");
          }
          if (OandaReceiver.fxclient != null && !OandaReceiver.fxclient.isLoggedIn()) {
            LOG.info("OandaReceiver.fxclient is not logged in, oandaReceiver will be restarted.");
          }
          LOG.info("curTime - lastCheckTime = " + (curTime - lastCheckTime) + "\tReconnect times:" + reconnectTime);
          OandaVssStartup.shutdown();
          OandaVssStartup.startupReceiver();
          lastCheckTime = System.currentTimeMillis(); //update time
        }
//        lastCheckTime is updated from the external, so it is not updated here.
        TimeUnit.SECONDS.sleep(reconnectTime <= 20 ? (OandaConfig.getInstance().getCheckInterval()) :
            (OandaConfig.getInstance().getCheckInterval() * 6));
      }
    } catch (InterruptedException e) {
      LOG.error("Exception caught in ReadTimeoutHandler. ", e.getMessage(), e);
    }
    LOG.info("ReadTimeoutHandler ends..");
  }
}
