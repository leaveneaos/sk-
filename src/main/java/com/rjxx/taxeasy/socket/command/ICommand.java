package com.rjxx.taxeasy.socket.command;

import com.rjxx.taxeasy.socket.SocketSession;

/**
 * Created by Administrator on 2017/1/4.
 */
public interface ICommand {

    /**
     * 命令接口
     *
     * @param commandId
     * @param data
     * @param socketSession
     * @return
     * @throws Exception
     */
    void run(String commandId, String data, SocketSession socketSession) throws Exception;

}
