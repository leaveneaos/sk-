package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.domains.ClientVersion;
import com.rjxx.taxeasy.domains.KpdVer;
import com.rjxx.taxeasy.service.ClientVersionService;
import com.rjxx.taxeasy.service.KpdVerService;
import com.rjxx.taxeasy.utils.ClientDesUtils;
import com.rjxx.taxeasy.vo.Version;
import com.rjxx.utils.DesUtils;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.XmlJaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
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

    @Autowired
    private ClientVersionService clientVersionService;

    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public String getVersion(String p) throws Exception {
        Version result = new Version();
        Map<String, String> queryMap = null;
        try {
            queryMap = ClientDesUtils.decryptClientQueryString(p);
        } catch (Exception e) {
            result.setSuccess("false");
            result.setMessage(e.getMessage());
            return generateVersionResult(result);
        }
        Map params = new HashMap();
        params.put("orderBy", "version_order desc");
        ClientVersion clientVersion = clientVersionService.findOneByParams(params);
        result.setTargetVersion(clientVersion.getVersion());
        result.setForceUpdate(true);
        result.setUpdateUrl(clientVersion.getDownloadUrl());
        result.setSuccess("true");
        return generateVersionResult(result);
    }

    /**
     * 更新版本号
     *
     * @param p
     * @return
     * @throws Exception
     */
    public String updateVersion(String p) throws Exception {
        Version result = new Version();
        Map<String, String> queryMap = null;
        try {
            queryMap = ClientDesUtils.decryptClientQueryString(p);
        } catch (Exception e) {
            result.setSuccess("false");
            result.setMessage(e.getMessage());
            return generateVersionResult(result);
        }
        String kpdid = queryMap.get("kpdid");
        String macAddr = queryMap.get("macAddr");
        String currentVersion = queryMap.get("version");
        KpdVer kpdVer = kpdVerService.findOneByParams(queryMap);
        if (kpdVer == null) {
            kpdVer = new KpdVer();
            kpdVer.setMacAddr(macAddr);
            kpdVer.setLrsj(new Date());
            kpdVer.setYxbz("1");
        }
        if (StringUtils.isNotBlank(kpdid)) {
            kpdVer.setKpdid(Integer.valueOf(kpdid));
        }
        kpdVer.setCurrentVer(currentVersion);
        kpdVer.setXgsj(new Date());
        kpdVerService.save(kpdVer);
        result.setSuccess("true");
        return generateVersionResult(result);
    }

    /**
     * 生成版本结果
     *
     * @param version
     * @return
     */
    private String generateVersionResult(Version version) throws Exception {
        String result = XmlJaxbUtils.toXml(version);
        result = DesUtils.DESEncrypt(result, DesUtils.GLOBAL_DES_KEY);
        return result;
    }

}
