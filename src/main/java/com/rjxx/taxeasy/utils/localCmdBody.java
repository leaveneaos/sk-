package com.rjxx.taxeasy.utils;

/**
 * Created by xlm on 2018/1/25.
 */
public class localCmdBody {


    private static localCmdBody single = null;

    // 静态工厂方法
    public static localCmdBody getInstance()
    {
        if (single == null)
        {
            single = new localCmdBody();
        }
        return single;
    }




    public CmdStru.CmdPackStru Pack_CMD_Json(int CmdId, String data,short TagFile){

        CmdStru.CmdPackStru pack=new CmdStru().new CmdPackStru();

        byte []Param=null;
        int Length=4;
        if(data!=null && data.length()>0 ) {
            FlowStru.ResStru res = ManageUtil.ConvertStrToBytes(data, TagFile);

            if (!res.isSuccess) {
                pack.ErrMsg = res.ErrMsg;

                return pack;
            }

            Param = (byte[]) res.obj;

            Length +=Param.length;
        }

        byte []BodyBuf=new byte[Length];

        byte []tempBuf = ManageUtil.toByteData(CmdId, 4);

        System.arraycopy(tempBuf, 0, BodyBuf,0, 4);

        if(Param!=null) {
            System.arraycopy(Param, 0, BodyBuf, 4, Param.length);
        }

        //byte[]BodyBuf=(byte[])res.obj;

        PackBody(pack,BodyBuf,CmdParam.ENCRYPT_NONE);

        return pack;
    }

    public void PackBody(CmdStru.CmdPackStru pack, byte[]buf, byte encryptType)
    {
        PackBody(pack,buf,encryptType,false,-1);

    }
    public void PackBody(CmdStru.CmdPackStru pack, byte[]buf,byte encryptType, boolean IsClientFlowNum, int ClientFlowNum){
        LocalCmdHeader.getInstance().PackCmdHeader(pack.Header,buf.length,encryptType,IsClientFlowNum,ClientFlowNum);
        byte []tempBuf=pack.Header.data;
        int totalLen=tempBuf.length+buf.length;
        byte[] totalbuf = new byte[totalLen];
        System.arraycopy(tempBuf, 0, totalbuf, 0, tempBuf.length);
        System.arraycopy(buf, 0, totalbuf, tempBuf.length, buf.length);
        pack.isSuccess=true;
        pack.TotalData=totalbuf;
    }

    /**
     * 解析通用报文
     * @param Buf
     */
    public CmdStru.GenralAckStru UnPack_CMD_SIMPLE_ACK(byte []Buf)
    {
        CmdStru.GenralAckStru stru=new CmdStru().new GenralAckStru();

        if(Buf.length!=8)
        {
            stru.isSuccess=false;
            return stru;
        }

        stru.CmdId=ManageUtil.ntohl(Buf[0], Buf[1], Buf[2], Buf[3]);

        stru.Result=ManageUtil.ntohl(Buf[4], Buf[5], Buf[6], Buf[7]);


        stru.isSuccess=true;

        return stru;
    }



    public CmdStru.CmdPackStru Pack_CMD_FPCX_CSYY(int CmdId, long RequestTrackId){

        CmdStru.CmdPackStru pack=new CmdStru().new CmdPackStru();

        int Length=4+8;

        byte []BodyBuf=new byte[Length];

        byte []tempBuf = ManageUtil.toByteData(CmdId, 4);

        System.arraycopy(tempBuf, 0, BodyBuf,0, 4);


        tempBuf = ManageUtil.toByteData(RequestTrackId);

        System.arraycopy(tempBuf, 0, BodyBuf, 4, 8);

        //byte[]BodyBuf=(byte[])res.obj;

        PackBody(pack,BodyBuf,CmdParam.ENCRYPT_NONE);

        return pack;
    }


    /**
     * 打包   通用报文
     * @return
     */
    public CmdStru.CmdPackStru Pack_CMD_GENRALACK(CmdStru.GenralAckStru stru)
    {
        CmdStru.CmdHeaderStru cmdHeader=new CmdStru().new CmdHeaderStru();
        LocalCmdHeader.getInstance().PackCmdHeader(cmdHeader, 12, CmdParam.ENCRYPT_NONE,true,stru.FlowNum);
        byte[] tempBuf=cmdHeader.data;

        int totalLen=tempBuf.length+12;

        byte[] buf = new byte[totalLen];

        System.arraycopy(tempBuf, 0, buf, 0, tempBuf.length);

        tempBuf = ManageUtil.toByteData(CmdParam.CMD_GENERALACK, 4);
        System.arraycopy(tempBuf, 0, buf, totalLen - 12, 4);

        tempBuf = ManageUtil.toByteData(stru.CmdId, 4);
        System.arraycopy(tempBuf, 0, buf, totalLen - 8, 4);

        tempBuf = ManageUtil.toByteData(stru.Result, 4);
        System.arraycopy(tempBuf, 0, buf, totalLen - 4, 4);

        CmdStru.CmdPackStru pack=new CmdStru().new CmdPackStru();

        pack.isSuccess=true;
        pack.TotalData=buf;

        return pack;
    }
}
