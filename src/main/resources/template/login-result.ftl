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
        </Xf>
        </#list>
    </XfList>
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
        </Kpd>
        </#list>
    </KpdList>
    </#if>
</TaxEasyLicense>