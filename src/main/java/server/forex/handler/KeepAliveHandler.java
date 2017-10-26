package server.forex.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.forex.protocol.ForexProtocol;
import server.forex.util.datastruct.ChannelID;

public class KeepAliveHandler extends ChannelInboundHandlerAdapter {
  private static Logger LOG = LoggerFactory.getLogger(KeepAliveHandler.class);

  private volatile ScheduledFuture<?> keepAliveFuture;
  private boolean isClient = true;
  private short keepAliveInterval;

  public KeepAliveHandler(boolean isClient, short keepAliveInterval) {
    this.isClient = isClient;
    this.keepAliveInterval = (keepAliveInterval == 0 ? 15 : keepAliveInterval);
  }

  public void channelActive(ChannelHandlerContext ctx) throws InterruptedException {
    ctx.fireChannelActive();
    if (this.keepAliveFuture == null) {
      LOG.info((this.isClient ? "Client: " : "Server: ") + ChannelID.getID(ctx.channel()) + " set keepAlive.");
      this.keepAliveFuture = ctx.executor().scheduleAtFixedRate(
          new KeepAliveHandler.KeepAliveTask(ctx), 0L, (long) this.keepAliveInterval, TimeUnit.SECONDS);
    }
  }

  private class KeepAliveTask implements Runnable {
    private final ChannelHandlerContext ctx;

    KeepAliveTask(ChannelHandlerContext ctx) {
      this.ctx = ctx;
    }

    @Override
    public void run() {
      if (this.ctx.channel().isWritable()) {
        KeepAliveHandler.LOG.info((KeepAliveHandler.this.isClient ? "Client" : "Server") +
            " Send KeepAlive {}", ChannelID.getID(this.ctx.channel()));
        ForexProtocol message = new ForexProtocol((byte) 0, 9, (byte) 1, new short[]{-101}, new byte[]{'\0'});
        this.ctx.writeAndFlush(message);
      }
    }
  }
}
