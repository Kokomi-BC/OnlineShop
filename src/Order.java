package src;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
// 订单主表实体
public class Order {
    private Integer orderId;
    private Integer userId;
    private BigDecimal totalAmount;
    private String status;
    private String shippingAddress;
    private String paymentMethod;
    private LocalDateTime createdTime;
    private LocalDateTime paymentTime;
    private LocalDateTime shippedTime;
    private LocalDateTime completedTime;
    private String remark;
    private List<OrderDetail> details = new ArrayList<>();
    // Getters
    public Integer getOrderId() { return orderId; }
    public Integer getUserId() { return userId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public LocalDateTime getShippedTime() { return shippedTime; }
    public LocalDateTime getCompletedTime() { return completedTime; }
    public String getRemark() { return remark; }
    public List<OrderDetail> getDetails() { return details; }
    // Setters
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(String status) { this.status = status; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
    public void setShippedTime(LocalDateTime shippedTime) { this.shippedTime = shippedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
    public void setRemark(String remark) { this.remark = remark; }
    public void setDetails(List<OrderDetail> details) { this.details = details; }

}

