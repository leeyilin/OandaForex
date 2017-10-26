package com.eastmoney.vss.oanda.input;

import com.eastmoney.vss.oanda.common.OandaConfig;
import com.oanda.fxtrade.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by keyu on 2016/1/9.
 */
public class OandaReceiver implements Runnable {
  public static final String VERSION = "2.7.1";
  private static Logger LOG = LoggerFactory.getLogger(OandaReceiver.class);
  // the FXClient object will perform all interactions with the OANDA FXGame/FXTrade server
  static FXClient fxclient;

  private OandaConfig config;

  public OandaReceiver(OandaConfig config) {
    this.config = config;
  }

  @Override
  public void run() {
    try {
      if (config.isLive()) {
        LOG.info("oanda fx: creating live FXClient object...");
        fxclient = API.createFXTrade(); // for live account
      } else {
        LOG.info("oanda fx: creating practice FXClient object...");
        fxclient = API.createFXGame();  // for practice
      }
//            connect and login to the server
      if (fxclient != null) {
        LOG.info("oanda fx: logging in...");
        fxclient.setProxy(false);
        fxclient.setWithRateThread(true);
        fxclient.setWithKeepAliveThread(true);
        try {
          fxclient.login(config.getLoginId(), config.getPasswd(), "oanda fx ");
          if (fxclient.isLoggedIn()) {
            LOG.info("oanda fx: logging success...");
            ReadTimeoutHandler.lastCheckTime = System.currentTimeMillis();
            if (ReadTimeoutHandler.reconnectTime != 0) {
              ReadTimeoutHandler.reconnectTime = 0;
            }
            RateTicker t = new RateTicker();
            try {
              fxclient.getRateTable().getEventManager().add(t);
//                        fxclient.getRateTable().getEventManager().remove(t);
            } catch (SessionException e) {
              LOG.error(e.getMessage() + e);
            }
          } else {
            fxclient = null;
            LOG.info("oanda fx: logging faild...");
          }
        } catch (InvalidUserException | InvalidPasswordException |
            SessionException | MultiFactorAuthenticationException e) {
          LOG.error("oanda fx: logging faild..." + e.getMessage() + e);
        }
      } else {
        System.out.println("Fail to create FXClient object.");
      }
    } catch (Exception oa) {
      LOG.error(oa.getMessage() + oa);
    }
  }

  // Done, quit now
  public void logout() {
    if (fxclient != null) {
      try {
        fxclient.logout();
        fxclient = null;
      } catch (Exception e) {
        LOG.error(e.getMessage() + e);
      }
    }
  }
}
