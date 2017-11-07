package com.rjxx.taxeasy.task;

import com.rjxx.taxeasy.socket.command.SendCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2017/1/5.
 */
@Service("taskManage")
public class TaskManage {

    private Map<Integer, KpdTask> map = new ConcurrentHashMap<>();

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    /**
     * 添加任务
     *
     * @param kpdid
     * @param sendCommand
     * @param message
     */
    public void addTask(String kpdid, SendCommand sendCommand, String message) {
        KpdTask kpdTask = map.get(kpdid);
        if (kpdTask == null) {
            kpdTask = new KpdTask(kpdid);
        }
        kpdTask.addTask(sendCommand, message);
        if (!kpdTask.isRunning()) {
            taskExecutor.execute(kpdTask);
        }
    }

}
