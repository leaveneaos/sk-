package com.rjxx.taxeasy.utils;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.bizcomm.utils.DataOperate;
import com.rjxx.taxeasy.domains.Fphxwsjl;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.FphxwsjlService;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.utils.DesUtils;
import com.rjxx.utils.HtmlUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
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
    @Autowired
    private FphxwsjlService fphxwsjlService;
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
                "<Request>\n" +
                "    <OperationItem>\n" +
                "        <SerialNumber>JY20170613161553252</SerialNumber>\n" +
                "        <OrderNumber>T67800</OrderNumber>\n" +
                "        <OperationType>11</OperationType>\n" +
                "    </OperationItem>\n" +
                "    <InvoiceItems count=\"4\">\n" +
                "        <InvoiceItem>\n" +
                "            <ReturnCode>9999</ReturnCode>\n" +
                "            <ReturnMessage>-1 金额错误</ReturnMessage>\n" +
                "            <InvoiceDate>20170613165539</InvoiceDate>\n" +
                "            <InvoiceStatus>开具失败</InvoiceStatus>\n" +
                "            <Amount>45045.04</Amount>\n" +
                "            <TaxAmount>4954.96</TaxAmount>\n" +
                "        </InvoiceItem>\n" +
                "        <InvoiceItem>\n" +
                "            <ReturnCode>9999</ReturnCode>\n" +
                "            <ReturnMessage>-1 金额错误</ReturnMessage>\n" +
                "            <InvoiceDate>20170613165539</InvoiceDate>\n" +
                "            <InvoiceStatus>开具失败</InvoiceStatus>\n" +
                "            <Amount>45045.04</Amount>\n" +
                "            <TaxAmount>4954.96</TaxAmount>\n" +
                "        </InvoiceItem>\n" +
                "        <InvoiceItem>\n" +
                "            <ReturnCode>9999</ReturnCode>\n" +
                "            <ReturnMessage>-1 金额错误</ReturnMessage>\n" +
                "            <InvoiceDate>20170613165539</InvoiceDate>\n" +
                "            <InvoiceStatus>开具失败</InvoiceStatus>\n" +
                "            <Amount>45045.04</Amount>\n" +
                "            <TaxAmount>4954.96</TaxAmount>\n" +
                "        </InvoiceItem>\n" +
                "        <InvoiceItem>\n" +
                "            <ReturnCode>9999</ReturnCode>\n" +
                "            <ReturnMessage>-1 金额错误</ReturnMessage>\n" +
                "            <InvoiceDate>20170613165539</InvoiceDate>\n" +
                "            <InvoiceStatus>开具失败</InvoiceStatus>\n" +
                "            <Amount>36036.05</Amount>\n" +
                "            <TaxAmount>3963.95</TaxAmount>\n" +
                "        </InvoiceItem>\n" +
                "    </InvoiceItems>\n" +
                "</Request>";
        String url="https://vrapi.fvt.tujia.com/Invoice/CallBack";
        String key="8e37be80cd6dcd8051d589d32f4d0ff2";
        String xfsh="500102010003643";
        String jylsh="201706122030";
        ClientDesUtils clientDesUtils=new ClientDesUtils();
        Kpls kpls=new Kpls();
        kpls.setGsdm("tujia");
        Map map= clientDesUtils.httpPost(ss,url,key,xfsh,jylsh);
        System.out.println(JSON.toJSON(map));
    }
    public  Map httpPost(String sendMes,String url,String key,String xfsh,String jylsh) throws Exception {

        String Secret=getSign(sendMes,key);
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().
                setSocketTimeout(60*1000).setConnectionRequestTimeout(60*1000).setConnectTimeout(60*1000).build();
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
            resultMap = handerReturnMes(buffer.toString());
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
                setSocketTimeout(60*1000).setConnectionRequestTimeout(60*1000).setConnectTimeout(60*1000).build();
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
            resultMap = handerReturnMes(buffer.toString());
            String returnCode=resultMap.get("ReturnCode").toString();
            String ReturnMessage=resultMap.get("ReturnMessage").toString();
            Fphxwsjl fphxwsjl=new Fphxwsjl();
            fphxwsjl.setGsdm(kpls.getGsdm());
            fphxwsjl.setEnddate(new Date());
            fphxwsjl.setReturncode(returnCode);
            fphxwsjl.setStartdate(new Date());
            fphxwsjl.setSecretKey(gsxx.getSecretKey());
            fphxwsjl.setSign(Secret);
            fphxwsjl.setWsurl(gsxx.getWsUrl());
            fphxwsjl.setReturncontent(sendMes);
            fphxwsjl.setReturnmessage(ReturnMessage);
            fphxwsjlService.save(fphxwsjl);
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
            resultMap.put(child.getName(), child.getText());// 返回结果
        }
        return resultMap;
    }
}
