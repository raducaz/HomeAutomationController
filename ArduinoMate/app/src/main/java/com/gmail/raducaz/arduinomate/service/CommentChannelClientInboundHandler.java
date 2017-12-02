package com.gmail.raducaz.arduinomate.service;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.CommentEntity;

import io.netty.channel.ChannelHandlerContext;

/**
 * Handler implementation for the echo client.  It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class CommentChannelClientInboundHandler extends TcpClientInboundHandler {

    private DataRepository dataRepository;

    private CommentEntity comment;

    /**
     * Creates a client-side handler.
     */
    public CommentChannelClientInboundHandler(CommentEntity comment, DataRepository repository, Long responseWaitTimeout) {
        this.comment = comment;
        dataRepository = repository;
        super.responseTimeout = responseWaitTimeout;
    }
    public CommentChannelClientInboundHandler(CommentEntity comment, DataRepository repository) {
        this(comment, repository, (long) 10);
    }


    private void updateCommentLog(String msg, boolean append)
    {
        if(append)
            comment.setLog(comment.getLog() + ">" + msg);
        else
            comment.setLog(msg);

        dataRepository.updateComment(comment);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        String commandText = comment.getText();
        channelActive(ctx, commandText);
        updateCommentLog("Command sent...", false);

        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, String msg) {

        updateCommentLog(msg, true);

        // Don't want to confirm to server that client received the message
        //ctx.write(msg);

        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        super.channelReadComplete(ctx);
    }

    public void onResponseTimeout()
    {
        updateCommentLog("Response timeout.", false);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        updateCommentLog(cause.getStackTrace().toString(), false);

        super.exceptionCaught(ctx, cause);
    }

}

