package server.forex.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.forex.util.datastruct.ChannelID;
import server.forex.util.datetime.DateUtil;


public class TimeoutHandler extends ChannelInboundHandlerAdapter {
  private static Logger LOG = LoggerFactory.getLogger(TimeoutHandler.class);

  private final long timeoutMillis;
  private volatile ScheduledFuture<?> timeout;
  private volatile long lastReadTime;
  private volatile int state;
  private boolean closed;
  private boolean isClient;

  public TimeoutHandler(int timeoutSeconds, boolean isClient) {
    this((long) timeoutSeconds, TimeUnit.SECONDS, isClient);
  }

  private TimeoutHandler(long timeout, TimeUnit unit, boolean isClient) throws NullPointerException {
    if (unit == null) {
      throw new NullPointerException("Lack of time unit.");
    } else {
      if (timeout <= 0L) {
        this.timeoutMillis = 0L;
      } else {
        this.timeoutMillis = Math.max(unit.toMillis(timeout), 1L);
      }
      this.isClient = isClient;
    }
  }

  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    if (ctx.channel().isActive() && ctx.channel().isRegistered()) {
      this.initialize(ctx);
    }
  }

  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    this.destroy();
  }

  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    if (ctx.channel().isActive()) {
      this.initialize(ctx);
    }
    super.channelRegistered(ctx);
  }

  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    this.initialize(ctx);
    super.channelActive(ctx);
  }

  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    this.destroy();
    super.channelInactive(ctx);
  }

  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    this.lastReadTime = System.currentTimeMillis();
    ctx.fireChannelRead(msg);
  }

  private void initialize(ChannelHandlerContext ctx) {
    switch (this.state) {
      case 1:
      case 2:
        return;
      default:
        this.state = 1;
        this.lastReadTime = System.currentTimeMillis();
        if (this.timeoutMillis > 0L) {
          this.timeout = ctx.executor().schedule(
              new TimeoutHandler.TimeoutTask(ctx), this.timeoutMillis, TimeUnit.MILLISECONDS);
        }
    }
  }

  private void destroy() {
    this.state = 2;
    if (this.timeout != null) {
      this.timeout.cancel(false);
      this.timeout = null;
    }
  }

  private void readTimedOut(ChannelHandlerContext ctx) throws Exception {
    if (!this.closed) {
      ctx.fireExceptionCaught(ReadTimeoutException.INSTANCE);
      LOG.info("{} not receive keepalive message({}s), disconnect from {}", new Object[]{
          this.isClient ? "Client" : "Server", (System.currentTimeMillis() - this.lastReadTime) / 1000L,
          ChannelID.getID(ctx.channel())
      });
      ctx.close();
      long currentTime = System.currentTimeMillis();
      System.out.println("In readTimeout(): CurrentTime - lastReadTime = " + (currentTime - this.lastReadTime));
      this.closed = true;
    }
  }

  private final class TimeoutTask implements Runnable {
    private final ChannelHandlerContext ctx;

    private TimeoutTask(ChannelHandlerContext ctx) {
      this.ctx = ctx;
    }

    @Override
    public void run() {
      if (this.ctx.channel().isOpen()) {
        long currentTime = System.currentTimeMillis();
        long nextDelay = TimeoutHandler.this.timeoutMillis - (currentTime - TimeoutHandler.this.lastReadTime);
//        Object[] obj = new Object[]{TimeoutHandler.this.isClient ? "CLient" : "server"};
        if (nextDelay <= 0L) {
//          System.out.println(String.format("Name: %s, timeoutMillis: %d, currentTime: %d, lastReadTime: %d",
//              obj[0].toString(), TimeoutHandler.this.timeoutMillis, currentTime, TimeoutHandler.this.lastReadTime));
//          System.out.println(String.format("Name: %s, nextDelay: %d", obj[0].toString(), nextDelay));

          TimeoutHandler.this.timeout = this.ctx.executor().schedule(
              this, TimeoutHandler.this.timeoutMillis, TimeUnit.MILLISECONDS);
          try {
            TimeoutHandler.this.readTimedOut(this.ctx);
          } catch (Throwable e) {
            this.ctx.fireExceptionCaught(e);
          }
        } else {
//          System.out.println(String.format("Name: %s, nextDelay: %d", obj[0].toString(), nextDelay));
          TimeoutHandler.this.timeout = this.ctx.executor().schedule(this, nextDelay, TimeUnit.MILLISECONDS);
        }
      }
    }
  }
}
