package com.gmail.raducaz.myechoclient;

/**
 * Created by Radu.Cazacu on 5/25/2017.
 */

import android.app.Activity;
import android.widget.TextView;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.CharsetUtil;

/**
 * Handler implementation for the echo client.  It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class MyNettyEchoClientHandler extends ChannelInboundHandlerAdapter {

    private String sentMsg;
    private final Activity activity;

    /**
     * Creates a client-side handler.
     */
    public MyNettyEchoClientHandler(String msg, Activity activity) {
        this.sentMsg = msg;
        this.activity = activity;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Add line terminator
        sentMsg += "\n";
        ByteBuf sendToServerMessage = Unpooled.wrappedBuffer(sentMsg.getBytes(io.netty.util.CharsetUtil.US_ASCII));
        ctx.writeAndFlush(sendToServerMessage);

        DisplayMessage("\nSENT:" + sentMsg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        final String sMsg = in.toString(io.netty.util.CharsetUtil.US_ASCII);

        DisplayMessage("\nRECEIVED:" + sMsg);

        // Don't want to confirm to server that client received the message
        //ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // Don't want to confirm to server that client received the message - nothing to flush
        //ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        if (cause instanceof ReadTimeoutException) {
            // timeout occurred
            DisplayMessage("Server response timeout error occurred.");
        } else {
            // other exception occurred
        }

        // Close the connection when an exception is raised.
        cause.printStackTrace();
        DisplayMessage(cause.getStackTrace().toString());
        ctx.close();
    }

    private void DisplayMessage(final String msg)
    {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView tvReceivedMsg = (TextView)activity.findViewById(R.id.tvReceivedMsg);
                tvReceivedMsg.append("\n" + msg);
            }
        });
    }
}

