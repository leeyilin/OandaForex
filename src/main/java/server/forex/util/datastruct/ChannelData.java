package server.forex.util.datastruct;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Wrap a channel and a data queue with the target data to send in this class.
// The data will be sent immediately as the background thread is executing.
public class ChannelData {
  public static Logger LOG = LoggerFactory.getLogger(ChannelData.class);

  private Channel channel;
  private final TaskQueue dataQueue = new TaskQueue() {
    protected int doTask(Object t) {
      try {
        ChannelData.this.write(t);
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
      return 0;
    }
  };

  public ChannelData(Channel channel) {
    this.channel = channel;
    this.dataQueue.setSleepSecond(0);
    this.dataQueue.start();
  }

  @Override
  public String toString() {
    return String.format("<ChannelData>: %s", ChannelID.getID(this.channel));
  }

  public Channel getChannel() {
    return this.channel;
  }

  public TaskQueue getDataQueue() {
    return this.dataQueue;
  }

  public String getID() {
    return ChannelID.getID(this.channel);
  }

  public void destory() {
    if (this.channel.isActive()) {
      this.channel.close();
    }
    this.dataQueue.stop();
  }

  public void add(Object t) {
    if (this.channel != null && this.channel.isActive()) {
      if (this.dataQueue.size() <= 10000) {
        this.dataQueue.put(t);
      } else {
        LOG.warn("ChannelData[{}] size:{}", ChannelID.getID(this.channel), this.dataQueue.size());
      }
    } else {
      this.dataQueue.stop();
      LOG.warn("ChannelData isConnected:" + this.channel.isActive());
    }
  }

  public boolean isWritable() {
    return this.channel.isWritable();
  }

  public void setChannel(Channel channel) {
    this.channel = channel;
  }

  public int size() {
    return this.dataQueue.size();
  }

  private void write(Object o) {
    if (this.channel != null && this.channel.isActive()) {
      int i = 0;
      while (!this.channel.isWritable() && i++ <= 1000) {
        try {
          Thread.sleep(1L);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      this.channel.writeAndFlush(o);
    } else {
      this.dataQueue.stop();
      LOG.warn("ChannelData isConnected:" + this.channel.isActive());
    }
  }
}
