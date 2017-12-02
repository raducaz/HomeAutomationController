//package com.gmail.raducaz.arduinomate.service;
//
//import android.util.Log;
//
//import com.gmail.raducaz.arduinomate.DataRepository;
//import com.gmail.raducaz.arduinomate.db.entity.ProductEntity;
//
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelFutureListener;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.channel.group.ChannelGroup;
//import io.netty.channel.group.DefaultChannelGroup;
//import io.netty.util.concurrent.GlobalEventExecutor;
//
///**
// * Created by Radu.Cazacu on 12/1/2017.
// */
//
//public class TcpServerInboundHandlerBackup extends ChannelInboundHandlerAdapter {
//
//    private String TAG = "TcpServerInboundHandler";
//    private static TcpServerInboundHandlerBackup sInstance;
//
//    private DataRepository dataRepository;
//
//    protected ChannelHandlerContext ctx;
//    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
//
//    private TcpServerInboundHandlerBackup(DataRepository repository) {
//        dataRepository = repository;
//    }
//    public static TcpServerInboundHandlerBackup getInstance(DataRepository repository) {
//        if (sInstance == null) {
//            synchronized (TcpServerInboundHandlerBackup.class) {
//                if (sInstance == null) {
//                    sInstance = new TcpServerInboundHandlerBackup(repository);
//                }
//            }
//        }
//        return sInstance;
//    }
//
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) {
//
//        this.ctx = ctx;
//
//        Channel incoming = ctx.channel();
//        channels.add(ctx.channel());
//
//        final ChannelFuture f = incoming.writeAndFlush("\r\n");
//        f.addListener(new ChannelFutureListener() {
//            public void operationComplete(ChannelFuture future) {
//                System.out.println("Complete");
//            }
//        });
//
//        for(Channel channel : channels) {
//            channel.writeAndFlush("[Server] - " + incoming.remoteAddress() + " has joined!\r\n");
//        }
//
//        System.out.println( "Players online: " + channels.size() );
//
//        // Add line terminator so Arduino knows when to interpret the line received
////        String msg = "OK\n";
////        ByteBuf sendToServerMessage = Unpooled.wrappedBuffer(msg.getBytes(io.netty.util.CharsetUtil.US_ASCII));
////        ctx.writeAndFlush(sendToServerMessage);
//    }
//
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) {
//
//        Channel incoming = ctx.channel();
//        for(Channel channel : channels) {
//            channel.writeAndFlush("[Server] - " + incoming.remoteAddress() + " has left!\r\n");
//        }
//        System.out.println( "[Server] - " + incoming.remoteAddress().toString() + " has left!" );
//        //channels.remove(ctx.channel());
//        System.out.println( "Players online: " + channels.size() );
//    }
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//
//        ByteBuf in = (ByteBuf) msg;
//        final String sMsg = in.toString(io.netty.util.CharsetUtil.US_ASCII);
//
//        // TODO: Handle the message from the client
//        ProductEntity productEntity = dataRepository.loadProductSync(1);
//        productEntity.setDescription(sMsg);
//        dataRepository.updateProduct(productEntity);
//
//        // Don't want to confirm to client that server received the message
//        //ctx.write(msg);
//    }
//
//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) {
//        // TODO: Check if Don't want to confirm to server that client received the message - nothing to flush ?
//        ctx.flush();
//    }
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//
//        cause.printStackTrace();
//        Log.e(TAG, cause.getMessage(), cause);
//        // Close the connection when an exception is raised.
//        closeConnection();
//    }
//
//    public void closeConnection()
//    {
//        if(ctx != null)
//            ctx.close();
//    }
//
//}
