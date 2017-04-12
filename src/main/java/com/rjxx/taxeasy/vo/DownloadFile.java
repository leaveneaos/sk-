package com.rjxx.taxeasy.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Administrator on 2017-04-07.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DownloadFile {

    @XmlElement(name = "Filename")
    private String filename;

    @XmlElement(name = "DownloadUrl")
    private String downloadUrl;

    @XmlElement(name = "InstallPath")
    private String installPath;

    @XmlElement(name = "NeedDecompress")
    private boolean needDecompress;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(String installPath) {
        this.installPath = installPath;
    }

    public boolean isNeedDecompress() {
        return needDecompress;
    }

    public void setNeedDecompress(boolean needDecompress) {
        this.needDecompress = needDecompress;
    }
}
