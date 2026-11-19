package mx.oaxinvest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OaxInvestApplication {
    public static void main(String[] args) {
        SpringApplication.run(OaxInvestApplication.class, args);
    }
}
