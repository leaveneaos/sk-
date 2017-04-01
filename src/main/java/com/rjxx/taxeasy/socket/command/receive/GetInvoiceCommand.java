package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.controller.InvoiceController;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.socket.ServerHandler;
import com.rjxx.taxeasy.socket.SocketSession;
import com.rjxx.taxeasy.socket.command.ICommand;
import com.rjxx.taxeasy.socket.command.SendCommand;
import com.rjxx.taxeasy.vo.InvoicePendingData;
import com.rjxx.utils.StringUtils;
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
        if (StringUtils.isBlank(fpzldm)) {
            return;
        }
        Integer kpdid = socketSession.getKpdid();
        logger.debug("-----------receive kpdid " + kpdid + " GetInvoice request---------");
        String[] fpzldmArr = fpzldm.split(",");
        for (String fpzl : fpzldmArr) {
            doKp(fpzl, kpdid);
        }
        logger.debug("---------kpdid " + kpdid + " complete do invoice,will send pending data---------");
        //执行完所有开票动作后，重新发送待开票数据
        InvoicePendingData invoicePendingData = invoiceController.generatePendingData(kpdid);
        String xml = XmlJaxbUtils.toXml(invoicePendingData);
        ServerHandler.sendMessage(kpdid, SendCommand.SendPendingData, xml, "", false);
    }

    /**
     * 执行开票
     *
     * @param fpzldm
     * @param kpdid
     */
    private void doKp(String fpzldm, int kpdid) throws Exception {
        Map params = new HashMap();
        params.put("fpzldm", fpzldm);
        params.put("kpdid", kpdid);
        params.put("fpztdm", "04");
        params.put("orderBy", "lrsj asc");
        Kpls kpls = null;
        int count = 0;
        do {
            kpls = kplsService.findOneByParams(params);
            if (kpls == null) {
                return;
            }
            if (count > 0) {
                Thread.sleep(5000);
            }
            kpls.setFpztdm("14");
            kplsService.save(kpls);
            invoiceController.doKp(kpls.getKplsh(), false);
            count++;
        } while (true);
    }
}
