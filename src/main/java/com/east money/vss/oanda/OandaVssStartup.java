package com.eastmoney.vss.oanda;

import com.eastmoney.vss.oanda.common.OandaConfig;
import server.forex.ServerStartup;
import server.forex.config.ServerConfig;
import com.eastmoney.vss.oanda.input.OandaReceiver;
import com.eastmoney.vss.oanda.input.ReadTimeoutHandler;
import com.eastmoney.vss.oanda.message.OandaMessageHandler;
import com.eastmoney.vss.oanda.output.MdsmqSender;
import server.forex.protocol.MDMessageContainer;
import server.forex.util.datastruct.MessageQueue;
import server.forex.output.MDMessageSender;
//import com.em.mdsserver.mq.SenderPoint;
//import com.em.mdsserver.protocol.dic.EMDictonary;
import server.forex.protocol.EMDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by keyu on 2016/1/9.
 */
public class OandaVssStartup {
  private static Logger LOG = LoggerFactory.getLogger(OandaVssStartup.class);
  private static OandaReceiver oandaFxReceiver;
  private static MessageQueue<MDMessageContainer> mdMessageQueue = new MessageQueue<>();
  private static OandaMessageHandler messageHandler = new OandaMessageHandler();

  public static void startupReceiver() {
    try {
      oandaFxReceiver = new OandaReceiver(OandaConfig.getInstance());
      new Thread(oandaFxReceiver).start();
    } catch (Exception e) {
      LOG.error(e.getMessage());
      e.printStackTrace();
    }
  }

  private static void startupServer() {
    try {
      Thread t = new Thread(new ServerStartup(ServerConfig.getInstance()));
      t.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void startupMessageHandler() {
    new Thread(messageHandler).start();
  }

  private static void startupMessageSender() {
    MDMessageSender mdMessageSender = new MDMessageSender(ServerConfig.getInstance());
    mdMessageSender.setMdMessageQueue(OandaVssStartup.mdMessageQueue);
    new Thread(mdMessageSender).start();
  }

  public static void shutdown() {
    if (oandaFxReceiver != null) {
      LOG.info("Shut down oandaReceiver.");
      oandaFxReceiver.logout();
    } else {
      LOG.info("Fail to shut down oandaReceiver cause it's null.");
    }
  }

  public static void main(String[] args) {
    if (args.length == 2 && args[0].endsWith(".xml") && args[1].endsWith(".xml")) {
      ServerConfig.getInstance().loadConfigFile(args[0]);
      OandaConfig.getInstance().load(ServerConfig.getInstance());
      EMDictionary.getInstance().loadConfigFile(args[1]);
//    The intermediate handler.
      MdsmqSender.getInstance().setMessageQueue(OandaVssStartup.mdMessageQueue);
//        new SenderPoint(mdMessageQueue).run();
      startupReceiver();
      new Thread(new ReadTimeoutHandler()).start();
      startupMessageHandler();
      startupServer();
      startupMessageSender();
    } else {
      System.out.println("Please provide {server_config}.xml and {templates}.xml.");
    }
  }
}
