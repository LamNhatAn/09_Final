package com.utetra.dto;

import java.math.BigDecimal;

public record AdminDashboardStats(
        long customers,
        long activeStaff,
        long activeBranches,
        long availableProducts,
        long pendingOrders,
        long processingOrders,
        long completedToday,
        BigDecimal revenueToday,
        BigDecimal totalRevenue,
        long feedbacks) {

    public long getCustomers() { return customers; }
    public long getActiveStaff() { return activeStaff; }
    public long getActiveBranches() { return activeBranches; }
    public long getAvailableProducts() { return availableProducts; }
    public long getPendingOrders() { return pendingOrders; }
    public long getProcessingOrders() { return processingOrders; }
    public long getCompletedToday() { return completedToday; }
    public BigDecimal getRevenueToday() { return revenueToday; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public long getFeedbacks() { return feedbacks; }
}
