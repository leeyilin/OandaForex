package server.forex.register;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import server.forex.protocol.ForexProtocol;
import server.forex.util.datastruct.ChannelData;

public class DataPusher {
  public static Logger LOG = LoggerFactory.getLogger(DataPusher.class);
  // The key is the channel Id.
  private ConcurrentMap<String, ChannelData> channelDataMap = new ConcurrentHashMap<>();
  // Categorize the clients by topic, which is the key here.
  private ConcurrentMap<Short, ConcurrentMap<String, ChannelData>> transChannelDataMap = new ConcurrentHashMap<>();
  private volatile AtomicBoolean check = new AtomicBoolean(false);
  private static DataPusher dataPusherInstance = new DataPusher();

  public static DataPusher getInstance() {
    return dataPusherInstance;
  }

  private DataPusher() {
    this.check.set(true);
    Thread thread = new Thread(new DataPusher.CheckThread());
    thread.setDaemon(true);
    thread.start();
  }

  private void destory() {
    this.check.set(false);
    this.channelDataMap.clear();
    this.transChannelDataMap.clear();
  }

  public ChannelData getChannelData(String id) {
    return (ChannelData) this.channelDataMap.get(id);
  }

  public synchronized void regist(ChannelData channelData) {
    this.channelDataMap.putIfAbsent(channelData.getID(), channelData);
  }

  public synchronized void regist(Short topic, ChannelData channelData) {
    this.channelDataMap.putIfAbsent(channelData.getID(), channelData);
    ConcurrentMap<String, ChannelData> channelList = this.transChannelDataMap.get(topic);
    if (channelList == null) {
      channelList = new ConcurrentHashMap<>();
      this.transChannelDataMap.put(topic, channelList);
    }
    if (channelList.putIfAbsent(channelData.getID(), channelData) == null) {
      System.out.println("DataPusher registers ChannelData: " + channelData.getID() + " topic: " + topic);
      LOG.info("DataPusher registers ChannelData:" + channelData.getID() + " topic:" + topic);
    }
  }

  public synchronized void cancel(String id) {
    ChannelData channelData = (ChannelData) this.channelDataMap.remove(id);
    if (channelData != null) {
      channelData.destory();
      LOG.info("DataPusher remove ChannelData:" + id);
    }
    for (ConcurrentMap channelDataList : this.transChannelDataMap.values()) {
      try {
        channelDataList.remove(id);
      } catch (Exception e) {
        LOG.error(e.getMessage());
        e.printStackTrace();
      }
    }
  }

  public void send(ForexProtocol message) {
    ConcurrentMap<String, ChannelData> transMap = this.transChannelDataMap.get(message.getTopic()[0]);
    if (transMap != null) {
      for (ChannelData channelData : transMap.values()) {
        try {
          channelData.add(message);
        } catch (Exception e) {
          LOG.error(e.getMessage());
          e.printStackTrace();
        }
      }
    } else {
//      System.out.println(String.format("Find out no client with topic(%d) to push data...", message.getTopic()[0]));
    }
  }

  //  Remove all the inactive registered channels.
//  However, it seems it could only check the first topic in this.transChannelDataMap.
  private class CheckThread implements Runnable {
    static final long MILLIS = 60000L;

    private CheckThread() {
    }

    public void run() {
      label46:
      while (DataPusher.this.check.get()) {
        try {
          StringBuffer sb = new StringBuffer();
          Iterator iter = DataPusher.this.transChannelDataMap.entrySet().iterator();
          while (true) {
            Map.Entry en;
            Collection channelDatas;
            do {
              if (!iter.hasNext()) {
                if (sb.length() > 0) {
                  DataPusher.LOG.info("******** stat " + sb.toString());
                }
                Thread.sleep(CheckThread.MILLIS);
                continue label46;
              }
              en = (Map.Entry) iter.next();
              channelDatas = ((ConcurrentMap) en.getValue()).values();
            } while (channelDatas == null);

            sb.append(" [topic:" + en.getKey() + " ");
            for (Object obj : channelDatas) {
              ChannelData channelData = (ChannelData) obj;
              if (channelData.getChannel().isActive()) {
                sb.append(channelData.getChannel().remoteAddress() + " ");
              } else {
                ((ConcurrentMap) en.getValue()).remove(channelData.getID());
                DataPusher.this.channelDataMap.remove(channelData.getID());
                channelData.destory();
              }
            }
            sb.append("] ");
            if (channelDatas.size() == 0) {
              DataPusher.this.transChannelDataMap.remove(en.getKey());
            }
          }
        } catch (Exception e) {
          DataPusher.LOG.error(e.getMessage());
          e.printStackTrace();
        }
      }
    }
  }
}


