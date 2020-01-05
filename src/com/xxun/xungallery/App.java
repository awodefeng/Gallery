package com.xxun.xungallery;

import android.app.Application;
import android.os.Environment;

import com.xxun.xungallery.tools.CrashHandler;
import com.xxun.xungallery.tools.LogTool;


public class App extends Application {

    // public static String SD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static App app;

    public static App get() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        // LogTool.getInstance().init(SD_PATH + "/AndroidCamera/Log", 30, true);
        CrashHandler.getInstance().init(app);
    }

    /**
     * 退出整个应用
     */
    public void exit() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
