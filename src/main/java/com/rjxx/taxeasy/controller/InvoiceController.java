package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponseUtils;
import com.rjxx.taxeasy.bizcomm.utils.SkService;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Kpspmx;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.KpspmxService;
import com.rjxx.taxeasy.socket.ServerHandler;
import com.rjxx.taxeasy.socket.command.SendCommand;
import com.rjxx.utils.TemplateUtils;
import com.rjxx.utils.XmlJaxbUtils;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/12.
 */
@RestController
@RequestMapping(value = "/invoice")
public class InvoiceController {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KplsService kplsService;

    @Autowired
    private KpspmxService kpspmxService;

    @Autowired
    private SkService skService;

    @RequestMapping(value = "/getCodeAndNo")
    public String getCodeAndNo(int kpdid, String fplxdm) throws Exception {
        try {
            String result = ServerHandler.sendMessage(kpdid, SendCommand.GetCodeAndNo, fplxdm);
            return result;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/invoice")
    public String invoice(String p) throws Exception {
        try {
            String kplshStr = skService.decryptSkServerParameter(p);
            int kplsh = Integer.valueOf(kplshStr);
            logger.debug("receive invoice request:" + kplsh);
            Kpls kpls = kplsService.findOne(kplsh);
            if (kpls == null) {
                InvoiceResponse response = InvoiceResponseUtils.responseError("开票流水号：" + kplsh + "没有该数据");
                return XmlJaxbUtils.toXml(response);
            }
            String xml = getInvoiceXml(kpls);
            logger.debug("kplsh:" + kplsh + " xml:");
            logger.debug(xml);
            xml = Base64.encodeBase64String(xml.getBytes("UTF-8"));
            Map params = new HashMap();
            params.put("xml", xml);
            params.put("kpls", kpls);
            String content = TemplateUtils.generateContent("invoice-request.ftl", params);
            logger.debug(content);
            String result = ServerHandler.sendMessage(kpls.getSkpid(), SendCommand.Invoice, content);
            return result;
        } catch (Exception e) {
            logger.error("", e);
            InvoiceResponse response = InvoiceResponseUtils.responseError(e.getMessage());
            return XmlJaxbUtils.toXml(response);
        }
    }

    /**
     * 获取发票的xml数据
     *
     * @param kpls
     * @return
     * @throws Exception
     */
    private String getInvoiceXml(Kpls kpls) throws Exception {
        Map params = new HashMap();
        params.put("kplsh", kpls.getKplsh());
        List<Kpspmx> kpspmxList = kpspmxService.findMxList(params);
        params.put("kpls", kpls);
        params.put("kpspmxList", kpspmxList);
        String content = TemplateUtils.generateContent("invoice-xml.ftl", params);
        return content;
    }

}
