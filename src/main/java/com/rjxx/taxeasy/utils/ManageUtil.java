package com.rjxx.taxeasy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by xlm on 2018/1/25.
 */
public class ManageUtil {


    private    static Logger logger = LoggerFactory.getLogger(ManageUtil.class);


    private static boolean bPrintManByteEnable = true;

    public static boolean isManByteEnable()
    {
        return bPrintManByteEnable;
    }

    public static void setPrintManByteEnable(boolean enable)
    {
        bPrintManByteEnable = enable;

    }

    /*
     * 打印byte数组
     */
    public static void printByte(byte[] data, String MsgHeader)
    {
        if (bPrintManByteEnable)
        {
            logger.info("ManageService", MsgHeader + " " + byteToHexString(data));
        }


    }

    /*
     * 打印异常日志
     */
    public static void ExLog(Exception e)
    {
        try
        {
            StringWriter sw = new StringWriter();

            e.printStackTrace(new PrintWriter(sw, true));

            String str = "Exception:" + sw.toString();

            logger.info(str);

        } catch (Exception ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();

        }
    }

    public static String byteToHexString(byte[] data)
    {
        String str = "";
        for (int i = 0; i < data.length; i++)
        {

            String value = Integer.toHexString(data[i] & 0xFF);

            if (value.length() == 1)
            {
                value = "0" + value;
            }

            str += " " + value;
        }

        return str;
    }

    public static String byteToHexStringNoSpace(byte[] data)
    {
        String str = "";
        for (int i = 0; i < data.length; i++)
        {

            String value = Integer.toHexString(data[i] & 0xFF);

            if (value.length() == 1)
            {
                value = "0" + value;
            }

            str += value;
        }

        return str;
    }

    public static byte[] hexStringToBytes(String hexString)
    {
        if (hexString == null || hexString.equals(""))
        {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++)
        {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c)
    {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static int ntohl(byte val1, byte val2, byte val3, byte val4)
    {
        return ((val1 << 24) & 0xFF000000) | ((val2 << 16) & 0x00FF0000)
                | ((val3 << 8) & 0x0000FF00) | (val4 & 0x000000FF);
    }

    public static int ntohs(byte val1, byte val2)
    {
        return ((val1 << 8) & 0x0000FF00) | (val2 & 0x000000FF);
    }

    public static long ntohl(byte[] byteNum)
    {
        long num = 0;
        for (int ix = 0; ix < 8; ++ix)
        {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }


    public static byte[] toByteData(long num)
    {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ++ix)
        {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    public static byte[] toByteData(int val, int len)
    {

        if (len != 2 && len != 4)
        {
            return null;
        }
        byte[] buf = new byte[len];
        if (len == 2)
        {
            buf[1] = (byte) (val & 0x00FF);
            buf[0] = (byte) ((val & 0xFF00) >> 8);
        } else
        {
            buf[3] = (byte) (val & 0x00FF);
            buf[2] = (byte) ((val & 0xFF00) >> 8);
            buf[1] = (byte) ((val & 0x00FF0000) >> 16);
            buf[0] = (byte) ((val & 0xFF000000) >> 24);
        }
        return buf;
    }


    public static String ReadTxtFile(String strFilePath)
    {
        String path = strFilePath;
        String content = ""; // 文件内容字符串
        // 打开文件
        File file = new File(path);
        // 如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory())
        {

            logger.info("The File doesn't not exist.");

        } else
        {
            try
            {
                InputStream instream = new FileInputStream(file);
                if (instream != null)
                {
                    InputStreamReader inputreader = new InputStreamReader(
                            instream, "UTF-8");// UTF-8 //GB2312
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    // 分行读取
                    while ((line = buffreader.readLine()) != null)
                    {
                        content += line + "\n";
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e)
            {

                logger.info("The File doesn't not exist.");
            } catch (IOException e)
            {

                logger.info(e.getMessage());
            }
        }

        if (content.length() > 0)
        {
            content = content.substring(0, content.length() - 1);
        }

        //content=content.replaceAll("\uFEFF","");


        logger.info("file content=" + content);

        return content;
    }




    public static int ConvertObjToInt(Object obj)
    {
        int Value = 0;
        try
        {
            Value = Integer.parseInt(String.valueOf(obj));
        } catch (Exception e)
        {
            // TODO: handle exception
            e.printStackTrace();

        }

        return Value;
    }


    public static long ConvertObjToLong(Object obj)
    {
        long Value = 0;
        try
        {
            Value = Long.parseLong(obj.toString());
        } catch (Exception e)
        {
            // TODO: handle exception
            e.printStackTrace();

        }

        return Value;
    }

    public static String ConvertDateToStr(Date date)
    {

        SimpleDateFormat formatter = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss.SSS");
        String GenerateTime = formatter.format(date);

        return GenerateTime;
    }

    public static byte[] subBytes(byte[] src, int begin, int count)
    {
        byte[] bs = new byte[count];
        for (int i = begin; i < begin + count; i++)
            bs[i - begin] = src[i];
        return bs;
    }


    /*
  * String  根据编码转化为 byte[]
  */
    public static FlowStru.ResStru ConvertStrToBytes(String value, short TagFile)
    {
        FlowStru.ResStru stru = new FlowStru().new ResStru();

        if (value == null) {
            stru.ErrMsg = "ConvertStrToByte,value=null";

            return stru;
        }

        byte[] conBuf = null;
        int Len = 0;
        if (value.length() > 0) {
            String Code = "ASCII";

            if (TagFile == CmdParam.TAG_FILE_UTF8 || TagFile == CmdParam.TAG_FILE_GZIPUTF8) {
                Code = "UTF-8";
            }

            //V
            try {
                conBuf = value.getBytes(Code);
                if (TagFile == CmdParam.TAG_FILE_GZIPUTF8) {
                    conBuf = ManageUtil.gZip(conBuf);

                    if (conBuf == null) {
                        stru.ErrMsg = "ConvertStrToByte,gZip转化失败";

                        return stru;
                    }

                }

                Len = conBuf.length;

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

                ManageUtil.ExLog(e);

                return stru;
            }


        }

        //
        byte[] buf = new byte[Len + 4];

        //T
        byte[] tempBuf = ManageUtil.toByteData(TagFile, 2);
        System.arraycopy(tempBuf, 0, buf, 0, 2);

        //L
        tempBuf = ManageUtil.toByteData(Len, 2);
        System.arraycopy(tempBuf, 0, buf, 2, 2);

        if (Len > 0) {
            System.arraycopy(conBuf, 0, buf, 4, conBuf.length);
        }

        stru.obj = buf;
        stru.isSuccess = true;

        return stru;


    }


    /*
     *  byte[]根据编码转化为 String
     */
    public static FlowStru.ResStru ConvertBytesToStr(byte[]Buf, short TagFile)
    {
        ManageUtil.printByte(Buf,"ConvertBytesToStr=");

        FlowStru.ResStru stru=new FlowStru().new ResStru();
        if(Buf.length<4)
        {
            stru.ErrMsg="ConvertBytesToStr，数据长度有误，Buf.length="+Buf.length;

            return stru;
        }

        //T
        int T = ManageUtil.ntohs(Buf[0], Buf[1]);
        if(T!=TagFile)
        {
            stru.ErrMsg="ConvertBytesToStr，T有误，T="+T;

            return stru;
        }

        //L
        int L = ManageUtil.ntohs(Buf[2], Buf[3]);

        if(Buf.length!=4+L)
        {

            stru.ErrMsg="ConvertBytesToStr，数据长度有误，Buf.length="+Buf.length+",4+L="+4+L;

            return stru;
        }


        if(L==0) {
            stru.isSuccess=true;
            stru.obj="";
            return stru;
        }

        byte[]KeyBuf=new byte[L];

        System.arraycopy(Buf, 4, KeyBuf, 0, L);

        try
        {
            String Code="ASCII";

            if(TagFile==CmdParam.TAG_FILE_UTF8)
            {
                Code="UTF-8";
            }

            String Value=new String(KeyBuf,Code);

            stru.obj=Value;

            stru.isSuccess=true;

            return stru;

        } catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();

            ManageUtil.ExLog(e);

            return stru;
        }


    }





    /***
     * 压缩GZip
     *
     * @param data
     * @return
     */
    public static byte[] gZip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.finish();
            gzip.close();
            b = bos.toByteArray();
            bos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            ManageUtil.ExLog(ex);
        }
        return b;
    }

    /***
     * 解压GZip
     *
     * @param data
     * @return
     */
    public static byte[] unGZip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[1024];
            int num = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, num);
            }
            b = baos.toByteArray();
            baos.flush();
            baos.close();
            gzip.close();
            bis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            ManageUtil.ExLog(ex);
        }
        return b;
    }
}
