// package com.prp.ocrservice.service;

// import com.prp.ocrservice.model.BankTransaction;
// import org.springframework.stereotype.Service;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;

// @Service
// public class BankStatementParser {

//     public List<BankTransaction> parse(String ocrText) {
//         List<BankTransaction> transactions = new ArrayList<>();

//         Pattern pattern = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})\\s+(.*?)\\s+([\\d,]+\\.\\d{2})\\s+([\\d,]+\\.\\d{2})");
//         Matcher matcher = pattern.matcher(ocrText);

//         while (matcher.find()) {
//             BankTransaction txn = new BankTransaction();
//             txn.setDate(matcher.group(1));
//             txn.setDescription(matcher.group(2));
//             txn.setAmount(Double.parseDouble(matcher.group(3).replace(",", "")));
//             txn.setBalance(Double.parseDouble(matcher.group(4).replace(",", "")));
//             transactions.add(txn);
//         }

//         return transactions;
//     }
// }


package com.prp.ocrservice.service;

import com.prp.ocrservice.model.BankTransaction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BankStatementParser {

    public List<BankTransaction> parse(String ocrText) {
        List<BankTransaction> transactions = new ArrayList<>();

        String[] lines = ocrText.split("\n");

        for (String line : lines) {
            try {
                // Example regex parsing logic (adjust as needed)
                String[] parts = line.split("\\s{2,}");
                if (parts.length >= 4) {
                    String date = parts[0].trim();
                    String description = parts[1].trim();
                    double amount = Double.parseDouble(parts[2].trim());
                    double balance = Double.parseDouble(parts[3].trim());

                    BankTransaction transaction = new BankTransaction(date, description, amount, balance);
                    transactions.add(transaction);
                }
            } catch (Exception ignored) {}
        }

        return transactions;
    }
}
