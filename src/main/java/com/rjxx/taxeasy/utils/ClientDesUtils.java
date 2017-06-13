package com.rjxx.taxeasy.utils;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.bizcomm.utils.DataOperate;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.utils.DesUtils;
import com.rjxx.utils.HtmlUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析客户端加密
 * Created by ZhangBing on 2017-03-09.
 */
@Service
public class ClientDesUtils {


    @Autowired
    private DataOperate dataOperate;

    @Autowired
    private GsxxService gsxxService;
    /**
     * 解密客户端的请求参数
     *
     * @param queryString
     * @return
     */
    public static Map<String, String> decryptClientQueryString(String queryString) throws Exception {
        if (StringUtils.isBlank(queryString)) {
            throw new Exception("参数不能为空");
        }
        try {
            String result = DesUtils.DESDecrypt(queryString, DesUtils.GLOBAL_DES_KEY);
            return HtmlUtils.parseQueryString(result);
        } catch (Exception e) {
            throw new Exception("非法请求");
        }
    }
    private static String getSign(String QueryData, String key) {
        String signSourceData = "data=" + QueryData + "&key=" + key;
        String newSign = DigestUtils.md5Hex(signSourceData);
        return newSign;
    }

    public static void main(String[] args) throws Exception {
        String ss="<?xml version="+'"'+"1.0"+'"'+" encoding="+'"'+"UTF-8"+'"'+" standalone="+'"'+"yes"+'"'+"?>" +
                "<Request>" +
                "    <OperationItem>" +
                "        <SerialNumber>JY2017061220265324</SerialNumber>" +
                "        <OrderNumber>Y7890666</OrderNumber>" +
                "        <OperationType>12</OperationType>" +
                "    </OperationItem>" +
                "    <InvoiceItems count="+'"'+"1"+'"'+">" +
                "        <InvoiceItem>" +
                "            <ReturnCode>0000</ReturnCode>" +
                "            <ReturnMessage></ReturnMessage>" +
                "            <InvoiceCode>150003522222</InvoiceCode>" +
                "            <InvoiceNumber>36432343</InvoiceNumber>" +
                "            <InvoiceDate>20170612202639</InvoiceDate>" +
                "            <InvoiceStatus>红冲发票</InvoiceStatus>" +
                "            <Amount>-1538.46</Amount>" +
                "            <TaxAmount>-261.54</TaxAmount>" +
                "            <PdfUrl>http://test.datarj.com/e-invoice-file/500102010003643/20170612/fe6f4fcd-a799-4453-91ac-64c12a1ea6cf.pdf</PdfUrl>" +
                "        </InvoiceItem>" +
                "    </InvoiceItems>" +
                "</Request>";
        String url="https://vrapi.fvt.tujia.com/Invoice/CallBack";
        String key="8e37be80cd6dcd8051d589d32f4d0ff2";
        String xfsh="500102010003643";
        String jylsh="201706122030";
        ClientDesUtils clientDesUtils=new ClientDesUtils();
       // Map map= clientDesUtils.httpPost(ss,url,key,xfsh,jylsh);
        //System.out.println(JSON.toJSON(map));
    }
    public  Map httpPost(String sendMes, Kpls kpls) throws Exception {
        Map parms=new HashMap();
        parms.put("gsdm",kpls.getGsdm());
        Gsxx gsxx=gsxxService.findOneByParams(parms);
        //String url="https://vrapi.fvt.tujia.com/Invoice/CallBack";
        String url=gsxx.getWsUrl();
        String Secret=getSign(sendMes,gsxx.getSecretKey());
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().
                setSocketTimeout(2000).setConnectTimeout(2000).build();
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-Type", "application/json");
        String strMessage = "";
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();
        Map resultMap = null;
        try {
            Map nvps = new HashMap();
            nvps.put("invoiceData", sendMes);
            nvps.put("sign", Secret);
            StringEntity requestEntity = new StringEntity(JSON.toJSONString(nvps), "utf-8");
            httpPost.setEntity(requestEntity);
            response = httpClient.execute(httpPost, new BasicHttpContext());
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("request url failed, http code=" + response.getStatusLine().getStatusCode()
                        + ", url=" + url);
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                reader = new BufferedReader(new InputStreamReader(entity.getContent(), "utf-8"));
                while ((strMessage = reader.readLine()) != null) {
                    buffer.append(strMessage);
                }
            }
            System.out.println("接收返回值:" + buffer.toString());
            //resultMap = handerReturnMes(buffer.toString());
        } catch (IOException e) {
            System.out.println("request url=" + url + ", exception, msg=" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (response != null) try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultMap;
    }

    /**
     * 接收返回报文并做后续处理
     *
     * @param returnMes
     *
     * @throws Exception
     */
    public  Map handerReturnMes(String returnMes) throws Exception {

        Document document = DocumentHelper.parseText(returnMes);
        Element root = document.getRootElement();
        List<Element> childElements = root.elements();
        Map resultMap = new HashMap();
        for (Element child : childElements) {
            if (child.elementText("ReturnCode").equals("0000")) {
                resultMap.put("ReturnCode", child.elementText("ReturnCode"));
                resultMap.put("ReturnMessage", child.elementText("ReturnMessage"));
            }else if(child.elementText("ReturnCode").equals("9999")){
                resultMap.put("ReturnCode", child.elementText("ReturnCode"));
                resultMap.put("ReturnMessage", child.elementText("ReturnMessage"));
            }
        }
        return resultMap;
    }
}
