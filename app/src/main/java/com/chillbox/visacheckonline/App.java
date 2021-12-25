package com.chillbox.visacheckonline;

import android.app.Application;

public class App extends Application {

    private static boolean activityVisible;


    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }


}
