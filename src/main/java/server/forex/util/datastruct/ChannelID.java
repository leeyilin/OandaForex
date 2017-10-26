package server.forex.util.datastruct;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

public class ChannelID {
  public ChannelID() {
  }

  public static String getID(Channel channel) {
    InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
    return socketAddress.getHostString() + ":" + socketAddress.getPort();
  }
}