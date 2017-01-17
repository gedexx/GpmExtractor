package com.gedexx.gpmextractor;

import android.app.Application;

import org.androidannotations.annotations.EApplication;

@EApplication
public class GpmExtractorApplication extends Application {

    public static String DATA_DIR_PATH = "/data/data/";
    public static String GPM_PACKAGE_NAME = "com.google.android.music";

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
