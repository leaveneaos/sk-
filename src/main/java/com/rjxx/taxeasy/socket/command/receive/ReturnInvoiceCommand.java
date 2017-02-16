package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.socket.SocketSession;
import com.rjxx.taxeasy.socket.command.ICommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2017/1/19.
 */
@Service("ReturnInvoiceCommand")
public class ReturnInvoiceCommand implements ICommand {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String commandId, String data, SocketSession socketSession) throws Exception {
        logger.info(data);
    }
}
