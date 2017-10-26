package server.forex;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.buffer.PooledByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.forex.config.ServerConfig;
import server.forex.handler.ServerDecoder;
import server.forex.handler.ServerEncoder;
import server.forex.handler.ServerHandler;
import server.forex.handler.KeepAliveHandler;
import server.forex.handler.TimeoutHandler;


public class ServerStartup implements Runnable {
  private static Logger LOG = LoggerFactory.getLogger(ServerStartup.class);

  private final int port;
  private short keepAliveInterval;
  private short keepAliveTimeout;

  public ServerStartup(ServerConfig serverConfig) {
    this.port = serverConfig.getPort();
    this.keepAliveInterval = (short) serverConfig.getCheckInterval();
    this.keepAliveTimeout = (short) serverConfig.getTimeout();
  }

  @Override
  public void run() {
    this.launch();
  }

  private void launch() {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ServerStartup.ServerInitializer())
          .option(ChannelOption.SO_BACKLOG, 1000)
          .option(ChannelOption.SO_REUSEADDR, true)
          .option(ChannelOption.SO_RCVBUF, 262144)
          .option(ChannelOption.SO_SNDBUF, 262144)
          .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
          .childOption(ChannelOption.SO_KEEPALIVE, true)
          .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

      ChannelFuture f = b.bind(this.port).sync();
      f.channel().closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

  public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    public ServerInitializer() {
    }

    public void initChannel(SocketChannel ch) throws Exception {
      ChannelPipeline pipeline = ch.pipeline();
      pipeline.addLast("decoder", new ServerDecoder());
      pipeline.addLast("encoder", new ServerEncoder());
      pipeline.addLast("handler", new ServerHandler());
      pipeline.addLast("keepAliveHandler", new KeepAliveHandler(false, ServerStartup.this.keepAliveInterval));
      pipeline.addLast("timeoutHandler", new TimeoutHandler(ServerStartup.this.keepAliveTimeout, false));
    }
  }

  public static void main(String[] args) throws Exception {
    ServerConfig myObj = ServerConfig.getInstance();
    myObj.loadConfigFile("config/server-forex.xml");
    Thread t = new Thread(new ServerStartup(myObj));
    t.setDaemon(false);
    t.run();

    System.out.println("Arrive here..");
  }
}
