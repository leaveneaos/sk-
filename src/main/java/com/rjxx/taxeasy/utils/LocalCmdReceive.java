package com.rjxx.taxeasy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by xlm on 2018/1/25.
 */
public class LocalCmdReceive {

    private static LocalCmdReceive single = null;
    private    static Logger logger = LoggerFactory.getLogger(LocalCmdReceive.class);

    // 静态工厂方法
    public static LocalCmdReceive getInstance()
    {

        if (single == null)
        {
            single = new LocalCmdReceive();
        }

        return single;
    }

    public void Proc(final byte[] buf)
    {
        if (buf == null)
        {
            return;
        }

        InvoiceApp.catcheThreadPool.execute(new Runnable()
        {

            @Override
            public void run()
            {

                try
                {
                    OnReceive(buf);
                } catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    ManageUtil.ExLog(e);
                }
            }
        });
    }


    public void OnReceive(byte[] buf) throws Exception
    {
        try
        {
            if(Arrays.equals(buf,"ok".getBytes()))
            {
                logger.info("ok");
                return;
            }


            CmdStru.CmdPackStru cmdPack = LocalCmdHeader.getInstance().UnPackCmdHeader(buf);

            if (!cmdPack.isSuccess)
            {
                logger.info("OnReceive：UnPackCmdHeader解析失败");
                return;
            }

            switch (cmdPack.cmdID)
            {


                case CmdParam.CMD_THIRDINVOICE_COMMON_FPKJRES:// 发票查询
                {
                    OnReceive_CMD_IRain(cmdPack,"FPKJ");

                    break;
                }

                case CmdParam.CMD_THIRDINVOICE_COMMON_FPCXRES:// 发票查询
                {
                    OnReceive_CMD_IRain(cmdPack,"FPCX");

                    break;
                }

                case CmdParam.CMD_GENERALACK:// 通用应答
                {
                    OnReceive_CMD_GENERALACK(cmdPack);
                    break;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            ManageUtil.ExLog(e);

        }
    }

    // ------------------------------------------------通用应答---------------------------------------------------
    // 通用应答
    private void OnReceive_CMD_GENERALACK(CmdStru.CmdPackStru cmdPack)
    {
        if (cmdPack == null)
        {
            return;
        }

        CmdStru.GenralAckStru Stru = localCmdBody.getInstance().UnPack_CMD_SIMPLE_ACK(
                cmdPack.Body);

        if (Stru.isSuccess)
        {

            switch (Stru.CmdId)
            {
                default:
                {
                    logger.info("通用应答未知命令="
                            + ManageUtil.byteToHexString(ManageUtil.toByteData(
                            Stru.CmdId, 4)) + ",内容="
                            + ManageUtil.byteToHexString(cmdPack.Body));

                    logger.info("异常包："+ManageUtil.byteToHexString(cmdPack.Body) );
                    break;
                }
            }
        } else
        {
            logger.info("应答报文解析失败，FlowNum=" + cmdPack.Header.flowNum);
        }
    }

    private void OnReceive_CMD_IRain(CmdStru.CmdPackStru cmdPack,String type)
    {
        byte []Buf=cmdPack.Body;

        FlowStru.ResStru res= ManageUtil.ConvertBytesToStr(Buf, CmdParam.TAG_FILE_UTF8);

        if(!res.isSuccess)
        {
            logger.info("OnReceive_CMD_IRain解析失败,Type="+type+",Err="+res.ErrMsg);

            return;
        }

        String Data=(String) res.obj;

        LocalCmdSend.getInstance().Send_CMD_GENRALACK(cmdPack, CmdParam.RESULT_SUCCESS);

        logger.info("OnReceive_CMD_IRain获取成功,Type="+type+",Data="+Data);

        logger.info(Data);
    }
}
