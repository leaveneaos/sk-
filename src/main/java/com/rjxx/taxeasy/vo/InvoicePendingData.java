package com.rjxx.taxeasy.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Administrator on 2017-03-21.
 */
@XmlRootElement(name = "InvoicePendingData")
@XmlAccessorType(XmlAccessType.FIELD)
public class InvoicePendingData {

    @XmlElement(name = "Success")
    private String success;

    @XmlElement(name = "Message")
    private String message;

    @XmlElement(name = "Kpdid")
    private int kpdid = 0;

    @XmlElement(name = "Ppkjsl")
    private int ppkjsl = 0;

    @XmlElement(name = "Zpkjsl")
    private int zpkjsl = 0;

    @XmlElement(name = "Dzpkjsl")
    private int dzpkjsl = 0;

    @XmlElement(name = "Pphcsl")
    private int pphcsl = 0;

    @XmlElement(name = "Zphcsl")
    private int zphcsl = 0;

    @XmlElement(name = "Dzphcsl")
    private int dzphcsl = 0;

    @XmlElement(name = "Ppzfsl")
    private int ppzfsl = 0;

    @XmlElement(name = "Zpzfsl")
    private int zpzfsl = 0;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getKpdid() {
        return kpdid;
    }

    public void setKpdid(int kpdid) {
        this.kpdid = kpdid;
    }

    public int getPpkjsl() {
        return ppkjsl;
    }

    public void setPpkjsl(int ppkjsl) {
        this.ppkjsl = ppkjsl;
    }

    public int getZpkjsl() {
        return zpkjsl;
    }

    public void setZpkjsl(int zpkjsl) {
        this.zpkjsl = zpkjsl;
    }

    public int getDzpkjsl() {
        return dzpkjsl;
    }

    public void setDzpkjsl(int dzpkjsl) {
        this.dzpkjsl = dzpkjsl;
    }

    public int getPphcsl() {
        return pphcsl;
    }

    public void setPphcsl(int pphcsl) {
        this.pphcsl = pphcsl;
    }

    public int getZphcsl() {
        return zphcsl;
    }

    public void setZphcsl(int zphcsl) {
        this.zphcsl = zphcsl;
    }

    public int getDzphcsl() {
        return dzphcsl;
    }

    public void setDzphcsl(int dzphcsl) {
        this.dzphcsl = dzphcsl;
    }

    public int getPpzfsl() {
        return ppzfsl;
    }

    public void setPpzfsl(int ppzfsl) {
        this.ppzfsl = ppzfsl;
    }

    public int getZpzfsl() {
        return zpzfsl;
    }

    public void setZpzfsl(int zpzfsl) {
        this.zpzfsl = zpzfsl;
    }
}
