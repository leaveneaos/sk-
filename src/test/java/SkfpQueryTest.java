

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.*;

import java.util.*;

public class SkfpQueryTest {

    private static Logger logger = LoggerFactory.getLogger(SkfpQueryTest.class);

        public static void main(String[] args) throws Exception{
            String xml2 ="<?xml version=\"1.0\" encoding=\"gbk\"?>"
                    + "<business id=\"DJXXCX\" comment=\"登记信息查询\">"
                    + "<REQUEST_COMMON_DJXXCX class=\"REQUEST_COMMON_DJXXCX\">"
                    + "<NSRSBH>50012345671180278</NSRSBH>"
                    + "</REQUEST_COMMON_DJXXCX>"
                    + "</business>";
            String xml3 ="<?xml version=\"1.0\" encoding=\"gbk\"?>"
                    + "<business id=\"FPKCCX\" comment=\"发票库存查询\">"
                    + "<REQUEST_COMMON_FPKCCX class=\"REQUEST_COMMON_FPKCCX\">"
                    + "<NSRSBH>50012345671180278</NSRSBH>"
                    + "</REQUEST_COMMON_FPKCCX>"
                    + "</business>";

            String xml ="<?xml version=\"1.0\" encoding=\"gbk\"?>"
                    + "<business id=\"FPKJ\" comment=\"发票开具\">"
                    + "<REQUEST_COMMON_FPKJ class=\"REQUEST_COMMON_FPKJ\">"
                    + "<COMMON_FPKJ_FPT class=\"COMMON_FPKJ_FPT\">"
                    + "<FPQQLSH>1020160804182216675</FPQQLSH>"
                    + "<KPLX>0</KPLX>"
                    + "<XSF_NSRSBH>50012345671180278</XSF_NSRSBH>"
                    + "<XSF_MC>兴业银行3</XSF_MC>"
                    + "<XSF_DZDH>上海市徐汇区桂平路680号31号楼301室34914180</XSF_DZDH>"
                    + "<XSF_YHZH>工行漕河泾开发区支行1001266319200354344</XSF_YHZH>"
                    + "<GMF_NSRSBH>333344442224434421</GMF_NSRSBH>"
                    + "<GMF_MC>乐乐乐</GMF_MC>"
                    + "<GMF_DZDH>ffff</GMF_DZDH>"
                    + "<GMF_YHZH>54335678324565444</GMF_YHZH>"
                    + "<KPR>王杰</KPR>"
                    + "<SKR>王杰</SKR>"
                    + "<FHR>王杰</FHR>"
                    + "<YFP_DM/>"
                    + "<YFP_HM/>"
                    + "<JSHJ>386.10</JSHJ>"
                    + "<HJJE>330.00</HJJE>"
                    + "<HJSE>56.10</HJSE>"
                    + "<BZ/>"
                    + "</COMMON_FPKJ_FPT>"
                    + "<COMMON_FPKJ_XMXXS class=\"COMMON_FPKJ_XMXX\" size=\"1\">"
                    + "<COMMON_FPKJ_XMXX>"
                    + "<FPHXZ>0</FPHXZ>"
                    + "<XMMC>软件开发</XMMC>"
                    + "<GGXH>a</GGXH>"
                    + "<DW>份</DW>"
                    + "<XMSL>1</XMSL>"
                    + "<XMDJ>330</XMDJ>"
                    + "<XMJE>330.00</XMJE>"
                    + "<SL>0.17</SL>"
                    + "<SE>56.10</SE>"
                    + "</COMMON_FPKJ_XMXX>"
                    + "</COMMON_FPKJ_XMXXS>"
                    + "</REQUEST_COMMON_FPKJ>"
                    + "</business>";

            String xml4="<?xml version=\"1.0\" encoding=\"gbk\"?>\n"
                    + "<business id=\"FPKJ\" comment=\"发票开具\">\n"
                    + "<REQUEST_COMMON_FPKJ class=\"REQUEST_COMMON_FPKJ\">\n"
                    + "<COMMON_FPKJ_FPT class=\"COMMON_FPKJ_FPT\">\n"
                    + "<FPQQLSH>16149$3597</FPQQLSH>\n"
                    + "<KPLX>0</KPLX>\n"
                    + "<BMB_BBH>10.0</BMB_BBH>\n"
                    + "<ZSFS>0</ZSFS>\n"
                    + "<XSF_NSRSBH>50012345671180278</XSF_NSRSBH>\n"
                    + "<XSF_MC>兴业银行3</XSF_MC>\n"
                    + "<XSF_DZDH>123、2222222</XSF_DZDH>\n"
                    + "<XSF_YHZH>321</XSF_YHZH>\n"
                    + "<GMF_NSRSBH></GMF_NSRSBH>\n"
                    + "<GMF_MC>张三</GMF_MC>\n"
                    + "<GMF_DZDH></GMF_DZDH>\n"
                    + "<GMF_YHZH>643</GMF_YHZH>\n"
                    + "<KPR>李四</KPR>\n"
                    + "<SKR>李四</SKR>\n"
                    + "<FHR>王五</FHR>\n"
                    + "<YFP_DM></YFP_DM>\n"
                    + "<YFP_HM></YFP_HM>\n"
                    + "<JSHJ>10600</JSHJ>\n"
                    + "<HJJE>10000</HJJE>\n"
                    + "<HJSE>600</HJSE>\n"
                    + "<KCE></KCE>\n"
                    + "<BZ>备注</BZ>\n"
                    + "</COMMON_FPKJ_FPT>\n"
                    + "<COMMON_FPKJ_XMXXS class=\"COMMON_FPKJ_XMXX\" size=\"1\">\n"
                    + "<COMMON_FPKJ_XMXX>\n"
                    + "<FPHXZ>0</FPHXZ>\n"
                    + "<SPBM>1010101070000000000</SPBM>\n"
                    + "<ZXBM></ZXBM>\n"
                    + "<YHZCBS>0</YHZCBS>\n"
                    + "<LSLBS></LSLBS>\n"
                    + "<ZZSTSGL></ZZSTSGL>\n"
                    + "<XMMC>ddd</XMMC>\n"
                    + "<GGXH></GGXH>\n"
                    + "<DW></DW>\n"
                    + "<XMSL></XMSL>\n"
                    + "<XMDJ></XMDJ>\n"
                    + "<XMJE>10000</XMJE>\n"
                    + "<SL>0.06</SL>\n"
                    + "<SE>600</SE>\n"
                    + "</COMMON_FPKJ_XMXX>\n"
                    + "</COMMON_FPKJ_XMXXS>\n"
                    + "</REQUEST_COMMON_FPKJ>\n"
                    + "</business>";


            String xml5= "<?xml version=\"1.0\" encoding=\"gbk\"?>"
                    + "<business id=\"FPCX\" comment=\"发票查询\">"
                    + "<REQUEST_COMMON_FPCX class=\"REQUEST_COMMON_FPCX\">"
                    + "<FPQQLSH>1114669174</FPQQLSH>"
                    + "</REQUEST_COMMON_FPCX>"
                    + "</business>";


            String xml6="<?xml version=\"1.0\" encoding=\"gbk\"?>"
                    + "<business id=\"FPKJ\" comment=\"发票开具\">"
                    + "<REQUEST_COMMON_FPKJ class=\"REQUEST_COMMON_FPKJ\">"
                    + "<COMMON_FPKJ_FPT class=\"COMMON_FPKJ_FPT\">"
                    + "<FPQQLSH>16149$3680</FPQQLSH>"
                    + "<KPLX>1</KPLX>"
                    + "<BMB_BBH>10.0</BMB_BBH>"
                    + "<ZSFS>0</ZSFS>"
                    + "<XSF_NSRSBH>9131000057076239X6</XSF_NSRSBH>"
                    + " <XSF_MC>泉盛二餐饮管理（上海）有限公司</XSF_MC>"
                    + " <XSF_DZDH>上海市奉贤区扶港路1088号5幢311室、021-59895352</XSF_DZDH>"
                    + " <XSF_YHZH>1001246909016281245</XSF_YHZH>"
                    + "<GMF_NSRSBH></GMF_NSRSBH>"
                    + "<GMF_MC>liuyan</GMF_MC>"
                    + " <GMF_DZDH></GMF_DZDH>"
                    + "<GMF_YHZH></GMF_YHZH>"
                    + "<KPR>于梦妮</KPR>"
                    + "<SKR></SKR>"
                    + " <FHR></FHR>"
                    + "<YFP_DM>031001600311</YFP_DM>"
                    + "<YFP_HM>42811426</YFP_HM>"
                    + "<JSHJ>-0.55</JSHJ>"
                    + " <HJJE>-0.52</HJJE>"
                    + " <HJSE>-0.03</HJSE>"
                    + " <KCE></KCE>"
                    + " <BZ></BZ>"
                    + "</COMMON_FPKJ_FPT>"
                    + "<COMMON_FPKJ_XMXXS class=\"COMMON_FPKJ_XMXX\" size=\"1\">"
                    + "<COMMON_FPKJ_XMXX>"
                    + "<FPHXZ>0</FPHXZ>"
                    + " <SPBM>3070401000000000000</SPBM>"
                    + " <ZXBM></ZXBM>"
                    + "  <YHZCBS>0</YHZCBS>"
                    + " <LSLBS></LSLBS>"
                    + " <ZZSTSGL></ZZSTSGL>"
                    + " <XMMC>餐费</XMMC>"
                    + "  <GGXH></GGXH>"
                    + " <DW></DW>"
                    + " <XMSL></XMSL>"
                    + "<XMDJ></XMDJ>"
                    + "  <XMJE>-0.52</XMJE>"
                    + " <SL>0.06</SL>"
                    + " <SE>-0.03</SE>"
                    + " </COMMON_FPKJ_XMXX>"
                    + " </COMMON_FPKJ_XMXXS>"
                    + " </REQUEST_COMMON_FPKJ>"
                    + "</business>";
            //新接的一个项目接口，非要用xml请求，找不到别的post方式，最终选用这种方式，将参数拼成xml字符串

            // File input = new File("test.xml");//如果是xml文件，可以这样写
            HttpPost httpPost = new HttpPost("http://210.14.78.228:7090/SKServer/SKDo");
            CloseableHttpResponse response = null;
            CloseableHttpClient httpClient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().
                    setSocketTimeout(120 * 1000).setConnectionRequestTimeout(120 * 1000).setConnectTimeout(120 * 1000).build();
            httpPost.setConfig(requestConfig);
            httpPost.addHeader("Content-Type", "text/xml");
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = null;
            String strMessage = "";
            try {
                StringEntity requestEntity = new StringEntity(xml5, "GBK");
                httpPost.setEntity(requestEntity);
                response = httpClient.execute(httpPost, new BasicHttpContext());
                if (response.getStatusLine().getStatusCode() != 200) {
                    System.out.println("调用接口出错！");
                }
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    reader = new BufferedReader(new InputStreamReader(entity.getContent(), "gbk"));
                    while ((strMessage = reader.readLine()) != null) {
                        buffer.append(strMessage);
                    }
                }
                System.out.println("接口返回报文："+buffer);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        private static OMElement xml2OMElement(String xml) throws XMLStreamException, UnsupportedEncodingException {
            ByteArrayInputStream xmlStream = new ByteArrayInputStream(xml.getBytes("GBK"));
            StAXBuilder builder = new StAXOMBuilder(xmlStream);
            OMElement documentElement = builder.getDocumentElement();
            return documentElement;
        }
        //final private static String webServiceUrl = "http://116.228.37.198:10002/SKServer/SKDo";
        private static Map xml2Map(OMElement doc, String listTagNames) {
            Map docMap = new HashMap();
            Iterator<OMElement> iter = doc.getChildElements();
            OMElement node;
            String tagName, tagText;
            while (iter.hasNext()) {
                node = iter.next();
                tagName = str2Trim(node.getLocalName());
                if (listTagNames.indexOf(tagName) > -1) {
                    docMap.put(tagName, xml2List(node, listTagNames));
                } else if (node.getChildElements().hasNext()) {
                    docMap.put(tagName, xml2Map(node, listTagNames));
                } else {
                    tagText = str2Trim(node.getText());
                    docMap.put(tagName, tagText);
                }
            }
            return docMap;
        }

        private static String str2Trim(String str) {
            return "".equals(str) ? null : str.trim();
        }

        private static List xml2List(OMElement doc, String listTagNames) {
            List list = new ArrayList();
            Iterator<OMElement> iter = doc.getChildElements();
            OMElement node;
            String tagText;
            while (iter.hasNext()) {
                node = iter.next();
                if (node.getChildElements().hasNext()) {
                    list.add(xml2Map(node, listTagNames));
                } else {
                    tagText = str2Trim(node.getText());
                    list.add(tagText);
                }
            }
            return list;
        }

    }
