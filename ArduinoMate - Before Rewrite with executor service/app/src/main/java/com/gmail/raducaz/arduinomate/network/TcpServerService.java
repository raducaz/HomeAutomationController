package com.gmail.raducaz.arduinomate.network;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.gmail.raducaz.arduinomate.Utils;

import java.util.ArrayList;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class TcpServerService extends IntentService {

    private String TAG = "ArduinoTcpServerService";
    private static final int NOTIFICATION_ID = 123;
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SEND_ASCII_TO_CLIENT = 3;
    public static final int MSG_SEND_BYTES_TO_CLIENT = 4;
    public static final int MSG_SEND_ASCII_TO_SERVER = 5;
    public static final int MSG_SEND_BYTES_TO_SERVER = 6;
    public static final int MSG_SEND_ECHO_TO_SERVER = 7;
    public static final int MSG_SEND_STOP_TO_SERVER = 10;
    public static final int MSG_SEND_EXIT_TO_CLIENT  = 20;
    public static final String MSG_KEY = "msg";

    ArrayList<Messenger> mClients = new ArrayList<Messenger>();

    private boolean debug_ = false;

    private TcpClientIncomingHandler tcpClientIncomingHandler;

    /* Constructor */
    public TcpServerService() {
        super("TcpServerService");
    }

    /* Start service and start Tcp Server - Entry point !! */
    @Override
    protected void onHandleIntent(Intent arg0) {

        Log.d(TAG, "onHandleIntent entered, starting Intent TcpServerService");

        startForeground(NOTIFICATION_ID, getNotification());

        // Start the Tcp Server and send status
        final boolean SSL = System.getProperty("ssl") != null;
        final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
        // Configure SSL.
        final SslContext sslCtx;
        sslCtx = null;

        // TODO: Configure SSL if needed
//        if (SSL) {
//            SelfSignedCertificate ssc = new SelfSignedCertificate();
//            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
//        } else {
//            sslCtx = null;
//        }

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        tcpClientIncomingHandler = new TcpClientIncomingHandler();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc()));
                            }
                            //p.addLast(new LoggingHandler(LogLevel.INFO));
                            p.addLast(tcpClientIncomingHandler);
                        }
                    });

            // Start the server.
            ChannelFuture f = b.bind(PORT).sync();

            // Wait until the server socket is closed.
            Log.d(TAG, "Tcp Server started, waiting for connections...");
            f.channel().closeFuture().sync();
        }
        catch(InterruptedException exc)
        {
            Log.d(TAG, "onHandleIntent InterruptedException: " + exc.getMessage());
            //throw exc;
        }
        catch (Exception generalExc)
        {
            throw generalExc;
        }
        finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            Log.d(TAG, "Tcp Server shutdown.");
        }

        stopForeground(true);

        Log.d(TAG, "onHandleIntent exited.");
        stopSelf();
    }

    /**
     * Tcp client connections handler
     * Handles the incoming messages from TCP clients
     */
    @Sharable
    class TcpClientIncomingHandler extends ChannelInboundHandlerAdapter {

        private ChannelHandlerContext ctx;

        public void forceClose()
        {
            if(ctx != null)
                ctx.close();
        }
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            this.ctx = ctx;

            // Great the client with a message
            String greatingMsg = "Hi" + "\n";
            ByteBuf sendToClientMessage = Unpooled.wrappedBuffer(greatingMsg.getBytes(io.netty.util.CharsetUtil.US_ASCII));
            ctx.writeAndFlush(sendToClientMessage);
        }
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf in = (ByteBuf) msg;
            final String receivedMsg = in.toString(io.netty.util.CharsetUtil.US_ASCII);

            sendStringToUIClients(receivedMsg);

            // Don't want to confirm to client that server received the message
            //ctx.write(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            // TODO: Check if Don't want to confirm to server that client received the message - nothing to flush ?
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // Close the connection when an exception is raised.
            cause.printStackTrace();
            ctx.close();
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger_ = new Messenger(new IncomingHandler());
    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger_.getBinder();
    }

    /** Messenger helper method - used to send message to Android service clients (ex. Main Activity)
     * The message is sent finally by IncomingHandler SubClass
     */
    public void sendStringToUIClients(String message) {
        try {
            Message msg = Message.obtain(null, TcpServerService.MSG_SEND_ASCII_TO_CLIENT, message);

            Bundle b = new Bundle();
            b.putCharSequence(TcpServerService.MSG_KEY, message);
            msg.setData(b);
            msg.replyTo = mMessenger_;
            mMessenger_.send(msg);
        } catch (RemoteException e) {
            Log.d(TAG, "sendMessageToClients Exception: " + Utils.getExceptionStack(e, true));
        }
    }
    /** Messenger helper method - used to send data to Android service clients (ex. Main Activity)
     * The message is sent finally by IncomingHandler SubClass
     */
    public void sendBytesToUIClients(byte[] data) {
        try {

            mMessenger_.send(createByteMessage(data,MSG_SEND_BYTES_TO_CLIENT));

        } catch (RemoteException e) {
            Log.d(TAG, "sendBytesToUIClients Exception: " + Utils.getExceptionStack(e, true));
        }
    }
    /** Messenger helper method - used to send exit signal to the Messenger handler
     * The message is sent finally by IncomingHandler SubClass
     */
    public void sendExitToUIClients() {

        sendMessageTo(createStringMessage(null, MSG_SEND_EXIT_TO_CLIENT), mMessenger_);

    }

    /** Create a Message instance with the text and specified What
     */
    public Message createByteMessage(byte[] data, int msgWhat)
    {
        Bundle b = new Bundle();
        b.putByteArray(TcpServerService.MSG_KEY, data);

        Message msg = Message.obtain(null, msgWhat);
        msg.setData(b);

        return msg;
    }
    public Message duplicateMessage(Message msg)
    {
        Message m = Message.obtain(null, msg.what);
        m.replyTo = msg.replyTo;
        m.setData(msg.getData());

        return m;
    }
    /** Create a Message instance with the data and specified What
     */
    public Message createStringMessage(String message, int msgWhat)
    {
        Bundle b = new Bundle();
        b.putCharSequence(TcpServerService.MSG_KEY, message);

        Message msg = Message.obtain(null, msgWhat);
        msg.setData(b);

        return msg;
    }
    /** Send the Message to the specified Messenger
     */
    public void sendMessageTo(Message message, Messenger to)
    {
        try {
            to.send(message);
        }
        catch (RemoteException e)
        {
            Log.d(TAG, "sendMessage: " + Utils.getExceptionStack(e, true));
        }
    }
    /** Send the Message to all the registered clients in our mMessenger_ Messenger
     */
    public void sendMessageToAllRegisteredClients(Message message)
    {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                mClients.get(i).send(message);
            } catch (RemoteException e) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
                Log.d(TAG, "sendMessageToAllRegistered(" + i + "):" + Utils.getExceptionStack(e, true));
            }
        }
    }
    /**
     * Messenger internal logic
     * Handles the incoming messages from UI clients (like MainActivity) and from self (like Accessory receive Thread)
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try {
                Log.d(TAG, "Service handleMessage: " +  msg.what);

                // Unpack the message data to Log it
                Bundle msgBundle = msg.getData();
                String msgText = null;
                byte[] msgData = new byte[0];

                Object msgDataObject = msgBundle.get(TcpServerService.MSG_KEY);
                if(msgDataObject != null) {
                    if (msgDataObject.getClass() == String.class)
                        msgText = (String) msgDataObject;

                    if (msgDataObject.getClass() == byte[].class)
                        msgData = (byte[]) msgDataObject;
                }

                switch (msg.what) {
                    case MSG_REGISTER_CLIENT:

                        Log.d(TAG, "Service handleMessage: MSG_REGISTER_CLIENT " + msg.replyTo);

                        // Register the client in the Messenger Handler
                        mClients.add(msg.replyTo);

                        break;
                    case MSG_UNREGISTER_CLIENT:
                        Log.d(TAG, "Service handleMessage: MSG_UNREGISTER_CLIENT " + msg.replyTo);

                        // UnRegister the client from the Messenger Handler
                        mClients.remove(msg.replyTo);

                        break;
                    case MSG_SEND_EXIT_TO_CLIENT:
                        Log.d(TAG, "Service handleMessage: MSG_SEND_EXIT_TO_CLIENT " + mClients.size());

                        sendMessageToAllRegisteredClients(createStringMessage(null, MSG_SEND_EXIT_TO_CLIENT));

                        break;
                    case MSG_SEND_ASCII_TO_CLIENT:
                        sendMessageToAllRegisteredClients(duplicateMessage(msg));
                        break;
                    case MSG_SEND_BYTES_TO_CLIENT:
                        sendMessageToAllRegisteredClients(duplicateMessage(msg));
                        break;
                    case MSG_SEND_ASCII_TO_SERVER:
                        // Should send data to TcpClients
                        break;
                    case MSG_SEND_BYTES_TO_SERVER:
                        // Should send data to TcpClients
                        break;
                    case MSG_SEND_STOP_TO_SERVER:
                        // Stop the TCP Server
                        tcpClientIncomingHandler.forceClose();
                        break;
                    case MSG_SEND_ECHO_TO_SERVER:
                        sendMessageToAllRegisteredClients(duplicateMessage(msg));
                        break;
                    default:
                        super.handleMessage(msg);
                }
            } catch (Exception ee) {
                if (debug_) {
                    Log.e(TAG, "Server handleMessage Exception: "+ Utils.getExceptionStack(ee, true));
                }
            }
        }
    }

    /* Notification used by startForeground method. Enable users to access the stop service action */
    Notification getNotification() {

        // TODO: Fill in the details on client connect
        Context context = getApplicationContext();
        String contentTitle = "Tcp Server";
        String contentText = "Starting Tcp Server...";

        // This can be changed if we want to launch an activity when notification clicked
        Intent notificationIntent = new Intent();
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(context)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(contentIntent)
                .build();

        return notification;
    }

}
