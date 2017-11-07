<?xml version="1.0" encoding="gbk" ?>
<TaxEasyLicense>
    <Success>${success}</Success>
    <Message>${message!}</Message>
    <SessionId>${sessionId!}</SessionId>
    <ExpireTime>${expireTime!}</ExpireTime>
    <#if xfList??>
    <XfList>
        <#list xfList as xf>
        <Xf>
            <Xfid>${xf.id!}</Xfid>
            <Xfsh>${xf.xfsh!}</Xfsh>
            <Xfmc>${xf.xfmc!}</Xfmc>
            <Xfdz>${xf.xfdz!}</Xfdz>
            <Xfdh>${xf.xfdh!}</Xfdh>
            <Xfyh>${xf.xfyh!}</Xfyh>
            <Xfyhzh>${xf.xfyhzh!}</Xfyhzh>
        </Xf>
        </#list>
    </XfList>
    </#if>
    <#if xfKzVoList??>
    <xfKzVoList>
        <#list xfKzVoList as xfkzvo>
            <XfKzVo>
                <Xfid>${xfkzvo.xfid!}</Xfid>
                <Sfdm>${xfkzvo.sfDm!}</Sfdm>
                <Sfmc>${xf.sfMc!}</Sfmc>
                <svrip>${xf.svrIp!}</svrip>
                <svrport>${xf.svrPort!}</svrport>
            </XfKzVo>
        </#list>
    </xfKzVoList>
    </#if>
    <#if kpdList??>
    <KpdList>
        <#list kpdList as kpd>
        <Kpd>
            <Kpdid>${kpd.id!}</Kpdid>
            <Kpddm>${kpd.kpddm!}</Kpddm>
            <Kpdmc>${kpd.kpdmc!}</Kpdmc>
            <Xfid>${kpd.xfid!}</Xfid>
            <Sksblx>${kpd.sbcs!"bw"}</Sksblx>
            <Skph>${kpd.skph!}</Skph>
            <Kplx>${kpd.kplx!}</Kplx>
            <Wrzs>${kpd.wrzs!"0"}</Wrzs>
        </Kpd>
        </#list>
    </KpdList>
    </#if>
</TaxEasyLicense>