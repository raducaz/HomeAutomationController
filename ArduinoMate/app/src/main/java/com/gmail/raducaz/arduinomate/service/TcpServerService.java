package com.gmail.raducaz.arduinomate.service;

import android.nfc.Tag;
import android.provider.ContactsContract.Data;
import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

public class TcpServerService implements Runnable {

    private String TAG = "TcpServerService";

    private static TcpServerService sInstance;
    private boolean isRunning;

    private DataRepository dataRepository;

    static final boolean SSL = System.getProperty("ssl") != null;
    static final String HOST = System.getProperty("host", "192.168.11.99");
    static final int PORT = Integer.parseInt(System.getProperty("port", "9090"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));
    // Configure SSL.
    final SslContext sslCtx = null;
    // TODO: Configure SSL if needed
//        if (SSL) {
//            SelfSignedCertificate ssc = new SelfSignedCertificate();
//            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
//        } else {
//            sslCtx = null;
//        }

    private final ExecutorService pool;

    private TcpServerService(DataRepository dataRepository) throws IOException {

        // Initialize a dynamic pool that starts the required no of threads according to the no of tasks submitted
        pool = Executors.newFixedThreadPool(1);
        this.dataRepository = dataRepository;

        // TODO: Initialization parameters can be sent here
    }
    public static TcpServerService getInstance(DataRepository dataRepository) throws IOException {
        if (sInstance == null) {
            synchronized (TcpServerInboundHandler.class) {
                if (sInstance == null) {
                    sInstance = new TcpServerService(dataRepository);
                }
            }
        }
        return sInstance;
    }

    public void run() {

        if(!isRunning) {
            pool.execute(new TcpServerServiceHandler());
            isRunning = true;
        }

    }

    public DataRepository getDataRepository() {
        return dataRepository;
    }

    public void shutdownAndAwaitTermination() {

        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }


    public class TcpServerServiceHandler implements Runnable {

        private final StringDecoder DECODER = new StringDecoder();
        private final StringEncoder ENCODER = new StringEncoder();

        public void run() {

            // Configure the server.
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        //.option(ChannelOption.SO_BACKLOG, 100)
                        .option(ChannelOption.AUTO_READ, true)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline p = ch.pipeline();
                                if (sslCtx != null) {
                                    p.addLast(sslCtx.newHandler(ch.alloc()));
                                }
                                //p.addLast(new LoggingHandler(LogLevel.INFO));
                                p.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                                p.addLast(DECODER);
                                p.addLast(ENCODER);
                                // Important to create a new instance for every new channel to accept multiple client connections
                                p.addLast(new TcpServerInboundHandler(getDataRepository()));
                            }
                        });

                // Start the server.
                ChannelFuture f = b.bind(PORT).sync();

                // Wait until the server socket is closed.
                f.channel().closeFuture().sync();
            }
            catch(InterruptedException exc)
            {
                Log.e(TAG, exc.getMessage());
            }
            catch (Exception generalExc)
            {
                Log.e(TAG, generalExc.getMessage());
            }
            finally {
                // Shut down all event loops to terminate all threads.
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }
    }
}

