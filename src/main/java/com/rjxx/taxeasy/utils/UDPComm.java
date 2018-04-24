package com.rjxx.taxeasy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.Future;

/**
 * Created by xlm on 2018/1/25.
 */
public class UDPComm {
    private    static Logger logger = LoggerFactory.getLogger(UDPComm.class);

    private DatagramSocket socket;
    DatagramPacket sPacket;

    private byte[] udpRxData;
    Future<?> rcvThread;

    private static UDPComm single = null;

    // 静态工厂方法
    public static UDPComm getInstance()
    {
        if (single == null)
        {
            single = new UDPComm();
        }
        return single;
    }

    private UDPComm()
    {

    }

    public void DestroyInstance()
    {
        try
        {
            DisConnectSocket();

            single = null;
            socket = null;
            sPacket = null;
            udpRxData = null;
            rcvThread.cancel(true);
            rcvThread = null;

        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ManageUtil.ExLog(e);
        }
    }

    public void InitSocket()
    {
        if (socket == null)
        {
            try
            {
                socket = new DatagramSocket();
                // socket.bind(null);
                socket.setReuseAddress(true);

                rcvThread = InvoiceApp.fixThreadPool.submit(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        byte[] rxData = new byte[2000];
                        DatagramPacket rcPkt = new DatagramPacket(rxData,
                                rxData.length);
                        while (true)
                        {
                            // LogUtils.manprintf( "Manage UDP begin rcv:");
                            try
                            {

                                socket.receive(rcPkt);
                                udpRxData = ManageUtil.subBytes(rxData, 0,
                                        rcPkt.getLength());
                                logger.info("------税空盒子开票返回应答---------"+ManageUtil.byteToHexString(rxData));
                                //CmdReceive.getInstance().AddRev(udpRxData);
                                logger.info("------税空盒子开票返回应答---------"+ManageUtil.byteToHexString(udpRxData));
                                // LogUtils.manprintf("rcv:"+ManageUtil.byteToHexString(udpRxData));
                                ManageUtil.printByte(udpRxData, "rcvByte:"
                                        + udpRxData.length + " " + "rcv:");
                                LocalCmdReceive.getInstance().Proc(udpRxData);
                            } catch (IOException e)
                            {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                ManageUtil.ExLog(e);
                                return;
                            }

                        }
                    }
                });

                logger.info("udp client init ok");
            } catch (SocketException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                ManageUtil.ExLog(e);
            }
        }
    }

    public void DisConnectSocket()
    {
        try
        {
            if (socket != null)
            {
                socket.close();
                socket = null;
            }
            if (sPacket != null)
                sPacket = null;
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ManageUtil.ExLog(e);
        }

    }


    /**
     *
     * @param buf
     * @return
     */
    public boolean Send(byte[] buf)
    {
        UDPComm.getInstance().InitSocket();
        byte[] txData = buf;
        int remotePort = ManageParam.Port;
        String url = ManageParam.Url;


        ManageUtil.printByte(txData, "send txDataLen:"+buf.length);

        logger.info( "send txData len:" + txData.length + " url:" +
                url + " port:"
                + remotePort);

        sPacket = null;

        try
        {
            sPacket = new DatagramPacket(txData, txData.length,
                    InetAddress.getByName(url), remotePort);

        } catch (UnknownHostException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ManageUtil.ExLog(e);
            return false;
        }
        if (sPacket != null)
        {

            try
            {
                socket.send(sPacket);

                try
                {
                    Thread.sleep(1000);
                } catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    ManageUtil.ExLog(e);
                }

                return true;
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                ManageUtil.ExLog(e);
            }

        }
        return false;
    }
}
