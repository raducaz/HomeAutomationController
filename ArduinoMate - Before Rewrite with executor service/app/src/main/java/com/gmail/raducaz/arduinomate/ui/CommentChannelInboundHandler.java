package com.gmail.raducaz.arduinomate.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.ContactsContract.Data;
import android.widget.TextView;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.CommentEntity;
import com.gmail.raducaz.arduinomate.model.Comment;
import com.gmail.raducaz.arduinomate.viewmodel.CommentViewModel;

import java.nio.charset.Charset;
import java.time.DateTimeException;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.os.Looper;

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

    private static final int ONE_SECOND = 1000;

    private DataRepository dataRepository;
    private Long startCommandTime;
    private Timer timer;
    private CommentEntity comment;
    private boolean isENDReceived;
    private Long responseTimeout;
    private ChannelHandlerContext ctx;
    /**
     * Creates a client-side handler.
     */
    public CommentChannelInboundHandler(CommentEntity comment, DataRepository repository, Long responseWaitTimeout) {
        this.comment = comment;
        dataRepository = repository;
        this.responseTimeout = responseWaitTimeout;
    }
    public CommentChannelInboundHandler(CommentEntity comment, DataRepository repository) {
        this(comment, repository, (long) 10);
    }

    public void closeConnection()
    {
        if(ctx != null)
            ctx.close();

        if(timer != null)
            timer.cancel();
    }

    private void updateCommentLog(String msg, boolean append)
    {
        if(append)
            comment.setLog(comment.getLog() + ">" + msg);
        else
            comment.setLog(msg);

        dataRepository.updateComment(comment);
    }

    private void startResponseCheckerTimer()
    {
        if(responseTimeout > 0) {
            timer = new Timer();

            // Update the elapsed time every second.
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    final long elapsedSecondsFromCommandStart = (SystemClock.elapsedRealtime() - startCommandTime) / 1000;
                    if(elapsedSecondsFromCommandStart >= responseTimeout ) {

                        updateCommentLog("Response timeout.", false);
                        closeConnection();
                        // Access the ctx from the main thread
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                updateCommentLog("Stopped waiting for a response.", false);
//                                ctx.close();
//                            }
//                        });
                    }
                }
            }, ONE_SECOND, ONE_SECOND);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;

        String commandText = comment.getText();

        // Add line terminator so Arduino knows when to interpret the line received
        commandText += "\n";
        ByteBuf sendToServerMessage = Unpooled.wrappedBuffer(commandText.getBytes(io.netty.util.CharsetUtil.US_ASCII));
        ctx.writeAndFlush(sendToServerMessage);

        updateCommentLog("Command sent...", false);

        startCommandTime = SystemClock.elapsedRealtime();
        startResponseCheckerTimer();
    }

//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) {
//
//
//    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        ByteBuf in = (ByteBuf) msg;
        final String sMsg = in.toString(io.netty.util.CharsetUtil.US_ASCII);

        updateCommentLog(sMsg, true);

        if(sMsg.equals("END"))
        {
            isENDReceived = true;
        }

        // Don't want to confirm to server that client received the message
        //ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // Don't want to confirm to server that client received the message - nothing to flush
        //ctx.flush();

        if(isENDReceived)
            closeConnection();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        if (cause instanceof ReadTimeoutException) {
            // timeout occurred

        } else {
            // other exception occurred

        }

        cause.printStackTrace();
        updateCommentLog(cause.getStackTrace().toString(), false);

        // Close the connection when an exception is raised.
        closeConnection();
    }

}

