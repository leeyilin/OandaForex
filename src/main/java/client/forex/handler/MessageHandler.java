package client.forex.handler;

import java.io.ByteArrayInputStream;
import java.util.List;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import server.forex.protocol.ForexProtocol;
import server.forex.protocol.MDDecoder;
import server.forex.protocol.MDMessageContainer;
import server.forex.protocol.MDMessage;
import client.forex.input.ForexData;
import client.forex.input.DataPool;


class MessageHandler {
  private ChannelHandlerContext channelHandlerContext;
  private MDDecoder mdDecoder;
  private DataPool<ForexData> dataPool;

  MessageHandler() {
    mdDecoder = new MDDecoder();
    dataPool = new DataPool<>();
  }

  private void requestForexData() {
    Channel channel = this.channelHandlerContext.channel();
    ForexProtocol message = new ForexProtocol((byte) 0, 9, (byte) 2,
        new short[]{3701}, new byte[]{'\0'});
    System.out.println("Begin to send message: " + message.toString());
    channel.writeAndFlush(message);
  }

  void onConnect(ChannelHandlerContext ctx) {
    this.channelHandlerContext = ctx;
    this.requestForexData();
  }

  void onDisConnect(ChannelHandlerContext ctx) {
    ctx.close();
    this.channelHandlerContext = null;
  }

  void onMessage(Object msg) {
    if (msg instanceof ForexProtocol) {
      ForexProtocol message = (ForexProtocol) msg;
      ByteArrayInputStream in = new ByteArrayInputStream(message.getContent());
      this.mdDecoder.setInputStream(in);
      MDMessageContainer mdMessageContainer = new MDMessageContainer();
      if (this.mdDecoder.decode(mdMessageContainer)) {
        List<MDMessage> messages = mdMessageContainer.getMessages();
        for (MDMessage mdMessage : messages) {
          ForexData forexData = new ForexData(mdMessage);
          this.dataPool.add(forexData);
          System.out.println(forexData);
        }
      }
    } else {
      System.out.println("Unknown protocol: " + msg.getClass().getName());
    }
  }
}
