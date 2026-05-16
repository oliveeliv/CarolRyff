package org.upiiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RyffApplication {
    public static void main(String[] args) {
        System.setProperty("spring.datasource.url", "jdbc:mysql://mysql-bc5f4e1-frodriguezo2401-cc7a.l.aivencloud.com:16664/defaultdb?ssl-mode=REQUIRED");
        System.setProperty("spring.datasource.username", "avnadmin");
        System.setProperty("spring.datasource.password", "AVNS_2B-JxmKGL1gyhNKG0CD");
        System.setProperty("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");

        System.setProperty("spring.jpa.hibernate.ddl-auto", "update");
        System.setProperty("spring.jpa.show-sql", "true");

        SpringApplication.run(RyffApplication.class, args);
    }
}