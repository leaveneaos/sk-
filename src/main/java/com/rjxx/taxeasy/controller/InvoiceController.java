package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.bizcomm.utils.GeneratePdfService;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponseUtils;
import com.rjxx.taxeasy.bizcomm.utils.SkService;
import com.rjxx.taxeasy.domains.Cszb;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.socket.ServerHandler;
import com.rjxx.taxeasy.socket.command.SendCommand;
import com.rjxx.taxeasy.utils.ClientDesUtils;
import com.rjxx.taxeasy.vo.InvoicePendingData;
import com.rjxx.utils.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
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

    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private CszbService cszbService;



    @RequestMapping(value = "/getCodeAndNo", method = {RequestMethod.GET, RequestMethod.POST})
    public String getCodeAndNo(String p) throws Exception {
        try {
            if (StringUtils.isBlank(p)) {
                throw new Exception("参数不能为空");
            }
            String params = skService.decryptSkServerParameter(p);
            Map<String, String> map = HtmlUtils.parseQueryString(params);
            String kpdid = map.get("kpdid");
            Skp skp = skpService.findOne(Integer.valueOf(map.get("kpdid")));
            Cszb cszb = cszbService.getSpbmbbh(skp.getGsdm(), skp.getXfid(), null, "sfzcdkpdkp");
            String sfzcdkpdkp = cszb.getCsz();
            if(sfzcdkpdkp.equals("是")){
                kpdid=skp.getSkph();
            }
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
            return invoiceService.voidInvoice(kplsh, false, 60000);
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
            Skp skp = skpService.findOne(kpls.getSkpid());
            Cszb cszb = cszbService.getSpbmbbh(skp.getGsdm(), skp.getXfid(), null, "sfzcdkpdkp");
            String sfzcdkpdkp = cszb.getCsz();
            String kpdid=null;
            if(sfzcdkpdkp.equals("是")){
                kpdid=skp.getSkph();
            }else{
                kpdid=kpls.getSkpid().toString();
            }
            String result = ServerHandler.sendMessage(kpdid, SendCommand.ReprintInvoice, content, kpls.getKplsh() + "");
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
            InvoiceResponse invoiceResponse = invoiceService.doKp(kplsh, false, 0);
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
        String kpdid = kpdidStr;
        result = invoiceService.generatePendingData(kpdid);
        return generateInvoicePendingDataResult(result);
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
    /**
     * 重新生成pdf
     * @param
     * @return
     */
    @RequestMapping(value = "/ReCreatePdf", method = {RequestMethod.GET, RequestMethod.POST})
    private String ReCreatePdf(String p) throws Exception {
        try {
            if (StringUtils.isBlank(p)) {
                throw new Exception("参数不能为空");
            }
            String kplshStr = skService.decryptSkServerParameter(p);
            int kplsh = Integer.valueOf(kplshStr);
            logger.debug("receive invoice request:" + kplsh);
            InvoiceResponse invoiceResponse  = invoiceService.generatePdf(kplsh);
            String result = XmlJaxbUtils.toXml(invoiceResponse);
            logger.debug(result);
            return result;
        }catch (Exception e){
            logger.error("", e);
            InvoiceResponse response = InvoiceResponseUtils.responseError(e.getMessage());
            return XmlJaxbUtils.toXml(response);
        }
    }
    /**
     * 税控服务器开票
     * @param
     * @return
     */
    @RequestMapping(value = "/SkServerKP", method = {RequestMethod.GET, RequestMethod.POST})
    public String skServerKP(String p) throws Exception {
        try {
            if (StringUtils.isBlank(p)) {
                throw new Exception("参数不能为空");
            }
            String kplshStr = skService.decryptSkServerParameter(p);
            int kplsh = Integer.valueOf(kplshStr);
            logger.debug("receive invoice request:" + kplsh);
            InvoiceResponse invoiceResponse  = invoiceService.skServerKP(kplsh);
            String result = XmlJaxbUtils.toXml(invoiceResponse);
            logger.debug(result);
            return result;
        }catch (Exception e){
            logger.error("", e);
            InvoiceResponse response = InvoiceResponseUtils.responseError(e.getMessage());
            return XmlJaxbUtils.toXml(response);
        }
    }

    /**
     * 税控盒子开票
     * @param
     * @return
     */
    @RequestMapping(value = "/skBoxKP", method = {RequestMethod.GET, RequestMethod.POST})
    public String skBoxKP(String p) throws Exception {
        try {
            if (StringUtils.isBlank(p)) {
                throw new Exception("参数不能为空");
            }
            String kplshStr = skService.decryptSkServerParameter(p);
            int kplsh = Integer.valueOf(kplshStr);
            logger.debug("receive invoice request:" + kplsh);
            InvoiceResponse invoiceResponse  = invoiceService.skBoxKP(kplsh);
            String result = XmlJaxbUtils.toXml(invoiceResponse);
            logger.debug(result);
            return result;
        }catch (Exception e){
            logger.error("", e);
            InvoiceResponse response = InvoiceResponseUtils.responseError(e.getMessage());
            return XmlJaxbUtils.toXml(response);
        }
    }
}
