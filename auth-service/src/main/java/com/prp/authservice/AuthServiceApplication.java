// package com.prp.authservice;

// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;

// import com.arangodb.springframework.annotation.EnableArangoRepositories;

// @SpringBootApplication
// @EnableArangoRepositories(basePackages = "com.prp.authservice.repository")


// public class AuthServiceApplication {
//     public static void main(String[] args) {
//         SpringApplication.run(AuthServiceApplication.class, args);
//     }
// }


// package com.prp.authservice;

// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import com.arangodb.springframework.annotation.EnableArangoRepositories;

// @SpringBootApplication(scanBasePackages = {"com.prp.authservice", "com.prp.commonconfig"})
// @EnableArangoRepositories(basePackages = "com.prp.authservice.repository")
// public class AuthServiceApplication {
//     public static void main(String[] args) {
//         SpringApplication.run(AuthServiceApplication.class, args);
//     }
// }


package com.prp.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.arangodb.springframework.annotation.EnableArangoRepositories;

@SpringBootApplication(scanBasePackages = {"com.prp.authservice", "com.prp.commonconfig"})
@EnableArangoRepositories(basePackages = "com.prp.authservice.repository")
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
