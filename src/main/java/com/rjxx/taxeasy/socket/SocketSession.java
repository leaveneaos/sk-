package com.rjxx.taxeasy.socket;

import org.apache.mina.core.session.IoSession;

import java.util.Date;

/**
 * 客户端连接的session
 * Created by Administrator on 2017/1/4.
 */
public class SocketSession {

    private Integer kpdid;

    private String desKey;

    private Date loginTime;

    private IoSession session;

    public Integer getKpdid() {
        return kpdid;
    }

    public void setKpdid(Integer kpdid) {
        this.kpdid = kpdid;
    }

    public String getDesKey() {
        return desKey;
    }

    public void setDesKey(String desKey) {
        this.desKey = desKey;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public IoSession getSession() {
        return session;
    }

    public void setSession(IoSession session) {
        this.session = session;
    }
}
