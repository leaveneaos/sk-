package com.rjxx.taxeasy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2017-05-12.
 */
public class KpUtils {

    private static Logger logger = LoggerFactory.getLogger(KpUtils.class);

    /**
     * 根据流水号获取开票流水号
     *
     * @param lsh
     * @return
     */
    public static int getKplshByLsh(String lsh) {
        int pos = lsh.indexOf("$");
        String kplsh = lsh;
        if (pos != -1) {
            kplsh = lsh.substring(0, pos);
        }
        try {
            return Integer.valueOf(kplsh);
        } catch (Exception e) {
            logger.error("Get Kplsh From lsh Error:" + lsh, e);
        }
        return 0;
    }

}
