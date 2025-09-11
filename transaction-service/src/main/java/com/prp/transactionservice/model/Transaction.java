package com.prp.transactionservice.model;

import com.arangodb.springframework.annotation.Document;
import org.springframework.data.annotation.Id;
import lombok.*;

import java.util.Date;
import java.util.List;

@Document("transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    private String id;

    private String userId;
    private String type;
    private Double amount;
    private String account;
    @Builder.Default
    private String category = "Uncategorized";
    @Builder.Default
    private Date date = new Date();
    private String notes;
    private List<String> tags;
    @Builder.Default
    private String source = "Manual";
    private String rawSms;
}
