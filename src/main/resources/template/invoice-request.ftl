<?xml version="1.0" encoding="gbk" ?>
<InvoiceRequest>
    <SerialNumber>${(kpls.kplsh?string('###'))!}</SerialNumber>
    <AutoInvoiceWay>${kpfs!"manual_import"}</AutoInvoiceWay>
    <InvType>${kpls.fpzldm!}</InvType>
    <ServiceType>${kpls.fpczlxdm!}</ServiceType>
    <ChargeTaxWay>${kpls.zsfs!0}</ChargeTaxWay>
    <InvoiceXml>${xml!}</InvoiceXml>
    <IsCaculateTailDiff>${IsCaculateTailDiff!"true"}</IsCaculateTailDiff>
</InvoiceRequest>