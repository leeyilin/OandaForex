package server.forex.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.forex.protocol.ForexProtocol;

public class ServerEncoder extends MessageToByteEncoder {
  private static Logger LOG = LoggerFactory.getLogger(ServerEncoder.class);

  public ServerEncoder() {
  }

  protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) {
    if (in instanceof ForexProtocol) {
      ForexProtocol message = (ForexProtocol) in;
      out.writeByte(message.getSplit());
      out.writeInt(message.getPackLen());
      out.writeByte(message.getType());
      out.writeByte(message.getTopicCount());
      for (byte i = 0; i < message.getTopicCount(); ++i) {
        out.writeShort(message.getTopic()[i]);
      }
      if (message.getContent() != null) {
        out.writeBytes(message.getContent());
      }
      out.setInt(1, out.writerIndex());
    }
  }
}
