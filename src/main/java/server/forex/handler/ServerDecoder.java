package server.forex.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import server.forex.protocol.ForexProtocol;

public class ServerDecoder extends ByteToMessageDecoder {
  public ServerDecoder() {
  }

  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
    if (in.readableBytes() >= 5) {
      in.markReaderIndex();
      byte split = in.readByte();
      if (split != 0) {
        throw new Exception("Unrecognized protocol!");
      } else {
        int packLen = in.readInt();
        if (in.readableBytes() < packLen - 5) {
          in.resetReaderIndex();
        } else {
          byte type = in.readByte();
          byte topicCount = in.readByte();
          short[] topic = new short[topicCount];
          for (byte i = 0; i < topicCount; ++i) {
            topic[i] = in.readShort();
          }
          if (packLen - (7 + topicCount * 2) > 0) {
            byte[] content = new byte[packLen - (7 + topicCount * 2)];
            in.readBytes(content);
            Object message = new ForexProtocol(split, packLen, type, topic, content);
            out.add(message);
          }
        }
      }
    }
  }
}
