package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.socket.SocketSession;
import com.rjxx.taxeasy.socket.command.ICommand;
import com.rjxx.utils.XmlJaxbUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by Administrator on 2017/1/19.
 */
@Service("ReturnInvoiceCommand")
public class ReturnInvoiceCommand implements ICommand {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KplsService kplsService;

    @Override
    public void run(String commandId, String data, SocketSession socketSession) throws Exception {
        logger.info(data);
        InvoiceResponse response = XmlJaxbUtils.convertXmlStrToObject(InvoiceResponse.class, data);
        String returnCode = response.getReturnCode();
        if ("0000".equals(returnCode)) {
            String lsh = response.getLsh();
            int pos = lsh.indexOf("$");
            int kplsh;
            if (pos != -1) {
                kplsh = Integer.valueOf(lsh.substring(0, pos));
            } else {
                kplsh = Integer.valueOf(lsh);
            }
            Kpls kpls = kplsService.findOne(kplsh);
            kpls.setFpdm(response.getFpdm());
            kpls.setFphm(response.getFphm());
            kpls.setFpztdm("00");
            kpls.setPrintflag("" + response.getPrintFlag());
            kpls.setKprq(DateUtils.parseDate(response.getKprq(), "yyyy-MM-dd"));
            kpls.setXgsj(new Date());
            kpls.setXgry(1);
            kplsService.save(kpls);
        }
    }
}
