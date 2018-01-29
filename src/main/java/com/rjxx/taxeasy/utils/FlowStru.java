package com.rjxx.taxeasy.utils;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by xlm on 2018/1/25.
 */
public class FlowStru {


    /*
     * 简单命令返回结果
     */
    public class ResStru extends BaseStru
    {
        public Object obj;

    }


    public class FotaVersionStru
    {
        //程序集类型
        public String DevModelId;

        //程序集版本
        public String VersionMain;

        //更新方式   0：可选   1：强制    2：静默
        public String UpdateType;

        //版本信息
        public String VersionInfo;

        //public Vector<FotaVersionPartStru> PartList=new Vector<FotaVersionPartStru>();


    }

    public class FotaVersionPartStru
    {
        public int FileType;

        public int FileVer;

    }

    public class ConfigStru
    {
        public String Group;
        public String Key;
        public String Value;
    }

    public class DeviceConfigRequest
    {
        public ArrayList<String> ConfigNames = new ArrayList<String>();
    }

    public class DeviceConfigSet
    {
        public Hashtable<String,String> ConfigSets = new Hashtable<String,String>();
    }


    /*
     * 设备ID，包括开票机ID,税控盘ID,匿名终端ID
     */
    public class DeviceIdListStru
    {
        public Hashtable<String,DeviceIdStru> Ids = new Hashtable<String,DeviceIdStru>();
    }

    public class DeviceIdStru
    {
        public int Id;

        public String SerialNo;
    }

}
