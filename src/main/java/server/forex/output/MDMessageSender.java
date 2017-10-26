package server.forex.output;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.em.mdsserver.message.PackageStat;
import server.forex.protocol.MDEncoder;
import server.forex.protocol.MDMessageContainer;
import server.forex.util.datastruct.MessageQueue;
import server.forex.protocol.MDMessage;
import server.forex.protocol.ForexProtocol;
import server.forex.config.ServerConfig;

public class MDMessageSender implements Runnable {
  private static Logger LOG = LoggerFactory.getLogger(MDMessageSender.class);
  private static int maxPackSize = 5000;

  private boolean isRunning = true;
  //  The key is the message type.
  private HashMap<Integer, MDMessageContainer> containers = new HashMap<>();
  private MDEncoder mdEncoder;
  private MessageQueue mdMessageQueue;
  private int sid = 1;
  private int protocolVersion;
  private int compressType;

  public void setMdMessageQueue(MessageQueue mdMessageQueue) {
    this.mdMessageQueue = mdMessageQueue;
  }

  public void setStop() {
    this.isRunning = false;
  }

  public MDMessageSender(ServerConfig serverConfig) {
    this.mdEncoder = new MDEncoder(serverConfig);
    this.protocolVersion = serverConfig.getProtocolVersion();
    this.compressType = serverConfig.getCompressType();
//    PackageStat.getInstance();
  }

  @Override
  public void run() {
    while (this.isRunning) {
      try {
        if (!this.process()) {
          Thread.sleep(10L);
        }
      } catch (Exception e) {
        LOG.error("m101:" + e.toString());
      }
    }
    LOG.info(MDMessageSender.class.getName() + " exits thread...");
  }

  private boolean process() {
    boolean result = false;
    if (this.mdMessageQueue.getOutMDMessagesCount() == 0) {
      return result;
    } else {
      LOG.info("messages:{} ", this.mdMessageQueue.getOutMDMessagesCount());
      if (this.containers.size() > 0) {
        this.containers.clear();
      }
      Object out = this.mdMessageQueue.getOutMDMessage();
      while (out != null) {
        try {
          if (out instanceof MDMessage) {
            MDMessage message = (MDMessage) out;
            MDMessageContainer mdMessageContainer = this.containers.get(message.getMsgType());
            if (mdMessageContainer == null) {
              mdMessageContainer = new MDMessageContainer();
              mdMessageContainer.setMsgType(message.getMsgType());
              this.containers.put(message.getMsgType(), mdMessageContainer);
            }
            mdMessageContainer.add(message);
            if (mdMessageContainer.size() >= MDMessageSender.maxPackSize) {
              this.mdMessageContainerToSender(mdMessageContainer);
              result = true;
            }
          } else if (out instanceof MDMessageContainer) {
            this.mdMessageContainerToSender((MDMessageContainer) out);
          } else {
            System.out.println(String.format("Unknown message class: %s", out.getClass().getName()));
          }
          out = this.mdMessageQueue.getOutMDMessage();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      for (MDMessageContainer mdMC : this.containers.values()) {
        try {
          this.mdMessageContainerToSender(mdMC);
          result = true;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      return result;
    }
  }

  //  This function includes the real protocol.
  private void mdMessageContainerToSender(MDMessageContainer content) {
    if (content.size() > 0) {
      this.mdEncoder.setHeader(this.sid, 0, 0, this.protocolVersion,
          this.compressType, content.msgType());
      ForexProtocol message = new ForexProtocol((byte) 0, 0, (byte) 1,
          new short[]{(short) content.msgType()}, this.mdEncoder.encode(content));
      MessagePusher.getInstance().writeData(message);
      if (++this.sid < 0) {
        this.sid = 1;
      }
      content.clear();
    }
  }
}

