package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.domains.ClientLogin;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.domains.Xf;
import com.rjxx.taxeasy.domains.Yh;
import com.rjxx.taxeasy.service.ClientLoginService;
import com.rjxx.taxeasy.service.SkpService;
import com.rjxx.taxeasy.service.XfService;
import com.rjxx.taxeasy.service.YhService;
import com.rjxx.taxeasy.utils.ClientDesUtils;
import com.rjxx.taxeasy.vo.XfKzVo;
import com.rjxx.utils.DesUtils;
import com.rjxx.utils.PasswordUtils;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.TemplateUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * Created by Administrator on 2017/1/9.
 */
@Controller
@RequestMapping(value = "/client")
public class LoginController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private YhService yhService;

    @Autowired
    private ClientLoginService clientLoginService;

    @Autowired
    private XfService xfService;

    @Autowired
    private SkpService skpService;

    @Value("${rabbitmq.address}")
    private String MQhost;

    @Value("${socket.port:5672}")
    private int MQport;

    @Value("${rabbitmq.queueName:'queue_result_invoice'}")
    private int MQqueueName;

    @Value("${rabbitmq.username}")
    private int MQaccount;

    @Value("${rabbitmq.password}")
    private int MQpassword;

    @Value("${rabbitmq.virtualHost}")
    private int MQvhost;
    /**
     * 客户端登录
     *
     * @param p
     * @return
     */
    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String clientLogin(String p) throws Exception {
        Map map = new HashMap();
        Map<String, String> queryMap = null;
        try {
            queryMap = ClientDesUtils.decryptClientQueryString(p);
        } catch (Exception e) {
            map.put("success", "false");
            map.put("message", e.getMessage());
            return generateLoginResult(map);
        }
        String username = queryMap.get("username");
        String password = queryMap.get("password");
        String macAddr = queryMap.get("macAddr");
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            map.put("success", "false");
            map.put("message", "用户名或密码不正确");
            return generateLoginResult(map);
        }
        String encryptPassword = PasswordUtils.encrypt(password);
        Map params = new HashMap();
        params.put("dlyhid", username);
        Yh yh = yhService.findOneByParams(params);
        if (yh == null || !encryptPassword.equals(yh.getYhmm())) {
            map.put("success", "false");
            map.put("message", "用户名或密码不正确");
            return generateLoginResult(map);
        }
        String sessionId = UUID.randomUUID().toString();
        //保存客户端登录信息
        ClientLogin clientLogin = new ClientLogin();
        clientLogin.setLoginTime(new Date());
        clientLogin.setMacAddr(macAddr);
        clientLogin.setSessionId(sessionId);
        //目前默认1年有效期
        clientLogin.setExpireTime(DateUtils.addYears(new Date(), 1));
        clientLogin.setUserId(yh.getId());
        clientLoginService.save(clientLogin);
        String expireTime = DateFormatUtils.format(clientLogin.getExpireTime(), "yyyy-MM-dd");
        //获取销方信息和开票点信息
        List<Xf> xfList = xfService.getXfListByYhId(yh.getId());
        if (xfList == null || xfList.isEmpty()) {
            map.put("success", "false");
            map.put("message", "该用户没有销方，请到平台进行维护");
            return generateLoginResult(map);
        }
        map.put("xfList", xfList);
        Map params2 = new HashMap();
        params2.put("xfList", xfList);
        List<XfKzVo> xfKzVoList = xfService.findXfkzListByXfid(params2);
        map.put("xfKzVoList", xfKzVoList);
        List<Skp> kpdList = skpService.getSkpListByYhId(yh.getId());
        if (kpdList == null || kpdList.isEmpty()) {
            map.put("success", "false");
            map.put("message", "该用户没有开票点信息，请到平台进行维护");
            return generateLoginResult(map);
        }
        for (Skp skp : kpdList) {
            if (StringUtils.isNotBlank(skp.getSbcs())) {
                if ("1".equals(skp.getSbcs())) {
                    skp.setSbcs("bw");
                } else {
                    skp.setSbcs("hx");
                }
            }
            String kplx = skp.getKplx();
            if (StringUtils.isNotBlank(kplx)) {
                StringBuilder kplxSB = new StringBuilder();
                if (kplx.contains("01")) {
                    kplxSB.append("01,");
                }
                if (kplx.contains("02")) {
                    kplxSB.append("02,");
                }
                if (kplx.contains("12")) {
                    kplxSB.append("12,");
                }
                if (kplxSB.length() > 0) {
                    skp.setKplx(kplxSB.substring(0, kplxSB.length() - 1));
                }
            }
        }
        map.put("kpdList", kpdList);
        map.put("success", "true");
        map.put("sessionId", sessionId);
        map.put("expireTime", expireTime);
        map.put("MQhost", MQhost);
        map.put("MQport", MQport);
        map.put("MQaccount", MQaccount);
        map.put("MQpassword", MQpassword);
        map.put("MQvhost", MQvhost);
        map.put("MQqueueName", MQqueueName);
        return generateLoginResult(map);
    }

    /**
     * 生成登录结果
     *
     * @param data
     * @return
     */
    private String generateLoginResult(Map data) throws Exception {
        String str = TemplateUtils.generateContent("login-result.ftl", data);
        String result = DesUtils.DESEncrypt(str, DesUtils.GLOBAL_DES_KEY);
        return result;
    }

    @Value("${socket.server}")
    private String socketServer;

    @Value("${socket.port}")
    private int socketPort;

    @RequestMapping(value = "/getConnectInfo", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String getConnectInfo(String p) throws Exception {
        Map map = new HashMap();
        Map<String, String> queryMap = null;
        try {
            queryMap = ClientDesUtils.decryptClientQueryString(p);
        } catch (Exception e) {
            map.put("success", "false");
            map.put("message", e.getMessage());
            return generateLoginResult(map);
        }
        //参数形式，sessionId=123
        //校验sessionId，暂时忽略
        String connectStr = "ip=" + socketServer + "&port=" + socketPort;
        String encryptStr = DesUtils.DESEncrypt(connectStr, DesUtils.GLOBAL_DES_KEY);
        return encryptStr;
    }


}
