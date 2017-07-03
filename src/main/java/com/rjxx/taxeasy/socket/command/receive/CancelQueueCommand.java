package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.socket.SocketSession;
import com.rjxx.taxeasy.socket.command.ICommand;
import com.rjxx.utils.XmlJaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xlm on 2017/7/3.
 */
@Service("CancelQueueCommand")
public class CancelQueueCommand implements ICommand {

    @Autowired
    private KplsService kplsService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public void run(String commandId, String data, SocketSession socketSession) throws Exception {
        logger.info(data);
        InvoiceResponse response = XmlJaxbUtils.convertXmlStrToObject(InvoiceResponse.class, data);
        String returnCode = response.getReturnCode();
        if ("8888".equals(returnCode)) {
            String lsh = response.getLsh();
            int pos = lsh.indexOf("$");
            int kplsh;
            if (pos != -1) {
                kplsh = Integer.valueOf(lsh.substring(0, pos));
            } else {
                kplsh = Integer.valueOf(lsh);
            }
            logger.info("------------KPLSH----------------"+kplsh);
            int posCommand=lsh.indexOf("￥");
            String  Command=lsh.substring(posCommand, lsh.length());
            logger.info("------------命令----------------"+Command);
            if(Command.equals("ReturnVoidInvoice")||Command.equals("ReturnInvoiceFile")
               ||Command.equals("ReturnReprintInvoice")||Command.equals("ReturnInvoice")){
                Kpls kpls = kplsService.findOne(kplsh);
                kpls.setFpztdm("04");
                kplsService.save(kpls);
            }
        }
    }
}
