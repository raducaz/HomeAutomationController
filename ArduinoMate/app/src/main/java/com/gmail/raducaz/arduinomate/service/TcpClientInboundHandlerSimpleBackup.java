//package com.gmail.raducaz.arduinomate.service;
//
//import android.os.SystemClock;
//import android.util.Log;
//
//import java.util.Timer;
//import java.util.TimerTask;
//
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.channel.SimpleChannelInboundHandler;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.timeout.ReadTimeoutException;
//
///**
// * Created by Radu.Cazacu on 12/1/2017.
// */
//
//public abstract class TcpClientInboundHandlerSimpleBackup extends SimpleChannelInboundHandler<String> {
//
//    private String TAG = "TcpClientInboundHandlerSimpleBackup";
//
//    private static final int ONE_SECOND = 1000;
//    private Long startCommandTime;
//    private Timer timer;
//    private boolean isENDReceived;
//    protected Long responseTimeout;
//    protected ChannelHandlerContext ctx;
//
//
//    private void startResponseCheckerTimer()
//    {
//        if(responseTimeout > 0) {
//            timer = new Timer();
//
//            // Update the elapsed time every second.
//            timer.scheduleAtFixedRate(new TimerTask() {
//                @Override
//                public void run() {
//                    final long elapsedSecondsFromCommandStart = (SystemClock.elapsedRealtime() - startCommandTime) / 1000;
//                    if(elapsedSecondsFromCommandStart >= responseTimeout ) {
//
//                        onResponseTimeout();
//                        closeConnection();
//
//                        // Access the ctx from the main thread
////                        new Handler(Looper.getMainLooper()).post(new Runnable() {
////                            @Override
////                            public void run() {
////                                updateCommentLog("Stopped waiting for a response.", false);
////                                ctx.close();
////                            }
////                        });
//                    }
//                }
//            }, ONE_SECOND, ONE_SECOND);
//        }
//    }
//
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) {
//
//        this.ctx = ctx;
//
//        startCommandTime = SystemClock.elapsedRealtime();
//        startResponseCheckerTimer();
//    }
//    public void channelActive(ChannelHandlerContext ctx, String msg)
//    {
//        Channel outcoming = ctx.channel();
//        Log.d(TAG, "ChannelActive ");
//
//        // Add line terminator so Arduino knows when to interpret the line received
//        outcoming.writeAndFlush(msg);
//    }
//
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) {
//
//        Channel outcoming = ctx.channel();
//        Log.d(TAG, "ChannelInactive ");
//    }
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, String msg)
//    {
//        Channel outcoming = ctx.channel();
//        Log.d(TAG, "ChannelRead0-MSG " + msg);
//
//        if (msg.endsWith("END") || msg.endsWith("END\r\n")) {
//            Log.d(TAG, "ChannelRead0-END ");
//            closeConnection();
//        }
//    }
//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) {
//        // Don't want to confirm to server that client received the message - nothing to flush
//        //ctx.flush();
//    }
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//
//        if (cause instanceof ReadTimeoutException) {
//            // timeout occurred
//
//        } else {
//            // other exception occurred
//
//        }
//
//        cause.printStackTrace();
//        Log.e(TAG, cause.getMessage(), cause);
//
//        // Close the connection when an exception is raised.
//        closeConnection();
//    }
//
//    public void closeConnection()
//    {
//        if(ctx != null)
//            ctx.close();
//
//        if(timer != null)
//            timer.cancel();
//    }
//
//    public abstract void onResponseTimeout();
//}
