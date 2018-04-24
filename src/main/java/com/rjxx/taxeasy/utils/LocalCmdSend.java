package com.rjxx.taxeasy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xlm on 2018/1/25.
 */
public class LocalCmdSend {

    private static LocalCmdSend single = null;
    private    static Logger logger = LoggerFactory.getLogger(LocalCmdSend.class);

    // 静态工厂方法
    public static LocalCmdSend getInstance()
    {
        if (single == null)
        {
            single = new LocalCmdSend();
        }
        return single;
    }

    public void SendTest()
    {
        InvoiceApp.catcheThreadPool.execute(new Runnable() {

            @Override
            public void run() {

                try
                {
                    UDPComm.getInstance().Send("ok".getBytes());
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    ManageUtil.ExLog(e);
                }
            }
        });
    }

    public void SendByte()
    {
        InvoiceApp.catcheThreadPool.execute(new Runnable() {

            @Override
            public void run() {

                try
                {

                    String catchDir = "/sdcard/crvt/Test.txt";

                    String Content = ManageUtil.ReadTxtFile(catchDir).replace(" ", "");

                    logger.info("Content="+Content);

                    byte[] bytes=ManageUtil.hexStringToBytes(Content);

                    logger.info("bytes="+bytes.length);

                    UDPComm.getInstance().Send(bytes);
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    ManageUtil.ExLog(e);
                }
            }
        });
    }



    public void SendFPKJ()
    {
        InvoiceApp.catcheThreadPool.execute(new Runnable() {

            @Override
            public void run() {

                try
                {

                    String catchDir = "/sdcard/COMMON.txt";

                    String Content = ManageUtil.ReadTxtFile(catchDir);

                    CmdStru.CmdPackStru pack = localCmdBody.getInstance().Pack_CMD_Json(CmdParam.CMD_THIRDINVOICE_COMMON_FPKJ,Content,CmdParam.TAG_FILE_UTF8,1234);

                    if (pack.isSuccess) {

                        logger.info("[SendFPKJ_CSYY]1：FlowNum=" + pack.Header.flowNum
                                + ",发送UDP包成功");

                        UDPComm.getInstance().Send(pack.TotalData);
                        return;
                    } else {


                        logger.info("[SendFPKJ_CSYY]1：FlowNum=" + pack.Header.flowNum
                                + ",打包失败");

                        return;

                    }
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    ManageUtil.ExLog(e);
                }
            }
        });

    }

    public void SendFPCX(final long RequestTrackId, final int kplsh)
    {
        InvoiceApp.catcheThreadPool.execute(new Runnable() {

            @Override
            public void run() {

                try
                {
                    CmdStru.CmdPackStru pack = localCmdBody.getInstance().Pack_CMD_FPCX_CSYY(CmdParam.CMD_THIRDINVOICE_COMMON_FPCX,RequestTrackId, kplsh);

                    if (pack.isSuccess) {

                        logger.info("[SendCSCX_CSYY]1：FlowNum=" + pack.Header.flowNum
                                + ",发送UDP包成功");

                        UDPComm.getInstance().Send(pack.TotalData);
                        return;
                    } else {


                        logger.info("[SendFPCX_CSYY]1：FlowNum=" + pack.Header.flowNum
                                + ",打包失败");

                        return;

                    }
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    ManageUtil.ExLog(e);
                }
            }
        });

    }



    /**
     * 发送通用应答
     * @return
     */
    public void Send_CMD_GENRALACK(CmdStru.CmdPackStru cmdPack, int Result)
    {
        CmdStru.GenralAckStru genack = new CmdStru().new GenralAckStru();
        genack.CmdId = cmdPack.cmdID;
        genack.FlowNum =cmdPack.Header.flowNum;
        genack.Result =Result;

        CmdStru.CmdPackStru pack= localCmdBody.getInstance().Pack_CMD_GENRALACK(genack);

        if(pack.isSuccess)
        {
            UDPComm.getInstance().Send(pack.TotalData);
        }

    }
}
