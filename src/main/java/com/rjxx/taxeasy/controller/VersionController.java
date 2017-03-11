package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.domains.KpdVer;
import com.rjxx.taxeasy.service.KpdVerService;
import com.rjxx.taxeasy.utils.ClientDesUtils;
import com.rjxx.taxeasy.vo.Version;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017-03-08.
 */
@RequestMapping("/version")
@RestController
public class VersionController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${download.client.url:}")
    private String updateUrl;

    @Autowired
    private KpdVerService kpdVerService;

    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public String getVersion(String p) throws Exception {
        Map<String, String> map = new HashMap();
        Map<String, String> queryMap = null;
        try {
            queryMap = ClientDesUtils.decryptClientQueryString(p);
        } catch (Exception e) {
            map.put("success", "false");
            map.put("message", e.getMessage());
            return generateVersionResult(map);
        }
        String kpdid = queryMap.get("kpdid");
        String macAddr = queryMap.get("macAddr");
        Map params = new HashMap();
        KpdVer kpdVer = null;
        if (StringUtils.isNotBlank(kpdid)) {
            kpdVer = kpdVerService.findOneByKpdid(Integer.valueOf(kpdid));
        }
        Version version = new Version();
        if (kpdVer != null) {
            version.setCurrentVersion(kpdVer.getCurrentVer());
            version.setTargetVersion(kpdVer.getTargetVer());
        }
return null;
    }

    /**
     * 生成版本结果
     *
     * @param map
     * @return
     */
    private String generateVersionResult(Map<String, String> map) {
        return null;
    }

}
