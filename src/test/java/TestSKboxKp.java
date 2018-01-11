import com.rjxx.Application;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.service.InvoiceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by xlm on 2018/1/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class TestSKboxKp {

    @Autowired
    private InvoiceService invoiceService;

    @Test
    public void test(){
        try {
            InvoiceResponse invoiceResponse= invoiceService.skBoxKP(2220);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
