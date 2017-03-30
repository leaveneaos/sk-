package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.socket.SocketSession;
import com.rjxx.taxeasy.socket.command.ICommand;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2017-03-30.
 */
@Service("GetKcCommand")
public class ReturnGetKcCommand implements ICommand {

    @Override
    public void run(String commandId, String data, SocketSession socketSession) throws Exception {

    }
}
