package kz.qazaqpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class QazaqPayApplication {
    public static void main(String[] args) {
        SpringApplication.run(QazaqPayApplication.class, args);
    }
}
