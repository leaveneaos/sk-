package com.rjxx.taxeasy.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * Created by Administrator on 2017-04-19.
 */
@Controller
@RequestMapping("/uploadFile")
public class UploadFileController {

    @Value("${client-log.save.path:/mnt/client_logs}")
    private String clientLogSavePath;

    /**
     * 上传文件请求
     *
     * @param file  问阿金
     * @param kpdid 开票点id
     * @param wjlx  文件类型
     * @return
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String upload(MultipartFile file, int kpdid, String wjlx, String date) {
//        String savePath =
//        file.transferTo();

        return "1";
    }

}
