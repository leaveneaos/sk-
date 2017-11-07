package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.socket.SocketSession;
import com.rjxx.taxeasy.socket.command.ICommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2017/1/4.
 */
@Service("HBCommand")
public class HBCommand implements ICommand {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String commandId, String params, SocketSession socketSession) throws Exception {
        logger.debug("kpd:" + socketSession.getKpdid() + " heartbeat");
    }
}
