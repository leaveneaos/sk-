package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponseUtils;
import com.rjxx.taxeasy.bizcomm.utils.SeperateInvoiceUtils;
import com.rjxx.taxeasy.bizcomm.utils.SkService;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Kpspmx;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.KpspmxService;
import com.rjxx.taxeasy.service.SkpService;
import com.rjxx.taxeasy.socket.ServerHandler;
import com.rjxx.taxeasy.socket.command.SendCommand;
import com.rjxx.taxeasy.utils.ClientDesUtils;
import com.rjxx.taxeasy.vo.FptjVo;
import com.rjxx.taxeasy.vo.InvoicePendingData;
import com.rjxx.utils.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    @Autowired
    private SkpService skpService;

    @RequestMapping(value = "/getCodeAndNo", method = {RequestMethod.GET, RequestMethod.POST})
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
    @RequestMapping(value = "/voidInvoice", method = {RequestMethod.GET, RequestMethod.POST})
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
    @RequestMapping(value = "/reprintInvoice", method = {RequestMethod.GET, RequestMethod.POST})
    public String reprintInvoice(String p) throws Exception {
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
            String lsh = kpls.getKplsh() + "$" + System.currentTimeMillis();
            params.put("lsh", lsh);
            String content = TemplateUtils.generateContent("invoice-request.ftl", params);
            logger.debug(content);
            String result = ServerHandler.sendMessage(kpls.getSkpid(), SendCommand.ReprintInvoice, content, kpls.getKplsh() + "");
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
    @RequestMapping(value = "/invoice", method = {RequestMethod.GET, RequestMethod.POST})
    public String invoice(String p) throws Exception {
        try {
            if (StringUtils.isBlank(p)) {
                throw new Exception("参数不能为空");
            }
            String kplshStr = skService.decryptSkServerParameter(p);
            int kplsh = Integer.valueOf(kplshStr);
            logger.debug("receive invoice request:" + kplsh);
            InvoiceResponse invoiceResponse = doKp(kplsh, true, 120000);
            String result = XmlJaxbUtils.toXml(invoiceResponse);
            logger.debug(result);
            return result;
        } catch (Exception e) {
            logger.error("", e);
            InvoiceResponse response = InvoiceResponseUtils.responseError(e.getMessage());
            return XmlJaxbUtils.toXml(response);
        }
    }

    /**
     * 执行开票
     *
     * @param kplsh
     * @param wait    等待开票结果
     * @param timeout 等待超时时间
     */
    public InvoiceResponse doKp(int kplsh, boolean wait, long timeout) throws Exception {
        Kpls kpls = kplsService.findOne(kplsh);
        if (kpls == null) {
            InvoiceResponse response = InvoiceResponseUtils.responseError("开票流水号：" + kplsh + "没有该数据");
            return response;
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
        String result = ServerHandler.sendMessage(kpls.getSkpid(), SendCommand.Invoice, content, lsh, wait, timeout);
        if (StringUtils.isBlank(result)) {
            InvoiceResponse response = InvoiceResponseUtils.responseError("客户端没有返回结果，请去开票软件确认");
            return response;
        }
        if (result.contains("<Response>")) {
            InvoiceResponse invoiceResponse = XmlJaxbUtils.convertXmlStrToObject(InvoiceResponse.class, result);
            invoiceResponse.setKpddm(kpls.getKpddm());
            invoiceResponse.setJylsh(kpls.getJylsh());
            return invoiceResponse;
        } else {
            kpls = kplsService.findOne(kplsh);
            InvoiceResponse invoiceResponse = new InvoiceResponse();
            if ("00".equals(kpls.getFpztdm())) {
                invoiceResponse.setReturnCode("0000");
                invoiceResponse.setFpdm(kpls.getFpdm());
                invoiceResponse.setFphm(kpls.getFphm());
                invoiceResponse.setKprq(DateFormatUtils.format(kpls.getKprq(), "yyyy-MM-dd HH:mm:ss"));
                invoiceResponse.setKpddm(kpls.getKpddm());
                invoiceResponse.setJylsh(kpls.getJylsh());
                invoiceResponse.setPrintFlag(Integer.valueOf(kpls.getPrintflag()));
            } else if ("05".equals(kpls.getFpztdm())) {
                invoiceResponse.setReturnCode("9999");
                invoiceResponse.setReturnMessage(kpls.getErrorReason());
            } else {
                invoiceResponse.setReturnCode("9999");
                invoiceResponse.setReturnMessage("未知异常，请联系软件服务商");
            }
            return invoiceResponse;
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
        int skpid = kpls.getSkpid();
        Skp skp = skpService.findOne(skpid);
        //文本方式，需要重新进行价税分离
        if ("1".equals(skp.getJkfs())) {
            SeperateInvoiceUtils.repeatSeparatePrice(kpspmxList);
        }
        params.put("kpls", kpls);
        params.put("kpspmxList", kpspmxList);
        String gfyhzh = (kpls.getGfyh() == null ? "" : kpls.getGfyh()) + (kpls.getGfyhzh() == null ? "" : kpls.getGfyhzh());
        String gfdzdh = (kpls.getGfdz() == null ? "" : kpls.getGfdz()) + (kpls.getGfdh() == null ? "" : kpls.getGfdh());
        gfyhzh = gfyhzh.trim();
        gfdzdh = gfdzdh.trim();
        if (StringUtils.isBlank(gfyhzh)) {
            gfyhzh = "　";
        }
        if (StringUtils.isBlank(gfdzdh)) {
            gfdzdh = "　";
        }
        params.put("gfyhzh", gfyhzh);
        params.put("gfdzdh", gfdzdh);
        String templateName = "invoice-xml.ftl";
        if ("12".equals(kpls.getFpzldm())) {
            templateName = "dzfp-xml.ftl";
        }
        if ("11".equals(kpls.getFpczlxdm())) {
            params.put("kplx", "0");
        } else if ("12".equals(kpls.getFpczlxdm()) || "13".equals(kpls.getFpczlxdm())) {
            params.put("kplx", "1");
        }
        String content = TemplateUtils.generateContent(templateName, params);
        return content;
    }

    /**
     * 获取待开数据
     *
     * @param p
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getPendingData", method = {RequestMethod.GET, RequestMethod.POST})
    public String getPendingData(String p) throws Exception {
        InvoicePendingData result = new InvoicePendingData();
        Map<String, String> queryMap = null;
        try {
            queryMap = ClientDesUtils.decryptClientQueryString(p);
        } catch (Exception e) {
            result.setSuccess("false");
            result.setMessage(e.getMessage());
            return generateInvoicePendingDataResult(result);
        }
        String kpdidStr = queryMap.get("kpdid");
        int kpdid = Integer.valueOf(kpdidStr);
        result = this.generatePendingData(kpdid);
        return generateInvoicePendingDataResult(result);
    }

    /**
     * 生成待开票数据
     *
     * @return
     */
    public InvoicePendingData generatePendingData(int kpdid) {
        InvoicePendingData result = new InvoicePendingData();
        List<FptjVo> fptjVoList = kplsService.findFpdbtjjgByKpdid(kpdid);
        for (FptjVo fptjVo : fptjVoList) {
            String fpczlxdm = fptjVo.getFpczlxdm();
            String fpzldm = fptjVo.getFpzldm();
            int cnt = fptjVo.getCnt();
            if ("11".equals(fpczlxdm)) {
                //开具
                if ("01".equals(fpzldm)) {
                    result.setZpkjsl(cnt);
                } else if ("02".equals(fpzldm)) {
                    result.setPpkjsl(cnt);
                } else if ("12".equals(fpzldm)) {
                    result.setDzpkjsl(cnt);
                }
            } else if ("12".equals(fpczlxdm)) {
                //红冲
                if ("01".equals(fpzldm)) {
                    result.setZphcsl(cnt);
                } else if ("02".equals(fpzldm)) {
                    result.setPphcsl(cnt);
                } else if ("12".equals(fpzldm)) {
                    result.setDzphcsl(cnt);
                }
            } else if ("14".equals(fpczlxdm)) {
                //作废
                if ("01".equals(fpzldm)) {
                    result.setZpzfsl(cnt);
                } else if ("02".equals(fpzldm)) {
                    result.setPpzfsl(cnt);
                }
            }
        }
        result.setKpdid(kpdid);
        result.setSuccess("true");
        return result;
    }

    /**
     * 生成版本结果
     *
     * @param invoicePendingData
     * @return
     */
    private String generateInvoicePendingDataResult(InvoicePendingData invoicePendingData) throws Exception {
        String result = XmlJaxbUtils.toXml(invoicePendingData);
        result = DesUtils.DESEncrypt(result, DesUtils.GLOBAL_DES_KEY);
        return result;
    }

}
