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
public class UploadFile {

    @XmlElement(name = "FilePath")
    private String filePath;

    @XmlElement(name = "NeedCompress")
    private boolean needCompress = true;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isNeedCompress() {
        return needCompress;
    }

    public void setNeedCompress(boolean needCompress) {
        this.needCompress = needCompress;
    }
}
