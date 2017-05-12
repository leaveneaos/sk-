package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.bizcomm.utils.DataOperate;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.socket.SocketSession;
import com.rjxx.taxeasy.socket.command.ICommand;
import com.rjxx.taxeasy.utils.KpUtils;
import com.rjxx.utils.XmlJaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by Administrator on 2017/1/4.
 */
@Service
public class ReturnReprintInvoiceCommand implements ICommand {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataOperate dataOperate;

    @Autowired
    private KplsService kplsService;

    @Override
    public void run(String commandId, String params, SocketSession socketSession) throws Exception {
        InvoiceResponse response = XmlJaxbUtils.convertXmlStrToObject(InvoiceResponse.class, params);
        String lsh = response.getLsh();
        int kplsh = KpUtils.getKplshByLsh(lsh);
        if (kplsh == 0) {
            logger.warn("Return Message Error,lsh is invalid:" + lsh);
            logger.warn(params);
            return;
        }
        Kpls kpls = kplsService.findOne(kplsh);
        if (kpls == null) {
            logger.warn("kpls is not exists:" + lsh);
            logger.warn(params);
            return;
        }
        if ("0000".equals(response.getReturnCode())) {
            kpls.setPrintflag("1");
            dataOperate.saveLog(kplsh, "00", "", "ReprintInvoice", "", 1, "", "" + kpls);
        } else {
            kpls.setPrintflag("2");
            dataOperate.saveLog(kplsh, "99", "", "ReprintInvoice", response.getReturnMessage(), 1, "", "" + kpls);
        }
        kpls.setXgsj(new Date());
        kplsService.save(kpls);
    }
}
