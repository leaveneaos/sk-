package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.bizcomm.utils.SkService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.KpspmxService;
import com.rjxx.taxeasy.socket.ServerHandler;
import com.rjxx.taxeasy.socket.command.SendCommand;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2017-02-28.
 */
@RestController
@RequestMapping(value = "/test")
public class TestController {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KplsService kplsService;

    @Autowired
    private KpspmxService kpspmxService;

    @Autowired
    private SkService skService;

    @Autowired
    private InvoiceController invoiceController;

    @RequestMapping(value = "/getCodeAndNo")
    public String getCodeAndNo(int kpdid, String fplxdm) throws Exception {
        try {
            String result = ServerHandler.sendMessage(kpdid, SendCommand.GetCodeAndNo, fplxdm);
            return result;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * 测试，上线后删除
     *
     * @param kplsh
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/invoice")
    @ResponseBody
    public String invoice(int kplsh) throws Exception {
        String encryptKplshStr = skService.encryptSkServerParameter("" + kplsh);
        String result = invoiceController.invoice(encryptKplshStr);
        return result;
    }

    /**
     * 测试，上线后删除
     *
     * @param kplsh
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/voidInvoice")
    @ResponseBody
    public String voidInvoice(int kplsh) throws Exception {
        String encryptKplshStr = skService.encryptSkServerParameter("" + kplsh);
        String result = invoiceController.voidInvoice(encryptKplshStr);
        return result;
    }
    
    @RequestMapping(value = "/repeatInovice")
    @ResponseBody
    public String repeatInovice(int kplsh) throws Exception {
        String encryptKplshStr = skService.encryptSkServerParameter("" + kplsh);
        String result = invoiceController.repeatInvoice(encryptKplshStr);
        return result;
    }

}
