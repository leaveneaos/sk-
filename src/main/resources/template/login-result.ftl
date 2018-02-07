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
            <Xfid>${xf.id?replace(",","")}</Xfid>
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
                <Xfid>${xfkzvo.xfid?replace(",","")}</Xfid>
                <Sfdm>${xfkzvo.sfDm?replace(",","")}</Sfdm>
                <Sfmc>${xfkzvo.sfMc!}</Sfmc>
                <svrip>${xfkzvo.svrIp!}</svrip>
                <svrport>${xfkzvo.svrPort?replace(",","")}</svrport>
            </XfKzVo>
        </#list>
    </xfKzVoList>
    </#if>
    <#if kpdList??>
    <KpdList>
        <#list kpdList as kpd>
        <Kpd>
            <Kpdid>${kpd.id?replace(",","")}</Kpdid>
            <Kpddm>${kpd.kpddm!}</Kpddm>
            <Kpdmc>${kpd.kpdmc!}</Kpdmc>
            <Xfid>${kpd.xfid?replace(",","")}</Xfid>
            <Sksblx>${kpd.sbcs!"bw"}</Sksblx>
            <Skph>${kpd.skph!}</Skph>
            <Kplx>${kpd.kplx!}</Kplx>
            <Wrzs>${kpd.wrzs!"0"}</Wrzs>
            <Jkfs>${kpd.jkfs!}</Jkfs>
            <Zsmm>${kpd.zsmm!}</Zsmm>
            <Zcm>${kpd.zcm!}</Zcm>
            <Skpmm>${kpd.skpmm!}</Skpmm>
        </Kpd>
        </#list>
    </KpdList>
    </#if>
    <MQList>
        <MQ>
            <MQhost>${MQhost!}</MQhost>
            <MQport>${MQport!}</MQport>
            <MQqueueName>${MQqueueName!}</MQqueueName>
            <MQaccount>${MQaccount!}</MQaccount>
            <MQpassword>${MQpassword!}</MQpassword>
            <MQvhost>${MQvhost!}</MQvhost>
        </MQ>
    </MQList>
</TaxEasyLicense>