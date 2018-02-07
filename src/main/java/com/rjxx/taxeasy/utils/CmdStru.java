package com.rjxx.taxeasy.utils;

import java.util.Date;
import java.util.Hashtable;

/**
 * Created by xlm on 2018/1/25.
 */
public class CmdStru {

    private static CmdStru single = null;

    // 静态工厂方法
    public static CmdStru getInstance() {
        if (single == null) {
            single = new CmdStru();
        }
        return single;
    }

    public class CmdPackMutiStru extends BaseStru
    {
        public Hashtable<Integer, CmdPackStru> FrameList = new Hashtable<Integer, CmdPackStru>();

        public Date AddTime=new Date();

        public int FrameCount;

        public boolean IsOver;

    }

    public class CmdPackStru extends BaseStru {

        public CmdHeaderStru Header = new CmdHeaderStru();

        public int cmdID;

        public byte[] Body;

        public byte[] TotalData;

        public boolean IsOver;

    }

    public class CmdHeaderStru {
        // 协议版本
        public byte proVer;

        // 数据包总长度
        public short totalLen;

        // 流水号
        public short flowNum;

        // 数据段加密类型
        public byte ctryptType;

        // ID类型
        public byte idType;

        // ID长度
        public byte idLen;

        //报文头数据
        public byte[] data;

        //是否多帧
        public boolean IsMutiFrame=false;

        //帧号
        public int FrameNum=0;

        //帧数
        public int FrameCount=0;

        //多帧数据校验和
        public byte[]FrameDataSha1=new byte[2];

        //终端SN
        public String SN;
    }

    /*
     * 通用应答
     */
    public class GenralAckStru extends BaseStru {
        public int CmdId;

        public int Result;


    }

}
