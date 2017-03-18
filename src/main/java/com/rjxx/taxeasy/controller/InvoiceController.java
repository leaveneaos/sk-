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
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.TemplateUtils;
import com.rjxx.utils.XmlJaxbUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
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
    public String getCodeAndNo(String p) throws Exception {
        try {
            if (StringUtils.isBlank(p)) {
                throw new Exception("参数不能为空");
            }
            String params = skService.decryptSkServerParameter(p);
            Map<String, String> map = HtmlUtils.parseQueryString(params);
            int kpdid = Integer.valueOf(map.get("kpdid"));
            String fplxdm = map.get("fplxdm");
            String result = ServerHandler.sendMessage(kpdid, SendCommand.GetCodeAndNo, fplxdm);
            logger.debug(result);
            return result;
        } catch (Exception e) {
            logger.error("", e);
            InvoiceResponse response = InvoiceResponseUtils.responseError(e.getMessage());
            return XmlJaxbUtils.toXml(response);
        }
    }

    /**
     * 作废发票
     *
     * @param p
     * @return
     * @throws Exception
     */
    public String voidInvoice(String p) throws Exception {
        try {
            if (StringUtils.isBlank(p)) {
                throw new Exception("参数不能为空");
            }
            String kplshStr = skService.decryptSkServerParameter(p);
            int kplsh = Integer.valueOf(kplshStr);
            logger.debug("receive void invoice request:" + kplsh);
            Kpls kpls = kplsService.findOne(kplsh);
            if (kpls == null) {
                InvoiceResponse response = InvoiceResponseUtils.responseError("开票流水号：" + kplsh + "没有该数据");
                return XmlJaxbUtils.toXml(response);
            }
            if (StringUtils.isBlank(kpls.getFpdm()) || StringUtils.isBlank(kpls.getFphm())) {
                InvoiceResponse response = InvoiceResponseUtils.responseError("开票流水号：" + kplsh + "没有发票代码或号码，无法作废");
                return XmlJaxbUtils.toXml(response);
            }
            Map params = new HashMap();
            params.put("kpls", kpls);
            String commandId = kpls.getKplsh() + "$" + System.currentTimeMillis();
            params.put("lsh", kpls.getKplsh() + "");
            String content = TemplateUtils.generateContent("invoice-request.ftl", params);
            logger.debug(content);
            String result = ServerHandler.sendMessage(kpls.getSkpid(), SendCommand.VoidInvoice, content, commandId);
            logger.debug(result);
            return result;
        } catch (Exception e) {
            logger.error("", e);
            InvoiceResponse response = InvoiceResponseUtils.responseError(e.getMessage());
            return XmlJaxbUtils.toXml(response);
        }
    }

    /**
     * 发票重打
     *
     * @param p
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/reprintInovice")
    public String reprintInovice(String p) throws Exception {
        try {
            if (StringUtils.isBlank(p)) {
                throw new Exception("参数不能为空");
            }
            String kplshStr = skService.decryptSkServerParameter(p);
            int kplsh = Integer.valueOf(kplshStr);
            logger.debug("receive void invoice request:" + kplsh);
            Kpls kpls = kplsService.findOne(kplsh);
            if (kpls == null) {
                InvoiceResponse response = InvoiceResponseUtils.responseError("开票流水号：" + kplsh + "没有该数据");
                return XmlJaxbUtils.toXml(response);
            }
            if (StringUtils.isBlank(kpls.getFpdm()) || StringUtils.isBlank(kpls.getFphm())) {
                InvoiceResponse response = InvoiceResponseUtils.responseError("开票流水号：" + kplsh + "没有发票代码或号码，无法重打");
                return XmlJaxbUtils.toXml(response);
            }
            Map params = new HashMap();
            params.put("kpls", kpls);
            String content = TemplateUtils.generateContent("invoice-request.ftl", params);
            logger.debug(content);
            String result = ServerHandler.sendMessage(kpls.getSkpid(), SendCommand.RepeatInvoice, content, kpls.getKplsh() + "");
            InvoiceResponse invoiceResponse = XmlJaxbUtils.convertXmlStrToObject(InvoiceResponse.class, result);
            invoiceResponse.setKpddm(kpls.getKpddm());
            invoiceResponse.setJylsh(kpls.getJylsh());
            result = XmlJaxbUtils.toXml(invoiceResponse);
            logger.debug(result);
            return result;
        } catch (Exception e) {
            logger.error("", e);
            InvoiceResponse response = InvoiceResponseUtils.responseError(e.getMessage());
            return XmlJaxbUtils.toXml(response);
        }
    }

    /**
     * 打印发票
     *
     * @param p
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/invoice")
    public String invoice(String p) throws Exception {
        try {
            if (StringUtils.isBlank(p)) {
                throw new Exception("参数不能为空");
            }
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
            String lsh = kpls.getKplsh() + "$" + System.currentTimeMillis();
            params.put("lsh", lsh);
            String content = TemplateUtils.generateContent("invoice-request.ftl", params);
            logger.debug(content);
            String result = ServerHandler.sendMessage(kpls.getSkpid(), SendCommand.Invoice, content, lsh);
            if (StringUtils.isBlank(result)) {
                InvoiceResponse response = InvoiceResponseUtils.responseError("客户端没有返回结果，请去开票软件确认");
                return XmlJaxbUtils.toXml(response);
            }
            InvoiceResponse invoiceResponse = XmlJaxbUtils.convertXmlStrToObject(InvoiceResponse.class, result);
            invoiceResponse.setKpddm(kpls.getKpddm());
            invoiceResponse.setJylsh(kpls.getJylsh());
            result = XmlJaxbUtils.toXml(invoiceResponse);
            logger.debug(result);
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
        if (kpspmxList == null || kpspmxList.isEmpty()) {
            throw new Exception("没有商品明细");
        }
        params.put("kpls", kpls);
        params.put("kpspmxList", kpspmxList);
        String templateName = "invoice-xml.ftl";
        if ("12".equals(kpls.getFpzldm())) {
            templateName = "dzfp-xml.ftl";
        }
        String content = TemplateUtils.generateContent(templateName, params);
        return content;
    }

}
