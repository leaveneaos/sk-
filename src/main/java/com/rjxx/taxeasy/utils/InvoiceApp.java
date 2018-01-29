package com.rjxx.taxeasy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by xlm on 2018/1/25.
 */
public class InvoiceApp {
   // private static Context context;
    private static InvoiceApp instance;
    private    static Logger logger = LoggerFactory.getLogger(InvoiceApp.class);

    public static InvoiceApp getsInstance() {
        return instance;
    }

  /*  @Override
    public void onCreate() {
        logger.info("app create");
        super.onCreate();


        instance = this;

        //获取Context
        context = getApplicationContext();
    }*/

   /* @Override
    public void onTerminate() {
        logger.info("app onTerminate");
        threadPoolShutDown();
        super.onTerminate();
    }
*/
    public static int SendCount;

    public static int RevCount;

    public static ScheduledExecutorService scheduledThreadPool = Executors
            .newScheduledThreadPool(10);
    public static ExecutorService catcheThreadPool = Executors
            .newCachedThreadPool();
    public static ExecutorService fixThreadPool = Executors
            .newFixedThreadPool(3);

    public static void threadPoolShutDown() {
        scheduledThreadPool.shutdownNow();
        catcheThreadPool.shutdownNow();
        fixThreadPool.shutdownNow();
    }

/*
    public static Context getContextObject(){
        return context;
    }
*/

    public static HashMap<String,String> getsjdmMap()
    {
        HashMap<String,String> Map=new HashMap<String,String>();

        Map.put("36","9AA636995A0648EF0C87DAAD54F604FE297D5324352B30EA9834D4A44FB23BD0");

        return Map;
    }

}
