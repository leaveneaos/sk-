import com.rjxx.Application;
import com.rjxx.taxeasy.bizcomm.utils.MailService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by xlm on 2017/11/28.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class SendMailTest {
    @Autowired
    private MailService mailService;

    @Test
    public void send(){
        String [] to=new String[1];
        to[0]="1084854604@qq.com";
         mailService.sendSimpleMail(to,"电子发票","测试");
    }
}
