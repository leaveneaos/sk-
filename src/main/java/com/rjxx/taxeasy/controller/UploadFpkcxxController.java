package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponseUtils;
import com.rjxx.taxeasy.bizcomm.utils.UploadFpckxxService;
import com.rjxx.taxeasy.invoice.ResponeseUtils;
import com.rjxx.utils.DesUtils;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.XmlJaxbUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;



/**
 * @author: zsq
 * @date: 2018/5/16 18:19
 * @describe:客户端交互：客户端上传    商品编码版本号，发票库存信息
 */
@Controller
@RequestMapping("/uploadFpkcxx")
public class UploadFpkcxxController {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    @Autowired
    private UploadFpckxxService uploadFpckxxService;


    /**
     * 获取商品编码版本号，发票库存信息
     * @param p
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getuploadFpkcxx", method = {RequestMethod.GET, RequestMethod.POST})
    public String getuploadFpkcxx(String p) throws Exception {
        logger.info("进入-upload---------------------"+p);
        if (StringUtils.isBlank(p)) {
            throw new Exception("参数不能为空");
        }
        String key = DesUtils.GLOBAL_DES_KEY;
        try {
            String uploadData = DesUtils.DESDecrypt(p, key);
            logger.info("解密之后的数据--------------"+uploadData);
            if(StringUtils.isBlank(uploadData)){
                return ResponeseUtils.error("数据解密失败");
            }
            InvoiceResponse invoiceResponse = uploadFpckxxService.UploadFpckxx(uploadData);
            String result = XmlJaxbUtils.toXml(invoiceResponse);
            logger.debug(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            InvoiceResponse response = InvoiceResponseUtils.responseError(e.getMessage());
            return XmlJaxbUtils.toXml(response);
        }
    }
}
