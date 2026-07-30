package in.shivam.retaillite.invoice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "retaillite.company")
public record CompanyProperties(

        String name,

        Address address,

        String phone,

        String email,

        String website,

        String gstNumber,

        Resource logo

) {

    public record Address(
            String line1,
            String line2
    ) {
    }
}