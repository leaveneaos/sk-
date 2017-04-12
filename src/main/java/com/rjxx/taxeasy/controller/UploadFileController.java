package com.rjxx.taxeasy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Administrator on 2017-04-19.
 */
@Controller
@RequestMapping("/uploadFile")
public class UploadFileController {

    /**
     * 上传文件请求
     *
     * @param file
     * @param kpdid
     * @param fileName
     * @return
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String upload(MultipartFile file, int kpdid, String fileName) {


        return "1";
    }

}
