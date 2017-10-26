package server.forex.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.forex.util.datastruct.ChannelID;
import server.forex.register.DataPusher;
import server.forex.protocol.ForexProtocol;
import server.forex.util.datastruct.ChannelData;

public class ServerHandler extends ChannelInboundHandlerAdapter {
  private static Logger LOG = LoggerFactory.getLogger(ServerHandler.class);

  public ServerHandler() {
  }

  public void channelActive(ChannelHandlerContext ctx) {
    ctx.fireChannelActive();
    System.out.println(String.format("One Client: %s connected! See what it requests...", ChannelID.getID(ctx.channel())));
    LOG.info("One Client,{}", ChannelID.getID(ctx.channel()));
  }

  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ctx.fireChannelRead(msg);
    ForexProtocol message = (ForexProtocol) msg;
    if (message.getType() == 2) {
      System.out.println("Server receives a new request: " + message.toString());
      for (int i = 0; i < message.getTopic().length; ++i) {
        LOG.info("Recv Client... {}", message.getTopic()[i]);
        DataPusher.getInstance().regist(message.getTopic()[i], new ChannelData(ctx.channel()));
      }
    } else {
//      System.out.println("Unsupported message type: " + message.getType());
    }
  }

  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    ctx.fireChannelInactive();
    System.out.println(String.format("Inactive Client: %s", ChannelID.getID(ctx.channel())));
    LOG.info("Inactive@Client,{}", ChannelID.getID(ctx.channel()));
    DataPusher.getInstance().cancel(ChannelID.getID(ctx.channel()));
  }
}

