package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.socket.SocketSession;
import com.rjxx.taxeasy.socket.command.ICommand;
import org.apache.mina.core.session.IoSession;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2017-03-22.
 */
@Service("InvoiceMakeCommand")
public class InvoiceMakeCommand implements ICommand {

    @Override
    public void run(String commandId, String data, SocketSession socketSession) throws Exception {
        IoSession session = socketSession.getSession();
        session.setAttribute("fplxdm",data);
    }
}
