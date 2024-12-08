package com.app.model;

public class LoanRequest {

    private Long customerId;
    private Double amount;
    private Double interestRate;
    private Integer installments;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getInstallments() {
        return installments;
    }

    public void setInstallments(Integer installments) {
        this.installments = installments;
    }

    public LoanRequest(Long customerId, Double amount, Double interestRate, Integer installments) {
        this.customerId = customerId;
        this.amount = amount;
        this.interestRate = interestRate;
        this.installments = installments;
    }
}
