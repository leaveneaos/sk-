package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.controller.InvoiceController;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.socket.ServerHandler;
import com.rjxx.taxeasy.socket.SocketSession;
import com.rjxx.taxeasy.socket.command.ICommand;
import com.rjxx.taxeasy.socket.command.SendCommand;
import com.rjxx.taxeasy.vo.InvoicePendingData;
import com.rjxx.utils.DesUtils;
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

    public static final String DO_INVOICE = "DO_INVOICE";

    public static final String SK_SERVER_DES_KEY = "R1j2x3x4";

    @Autowired
    private KplsService kplsService;

    @Autowired
    private InvoiceController invoiceController;

    @Override
    public void run(String commandId, String data, SocketSession socketSession) throws Exception {
        try {
            Boolean isDoInvoice = (Boolean) socketSession.getSession().getAttribute(DO_INVOICE);
            if (isDoInvoice != null && isDoInvoice) {
                logger.warn("warning:" + socketSession.getKpdid() + " receive GetInvoiceCommand repeat");
                return;
            } else {
                socketSession.getSession().setAttribute(DO_INVOICE, true);
            }
            String fpzldm = data;
            if (StringUtils.isBlank(fpzldm)) {
                return;
            }
            Integer kpdid = socketSession.getKpdid();
            logger.debug("-----------receive kpdid " + kpdid + " GetInvoice request---------");

            doKp(fpzldm, kpdid);

        } catch (Exception e) {
            logger.error("", e);
        } finally {
            socketSession.getSession().setAttribute(DO_INVOICE, false);
        }

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
        params.put("orderBy", "kplsh");
        Kpls kpls = null;
        kpls = kplsService.findOneByParams(params);
        if (kpls == null) {
            InvoicePendingData invoicePendingData = invoiceController.generatePendingData(kpdid);
            String xml = XmlJaxbUtils.toXml(invoicePendingData);
            ServerHandler.sendMessage(kpdid, SendCommand.SendPendingData, xml, "", false, 1);
            return;
        }
        if(kpls.getFpczlxdm().equals("14")){
            kpls.setFpztdm("10");//待作废数据
            kplsService.save(kpls);
            String encryptStr = encryptSkServerParameter(kpls.getKplsh() + "");
            invoiceController.voidInvoice(encryptStr);
        }else{
            kpls.setFpztdm("14");
            kplsService.save(kpls);
            invoiceController.doKp(kpls.getKplsh(), true, 1);
        }

    }
    /**
     * 加密税控服务参数
     *
     * @param params
     * @return
     */
    public String encryptSkServerParameter(String params) throws Exception {
        return DesUtils.DESEncrypt(params, SK_SERVER_DES_KEY);
    }
}
