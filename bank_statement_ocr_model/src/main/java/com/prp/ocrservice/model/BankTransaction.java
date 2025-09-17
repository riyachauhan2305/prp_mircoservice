// package com.prp.ocrservice.model;

// public class BankTransaction {
//     private String date;
//     private String description;
//     private Double amount;
//     private Double balance;

//     public String getDate() { return date; }
//     public void setDate(String date) { this.date = date; }

//     public String getDescription() { return description; }
//     public void setDescription(String description) { this.description = description; }

//     public Double getAmount() { return amount; }
//     public void setAmount(Double amount) { this.amount = amount; }

//     public Double getBalance() { return balance; }
//     public void setBalance(Double balance) { this.balance = balance; }
// }

package com.prp.ocrservice.model;

public class BankTransaction {
    private String date;
    private String description;
    private double amount;
    private double balance;

    public BankTransaction() {}

    public BankTransaction(String date, String description, double amount, double balance) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.balance = balance;
    }

    // Getters and Setters

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
