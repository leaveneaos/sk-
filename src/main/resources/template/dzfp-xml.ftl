<?xml version="1.0" encoding="GBK"?>
<business id="FPKJ" comment="发票开具" version="2.0">
    <REQUEST_COMMON_FPKJ class="REQUEST_COMMON_FPKJ">
        <COMMON_FPKJ_FPT class="COMMON_FPKJ_FPT">
            <FPQQLSH>${kpls.kplsh?string('###')}</FPQQLSH>
            <KPLX>0</KPLX>
            <SGBZ>0</SGBZ>
            <HSBZ>0</HSBZ>
            <XSF_NSRSBH>${kpls.xfsh}</XSF_NSRSBH>
            <XSF_MC>${kpls.xfmc}</XSF_MC>
            <XSF_DZDH>${kpls.xfdz!}　${kpls.xfdh!}</XSF_DZDH>
            <XSF_YHZH>${kpls.xfyh!}　${kpls.xfyhzh!}</XSF_YHZH>
            <GMF_NSRSBH>${kpls.gfsh!}</GMF_NSRSBH>
            <GMF_MC>${kpls.gfmc}</GMF_MC>
            <GMF_DZDH>${kpls.gfdz!}　${kpls.gfdh!}</GMF_DZDH>
            <GMF_YHZH>${kpls.gfyh!}　${kpls.gfyhzh!}</GMF_YHZH>
            <KPR>${kpls.kpr!}</KPR>
            <SKR>${kpls.skr!}</SKR>
            <FHR>${kpls.fhr!}</FHR>
            <YFP_DM>${kpls.hzyfpdm!}</YFP_DM>
            <YFP_HM>${kpls.hzyfphm!}</YFP_HM>
            <JSHJ>${kpls.jshj!}</JSHJ>
            <HJJE>${kpls.hjje!}</HJJE>
            <HJSE>${kpls.hjse!}</HJSE>
            <BZ>${kpls.bz!}</BZ>
            <BMB_BBH>${spbmbbh!"12.0"}</BMB_BBH>
        </COMMON_FPKJ_FPT>
        <COMMON_FPKJ_XMXXS size="${mxCount!1}" class="COMMON_FPKJ_XMXX">
            <#list kpspmxList as kpspmx>
            <COMMON_FPKJ_XMXX>
                <FPHXZ>${kpspmx.fphxz!0}</FPHXZ>
                <XMMC>${kpspmx.spmc}</XMMC>
                <GGXH>${kpspmx.spggxh!}</GGXH>
                <DW>${kpspmx.spdw!}</DW>
                <XMSL>${(kpspmx.sps?string('#.######'))!}</XMSL>
                <XMDJ>${(kpspmx.spdj?string('#.######'))!}</XMDJ>
                <XMJE>${kpspmx.spje?string('#.######')}</XMJE>
                <SL>${kpspmx.spsl?string('#.######')}</SL>
                <SE>${kpspmx.spse?string('#.######')}</SE>
                <SPBM>${kpspmx.spdm}</SPBM>
                <ZXBM>${kpspmx.zxbm!}</ZXBM>
                <YHZCBS>${kpspmx.yhslbs!0}</YHZCBS>
                <LSLBS>${kpspmx.lslbs!0}</LSLBS>
                <ZZSTSGL>${yhzcbs!}</ZZSTSGL>
            </COMMON_FPKJ_XMXX>
            </#list>
        </COMMON_FPKJ_XMXXS>
    </REQUEST_COMMON_FPKJ>
</business>