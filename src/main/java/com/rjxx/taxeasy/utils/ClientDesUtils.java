package com.rjxx.taxeasy.utils;

import com.rjxx.utils.DesUtils;
import com.rjxx.utils.HtmlUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * 解析客户端加密
 * Created by ZhangBing on 2017-03-09.
 */
public class ClientDesUtils {

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

}
