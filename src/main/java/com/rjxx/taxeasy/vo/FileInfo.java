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
public class FileInfo {

    @XmlElement(name = "Filename")
    private String filename;

    @XmlElement(name = "FileSize")
    private String fileSize;

    @XmlElement(name = "FileVersion")
    private String fileVersion;

    @XmlElement(name = "CreateTime")
    private String createTime;

    @XmlElement(name = "LastModifyTime")
    private String lastModifyTime;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileVersion() {
        return fileVersion;
    }

    public void setFileVersion(String fileVersion) {
        this.fileVersion = fileVersion;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(String lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }
}
