package com.rjxx.taxeasy.socket.domains;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Administrator on 2017/1/12.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "LoginInfo")
public class LoginInfo {

    @XmlElement(name = "Kpdid")
    public Integer Kpdid;

    @XmlElement(name = "SessionId")
    public String SessionId;

    @XmlElement(name = "MacAddr")
    public String MacAddr;

}
