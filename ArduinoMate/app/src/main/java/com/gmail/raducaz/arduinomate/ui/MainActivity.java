package com.gmail.raducaz.arduinomate.ui;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.Utils;
import com.gmail.raducaz.arduinomate.model.Comment;
import com.gmail.raducaz.arduinomate.model.Product;
import com.gmail.raducaz.arduinomate.service.TcpServerIntentService;

public class MainActivity extends AppCompatActivity {

    public static final int CHAR_SEQUENCE_TYPE 				= 10;
    public static final int BYTE_SEQUENCE_TYPE 				= 11;
    public static final int INFO_MESSAGE_TYPE 				= 22;
    public static final int DEBUG_MESSAGE_TYPE 				= 24;
    public static final int CONNECTION_ACTION 				= 100;

    private static final String TAG = "ArduinoMateMainActivity";

    /** Messenger for communicating with Tcp Server Service. */
    Messenger mService_ = null;
    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;
    boolean debug_ = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Add product list fragment if this is first creation
        if (savedInstanceState == null) {
            ProductListFragment fragment = new ProductListFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, ProductListFragment.TAG).commit();
        }

        // TODO: Start the service intent and bind to it - this should be running continuously
        // Start service intent...here
        doBindService();
    }

    /** Shows the product detail fragment */
    public void show(Product product) {

        ProductFragment productFragment = ProductFragment.forProduct(product.getId());

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("product")
                .replace(R.id.fragment_container,
                        productFragment, null).commit();
    }

    /** Shows the comment detail fragment */
    public void show(Comment comment) {

        CommentFragment commentFragment = CommentFragment.forComment(comment.getId());

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("comment")
                .replace(R.id.fragment_container,
                        commentFragment, null).commit();


    }

    public void startTcpServerService(Comment comment)
    {
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, TcpServerIntentService.class);
        // Add extras to the bundle
        i.putExtra("foo", "bar");
        // Start the service
        startService(i);
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger_ = new Messenger(new IncomingHandler());

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try {
                Log.d(TAG, "USBActivity handleMessage: " + msg.what);
                switch (msg.what) {
                    case TcpServerIntentService.MSG_SEND_ASCII_TO_CLIENT:
                        Bundle b = msg.getData();
                        CharSequence asciiMessage = b.getCharSequence(TcpServerIntentService.MSG_KEY);
                        logMessage("USBActivity handleMessage: TO_CLIENT " + asciiMessage);
                        showMessage(asciiMessage);
                        break;
                    case TcpServerIntentService.MSG_SEND_BYTES_TO_CLIENT:
                        Bundle bb = msg.getData();
                        byte[] data = bb.getByteArray(TcpServerIntentService.MSG_KEY);
                        signalToUi(BYTE_SEQUENCE_TYPE, data);
                        break;
                    case TcpServerIntentService.MSG_SEND_ASCII_TO_SERVER:
                        Bundle sb = msg.getData();
                        CharSequence sAsciiMessage = sb.getCharSequence(TcpServerIntentService.MSG_KEY);
                        Log.d(TAG, "USBActivity handleMessage: TO_SERVER " + sAsciiMessage);
                        showMessage(sAsciiMessage);
                        break;
                    case TcpServerIntentService.MSG_SEND_EXIT_TO_CLIENT:
                        try {
                            if (debug_) showMessage("on Exit Signal\n");
                        } catch (Exception e) {
                            if (debug_) showMessage("Close App: " +e.getMessage() + "\n");
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                }
            } catch (Exception ee) {
                if (debug_) {
                    Log.e(TAG, "Client handleMessage Exception: "+ Utils.getExceptionStack(ee, true));
                }
            }
        }
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService_ = new Messenger(service);
            logMessage("Attached.");

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        TcpServerIntentService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger_;
                mService_.send(msg);

            } catch (RemoteException e) {
                logMessage("Problem connecting to Server: " + e.getMessage());
            }

            // As part of the sample, tell the user what happened.
            logMessage("Server Connected");
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService_ = null;
            logMessage("Server Disconnected");
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(this,
                TcpServerIntentService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        logMessage("Bound.");
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService_ != null) {
                try {
                    Message msg = Message.obtain(null,
                            TcpServerIntentService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger_;
                    mService_.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            logMessage("Unbound.");
        }
    }

    protected void showMessage(CharSequence message) {
        signalToUi(CHAR_SEQUENCE_TYPE, message);
    }

    @Override
    public void onDestroy() {
        logMessage("onDestroy");
        close();

        doUnbindService();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        logMessage("onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        logMessage("onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        logMessage("onStop");
        doUnbindService();
        super.onStop();
    }

    @Override
    public void onStart() {
        logMessage("onStart");
        doBindService();
        super.onStart();
    }

    @Override
    public void onRestart() {
        logMessage("onRestart");
        super.onRestart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: Handle item selection in the preferences menu
        switch (item.getItemId()) {
//            case R.id.mainMenuConnect:
//                onConnectMenu();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onConnectMenu() {

        doUnbindService();

        Intent stopServiceIntent = new Intent(this, TcpServerIntentService.class);
        this.stopService(stopServiceIntent);

        SystemClock.sleep(1500);

        Intent startServiceIntent = new Intent(this, TcpServerIntentService.class);
        startService(startServiceIntent);

        SystemClock.sleep(1500);
        doBindService();
    }

    public void disconnect() {
        doUnbindService();
    }

    public void close() {
        disconnect();
    }

    public void signalToUi(int type, Object data) {
        Runnable runnable = null;
        if (type == CONNECTION_ACTION) {
            onConnectMenu();
        } else if (type == CHAR_SEQUENCE_TYPE) {
            if (data == null || ((CharSequence) data).length() == 0) {
                return;
            }
            final CharSequence tmpData = (CharSequence) data;
            //addToHistory(tmpData);
        } else if (( type == DEBUG_MESSAGE_TYPE || type == INFO_MESSAGE_TYPE )) {
            if (data == null || ((CharSequence) data).length() == 0) {
                return;
            }
            final CharSequence tmpData = (CharSequence) data;
            //addToHistory(tmpData);
        } else if (type == BYTE_SEQUENCE_TYPE) {
            if (data == null || ((byte[]) data).length == 0) {
                return;
            }
            final byte[] byteArray = (byte[]) data;
            //addToHistory(byteArray);
        }
    }

    public void logMessage(String msg) {
        logMessage(msg, null);
    }

    public void logMessage(String msg, Exception e) {
        if (debug_) {
            Log.d(TAG, msg + "\n" + Utils.getExceptionStack(e, true));
        }
    }
}













/// OLD CODE - not used
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.widget.GridView;
//
//import com.gmail.raducaz.arduinomate.ConfigurationGridAdapter;
//import com.gmail.raducaz.arduinomate.R;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MainActivity extends AppCompatActivity {
//
//    GridView gridView;
//    ConfigurationGridAdapter adapter;
//    List<String> list;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main_old);
//
//        list = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            list.add("Test " + i);
//        }
//        gridView = findViewById(R.id.configurationGridView);
//        adapter = new ConfigurationGridAdapter(this, list);
//        gridView.setAdapter(adapter);
//    }
//}
