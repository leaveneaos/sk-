package com.rjxx.taxeasy.socket.command;

/**
 * Created by Administrator on 2017/1/4.
 */
public enum SendCommand {
    //开具发票，重新打印，设置加密的key，踢出登录，获取发票代码发票号码
    Invoice, SetDesKey, Logout, GetCodeAndNo, VoidInvoice,RepeatInvoice

}
