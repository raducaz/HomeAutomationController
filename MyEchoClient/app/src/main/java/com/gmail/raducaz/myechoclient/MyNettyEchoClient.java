package com.gmail.raducaz.myechoclient;

/**
 * Created by Radu.Cazacu on 5/25/2017.
 */
import android.app.Activity;
import android.os.AsyncTask;
import android.widget.TextView;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;


import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Sends one message when a connection is open and echoes back any received
 * data to the server.  Simply put, the echo client initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 * String - input param, Integer progress, String Output param
 */
public class MyNettyEchoClient extends AsyncTask<Activity, Void, Void> {

    String sentMsg;
    Activity activity;
    static final boolean SSL = System.getProperty("ssl") != null;
    static final String HOST = System.getProperty("host", "192.168.11.100");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

    public MyNettyEchoClient(String msg)
    {
        this.sentMsg = msg;
    }

    protected Void doInBackground(final Activity... activity) {

        this.activity = activity[0];
        // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("readTimeoutHandler", new ReadTimeoutHandler(5));
                            p.addLast(new MyNettyEchoClientHandler(sentMsg, activity[0]));
                        }
                    });

            // Start the client.
            ChannelFuture f = b.connect(HOST, PORT).sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        }
        catch(InterruptedException exc)
        {
            DisplayMessage(exc.getMessage());
        }
        catch (Exception generalExc)
        {
            DisplayMessage(generalExc.getMessage());
        }
        finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();

            return null;
        }
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