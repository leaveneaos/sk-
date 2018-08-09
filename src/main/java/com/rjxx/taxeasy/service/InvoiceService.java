package com.rjxx.taxeasy.service;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.rjxx.taxeasy.bizcomm.utils.*;
import com.rjxx.taxeasy.config.RabbitmqUtils;
import com.rjxx.taxeasy.domains.Cszb;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Kpspmx;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.socket.ServerHandler;
import com.rjxx.taxeasy.socket.command.SendCommand;
import com.rjxx.taxeasy.utils.*;
import com.rjxx.taxeasy.vo.InvoicePendingData;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.TemplateUtils;
import com.rjxx.utils.XmlJaxbUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.support.PublisherCallbackChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * InvoiceController的service
 * Created by Administrator on 2017-05-31.
 */
@Service("invoiceService")
public class InvoiceService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KplsService kplsService;

    @Autowired
    private KpspmxService kpspmxService;

    @Autowired
    private SkpService skpService;

    @Autowired
    private RabbitmqUtils rabbitmqUtils;
    @Autowired
    private CszbService cszbService;
    @Autowired
    private GeneratePdfService generatePdfService;
    @Autowired
    private FpclService fpclService;

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
        if ("11".equals(kpls.getFpczlxdm()) || "12".equals(kpls.getFpczlxdm()) || "13".equals(kpls.getFpczlxdm())||"14".equals(kpls.getFpczlxdm())) {
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
        String result = null;
        String kpdid=null;
        try {
            Skp skp = skpService.findOne(kpls.getSkpid());
            Cszb cszb = cszbService.getSpbmbbh(skp.getGsdm(), skp.getXfid(), null, "sfzcdkpdkp");
            String sfzcdkpdkp = cszb.getCsz();
            if(sfzcdkpdkp.equals("是")){
                kpdid=skp.getSkph();
            }else{
                kpdid=kpls.getSkpid().toString();
            }
            result = ServerHandler.sendMessage(kpdid, SendCommand.Invoice, content, lsh, wait, timeout);
            logger.debug("----------客户端返回结果-------------"+result);
        } catch (Exception e) {
            result = e.getMessage();
        }
        if (StringUtils.isBlank(result)) {
            InvoiceResponse response = InvoiceResponseUtils.responseSuccess("成功发送客户端");
            return response;
        } else if (result.contains("开票点：") && result.contains("没有连上服务器")) {
            kpls.setFpztdm("04");
            kplsService.save(kpls);
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
                if (result.contains("开票点：") && result.contains("没有连上服务器")&&!kpls.getFpztdm().equals("04")) {
                    kpls.setFpztdm("04");
                    kplsService.save(kpls);
                }
                invoiceResponse.setReturnCode("9999");
                invoiceResponse.setReturnMessage("未知异常，发送结果为"+result);
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
        List<Kpspmx> kpspmxListnew=SeperateInvoiceUtils.repeatSeparatePrice(kpls,kpspmxList);
        //解决不了航信xml导入6位金额相加校验，开票四舍五入后2位金额相加校验
        /*if(null !=skp.getSbcs()&& !skp.getSbcs().equals("") && skp.getSbcs().equals("2")){
            double mxjehj =0d;
            if(!kpspmxListnew.isEmpty()){
                for(int i =0;i<kpspmxListnew.size();i++){
                    mxjehj = mxjehj + kpspmxListnew.get(i).getSpje();
                }
            }
            BigDecimal jehj = new BigDecimal(mxjehj);
            kpls.setHjje(jehj.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
            kpls.setHjse(kpls.getJshj()-kpls.getHjje());
        }*/
        //kpspmxService.save(kpspmxListnew);
        int xfid = skp.getXfid();
        int kpdid = skp.getId();
        Cszb cszb = cszbService.getSpbmbbh(kpls.getGsdm(), xfid, kpdid, "spbmbbh");
        String spbmbbh = cszb.getCsz();
        params.put("spbmbbh",spbmbbh);
        params.put("kpls", kpls);
        params.put("kpspmxList", kpspmxListnew);
        String gfyhzh = (kpls.getGfyh() == null ? "" : kpls.getGfyh()) + (kpls.getGfyhzh() == null ? "" : kpls.getGfyhzh());
        String gfdzdh = (kpls.getGfdz() == null ? "" : kpls.getGfdz()) + (kpls.getGfdh() == null ? "" : kpls.getGfdh());
        String xfyhzh =  (kpls.getXfyh() == null ? "" : kpls.getXfyh()) + (kpls.getXfyhzh() == null ? "" : kpls.getXfyhzh());
        String xfdzdh = (kpls.getXfdz() == null ? "" : kpls.getXfdz()) + (kpls.getXfdh() == null ? "" : kpls.getXfdh());
        gfyhzh = gfyhzh.trim();
        gfdzdh = gfdzdh.trim();
        xfyhzh = xfyhzh.trim();
        xfdzdh = xfdzdh.trim();
        if (StringUtils.isBlank(gfyhzh)) {
            gfyhzh = "　";
        }
        if (StringUtils.isBlank(gfdzdh)) {
            gfdzdh = "　";
        }
        if (StringUtils.isBlank(xfyhzh)) {
            xfyhzh = "　";
        }
        if (StringUtils.isBlank(xfdzdh)) {
            xfdzdh = "　";
        }
        params.put("gfyhzh", gfyhzh);
        params.put("gfdzdh", gfdzdh);
        params.put("xfyhzh", xfyhzh);
        params.put("xfdzdh", xfdzdh);
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
    public InvoicePendingData generatePendingData(String kpdid) {
        String skph=null;
        InvoicePendingData result = new InvoicePendingData();
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
        try {
            Channel channel = ((PublisherCallbackChannel) rabbitmqUtils.getChannel()).getDelegate();
            String zpQueueName = rabbitmqUtils.getQueueName(skph, "01");
            int zpkjsl = (int) channel.messageCount(zpQueueName);
            result.setZpkjsl(zpkjsl);
            String ppQueueName = rabbitmqUtils.getQueueName(skph, "02");
            int ppkjsl = (int) channel.messageCount(ppQueueName);
            result.setPpkjsl(ppkjsl);
            String dzpQueueName = rabbitmqUtils.getQueueName(skph, "12");
            int dzpkjsl = (int) channel.messageCount(dzpQueueName);
            result.setDzpkjsl(dzpkjsl);
            channel.close();
            logger.debug(kpdid + "-----" + "zp:" + zpkjsl + ",pp:" + ppkjsl + ",dzp:" + dzpkjsl);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("", e);
        }
        result.setKpdid(123);
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
           String  xml = getInvoiceXml(kpls);
            logger.debug("kplsh:" + kplsh + " xml:");
            logger.debug(xml);
            xml = Base64.encodeBase64String(xml.getBytes("UTF-8"));
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
            params.put("xml", xml);
            String commandId = kpls.getKplsh() + "$" + System.currentTimeMillis();
            params.put("lsh", kpls.getKplsh() + "");
            String content = TemplateUtils.generateContent("invoice-request.ftl", params);
            logger.debug(content);
            String result = null;
            String kpdid=null;
            try {
                Skp skp = skpService.findOne(kpls.getSkpid());
                Cszb cszb = cszbService.getSpbmbbh(skp.getGsdm(), skp.getXfid(), null, "sfzcdkpdkp");
                String sfzcdkpdkp = cszb.getCsz();
                if(sfzcdkpdkp.equals("是")){
                    kpdid=skp.getSkph();
                }else{
                    kpdid=kpls.getSkpid().toString();
                }
                result = ServerHandler.sendMessage(kpdid, SendCommand.VoidInvoice, content, commandId, wait, timeout);
            } catch (Exception e) {
                result = e.getMessage();
            }
            if (StringUtils.isBlank(result)) {
                InvoiceResponse response = InvoiceResponseUtils.responseSuccess("成功发送客户端");
            } else if (result.contains("开票点：") && result.contains("没有连上服务器")) {
                kpls.setFpztdm("04");
                kplsService.save(kpls);
            }
            logger.debug(result);
            return result;
        } catch (Exception e) {
            logger.error("", e);
            InvoiceResponse response = InvoiceResponseUtils.responseError(e.getMessage());
            return XmlJaxbUtils.toXml(response);
        }
    }

    public  InvoiceResponse generatePdf(int kplsh) {
        InvoiceResponse invoiceResponse=new InvoiceResponse();
       try {
           generatePdfService.generatePdf(kplsh);
           Kpls kpls = kplsService.findOne(kplsh);
           kpls.setFpztdm("00");
           kpls.setErrorReason("成功");
           kplsService.save(kpls);
           invoiceResponse.setReturnCode("0000");
       }catch (Exception e){
           invoiceResponse.setReturnCode("9999");
           invoiceResponse.setReturnMessage(e.getMessage());
           e.printStackTrace();
       }
        return invoiceResponse;
    }

    public InvoiceResponse skServerKP(int kplsh) {
        InvoiceResponse invoiceResponse=new InvoiceResponse();
        try{
            fpclService.skServerKP(kplsh);
            invoiceResponse.setReturnCode("0000");
        }catch (Exception e){
            invoiceResponse.setReturnCode("9999");
            invoiceResponse.setReturnMessage(e.getMessage());
            e.printStackTrace();
        }
        return invoiceResponse;
    }


    public InvoiceResponse skServerQuery(int kplsh) {
        InvoiceResponse invoiceResponse=new InvoiceResponse();
        Kpls kpls = kplsService.findOne(kplsh);
        Cszb cszb2 = cszbService.getSpbmbbh(kpls.getGsdm(), kpls.getXfid(), kpls.getSkpid(), "skurl");
        String url = cszb2.getCsz();
        Map resultMap = new HashMap();
        try{
            String queryStr= "<?xml version=\"1.0\" encoding=\"gbk\"?>"
                    + "<business id=\"FPCX\" comment=\"发票查询\">"
                    + "<REQUEST_COMMON_FPCX class=\"REQUEST_COMMON_FPCX\">"
                    + "<FPQQLSH>"+kplsh+"</FPQQLSH>"
                    + "</REQUEST_COMMON_FPCX>"
                    + "</business>";
            logger.info("调用税控服务器电子发票查询接口：kplsh+"+kplsh+",查询报文="+queryStr);
            resultMap=fpclService.DzfphttpPost(queryStr, url, kpls.getDjh() + "$" + kpls.getKplsh(), kpls.getXfsh(),
                    kpls.getJylsh(),2);
            fpclService.updateKpls(resultMap);
            String returncode = resultMap.get("RETURNCODE").toString();
            invoiceResponse.setFphm(resultMap.get("FP_HM").toString());
            invoiceResponse.setReturnCode(returncode);
        }catch (Exception e){
            //Kpls kpls=kplsService.findOne(Integer.parseInt(key));
            invoiceResponse.setReturnCode("9999");
            /*kpls.setFpztdm("04");
            kpls.setErrorReason(e.getMessage());
            kplsService.save(kpls);*/
            e.printStackTrace();
        }
        return invoiceResponse;
    }

    public InvoiceResponse skBoxKP(int kplsh) throws  Exception{
        Kpls kpls = kplsService.findOne(kplsh);
        Map params = new HashMap();
        params.put("kplsh", kpls.getKplsh());
        List<Kpspmx> kpspmxList = kpspmxService.findMxList(params);
        if (kpspmxList == null || kpspmxList.isEmpty()) {
            throw new Exception("没有商品明细");
        }
        String lsh = kpls.getKplsh() + "$" + System.currentTimeMillis();
        params.put("lsh", lsh);
        String result = null;
        String kpdid=null;
        try {
            Skp skp = skpService.findOne(kpls.getSkpid());
            Cszb cszb = cszbService.getSpbmbbh(skp.getGsdm(), skp.getXfid(), null, "sfzcdkpdkp");
            String sfzcdkpdkp = cszb.getCsz();
            if(sfzcdkpdkp.equals("是")){
                kpdid=skp.getSkph();
            }else{
                kpdid=kpls.getSkpid().toString();
            }
            String content=getJsonKpData(kpls,kpspmxList);
            //result = ServerHandler.sendMessage(kpdid, SendCommand.BoxInvoice, content, lsh, false, 0);
            //logger.debug("----------客户端返回结果-------------"+result);
        } catch (Exception e) {
            result = e.getMessage();
            e.printStackTrace();
        }
         return null;
    }

    private String getJsonKpData(Kpls kpls, List<Kpspmx> kpspmxList) {
            String hex= null;
            try {
            Map kpdata=new HashMap();
            kpdata.put("OpType",3);
            kpdata.put("PurchaserName",kpls.getGfmc());
            kpdata.put("PurchaserTaxId",kpls.getGfsh());
            kpdata.put("TotalTax",new BigDecimal(kpls.getHjse()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
            kpdata.put("TotalAmountWithTax",new BigDecimal(kpls.getJshj()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
            kpdata.put("Payee",kpls.getSkr());
            kpdata.put("Drawee",kpls.getKpr());
            kpdata.put("Remark",kpls.getBz());

            List ItemsList=new ArrayList();
            for(Kpspmx kpspmx:kpspmxList){
                Map itemMap=new HashMap();
                itemMap.put("ItemName",kpspmx.getSpmc());
                if(null!=kpspmx.getSpdj()){
                    itemMap.put("UnitPriceWithoutTax",new BigDecimal(kpspmx.getSpdj()).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
                }else{
                    itemMap.put("UnitPriceWithoutTax","");
                }
                if(null!=kpspmx.getSps()){
                    itemMap.put("Quantity",new BigDecimal(kpspmx.getSps()).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
                }else{
                    itemMap.put("Quantity","");
                }
                itemMap.put("AmountWithoutTax",new BigDecimal(kpspmx.getSpje()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                itemMap.put("TaxRate",kpspmx.getSpsl());
                itemMap.put("Tax",new BigDecimal(kpspmx.getSpse()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                if(null!=kpspmx.getSpdj()){
                    BigDecimal sps = new BigDecimal(kpspmx.getSps());
                    sps = sps.setScale(2,BigDecimal.ROUND_HALF_UP);
                    BigDecimal jshj = new BigDecimal(kpspmx.getSpje() + kpspmx.getSpse());
                    BigDecimal djWithTax = jshj.divide(sps, 3, BigDecimal.ROUND_HALF_UP);
                    itemMap.put("UnitPriceWithTax",djWithTax.doubleValue());
                }else{
                    itemMap.put("UnitPriceWithTax","");
                }
                itemMap.put("AmountWithTax", new BigDecimal(kpspmx.getSpje() + kpspmx.getSpse()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                ItemsList.add(itemMap);
            }
            kpdata.put("Items",ItemsList);
            kpdata.put("RequestTrackId",kpls.getKplsh());
            String jsonStr=JSON.toJSONString(kpdata);//json数据字符串
            logger.debug("----------json字符串-------------"+JSON.toJSONString(kpdata));
           /* hex = StringUtils.bytes2HexString(jsonStr.getBytes("UTF-8"));//16进制json字符串
            logger.debug("----------16进制json字符串-------------"+hex);

            Integer length=jsonStr.getBytes().length;
            String hexLength=length.toHexString(length);//16进制数据字节长度
            logger.debug("----------16进制json数据字节长度-------------"+hexLength);

            String hexkplsh= kpls.getKplsh().toHexString(kpls.getKplsh());//16进制的流水号
            logger.debug("----------16进制的流水号-------------"+hexkplsh);

                *//**
                 * 设备ID转16进制数据
                 *//*
            String IDhex=StringUtils.bytes2HexString("A1".getBytes("UTF-8"))+"20"+StringUtils.bytes2HexString("ABC12345".getBytes("UTF-8"));
            logger.debug("----------设备ID转16进制数据-------------"+IDhex);
            Integer IDlength=IDhex.getBytes().length+96;
            *//**
             * ID属性二进制转16进制
             *//*
            String  IDlengthHex=Integer.toHexString(Integer.parseInt("011"+IDlength.toBinaryString(IDlength), 2));
            logger.debug("----------ID属性二进制转16进制-------------"+IDlengthHex);
            Integer total=("000501010009"+hexLength+hex).getBytes().length;//数据包总长度
            String  tolalHex=total.toHexString(total);
            logger.debug("----------数据包总长度转16进制-------------"+tolalHex);
            hex="5601"+tolalHex+hexkplsh+"00"+IDlengthHex+IDhex+"000501010009"+hexLength+hex;*/
            CmdStru.CmdPackStru pack = localCmdBody.getInstance().Pack_CMD_Json(CmdParam.CMD_THIRDINVOICE_COMMON_FPKJ,jsonStr,CmdParam.TAG_FILE_UTF8,kpls.getKplsh());
                //CmdStru.CmdPackStru pack = localCmdBody.getInstance().Pack_CMD_FPCX_CSYY(CmdParam.CMD_THIRDINVOICE_COMMON_FPCX,2220);

                if (pack.isSuccess) {
                    logger.info("[SendFPKJ_CSYY]1：FlowNum=" + pack.Header.flowNum
                            + ",发送UDP包成功");
                    hex= ManageUtil.byteToHexString(pack.TotalData);
                    //UDPComm.getInstance().InitSocket();
                    boolean f=  UDPComm.getInstance().Send(pack.TotalData);
                    System.out.println(f);
                } else {
                    logger.info("[SendFPKJ_CSYY]1：FlowNum=" + pack.Header.flowNum
                            + ",打包失败");
                    hex="";
                }
            logger.debug("----------开票数据指令-------------"+hex);
            } catch (Exception e) {
            e.printStackTrace();
        }
        return hex;
    }
}
