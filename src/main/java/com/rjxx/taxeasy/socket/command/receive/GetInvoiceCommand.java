package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.controller.InvoiceController;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.KplsService;
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

import java.util.HashMap;
import java.util.Map;

/**
 * 接收开票命令，并且发送开票命令
 * Created by Administrator on 2017-03-22.
 */
@Service("GetInvoiceCommand")
public class GetInvoiceCommand implements ICommand {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KplsService kplsService;

    @Autowired
    private InvoiceController invoiceController;

    @Override
    public void run(String commandId, String data, SocketSession socketSession) throws Exception {
        String fpzldm = data;
        Integer kpdid = socketSession.getKpdid();
        logger.debug("-----------receive kpdid " + kpdid + " GetInvoice request---------");
        Map params = new HashMap();
        params.put("fpzldm", fpzldm);
        params.put("kpdid", kpdid);
        params.put("fpztdm", "04");
        params.put("orderBy", "lrsj asc");
        Kpls kpls = kplsService.findOneByParams(params);
        if (kpls == null) {
            logger.info("-----kpdid " + kpdid + " has no pending data-------");
            InvoicePendingData invoicePendingData = invoiceController.generatePendingData(kpdid);
            String xml = XmlJaxbUtils.toXml(invoicePendingData);
            ServerHandler.sendMessage(kpdid, SendCommand.SendPendingData, xml, "", false);
            return;
        }
        kpls.setFpztdm("14");
        kplsService.save(kpls);
        invoiceController.doKp(kpls.getKplsh(), false);
    }
}
