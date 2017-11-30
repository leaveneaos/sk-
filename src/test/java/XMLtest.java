import com.rjxx.Application;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.utils.ParseInvoiceFileUtils;
import com.rjxx.utils.XmlJaxbUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by xlm on 2017/11/29.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class XMLtest
{
    @Autowired
    private ParseInvoiceFileUtils parseInvoiceFileUtils;
    @Test
    public void test(){
        String content="<?xml version=\"1.0\" encoding=\"gbk\"?><Response id=\"FPKJ\" comment=\"发票开具\"><RESPONSE_COMMON_FPKJ class=\"RESPONSE_COMMON_FPKJ\"><FPQQLSH>1111487634</FPQQLSH><FP_DM>031001600411</FP_DM><FP_HM>33674326</FP_HM><KPRQ>20171129145547</KPRQ><JQBH>661600247646</JQBH><FP_MW>-18399+6>+&lt;*7--166463&gt;369&lt;*&gt;158550&gt;0&gt;&gt;*317944*42918+387041&lt;938//6-14&lt;16-77664&lt;79+7920+3+8550&gt;0&gt;&gt;*317944*2-45</FP_MW><JYM>44651022642238997958</JYM><EWM/><BZ></BZ><RETURNCODE>0000</RETURNCODE><RETURNMSG>4011-开票成功 [0000,]</RETURNMSG></RESPONSE_COMMON_FPKJ></Response>";
        try {
            InvoiceResponse response = XmlJaxbUtils.convertXmlStrToObject(InvoiceResponse.class, content);
            parseInvoiceFileUtils.updateInvoiceResult(response);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
