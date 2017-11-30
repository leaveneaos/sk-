package com.rjxx.taxeasy.socket.command.receive;

import com.rjxx.taxeasy.domains.Cszb;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.CszbService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.socket.SocketSession;
import com.rjxx.taxeasy.socket.command.ICommand;
import com.rjxx.taxeasy.socket.command.SendCommand;
import com.rjxx.taxeasy.socket.domains.LoginInfo;
import com.rjxx.taxeasy.domains.ClientLogin;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.service.ClientLoginService;
import com.rjxx.taxeasy.service.SkpService;
import com.rjxx.utils.DesUtils;
import com.rjxx.utils.MD5Util;
import com.rjxx.utils.XmlJaxbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Administrator on 2017/1/5.
 */
@Service("LoginCommand")
public class LoginCommand implements ICommand {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ClientLoginService clientLoginService;

    @Autowired
    private SkpService skpService;

    @Autowired
    private KplsService kplsService;
    @Autowired
    private CszbService cszbService;

    @Override
    public void run(String commandId, String data, SocketSession socketSession) throws Exception {
        data = DesUtils.DESDecrypt(data, DesUtils.GLOBAL_DES_KEY);
        LoginInfo loginInfo = XmlJaxbUtils.convertXmlStrToObject(LoginInfo.class, data);
        String kpdid = loginInfo.Kpdid;
        String sessionId = loginInfo.SessionId;
        String macAddr = loginInfo.MacAddr;
        IoSession ioSession = socketSession.getSession();
        if (kpdid .equals("0") || StringUtils.isBlank(sessionId) || StringUtils.isBlank(macAddr)) {
            logout(ioSession, "非法登录");
            return;
        }
        //验证登录信息
//        Map paramsMap = new HashMap();
//        paramsMap.put("sessionId", sessionId);
//        paramsMap.put("orderBy", "login_time desc");
//        ClientLogin clientLogin = clientLoginService.findOneByParams(paramsMap);
//        //登录信息不存在或者超时了，要求重新登录
//        if (clientLogin == null || clientLogin.getExpireTime().getTime() < System.currentTimeMillis()) {
//            logger.info("kpdid:" + kpdid + " session has expired,client will logout!!!");
//            logout(ioSession, "License已过期");
//            return;
//        }
        //验证mac不通过
//        if (!clientLogin.getMacAddr().equals(macAddr)) {
//            logout(ioSession, "License已过期");
//            return;
//        }
        //校验开票点
        Skp skp = skpService.findOne(Integer.parseInt(kpdid));
        if (skp == null) {
            logger.info("kpdid:" + kpdid + " not exists,client will logout!!!");
            logout(ioSession, "License已过期");
            return;
        }

        //验证通过，产生desKey
        String desKey = MD5Util.generatePassword(kpdid).substring(0, 8);
        socketSession.setDesKey(desKey);
        socketSession.setLoginTime(new Date());
        Cszb cszb = cszbService.getSpbmbbh(skp.getGsdm(), skp.getXfid(), null, "sfzcdkpdkp");
        String sfzcdkpdkp = cszb.getCsz();
        if(sfzcdkpdkp.equals("是")){
            kpdid=skp.getSkph();
        }
        socketSession.setKpdid(kpdid);
        logger.info("kpd:" + kpdid + " " + ioSession + " connect to server");
        ioSession.setAttribute("kpdid", kpdid);
        String newCommandId = "";
        ioSession.write(SendCommand.SetDesKey + " " + newCommandId + " " + socketSession.getDesKey());

        return;
    }

    /**
     * 客户端的登出
     *
     * @param ioSession
     */
    private void logout(IoSession ioSession, String message) throws Exception {
        ioSession.write(SendCommand.Logout + " " + " " + message);
//        Thread.sleep(1000l);
//        ioSession.closeNow();
    }

}
