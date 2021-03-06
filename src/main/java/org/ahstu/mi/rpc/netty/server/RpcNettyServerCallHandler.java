package org.ahstu.mi.rpc.netty.server;


import org.ahstu.mi.common.MiLogger;
import org.ahstu.mi.common.MiResult;
import org.ahstu.mi.common.MiSendDTO;
import org.ahstu.mi.provider.MiServiceDynamicCall;
import org.ahstu.mi.rpc.netty.MiChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by renyueliang on 17/5/18.
 */
public class RpcNettyServerCallHandler extends MiChannelHandlerAdapter {

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        MiSendDTO sendDTO = (MiSendDTO) msg;

        try {
            if (sendDTO != null) {

                MiLogger.record("server receive message requestId:" + sendDTO.getRequestId());
                long start = System.currentTimeMillis();
                MiResult miResult = MiServiceDynamicCall.call(sendDTO);
                miResult.setRequestId(sendDTO.getRequestId());
                long end = System.currentTimeMillis();
                MiLogger.record("server method call requestId:" + sendDTO.getRequestId()
                        + ", spend time: " + (end - start) + "ms");
                sendMessage(miResult, ctx);
                long sendEnd = System.currentTimeMillis();
                MiLogger.record("server send result requestId:" + sendDTO.getRequestId() + ", spend time: "
                        + (sendEnd - end) + "ms, " + (sendEnd - start) + "ms");

            }
        } catch (Throwable e) {
            MiLogger.getLogger().error("requestId:" + sendDTO.getRequestId() + " errorCode:" + e.getMessage(), e);
        }

    }

    private void sendMessage(MiResult miResult, ChannelHandlerContext ctx) {
        ctx.writeAndFlush(miResult);

    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelReadComplete();
        ctx.flush();
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
