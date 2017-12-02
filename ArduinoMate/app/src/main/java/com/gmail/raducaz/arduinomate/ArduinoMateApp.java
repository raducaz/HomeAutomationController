package com.gmail.raducaz.arduinomate;

import android.app.Application;

import com.gmail.raducaz.arduinomate.db.AppDatabase;
import com.gmail.raducaz.arduinomate.service.TcpServerInboundHandler;
import com.gmail.raducaz.arduinomate.service.TcpServerService;

import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

public class ArduinoMateApp extends Application {

    private AppExecutors mAppExecutors;
    private TcpServerService tcpServerService;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppExecutors = new AppExecutors();

        // Start the tcp server
        try {
            tcpServerService = TcpServerService.getInstance(this.getRepository());
            this.getNetworkExecutor().execute(tcpServerService);
        }
        catch (IOException exc)
        {
            //TODO: handle it somehow
        }
    }

    public Executor getDbExecutor()
    {
        return mAppExecutors.diskIO();
    }
    public Executor getNetworkExecutor()
    {
        return mAppExecutors.networkIO();
    }
    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this, mAppExecutors);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(getDatabase());
    }
}
