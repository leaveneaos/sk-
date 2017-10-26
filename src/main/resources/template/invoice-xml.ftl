<?xml version="1.0" encoding="GBK"?>
<Kp>
    <Version>2.0</Version>
    <Fpxx>
        <Zsl>1</Zsl>
        <Fpsj>
            <Fp>
                <Djh>${kpls.kplsh?string('###')}</Djh>
                <Gfmc>${kpls.gfmc!}</Gfmc>
                <Gfsh>${kpls.gfsh!}</Gfsh>
                <Gfyhzh>${gfyhzh!}</Gfyhzh>
                <Gfdzdh>${gfdzdh!}</Gfdzdh>
                <xhdwsbh>${kpls.xfsh!}</xhdwsbh>
                <xhdwmc>${kpls.xfmc!}</xhdwmc>
                <xhdwdzdh>${xfdzdh!}</xhdwdzdh>
                <xhdwyhzh>${xfyhzh!}</xhdwyhzh>
                <kpr>${kpls.kpr!}</kpr>
                <qdbz>${kpls.sfdyqd!}</qdbz>
                <hjje>${(kpls.hjje?string('#.##'))!}</hjje>
                <hjse>${(kpls.hjse?string('#.##'))!}</hjse>
                <jshj>${(kpls.jshj?string('#.##'))!}</jshj>
                <Bz>${kpls.bz!}</Bz>
                <Fhr>${kpls.fhr!}</Fhr>
                <Skr>${kpls.skr!}</Skr>
                <Spbmbbh>${spbmbbh!"12.0"}</Spbmbbh>
                <Hsbz>${hsbz!0}</Hsbz>
                <Sgbz>${sgbz!0}</Sgbz>
                <Spxx>
                    <#list kpspmxList as kpspmx>
                    <Sph>
                        <Xh>${kpspmx.spmxxh?string('###')}</Xh>
                        <fphxz>${kpspmx.fphxz!}</fphxz>
                        <Spmc>${kpspmx.spmc!}</Spmc>
                        <Ggxh>${kpspmx.spggxh!}</Ggxh>
                        <Jldw>${kpspmx.spdw!}</Jldw>
                        <Spbm>${kpspmx.spdm}</Spbm>
                        <Qyspbm>${kpspmx.zxbm!}</Qyspbm>
                        <Syyhzcbz>${kpspmx.yhslbs!0}</Syyhzcbz>
                        <Lslbz>${kpspmx.lslbs!0}</Lslbz>
                        <Yhzcsm>${yhzcbs!}</Yhzcsm>
                        <Dj>${(kpspmx.spdj?string('#.###############'))!}</Dj>
                        <Sl>${(kpspmx.sps?string('#.###############'))!}</Sl>
                        <Je>${(kpspmx.spje?string('#.######'))!}</Je>
                        <Se>${(kpspmx.spse?string('#.######'))!}</Se>
                        <Slv>${(kpspmx.spsl?string('#.######'))!}</Slv>
                        <Kce>${(kpspmx.kce?string('#.######'))!}</Kce>
                    </Sph>
                    </#list>
                </Spxx>
            </Fp>
        </Fpsj>
    </Fpxx>
</Kp>