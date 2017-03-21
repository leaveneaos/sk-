package com.rjxx.taxeasy.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Administrator on 2017-03-09.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Version {

    @XmlElement
    private String Success;

    @XmlElement
    private String Message;

    @XmlElement
    private Integer Kpdid;

    @XmlElement
    private String CurrentVersion;

    @XmlElement
    private String TargetVersion;

    @XmlElement
    private boolean ForceUpdate;

    @XmlElement
    private String UpdateUrl;

    public String getSuccess() {
        return Success;
    }

    public void setSuccess(String success) {
        Success = success;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public Integer getKpdid() {
        return Kpdid;
    }

    public void setKpdid(Integer kpdid) {
        Kpdid = kpdid;
    }

    public String getCurrentVersion() {
        return CurrentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        CurrentVersion = currentVersion;
    }

    public String getTargetVersion() {
        return TargetVersion;
    }

    public void setTargetVersion(String targetVersion) {
        TargetVersion = targetVersion;
    }

    public boolean isForceUpdate() {
        return ForceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        ForceUpdate = forceUpdate;
    }

    public String getUpdateUrl() {
        return UpdateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        UpdateUrl = updateUrl;
    }
}
