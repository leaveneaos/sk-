import com.rjxx.Application;
import com.rjxx.taxeasy.bizcomm.utils.GeneratePdfService;
import com.rjxx.taxeasy.bizcomm.utils.pdf.PdfDocumentGenerator;
import com.rjxx.taxeasy.domains.Jyls;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.KplsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class GeneratePdfTest {

    @Autowired
    private PdfDocumentGenerator pdfDocumentGenerator;

    @Autowired
    private KplsService kplsService;

    @Test
    public void testGeneratePdf() throws Exception {
        int kplsh = 83214;
        Kpls kpls = kplsService.findOne(kplsh);
        Map map = new HashMap();
        int xfid = kpls.getXfid();
        Jyls jyls = new Jyls();
        jyls.setDdh(kpls.getJylsh());
        String sfmc = "上海";
        map.put("SFMC",sfmc);
        map.put("FP_DM", kpls.getFpdm());
        map.put("FP_HM", kpls.getFphm());
        map.put("KPRQ", kpls.getKprq());
        map.put("JQBH", kpls.getSksbm());
        map.put("JYM", kpls.getJym());
        map.put("FP_MW", kpls.getMwq());
        map.put("EWM", kpls.getFpEwm());
        map.put("pdfUrl",kpls.getPdfurl());
        pdfDocumentGenerator.GeneratPDF(map, jyls, kpls);

    }


}
