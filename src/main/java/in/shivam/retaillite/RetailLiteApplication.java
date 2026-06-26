package in.shivam.retaillite;

import in.shivam.retaillite.payment.PaymentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableConfigurationProperties(PaymentProperties.class)
public class RetailLiteApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext ctx=SpringApplication.run(RetailLiteApplication.class, args);

    }

}
