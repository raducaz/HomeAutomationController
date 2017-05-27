package com.gmail.raducaz.myechoclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
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

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button)findViewById(R.id.btnSend);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                final EditText txtMsg = (EditText)findViewById(R.id.txtMsg);
                String msg = txtMsg.getText().toString();

                MyNettyEchoClient nettyClient = new MyNettyEchoClient(msg);
                nettyClient.execute(MainActivity.this);
                txtMsg.setText("");
            }
        });


    }


}
