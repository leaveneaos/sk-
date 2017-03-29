package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.domains.Jyls;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.KpspmxService;
import com.rjxx.taxeasy.socket.SocketSession;
import com.rjxx.taxeasy.socket.command.ICommand;
import com.rjxx.taxeasy.vo.Kpspmxvo;
import com.rjxx.time.TimeUtil;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.XmlJaxbUtils;
import org.apache.commons.codec.binary.Base64;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;

import java.io.StringReader;
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

    @Override
    public void run(String commandId, String data, SocketSession socketSession) throws Exception {
        logger.debug(data);
        InvoiceResponse response = XmlJaxbUtils.convertXmlStrToObject(InvoiceResponse.class, data);
        String returnCode = response.getReturnCode();
        if ("0000".equals(returnCode)) {
            String content = response.getReturnMessage();
            content = new String(Base64.decodeBase64(content), "UTF-8");
            logger.debug(content);
            String lsh = response.getLsh();
            int pos = lsh.indexOf("$");
            int kplsh;
            if (pos != -1) {
                kplsh = Integer.valueOf(lsh.substring(0, pos));
            } else {
                kplsh = Integer.valueOf(lsh);
            }
            Kpls kpls = kplsService.findOne(kplsh);
            if (kpls == null) {
                logger.info(kplsh + "该条数据不存在");
                return;
            }
            String fpzldm = kpls.getFpzldm();
            if ("12".equals(fpzldm)) {
                //按电子发票返回的结果处理
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
                        return;
                }
                String dzfpReturnCode = resultMap.get("RETURNCODE");
                if (!"0000".equals(dzfpReturnCode)) {
                    //返回结果不正确
                    String dzfpReturnMsg = resultMap.get("RETURNMSG");
                    kpls.setFpztdm("05");
                    if (StringUtils.isBlank(dzfpReturnMsg)) {
                        kpls.setErrorReason("未知异常，开票软件没有返回结果，请去开票软件确认");
                    } else {
                        kpls.setErrorReason(dzfpReturnMsg);
                    }
                    kplsService.save(kpls);
                    updateJyls(kpls.getDjh(), "92");
                    logger.error("dzfp return xml error!!!kplsh:" + kplsh + ",xml:" + content);
                    return;
                }
                //保存正常结果
                updateKpls(resultMap, kpls);
                String czlxdm = kpls.getFpczlxdm();
                if ("12".equals(czlxdm) || "13".equals(czlxdm)) {
                    updateJyls(kpls.getDjh(), "91");
                   if(!kpls.getHkFphm().equals("")&&!kpls.getHkFpdm().equals("")){
                	 Kpls ykpls=kplsService.findByyfphm(kpls);
                	  Map param2 = new HashMap<>();
          			  param2.put("kplsh", ykpls.getKplsh());
	          			// 全部红冲后修改
	          			Kpspmxvo mxvo = kpspmxService.findKhcje(param2);
	          			if (mxvo.getKhcje() == 0) {
	          				param2.put("fpztdm", "02");
	          				kplsService.updateFpczlx(param2);
	          			} else {
	          				param2.put("fpztdm", "01");
	          				kplsService.updateFpczlx(param2);
	          			}
                   }
                } else {
                    updateJyls(kpls.getDjh(), "21");
                }
            } else {
                throw new Exception("纸质票批量导入返回结果还未处理");
            }
        }
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
            kpls.setFpztdm("00");
            kpls.setErrorReason(null);
            kpls.setXgsj(new Date());
            kplsService.save(kpls);
        } catch (Exception e) {
            e.printStackTrace();
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
