package com.example.mislugares;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.twitter.sdk.android.core.Twitter;

public class MisLugares extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Twitter.initialize(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
    }
}
