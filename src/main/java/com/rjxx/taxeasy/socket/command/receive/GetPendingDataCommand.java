package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.controller.InvoiceController;
import com.rjxx.taxeasy.socket.ServerHandler;
import com.rjxx.taxeasy.socket.SocketSession;
import com.rjxx.taxeasy.socket.command.ICommand;
import com.rjxx.taxeasy.socket.command.SendCommand;
import com.rjxx.taxeasy.vo.InvoicePendingData;
import com.rjxx.utils.XmlJaxbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 获取待开票数据
 * Created by Administrator on 2017-03-24.
 */
@Service("GetPendingDataCommand")
public class GetPendingDataCommand implements ICommand {

    @Autowired
    private InvoiceController invoiceController;

    @Override
    public void run(String commandId, String data, SocketSession socketSession) throws Exception {
        int kpdid = socketSession.getKpdid();
        InvoicePendingData invoicePendingData = invoiceController.generatePendingData(kpdid);
        String xml = XmlJaxbUtils.toXml(invoicePendingData);
        ServerHandler.sendMessage(kpdid, SendCommand.SendPendingData, xml, "", false);
    }
}
