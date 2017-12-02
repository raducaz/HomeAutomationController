package com.gmail.raducaz.arduinomate.network;

import android.os.AsyncTask;

import com.gmail.raducaz.arduinomate.ui.CommentChannelInboundHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Sends one message when a connection is open and echoes back any received
 * data to the server.  Simply put, the echo client initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 * String - input param, Integer progress, String Output param
 */
//AsyncTask<Params, Progress, Result>
public class TcpClient extends AsyncTask<ChannelInboundHandlerAdapter, Void, Void> {

    private static TcpClient sInstance;

    static final boolean SSL = System.getProperty("ssl") != null;
    static final String HOST = System.getProperty("host", "192.168.11.100");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

    private ChannelInboundHandlerAdapter channelInboundHandler;
    EventLoopGroup group;
    private boolean isTaskRunning;

    public TcpClient(String serverIp, String serverPort)
    {

        // TODO: Initialization parameters can be sent here
    }
    public void stop()
    {
        if(isTaskRunning && channelInboundHandler != null)
            ((CommentChannelInboundHandler) channelInboundHandler).closeConnection();
    }
    protected Void doInBackground(ChannelInboundHandlerAdapter... channelInboundHandlers) {
        if(channelInboundHandlers.length == 0) return null;

        isTaskRunning = true;

        channelInboundHandler = channelInboundHandlers[0];

        // Configure the client.
        group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    //.option(ChannelOption.TCP_NODELAY, true) // incompatible with autoread option
                    .option(ChannelOption.AUTO_READ, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                    .handler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(channelInboundHandler);
                        }
                    });

            // Start the client.
            ChannelFuture f = b.connect(HOST, PORT).sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        }
        catch(InterruptedException exc)
        {
            throw exc;
        }
        catch (Exception generalExc)
        {
            throw generalExc;
        }
        finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();

            isTaskRunning = false;

            return null;
        }
    }

}