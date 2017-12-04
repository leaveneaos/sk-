package com.rjxx.taxeasy.socket.command.receive;

import com.alibaba.fastjson.JSON;
import com.rjxx.comm.utils.ApplicationContextUtils;
import com.rjxx.taxeasy.bizcomm.utils.GeneratePdfService;
import com.rjxx.taxeasy.bizcomm.utils.HttpUtils;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.socket.SocketSession;
import com.rjxx.taxeasy.socket.command.ICommand;
import com.rjxx.taxeasy.socket.domains.ReturnInvoiceFile;
import com.rjxx.taxeasy.utils.ClientDesUtils;
import com.rjxx.taxeasy.utils.ParseInvoiceFileUtils;
import com.rjxx.taxeasy.vo.Kpspmxvo;
import com.rjxx.time.TimeUtil;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.XmlJaxbUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.time.DateUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017/1/19.
 */
@Service("ReturnInvoiceFileCommand")
public class ReturnInvoiceFileCommand implements ICommand {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KplsService kplsService;

    @Autowired
    private JylsService jylsService;

    @Autowired
    private KpspmxService kpspmxService;

    @Autowired
    private ClientFileService clientFileService;

    @Autowired
    private ParseInvoiceFileUtils parseInvoiceFileUtils;

    @Autowired
    private GeneratePdfService generatePdfService;

    @Autowired
    private ClientDesUtils clientDesUtils;

    @Autowired
    private GsxxService gsxxService;
    @Autowired
    private  CszbService cszbService;
    @Override
    public void run(String commandId, String data, SocketSession socketSession) throws Exception {
        logger.debug(data);
        ReturnInvoiceFile returnInvoiceFile = XmlJaxbUtils.convertXmlStrToObject(ReturnInvoiceFile.class, data);
        String returnCode = returnInvoiceFile.getReturnCode();

        if ("0000".equals(returnCode)) {
            boolean bulkImportResultFlag = returnInvoiceFile.isBulkImportResultFlag();
            String content = returnInvoiceFile.getFileContent();
            content = new String(Base64.decodeBase64(content), "UTF-8");
            String lsh = returnInvoiceFile.getLsh();
            int pos = lsh.indexOf("$");
            int kplsh;
            if (pos != -1) {
                kplsh = Integer.valueOf(lsh.substring(0, pos));
            } else {
                kplsh = Integer.valueOf(lsh);
            }
            saveFile(content, kplsh);
            logger.debug(content);

            Kpls kpls = kplsService.findOne(kplsh);
            Cszb cszb = cszbService.getSpbmbbh(kpls.getGsdm(), kpls.getXfid(), kpls.getSkpid(), "kpfs");

            if (!bulkImportResultFlag) {
                //不是批量导入，如果原来没有结果，就将结果入库
                if (StringUtils.isBlank(kpls.getFphm())) {
                    InvoiceResponse response = XmlJaxbUtils.convertXmlStrToObject(InvoiceResponse.class, content);
                    parseInvoiceFileUtils.updateInvoiceResult(response);
                }
                return;
            }
            if (kpls == null) {
                logger.info(kplsh + "该条数据不存在");
                return;
            }
            String fpzldm = kpls.getFpzldm();
            if ("12".equals(fpzldm)) {
                //解析电子发票返回的结果
                Map<String, String> resultMap = new HashMap<>();
                boolean suc = parseDzfpResultXml(resultMap, content);
                if (!suc) {
                    //解析xml异常
                    kpls.setFpztdm("05");
                    kpls.setErrorReason("返回的xml异常，无法解析");
                    kpls.setXgsj(new Date());
                    kplsService.save(kpls);
                    updateJyls(kpls.getDjh(), "92");
                    logger.error("dzfp return xml error!!!kplsh:" + kplsh + ",xml:" + content);
                    Map parms=new HashMap();
                    parms.put("gsdm",kpls.getGsdm());
                    Gsxx gsxx=gsxxService.findOneByParams(parms);
                    //String url="https://vrapi.fvt.tujia.com/Invoice/CallBack";
                    String url=gsxx.getCallbackurl();
                    if(!("").equals(url)&&url!=null){
                        String returnmessage=null;
                        if(!kpls.getGsdm().equals("Family")&&!kpls.getGsdm().equals("fwk")) {
                            returnmessage = generatePdfService.CreateReturnMessage(kpls.getKplsh());
                            //输出调用结果
                            logger.info("回写报文" + returnmessage);
                            if (returnmessage != null && !"".equals(returnmessage)) {
                                Map returnMap = clientDesUtils.httpPost(returnmessage, kpls);
                                logger.info("返回报文" + JSON.toJSONString(returnMap));
                            }
                        }else if(kpls.getGsdm().equals("fwk")){
                            returnmessage = generatePdfService.CreateReturnMessage3(kpls.getKplsh());
                            logger.info("回写报文" + returnmessage);
                            if (returnmessage != null && !"".equals(returnmessage)) {
                                String ss= HttpUtils.netWebService(url,"CallBack",returnmessage,gsxx.getAppKey(),gsxx.getSecretKey());
                                logger.info("返回报文" + ss);
                            }
                        }
                    }
                    return;
                }
                String dzfpReturnCode = resultMap.get("RETURNCODE");
                if (!"0000".equals(dzfpReturnCode)) {
                    //返回结果不正确
                    String dzfpReturnMsg = resultMap.get("RETURNMSG");
                    if ("-99 流水号重复".equals(dzfpReturnMsg)) {
                        //返回重复结果
                        if (StringUtils.isNotBlank(kpls.getFphm())) {
                            //状态正常，已经更新过了，不做处理了
                            logger.warn("kplsh:" + kpls.getKplsh() + " -99 流水号重复");
                            return;
                        }
                    }
                    kpls.setFpztdm("05");
                    if (StringUtils.isBlank(dzfpReturnMsg)) {
                        kpls.setErrorReason("未知异常，开票软件没有返回结果，请去开票软件确认");
                    } else {
                        kpls.setErrorReason(dzfpReturnMsg);
                    }
                    kplsService.save(kpls);
                    updateJyls(kpls.getDjh(), "92");
                    logger.error("dzfp return xml error!!!kplsh:" + kplsh + ",xml:" + content);
                    Map parms=new HashMap();
                    parms.put("gsdm",kpls.getGsdm());
                    Gsxx gsxx=gsxxService.findOneByParams(parms);
                    //String url="https://vrapi.fvt.tujia.com/Invoice/CallBack";
                    String url=gsxx.getCallbackurl();
                    if(!("").equals(url)&&url!=null){
                        String returnmessage=null;
                        if(!kpls.getGsdm().equals("Family")&&!kpls.getGsdm().equals("fwk")) {
                            returnmessage = generatePdfService.CreateReturnMessage(kpls.getKplsh());
                            //输出调用结果
                            logger.info("回写报文" + returnmessage);
                            if (returnmessage != null && !"".equals(returnmessage)) {
                                Map returnMap = clientDesUtils.httpPost(returnmessage, kpls);
                                logger.info("返回报文" + JSON.toJSONString(returnMap));
                            }
                        }else if(kpls.getGsdm().equals("fwk")){
                            returnmessage = generatePdfService.CreateReturnMessage3(kpls.getKplsh());
                            logger.info("回写报文" + returnmessage);
                            if (returnmessage != null && !"".equals(returnmessage)) {
                                String ss= HttpUtils.netWebService(url,"CallBack",returnmessage,gsxx.getAppKey(),gsxx.getSecretKey());
                            }
                        }
                    }
                    return;
                }
                //保存正常结果
                updateKpls(resultMap, kpls);
                String czlxdm = kpls.getFpczlxdm();
                if ("12".equals(czlxdm) || "13".equals(czlxdm)) {
                    updateJyls(kpls.getDjh(), "91");
                    if (kpls.getHzyfphm() != null && kpls.getHzyfpdm() != null) {
                        kpls.setJylsh("");
                        Kpls parms=new Kpls();
                        parms.setFpdm(kpls.getHzyfpdm());
                        parms.setFphm(kpls.getHzyfphm());
                        Kpls ykpls = kplsService.findByfphm(parms);
                        Map param2 = new HashMap<>();
                        param2.put("kplsh", ykpls.getKplsh());
                        // 全部红冲后修改
                        //Kpspmxvo mxvo = kpspmxService.findKhcje(param2);
                        //if (mxvo.getKhcje() == 0) {
                            param2.put("fpztdm", "02");
                            kplsService.updateFpczlx(param2);
                       /* } else {
                            param2.put("fpztdm", "01");
                            kplsService.updateFpczlx(param2);
                        }*/
                    }
                } else {
                    updateJyls(kpls.getDjh(), "21");
                }
                //此处开始生成pdf

                generatePdfService.generatePdf(kplsh);
                Map parms=new HashMap();
                parms.put("gsdm",kpls.getGsdm());
                Gsxx gsxx=gsxxService.findOneByParams(parms);
                //String url="https://vrapi.fvt.tujia.com/Invoice/CallBack";
                String url=gsxx.getCallbackurl();
                if(!("").equals(url)&&url!=null){
                    String returnmessage=null;
                    if(!kpls.getGsdm().equals("Family")&&!kpls.getGsdm().equals("fwk")) {
                        returnmessage = generatePdfService.CreateReturnMessage(kpls.getKplsh());
                        //输出调用结果
                        logger.info("回写报文" + returnmessage);
                        if (returnmessage != null && !"".equals(returnmessage)) {
                            Map returnMap = clientDesUtils.httpPost(returnmessage, kpls);
                            logger.info("返回报文" + JSON.toJSONString(returnMap));
                        }
                    }else if(kpls.getGsdm().equals("fwk")){
                        returnmessage = generatePdfService.CreateReturnMessage3(kpls.getKplsh());
                        logger.info("回写报文" + returnmessage);
                        if (returnmessage != null && !"".equals(returnmessage)) {
                             String ss= HttpUtils.netWebService(url,"CallBack",returnmessage,gsxx.getAppKey(),gsxx.getSecretKey());
                             String fwkReturnMessageStr=fwkReturnMessage(kpls);
                            logger.info("----------sap回写报文----------" + fwkReturnMessageStr);
                            String Data= HttpUtils.doPostSoap1_2("https://my337109.sapbydesign.com/sap/bc/srt/scs/sap/yyb40eysay_managegoldentaxinvo?sap-vhost=my337109.sapbydesign.com", fwkReturnMessageStr, null,"wendy","Welcome9");
                            logger.info("----------fwk平台回写返回报文--------" + ss);
                            logger.info("----------sap回写返回报文----------" + Data);

                        }
                    }
                }
            } else {
                //解析纸质票批量导入的结果
                Map<String, String> retMap = parseInvoiceFileUtils.parseZZPBulkImportText(content);
                String kjjg = retMap.get("kjjg");
                if ("0".equals(kjjg)) {
                    kpls.setFpztdm("05");
                    kpls.setErrorReason(retMap.get("sbyy"));
                    kpls.setXgsj(new Date());
                    kplsService.save(kpls);
                    updateJyls(kpls.getDjh(), "92");
                } else {
                    String fpdm = retMap.get("fpdm");
                    String fphm = retMap.get("fphm");
                    String kprq = retMap.get("kprq");
                    kpls.setFpdm(fpdm);
                    kpls.setFphm(fphm);
                    kpls.setKprq(DateUtils.parseDate(kprq, new String[]{"yyyy-MM-dd HH:mm:ss"}));
                    kpls.setFpztdm("00");
                    kpls.setXgsj(new Date());
                    Cszb cszb1 = cszbService.getSpbmbbh(kpls.getGsdm(),kpls.getXfid(),kpls.getSkpid(),"zpsfscpdf");
                    if(null !=cszb1 && cszb1.getCsz().equals("是")){
                        kpls.setJym("10497438135598948527");
                        kpls.setMwq("03*6<7-4937->9/1-544>0*1<76-</+0<<**87>-+>6+462+4145-1<+86*6<7-4937->9/1-538/0*>>687-44/8>4/*>010/17196-70/2>*81");
                    }
                    kplsService.save(kpls);
                    updateJyls(kpls.getDjh(), "91");
                    //20171204纸质专票生成pdf
                    if(null !=cszb1 && cszb1.getCsz().equals("是")){
                        generatePdfService.generatePdf(kplsh);
                    }
                }

                Map parms=new HashMap();
                parms.put("gsdm",kpls.getGsdm());
                Gsxx gsxx=gsxxService.findOneByParams(parms);
                //String url="https://vrapi.fvt.tujia.com/Invoice/CallBack";
                String url=gsxx.getCallbackurl();
                if(!("").equals(url)&&url!=null){
                    String returnmessage=null;
                    if(!kpls.getGsdm().equals("Family")&&!kpls.getGsdm().equals("fwk")) {
                        returnmessage = generatePdfService.CreateReturnMessage(kpls.getKplsh());
                        //输出调用结果
                        logger.info("回写报文" + returnmessage);
                        if (returnmessage != null && !"".equals(returnmessage)) {
                            Map returnMap = clientDesUtils.httpPost(returnmessage, kpls);
                            logger.info("返回报文" + JSON.toJSONString(returnMap));
                        }
                    }else if(kpls.getGsdm().equals("fwk")){
                        returnmessage = generatePdfService.CreateReturnMessage3(kpls.getKplsh());
                        logger.info("回写报文" + returnmessage);
                        if (returnmessage != null && !"".equals(returnmessage)) {
                            String ss= HttpUtils.netWebService(url,"CallBack",returnmessage,gsxx.getAppKey(),gsxx.getSecretKey());
                        }
                    }
                }
            }
        } else {
            throw new Exception("return invoice result file 9999 impossible");
        }
    }
    public String   fwkReturnMessage(Kpls kpls) {
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String result="Succeed";
        if(kpls.getFpczlxdm().equals("12")){
            result="CancelSucceed";
        }
        String ss="\n" +
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:glob=\"http://sap.com/xi/SAPGlobal20/Global\">\n" +
                "   <soap:Header/>\n" +
                "   <soap:Body>\n" +
                "      <glob:GoldenTaxGoldenTaxCreateRequest_sync>\n" +
                "         <BasicMessageHeader></BasicMessageHeader>\n" +
                "         <GoldenTax>\n" +
                "            <CutInvID>"+kpls.getJylsh()+"</CutInvID>\n" +
                "            <GoldenTaxID>"+kpls.getFphm()+"</GoldenTaxID>\n" +
                "            <GoldenTaxDate>\n" +
                "               <StartDateTime>"+sim.format(kpls.getKprq())+"</StartDateTime>\n" +
                "               <EndDateTime>"+sim.format(kpls.getKprq())+"</EndDateTime>\n" +
                "            </GoldenTaxDate>\n" +
                "            <GoldenTaxResult>"+result+"</GoldenTaxResult>\n" +
                "            <GoldenTaxCode>"+kpls.getFpdm()+"</GoldenTaxCode>\n" +
                "         </GoldenTax>\n" +
                "      </glob:GoldenTaxGoldenTaxCreateRequest_sync>\n" +
                "   </soap:Body>\n" +
                "</soap:Envelope>";
        return ss;
    }
    /**
     * 更新交易流水状态
     *
     * @param djh
     * @param clztdm
     */
    private void updateJyls(int djh, String clztdm) {
        Jyls jyls = jylsService.findOne(djh);
        if (jyls != null) {
            jyls.setClztdm(clztdm);
            jyls.setXgsj(new Date());
            jylsService.save(jyls);
        }
    }

    /**
     * 将文件保存到备份目录中
     *
     * @param data
     * @param kplsh
     */
    private void saveFile(String data, int kplsh) {
        //解析开票流水号
        ClientFile clientFile = new ClientFile();
        clientFile.setRefId(kplsh);
        clientFile.setContent(data);
        clientFile.setLrsj(new Date());
        clientFileService.save(clientFile);
    }

    /**
     * 在t_kpls及t_kpspmx 表中增加记录
     *
     * @param map
     * @param kpls
     */
    private void updateKpls(Map<String, String> map, Kpls kpls) {
        // 保存已开发票结果主表
        try {
            String fpdm = map.get("FP_DM");
            String fphm = map.get("FP_HM");
            String czlx = kpls.getFpczlxdm();
            kpls.setFpdm(fpdm);
            kpls.setFphm(fphm);
            if ("13".equals(czlx)) {
                kpls.setHkbz("1");
            } else {
                kpls.setHkbz("0");
            }
            kpls.setFpEwm(map.get("EWM"));
            kpls.setSksbm(map.get("JQBH"));
            kpls.setMwq(map.get("FP_MW"));
            kpls.setJym(map.get("JYM"));
            String kprq = map.get("KPRQ");
            kpls.setKprq(TimeUtil.getSysDateInDate(kprq, null));
            String bz = map.get("BZ");
            if (StringUtils.isNotBlank(bz)) {
                kpls.setBz(bz);
            }
            kpls.setFpztdm("00");
            kpls.setErrorReason(null);
            kpls.setXgsj(new Date());
            kplsService.save(kpls);
        } catch (Exception e) {
            logger.error("", e);
        }

    }

    /**
     * 解析电子发票返回的xml
     *
     * @param map
     * @param xml
     * @return
     */
    private boolean parseDzfpResultXml(Map<String, String> map, String xml) {
        // 创建SAXReader的对象sr
        SAXReader sr = new SAXReader();
        StringReader s = null;
        s = new StringReader(xml);
        InputSource is = new InputSource(s);
        Document doc = null;
        boolean flag = false;
        try {
            // 通过sr对象的read方法加载xml，获取document对象
            doc = sr.read(is);
            if (doc == null)
                return flag;
            // doc = DocumentHelper.parseText(xxml);
            // 通过doc对象获取根节点business
            Element business = doc.getRootElement();
            // System.out.println(business.getName());//根节点的name
            // 获取根节点的属性
            List<Attribute> bus = business.attributes();
            for (Attribute bu : bus) {
                map.put(bu.getName(), bu.getValue());
                // System.out.println(bu.getName()+bu.getValue());
            }
            // 通过business对象的elementIterator()方法获取迭代器
            Iterator it = business.elementIterator();
            // 遍历迭代器，获取根节点的信息
            while (it.hasNext()) {
                // 得到每一个body
                Element body = (Element) it.next();
                // 获取body的属性名与属性值
                List<Attribute> bodys = body.attributes();
                for (Attribute b : bodys) {
                    // System.out.println(body.getName());//根节点的子节点的name
                    map.put(b.getName(), b.getValue());
                    // System.out.println(b.getName()+b.getValue());
                }
                // 通过body对象的elementIterator()方法获取迭代器,获取body的节点名与节点值
                Iterator itt = body.elementIterator();
                while (itt.hasNext()) {
                    // 得到每一个body的节点
                    Element bos = (Element) itt.next();
                    map.put(bos.getName(), bos.getStringValue());
                    // System.out.println(bos.getName()+ bos.getStringValue());
                }
                flag = true;
            }
            s.close();
        } catch (Exception e) {
            logger.error("解析税控盘返回报文失败", e);
            flag = false;
        }
        return flag;
    }

}
