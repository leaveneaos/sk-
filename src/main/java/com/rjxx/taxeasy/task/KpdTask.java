package com.rjxx.taxeasy.task;

import com.rjxx.taxeasy.socket.ServerHandler;
import com.rjxx.taxeasy.socket.command.SendCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Administrator on 2017/1/5.
 */
public class KpdTask implements Runnable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String kpdid;

    /**
     * 普通任务
     */
    private LinkedBlockingDeque<CommandWrapper> deque1 = new LinkedBlockingDeque<>();

    /**
     * 紧急任务
     */
    private LinkedBlockingDeque<CommandWrapper> deque2 = new LinkedBlockingDeque<>();

    private boolean running = false;

    public KpdTask(String kpdid) {
        this.kpdid = kpdid;
    }

    @Override
    public void run() {
        if (!running) {
            running = true;
        } else {
            return;
        }
        //计数器，如果超过30(1分钟都没有数据)，则释放线程
        int count = 1;
        while (count <= 30) {
            CommandWrapper commandWrapper = deque2.pollFirst();
            if (commandWrapper == null) {
                commandWrapper = deque1.pollFirst();
            }
            if (commandWrapper == null) {
                count++;
                try {
                    Thread.sleep(2000l);
                } catch (Exception e) {
                    logger.error("", e);
                }
                continue;
            }
            count = 1;
            try {
                ServerHandler.sendMessage(kpdid, commandWrapper.getSendCommand(), commandWrapper.getMessage());
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        running = false;
    }

    /**
     * 判断是否正在运行中
     *
     * @return
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * 添加任务
     *
     * @param sendCommand
     * @param message
     * @param urgent      是否紧急任务
     */
    public void addTask(SendCommand sendCommand, String message, boolean urgent) {
        CommandWrapper wrapper = new CommandWrapper(sendCommand, message);
        if (!urgent) {
            deque1.add(wrapper);
        } else {
            deque2.add(wrapper);
        }
    }

    /**
     * 添加任务
     *
     * @param sendCommand
     * @param message
     */
    public void addTask(SendCommand sendCommand, String message) {
        addTask(sendCommand, message, false);
    }
}
