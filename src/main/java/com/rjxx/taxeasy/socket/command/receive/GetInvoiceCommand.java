package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.controller.InvoiceController;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.socket.SocketSession;
import com.rjxx.taxeasy.socket.command.ICommand;
import com.rjxx.utils.StringUtils;
import org.apache.mina.core.session.IoSession;
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
        IoSession session = socketSession.getSession();
        Integer kpdid = (Integer) session.getAttribute("kpdid");
        if (StringUtils.isBlank(fpzldm) || kpdid == null) {
            logger.info("--------unknow fpzldm " + fpzldm + " or kpdid " + kpdid + "---------");
            return;
        }
        Map params = new HashMap();
        params.put("fpzldm", fpzldm);
        params.put("kpdid", kpdid);
        params.put("fpztdm", "04");
        params.put("orderBy", "lrsj asc");
        Kpls kpls = kplsService.findOneByParams(params);
        if (kpls == null) {
            logger.info("-----------has no invoice pending data--------------");
            return;
        }
        kpls.setFpztdm("14");
        kplsService.save(kpls);
        invoiceController.doKp(kpls.getKplsh(), false);
    }
}
