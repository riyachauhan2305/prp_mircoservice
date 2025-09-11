// package com.prp.transactionservice;


// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication
// public class TransactionServiceApplication {

//     public static void main(String[] args) {
//         SpringApplication.run(TransactionServiceApplication.class, args);
//     }
// }


package com.prp.transactionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.data.arangodb.repository.config.EnableArangoRepositories;
import com.arangodb.springframework.annotation.EnableArangoRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.prp.transactionservice",
        "com.prp.authservice",
        "com.prp.commonconfig"
})
@EnableArangoRepositories(basePackages = "com.prp.transactionservice.repository")
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }
}
