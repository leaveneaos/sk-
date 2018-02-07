package com.rjxx.taxeasy.utils;

/**
 * Created by xlm on 2018/1/25.
 */
public class CmdParam {


    public static int clientFlowNum=0;


    public static int AddClientFlowNum()
    {
        //防止大于Int最大值
        if(clientFlowNum>=32767)
        {
            clientFlowNum=0;
        }

        clientFlowNum++;
        return clientFlowNum;
    }

    public static final int RESULT_SUCCESS =  0;

    //------------------------------------------------命令ID---------------------------------------------------
    public static final int CMD_GENERALACK = 0x00000009;//通用应答




    //------------------------------------------------第三方开票--------------------------------------------

    public static final int CMD_THIRDINVOICE_COMMON_FPKJ = 0x00050101;//发票开具
    public static final int CMD_THIRDINVOICE_COMMON_FPKJRES = 0x00050102;//发票开具应答
    public static final int CMD_THIRDINVOICE_COMMON_FPCX = 0x00050103;//发票查询
    public static final int CMD_THIRDINVOICE_COMMON_FPCXRES = 0x40050103;//发票查询应答



    //------------------------------------------------加密类型---------------------------------------------------
    public static final byte ENCRYPT_NONE = 0x00;




    public static final int PACKET_HEADERFLAG = 0x56;

    public static final short TAG_FILE_UTF8 = 0x0009;
    public static final short TAG_FILE_GZIPUTF8 = 0x000C;
}
