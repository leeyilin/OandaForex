package com.eastmoney.vss.oanda.common;

import server.forex.config.ServerConfig;
/**
 * Created by keyu on 2016/1/9.
 */

public class OandaConfig {
  private String loginId;
  private String passwd;
  private boolean isLive = false;
  private int timeOut;
  private int checkInterval;
  private int maxReconnectTimes;
  private static OandaConfig oandaConfigInstance = new OandaConfig();

  public static OandaConfig getInstance() {
    return oandaConfigInstance;
  }

  private OandaConfig() {}

  public void load(ServerConfig serverConfig) {
      loginId = serverConfig.getUsername();
      passwd = serverConfig.getPassword();
      isLive = serverConfig.isAccountLive();
      timeOut = serverConfig.getTimeout();
      checkInterval = serverConfig.getCheckInterval();
      maxReconnectTimes = serverConfig.getMaxReconnectionTimes();
  }

  public String getLoginId() {
    return loginId;
  }
  public String getPasswd() {
    return passwd;
  }
  public boolean isLive() {
    return isLive;
  }
  public int getTimeOut() {
    return timeOut;
  }
  public int getCheckInterval() {
    return checkInterval;
  }

  @Override
  public String toString() {
    return "OandaConfig{" +
        "loginId='" + loginId + '\'' +
        ", passwd='" + passwd + '\'' +
        ", isLive=" + isLive +
        ", timeOut=" + timeOut +
        ", checkInterval=" + checkInterval +
        ", maxReconnectTimes=" + maxReconnectTimes +
        '}';
  }
}
