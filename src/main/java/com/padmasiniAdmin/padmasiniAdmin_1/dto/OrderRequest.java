package com.padmasiniAdmin.padmasiniAdmin_1.dto;

public class OrderRequest {
    private long amount; // Price in Rupees (e.g., 1000)
    private String plan;

    // Getters and Setters
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
}
