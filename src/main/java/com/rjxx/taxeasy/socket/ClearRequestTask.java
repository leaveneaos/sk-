package com.rjxx.taxeasy.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2016/11/17.
 */
public class ClearRequestTask extends TimerTask {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, SocketRequest> map = new ConcurrentHashMap<>();

    @Override
    public void run() {
        try {
//            logger.debug("----start ClearRequestTask-----");
            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String commandId = iterator.next();
                SocketRequest socketRequest = map.get(commandId);
                if (socketRequest.timeout()) {
                    iterator.remove();
                    map.remove(commandId);
                    socketRequest.setException(new SocketTimeoutException("commandId:" + commandId + " has timeout"));
                    synchronized (socketRequest) {
                        socketRequest.notifyAll();
                    }
                    logger.info("TimeTask clear command:" + commandId);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public void setMap(Map<String, SocketRequest> map) {
        this.map = map;
    }
}
