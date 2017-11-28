package com.gmail.raducaz.arduinomate.ui;

import android.app.Activity;
import android.app.Fragment;
import android.widget.TextView;

import com.gmail.raducaz.arduinomate.model.Comment;

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
public class CommentChannelInboundHandler extends ChannelInboundHandlerAdapter {

    private Comment comment;

    /**
     * Creates a client-side handler.
     */
    public CommentChannelInboundHandler(Comment comment) {
        this.comment = comment;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        String commandText = comment.getText();
        // Add line terminator
        commandText += "\n";
        ByteBuf sendToServerMessage = Unpooled.wrappedBuffer(commandText.getBytes(io.netty.util.CharsetUtil.US_ASCII));
        ctx.writeAndFlush(sendToServerMessage);

        comment.setLog("SEND:"+commandText);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        final String sMsg = in.toString(io.netty.util.CharsetUtil.US_ASCII);

        comment.setLog("RECEIVED:"+sMsg);
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

        } else {
            // other exception occurred

        }

        // Close the connection when an exception is raised.
        cause.printStackTrace();
        comment.setLog(cause.getStackTrace().toString());
        ctx.close();
    }

}

