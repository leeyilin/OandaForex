package client.forex.handler;

//import com.em.mdsmq.listener.Listener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.forex.util.datastruct.ChannelID;
import server.forex.util.datetime.DateUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter {
  private static Logger LOG = LoggerFactory.getLogger(ClientHandler.class);

  private MessageHandler messageHandler;

  public ClientHandler() {
    messageHandler = new MessageHandler();
  }

  public void channelActive(ChannelHandlerContext ctx) throws InterruptedException {
    ctx.fireChannelActive();
    System.out.println("Connected Server: " + ChannelID.getID(ctx.channel()));
    LOG.info("Connected Server,{}", ChannelID.getID(ctx.channel()));
    this.messageHandler.onConnect(ctx);
  }

  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    ctx.fireChannelInactive();
    System.out.println("Inactive from Server: " + ChannelID.getID(ctx.channel()));

    long currenttime = DateUtil.getCuryyyyyyyyMMddHHmmssL();
    int timeStamp = (int) (currenttime % 1000000L);
    System.out.println("The channel will be closed...Current Time: " + timeStamp);
    LOG.info("Inactive@Server,{}", ChannelID.getID(ctx.channel()));
    this.messageHandler.onDisConnect(ctx);
  }

  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ctx.fireChannelRead(msg);
    this.messageHandler.onMessage(msg);
  }
}
