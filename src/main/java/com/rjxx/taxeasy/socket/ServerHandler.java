package com.rjxx.taxeasy.socket;

import com.rjxx.comm.utils.ApplicationContextUtils;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.service.SkpService;
import com.rjxx.taxeasy.socket.command.ICommand;
import com.rjxx.taxeasy.socket.command.ReceiveCommand;
import com.rjxx.taxeasy.socket.command.SendCommand;
import com.rjxx.taxeasy.socket.command.receive.LoginCommand;
import com.rjxx.utils.DesUtils;
import com.rjxx.utils.StringUtils;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2016/10/24.
 */
public class ServerHandler extends IoHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private static Map<Integer, SocketSession> cachedSession = new ConcurrentHashMap<>();

    private static Map<String, SocketRequest> cachedRequestMap = new ConcurrentHashMap<>();

    /**
     * 线程池执行任务
     */
    private static ThreadPoolTaskExecutor taskExecutor = null;

    static {
        Timer clearRequestTimer = new Timer();
        ClearRequestTask clearRequestTask = new ClearRequestTask();
        clearRequestTask.setMap(cachedRequestMap);
        clearRequestTimer.schedule(clearRequestTask, 60000, 5000);
    }

    /**
     * 向客户端发送消息
     *
     * @param kpdid
     * @param sendCommand
     * @param data        原数据，未加密的数据
     * @return
     * @throws Exception
     */
    public static String sendMessage(int kpdid, SendCommand sendCommand, String data, String commandId) throws Exception {
        return sendMessage(kpdid, sendCommand, data, commandId, true, 30000);
    }

    /**
     * 向客户端发送消息
     *
     * @param kpdid
     * @param sendCommand
     * @param data        原数据，未加密的数据
     * @return
     * @throws Exception
     */
    public static String sendMessage(int kpdid, SendCommand sendCommand, String data) throws Exception {
        return sendMessage(kpdid, sendCommand, data, null);
    }

    /**
     * 向客户端发送消息
     *
     * @param kpdid
     * @param sendCommand
     * @param data        原数据，未加密的数据
     * @param commandId   命令id
     * @param wait        是否等待返回结果,true需要等待返回结果，false不等待返回结果
     * @param timeout     等待超时时间
     * @return
     * @throws Exception
     */
    public static String sendMessage(int kpdid, SendCommand sendCommand, String data, String commandId, boolean wait, long timeout) throws Exception {
        if (StringUtils.isBlank(commandId)) {
            commandId = UUID.randomUUID().toString().replace("-", "");
        }
        SocketSession session = cachedSession.get(kpdid);
        if (session == null) {
            SkpService skpService = ApplicationContextUtils.getBean(SkpService.class);
            Skp skp = skpService.findOne(kpdid);
            if (skp != null) {
                throw new Exception("开票点：" + skp.getKpdmc() + "(" + skp.getKpddm() + ")" + "没有连上服务器");
            } else {
                throw new Exception("开票点：" + kpdid + "没有连上服务器");
            }
        }
        //加密数据
        if (!SendCommand.SetDesKey.equals(sendCommand) && StringUtils.isNotBlank(data)) {
            data = DesUtils.DESEncrypt(data, session.getDesKey());
        }
        String sendMessage = sendCommand + " " + commandId + " " + data;
        session.getSession().write(sendMessage);
        if (wait) {
            SocketRequest socketRequest = new SocketRequest();
            socketRequest.setCommandId(commandId);
            cachedRequestMap.put(commandId, socketRequest);
            if (timeout <= 0) {
                timeout = 30000;
            }
            synchronized (socketRequest) {
                socketRequest.wait(timeout);
            }
            if (socketRequest.getException() != null) {
                throw socketRequest.getException();
            }
//        logger.debug("---" + kpdid + " return message:" + socketRequest.getReturnMessage());
            return socketRequest.getReturnMessage();
        }
        return null;
    }

    /**
     * 发送信息
     *
     * @param session
     * @param sendCommand
     * @param data
     * @return
     * @throws Exception
     */
    public static void sendMessage(SocketSession session, SendCommand sendCommand, String data) throws Exception {
        System.out.println("send message session:" + session.getSession());
        String commandId = UUID.randomUUID().toString().replace("-", "");
        //加密数据
        if (!SendCommand.SetDesKey.name().equals(sendCommand) && StringUtils.isNotBlank(data)) {
            data = DesUtils.DESEncrypt(data, session.getDesKey());
        }
        String sendMessage = sendCommand + " " + commandId + " " + data;
        sendMessage(session.getSession(), sendMessage);
    }

    /**
     * 发送消息
     *
     * @param session
     * @param message
     */
    public static void sendMessage(IoSession session, String message) {
        session.write(message);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
//        session.setAttribute("openTime", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        Integer kpdid = (Integer) session.getAttribute("kpdid");
        if (kpdid != null) {
            SocketSession socketSession = cachedSession.get(kpdid);
            if (session == socketSession.getSession()) {
                logger.info("kpd:" + kpdid + " " + session + " has disconnected");
                cachedSession.remove(kpdid);
            } else {
                logger.info("old login kpd:" + kpdid + " " + session + " has disconnected");
            }
        } else {
            logger.info("unknown " + session + " has disconnected");
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        ReceiveTask receiveTask = new ReceiveTask();
        receiveTask.setMsg((String) message);
        receiveTask.setSession(session);
        if (taskExecutor == null) {
            taskExecutor = ApplicationContextUtils.getBean(ThreadPoolTaskExecutor.class);
        }
        taskExecutor.execute(receiveTask);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
//        logger.info("message:" + message);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        Integer kpdid = (Integer) session.getAttribute("kpdid");
        if (kpdid != null) {
            logger.info("kpd:" + kpdid + " " + session + " idle time out!!!");
        }
        logger.info("Server will interrupt the connection with the client");
        session.closeNow();
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        Integer kpdid = (Integer) session.getAttribute("kpdid");
        if (kpdid != null) {
            logger.error("kpd:" + kpdid + " " + session + " exception caught!!!", cause);
        }
    }

    /**
     * 接收任务
     */
    class ReceiveTask implements Runnable {

        private Logger logger = LoggerFactory.getLogger(this.getClass());

        private String msg;

        private IoSession session;

        @Override
        public void run() {
            try {
                logger.debug("receive message:" + msg);
                String[] arr = msg.split(" ");
                String commandName = arr[0];
                String commandId = "";
                String returnMessage = "";
                if (arr.length > 1) {
                    commandId = arr[1];
                }
                if (arr.length > 2) {
                    returnMessage = arr[2];
                }
                Integer kpdid = (Integer) session.getAttribute("kpdid");
                if (kpdid == null) {
                    if (ReceiveCommand.Login.name().equals(commandName)) {
                        //假如是登录命令，此处处理登录命令
                        LoginCommand loginCommand = ApplicationContextUtils.getBean(LoginCommand.class);
                        SocketSession socketSession = new SocketSession();
                        socketSession.setSession(session);
                        loginCommand.run(null, returnMessage, socketSession);
                        if (socketSession.getKpdid() != null && socketSession.getKpdid() != 0) {
                            SocketSession old = cachedSession.get(socketSession.getKpdid());
                            if (old != null && old.getSession() != session) {
                                sendMessage(old, SendCommand.Logout, "开票点已在其他地方登录！！！");
                            }
                            cachedSession.put(socketSession.getKpdid(), socketSession);
                        }
                    } else if (ReceiveCommand.HB.name().equals(commandName)) {
                        //是心跳命令则进行判断
                        Integer count = (Integer) session.getAttribute("HBCount");
                        if (count == null) {
                            session.setAttribute("HBCount", 1);
                        } else {
                            if (count > 2) {
                                session.write(SendCommand.Logout + " " + " ");
                                Thread.sleep(2000l);
                                session.closeNow();
                            }
                            count++;
                            session.setAttribute("HBCount", count);
                        }
                    } else {
                        //假如没有登录过并且不是登录命令，断开连接
                        session.closeNow();
                    }
                    return;
                }
                SocketSession socketSession = cachedSession.get(kpdid);
                if (socketSession == null) {
                    //没有缓存session，关闭回话，理论上不会
                    session.closeNow();
                    return;
                }
                String beanName = commandName + "Command";
                ICommand command = null;
                try {
                    command = ApplicationContextUtils.getBean(beanName, ICommand.class);
                } catch (Exception e) {
                    logger.error("", e);
                }
                //使用des解密返回的信息
                if (StringUtils.isNotBlank(returnMessage)) {
                    returnMessage = DesUtils.DESDecrypt(returnMessage, socketSession.getDesKey());
                }
                //进行业务处理
                if (command != null) {
                    command.run(commandId, returnMessage, socketSession);
                }
                //存在commanId，需要唤醒原来的线程
                if (StringUtils.isNotBlank(commandId)) {
                    SocketRequest socketRequest = cachedRequestMap.remove(commandId);
                    if (socketRequest != null) {
                        if (StringUtils.isNotBlank(returnMessage)) {
                            socketRequest.setReturnMessage(returnMessage);
                        } else {
                            socketRequest.setReturnMessage("");
                        }
                        synchronized (socketRequest) {
                            socketRequest.notifyAll();
                        }
                    } else {
//                logger.debug(commandName + " commandId:" + commandId + " not found");
                    }
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }

        public void setSession(IoSession session) {
            this.session = session;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

}
