package com.rjxx.taxeasy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xlm on 2018/1/25.
 */
public class LocalCmdHeader {


    private     Logger logger = LoggerFactory.getLogger(this.getClass());


    private static LocalCmdHeader single = null;

    // 静态工厂方法
    public static LocalCmdHeader getInstance()
    {

        if (single == null)
        {
            single = new LocalCmdHeader();
        }

        return single;
    }

    /**
     * 解析报文头
     *
     * @param buffer
     *            数据包
     * @return
     */
    public CmdStru.CmdPackStru UnPackCmdHeader(byte[] buffer)// 新报文,直接看是否要解包操作
    {
        CmdStru.CmdPackStru cmdPack = new CmdStru().new CmdPackStru();

        if ((0x56 != buffer[0]) || (buffer.length < 12))// 8head+cmd
        {
            // idInfo = cmdBodyInfo;
            logger.info("buf len or pkt head error,append it");

            cmdPack.isSuccess = false;
            return cmdPack;

        } else
        {
            int HeaderLen = 8;

            cmdPack.Header.proVer = (byte) (buffer[1] & 0x0F);
            cmdPack.Header.totalLen = (short) ManageUtil.ntohs(buffer[2],
                    buffer[3]);
            cmdPack.Header.flowNum = (short) ManageUtil.ntohs(buffer[4],
                    buffer[5]);
            cmdPack.Header.ctryptType = (byte) (buffer[6]);
            cmdPack.Header.idType = (byte) ((buffer[7] & 0xE0) >> 5);
            cmdPack.Header.idLen = (byte) (buffer[7] & 0x1F);

            if (cmdPack.Header.totalLen != buffer.length)
            {// 报文长度不够,保留报文,退出
                logger.info("pkt analyse, buf len no enough ,append buf ,return it");
                cmdPack.isSuccess = false;
                return cmdPack;
            }
            // 未加密数据
            byte[] tempBuf = new byte[buffer.length - HeaderLen- cmdPack.Header.idLen];
            byte[] byteSN = new byte[cmdPack.Header.idLen];
            System.arraycopy(buffer, HeaderLen + cmdPack.Header.idLen,tempBuf, 0, tempBuf.length);
            System.arraycopy(buffer, HeaderLen, byteSN, 0, byteSN.length);
            String SN=new String(byteSN);
            cmdPack.Header.SN = SN;
            cmdPack.cmdID = (int) ManageUtil.ntohl(tempBuf[0], tempBuf[1],tempBuf[2], tempBuf[3]);
            //排除前4字节-指令ID
            cmdPack.Body = new byte[tempBuf.length - 4];
            // cmdPack.Body排除CmdId
            System.arraycopy(tempBuf, 4, cmdPack.Body, 0, tempBuf.length - 4);
            cmdPack.isSuccess=true;
            return cmdPack;
        }
    }

    public void PackCmdHeader(CmdStru.CmdHeaderStru cmdHeader, int cmdBodyLen, byte encryptType
            , boolean IsClientFlowNum, int ClientFlowNum)
    {
        if (cmdHeader == null)
        {
            cmdHeader = new CmdStru().new CmdHeaderStru();
        }

        byte[]SN=ManageParam.GetSN().getBytes();

        byte[] buf = null;
        byte[] tempBuf = null;
        int outPktLen = 8+SN.length;

        int TotalLen = outPktLen + cmdBodyLen;

        buf = new byte[outPktLen];
        buf[0] = CmdParam.PACKET_HEADERFLAG;
        buf[1] = 0x01;
        buf[2] = (byte) ((TotalLen >> 8) & 0xFF);
        buf[3] = (byte) (TotalLen & 0xFF);

        int FlowNum = 0;

        if (IsClientFlowNum)
        {
            FlowNum = ClientFlowNum;
        } else
        {
            FlowNum = CmdParam.AddClientFlowNum();
        }

        cmdHeader.flowNum = (short) FlowNum;

        tempBuf = ManageUtil.toByteData(FlowNum, 2);
        System.arraycopy(tempBuf, 0, buf, 4, 2);

        buf[6]=encryptType;

        buf[7]=(byte) (ManageParam.GetSN().length() | 0x60);

        if(SN.length>0) {
            System.arraycopy(SN, 0, buf, 8, SN.length);
        }

        cmdHeader.data = buf;

        return;
    }
}
