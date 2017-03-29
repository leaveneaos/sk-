package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.controller.InvoiceController;
import com.rjxx.taxeasy.socket.ServerHandler;
import com.rjxx.taxeasy.socket.SocketSession;
import com.rjxx.taxeasy.socket.command.ICommand;
import com.rjxx.taxeasy.socket.command.SendCommand;
import com.rjxx.taxeasy.vo.InvoicePendingData;
import com.rjxx.utils.XmlJaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 获取待开票数据
 * Created by Administrator on 2017-03-24.
 */
@Service("GetPendingDataCommand")
public class GetPendingDataCommand implements ICommand {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private InvoiceController invoiceController;

    @Override
    public void run(String commandId, String data, SocketSession socketSession) throws Exception {
        int kpdid = socketSession.getKpdid();
        InvoicePendingData invoicePendingData = invoiceController.generatePendingData(kpdid);
        String xml = XmlJaxbUtils.toXml(invoicePendingData);
        logger.debug(socketSession.getKpdid() + " SendPendingData:" + xml);
        ServerHandler.sendMessage(kpdid, SendCommand.SendPendingData, xml, "", false);
    }
}
