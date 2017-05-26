<?xml version="1.0" encoding="gbk" ?>
<InvoiceRequest>
    <SerialNumber>${lsh}</SerialNumber>
    <AutoInvoiceWay>${kpfs!"manual_import"}</AutoInvoiceWay>
    <InvType>${kpls.fpzldm!}</InvType>
    <ServiceType>${kpls.fpczlxdm!}</ServiceType>
    <InvoiceCode>${kpls.fpdm!}</InvoiceCode>
    <InvoiceNo>${kpls.fphm!}</InvoiceNo>
    <CNNoticeNo>${kpls.hztzdh!}</CNNoticeNo>
    <CNDNCode>${kpls.hzyfpdm!}</CNDNCode>
    <CNDNNo>${kpls.hzyfphm!}</CNDNNo>
    <ChargeTaxWay>${kpls.zsfs!0}</ChargeTaxWay>
    <InvoiceXml>${xml!}</InvoiceXml>
    <PrintFlag>${kpls.printflag!"0"}</PrintFlag>
    <IsCaculateTailDiff>${IsCaculateTailDiff!"true"}</IsCaculateTailDiff>
</InvoiceRequest>