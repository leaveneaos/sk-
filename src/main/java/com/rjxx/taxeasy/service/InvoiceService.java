package com.rjxx.taxeasy.service;

import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponseUtils;
import com.rjxx.taxeasy.bizcomm.utils.SeperateInvoiceUtils;
import com.rjxx.taxeasy.bizcomm.utils.SkService;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Kpspmx;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.socket.ServerHandler;
import com.rjxx.taxeasy.socket.command.SendCommand;
import com.rjxx.taxeasy.vo.FptjVo;
import com.rjxx.taxeasy.vo.InvoicePendingData;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.TemplateUtils;
import com.rjxx.utils.XmlJaxbUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * InvoiceController的service
 * Created by Administrator on 2017-05-31.
 */
@Service
public class InvoiceService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KplsService kplsService;

    @Autowired
    private KpspmxService kpspmxService;

    @Autowired
    private SkpService skpService;

    @Autowired
    private SkService skService;

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
        String xml = "";
        if ("11".equals(kpls.getFpczlxdm()) || (("12".equals(kpls.getFpczlxdm()) || "13".equals(kpls.getFpczlxdm())) && "12".equals(kpls.getFpzldm()))) {
            xml = getInvoiceXml(kpls);
            logger.debug("kplsh:" + kplsh + " xml:");
            logger.debug(xml);
            xml = Base64.encodeBase64String(xml.getBytes("UTF-8"));
        }
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
        SeperateInvoiceUtils.repeatSeparatePrice(kpspmxList);
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
            if ("11".equals(kpls.getFpczlxdm())) {
                params.put("kplx", "0");
            } else if ("12".equals(kpls.getFpczlxdm()) || "13".equals(kpls.getFpczlxdm())) {
                params.put("kplx", "1");
            }
        }
        String content = TemplateUtils.generateContent(templateName, params);
        return content;
    }

    /**
     * 生成待开票数据
     *
     * @return
     */
    public InvoicePendingData generatePendingData(int kpdid) {
        InvoicePendingData result = new InvoicePendingData();
        List<FptjVo> fptjVoList = kplsService.findFpdbtjjgByKpdid(kpdid);
        int zpkjsl = 0;
        int ppkjsl = 0;
        int dzpkjsl = 0;
        for (FptjVo fptjVo : fptjVoList) {
            String fpzldm = fptjVo.getFpzldm();
            int cnt = fptjVo.getCnt();
            if ("01".equals(fpzldm)) {
                zpkjsl += cnt;
            } else if ("02".equals(fpzldm)) {
                ppkjsl += cnt;
            } else if ("12".equals(fpzldm)) {
                dzpkjsl += cnt;
            }
        }
        result.setZpkjsl(zpkjsl);
        result.setPpkjsl(ppkjsl);
        result.setDzpkjsl(dzpkjsl);
        result.setKpdid(kpdid);
        result.setSuccess("true");
        return result;
    }

    /**
     * 作废发票
     *
     * @param kplsh
     * @param wait
     * @param timeout
     * @return
     * @throws Exception
     */
    public String voidInvoice(int kplsh, boolean wait, long timeout) throws Exception {
        try {
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

}
