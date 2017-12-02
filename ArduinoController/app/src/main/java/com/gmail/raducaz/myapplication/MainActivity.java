package com.gmail.raducaz.myapplication;

import mytcphelper.TcpClient;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView response = (TextView) findViewById(R.id.text);

        TcpClient myClient = new TcpClient("192.168.11.100", 8080, response);
        myClient.execute();
    }
}
