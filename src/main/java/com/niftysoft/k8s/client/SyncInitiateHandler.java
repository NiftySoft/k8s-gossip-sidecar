package com.niftysoft.k8s.client;

import com.niftysoft.k8s.data.stringstore.VolatileStringStore;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/** @author K. Alex Mills */
public class SyncInitiateHandler extends SimpleChannelInboundHandler {
  private static final InternalLogger log =
      InternalLoggerFactory.getInstance(SyncInitiateHandler.class);

  private final VolatileStringStore myStore;

  public SyncInitiateHandler(VolatileStringStore store) {
    this.myStore = store;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    log.debug("Initiating sync.");

    synchronized (myStore) { // Obtain write lock.
      log.debug("Writing local store during sync initiation.");
      ChannelFuture future = ctx.writeAndFlush(myStore);
    }
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, Object msg) {
    assert (msg.getClass().equals(VolatileStringStore.class));
    log.debug("Received remote store during client-initiated sync.");

    ctx.channel().close();

    VolatileStringStore otherStore = (VolatileStringStore) msg;

    myStore.mergeAllFresher(otherStore);
  }

  public void channelReadComplete(ChannelHandlerContext ctx) {

  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }
}
