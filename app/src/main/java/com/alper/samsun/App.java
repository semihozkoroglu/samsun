package com.alper.samsun;

import android.app.Application;

import com.alper.samsun.network.ServiceProvider;

/**
 * Created by semihozkoroglu on 27/06/16.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ServiceProvider.initialize();
    }
}