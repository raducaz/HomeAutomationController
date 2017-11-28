package com.gmail.raducaz.arduinomate;

import android.app.Application;

import com.gmail.raducaz.arduinomate.db.AppDatabase;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

public class ArduinoMateApp extends Application {

    private AppExecutors mAppExecutors;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppExecutors = new AppExecutors();
    }

    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this, mAppExecutors);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(getDatabase());
    }
}
