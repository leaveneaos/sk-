package com.rjxx.taxeasy.utils;

import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.domains.Jyls;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.utils.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 解析发票文件的utils
 * Created by Administrator on 2017-04-14.
 */
@Service
public class ParseInvoiceFileUtils {

    @Autowired
    private KplsService kplsService;

    @Autowired
    private JylsService jylsService;

    /**
     * 解析纸质票批量导入结果的文本<br>
     * kjjg：开具结果，0-失败，1-成功，
     * fpdm：发票代码，fphm：发票号码，
     * sbyy：失败原因，kplsh：开票流水号
     * kprq：开票日期
     *
     * @param content
     * @return
     */
    public Map<String, String> parseZZPBulkImportText(String content) {
        Map<String, String> retMap = new HashMap<>();
        if (StringUtils.isBlank(content)) {
            retMap.put("kjjg", "0");
            retMap.put("sbyy", "开票软件没有返回结果，请去开票软件确认本次开具结果");
            return retMap;
        }
        //开票日期
        int pos11 = content.indexOf("[");
        int pos12 = content.indexOf("]");
        if (pos11 != -1 && pos12 != -1) {
            String kprq = content.substring(pos11 + 1, pos12);
            retMap.put("kprq", kprq);
        }
        content = content.replace("：", ":").replace("，", ",");
        int pos1 = content.indexOf("单据号:");
        int pos2 = content.indexOf("开具结果:");
        int startIndex = pos1 + "单据号:".length();
        //开票流水号
        String resultdjh = content.substring(startIndex, pos2);
        if (resultdjh.endsWith(",")) {
            resultdjh = resultdjh.substring(0, resultdjh.length() - 1);
        }
        int pos111 = resultdjh.indexOf("-");
        if (pos111 != -1) {
            resultdjh = resultdjh.substring(0, pos111);
        }
        retMap.put("kplsh", resultdjh);
        int pos3 = content.indexOf(",", pos2);
        startIndex = pos2 + "开具结果:".length();
        String kjjg = content.substring(startIndex, pos3);
        retMap.put("kjjg", kjjg);
        if ("0".equals(kjjg)) {
            //开具失败
            int pos4 = content.indexOf("失败原因:", pos3);
            startIndex = pos4 + "失败原因:".length();
            String sbyy = content.substring(startIndex).trim();
            retMap.put("sbyy", sbyy);
            return retMap;
        }
        int pos5 = content.indexOf("发票信息:", pos3);
        int pos6 = content.indexOf(",", pos5);
        startIndex = pos5 + "发票信息:".length();
        String fpzl = content.substring(startIndex, pos6);
        String fpinfo = content.substring(pos6 + 1).trim();
        String[] fpInfoArr = fpinfo.split(",");
        String fpdm = fpInfoArr[0];
        String fphm = fpInfoArr[1];
        retMap.put("fpdm", fpdm);
        retMap.put("fphm", fphm);
        return retMap;
    }

    /**
     * 更新开票结果
     *
     * @param response
     * @return
     */
    public void updateInvoiceResult(InvoiceResponse response) throws Exception {
        String returnCode = response.getReturnCode();
        if ("0000".equals(returnCode)) {
            String lsh = response.getLsh();
            int pos = lsh.indexOf("$");
            int kplsh;
            if (pos != -1) {
                kplsh = Integer.valueOf(lsh.substring(0, pos));
            } else {
                kplsh = Integer.valueOf(lsh);
            }
            Kpls kpls = kplsService.findOne(kplsh);
            kpls.setFpdm(response.getFpdm());
            kpls.setFphm(response.getFphm());
            kpls.setFpztdm("00");
            kpls.setErrorReason(null);
            kpls.setPrintflag("" + response.getPrintFlag());
            kpls.setKprq(DateUtils.parseDate(response.getKprq(), "yyyy-MM-dd HH:mm:ss"));
            kpls.setXgsj(new Date());
            kpls.setXgry(1);
            if (StringUtils.isNotBlank(response.getReturnMessage())) {
                kpls.setErrorReason(response.getReturnMessage());
            } else {
                kpls.setErrorReason(null);
            }
            kplsService.save(kpls);
            Jyls jyls = jylsService.findOne(kpls.getDjh());
            jyls.setClztdm("91");
            jylsService.save(jyls);
        } else {
            String lsh = response.getLsh();
            int pos = lsh.indexOf("$");
            int kplsh;
            if (pos != -1) {
                kplsh = Integer.valueOf(lsh.substring(0, pos));
            } else {
                kplsh = Integer.valueOf(lsh);
            }
            Kpls kpls = kplsService.findOne(kplsh);
            kpls.setFpztdm("05");
            kpls.setErrorReason(response.getReturnMessage());
            kpls.setXgsj(new Date());
            kpls.setXgry(1);
            kplsService.save(kpls);
            Jyls jyls = jylsService.findOne(kpls.getDjh());
            jyls.setClztdm("92");
            jylsService.save(jyls);
        }
    }


}
