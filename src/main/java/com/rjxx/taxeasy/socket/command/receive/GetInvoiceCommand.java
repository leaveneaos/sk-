package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.config.RabbitmqUtils;
import com.rjxx.taxeasy.domains.Cszb;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.service.CszbService;
import com.rjxx.taxeasy.service.InvoiceService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.SkpService;
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
import java.util.List;
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
    private InvoiceService invoiceService;

    @Autowired
    private RabbitmqUtils rabbitmqUtils;

    @Autowired
    private SkpService skpService;
    @Autowired
    private CszbService cszbService;

    @Override
    public void run(String commandId, String data, SocketSession socketSession) throws Exception {
        try {
            String fpzldm = data;
            if (StringUtils.isBlank(fpzldm)) {
                return;
            }
            String kpdid = socketSession.getKpdid();
            logger.debug("-----------receive kpdid " + kpdid + " GetInvoice request---------");
            doKp(fpzldm, kpdid);

        } catch (Exception e) {
            logger.error("", e);
        }

    }

    /**
     * 从mq中获取数据
     *
     * @param kpdid
     * @param fpzldms
     * @return
     */
    private Kpls getDataFromMq(String kpdid, String fpzldms) throws Exception {
        String[] fpzldmArr = fpzldms.split(",");
        String skph=null;
        Map parms=new HashMap();
        parms.put("kpdid",kpdid);
        List<Skp> skpList=skpService.findSkpbySkph(parms);
        Skp skp=skpList.get(0);
        Cszb cszb = cszbService.getSpbmbbh(skp.getGsdm(), skp.getXfid(), null, "sfzcdkpdkp");
        String sfzcdkpdkp = cszb.getCsz();
        if(sfzcdkpdkp.equals("是")){
            skph=kpdid;
        }else{
            skph = skpService.findOne(Integer.parseInt(kpdid)).getSkph();
            if(null==skph||"".equals(skph)){
                skph=skpService.findOne(Integer.parseInt(kpdid)).getId().toString();
            }
        }
        for (String fpzldm : fpzldmArr) {
            do {
                String kplshStr = (String) rabbitmqUtils.receiveMsg(skph, fpzldm);
                if (StringUtils.isNotBlank(kplshStr)) {
                    int kplsh = Integer.valueOf(kplshStr);
                    Map params = new HashMap();
                    params.put("kplsh", kplsh);
                    Kpls kpls = kplsService.findOneByParams(params);
                    if (kpls != null) {
                        return kpls;
                    }
                } else {
                    break;
                }
            } while (true);
        }
        return null;
    }

    /**
     * 执行开票
     *
     * @param fpzldm
     * @param kpdid
     */
    private void doKp(String fpzldm, String  kpdid) throws Exception {
        Kpls kpls = getDataFromMq(kpdid, fpzldm);
        if (kpls == null) {
            InvoicePendingData invoicePendingData = invoiceService.generatePendingData(kpdid);
            String xml = XmlJaxbUtils.toXml(invoicePendingData);
            ServerHandler.sendMessage(kpdid, SendCommand.SendPendingData, xml, "", false, 1);
            return;
        }
        if (kpls.getFpczlxdm().equals("14")) {
            kpls.setFpztdm("14");
            kplsService.save(kpls);
            invoiceService.voidInvoice(kpls.getKplsh(), false, 0);
        } else {
            kpls.setFpztdm("14");
            kplsService.save(kpls);
            invoiceService.doKp(kpls.getKplsh(), false, 0);
        }

    }
}
