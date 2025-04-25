package src;
import java.math.BigDecimal;
import java.util.Date;
public class Order {
    private int orderId; // 订单ID
    private int userId; // 客户ID
    private int skuId; // 商品SKUID
    private int quantity; // 数量
    private BigDecimal price; // 下单时单价
    private BigDecimal totalAmount; // 订单总金额
    private String status; // 订单状态
    private String shippingAddress; // 收货地址
    private String paymentMethod; // 支付方式
    private Date createdTime; // 下单时间
    private Date paymentTime; // 支付时间
    private Date shippedTime; // 发货时间
    private Date completedTime; // 完成时间
    private String remark; // 备注

    public Order(int orderId, int userId, int skuId, int quantity, BigDecimal price, BigDecimal totalAmount, String status, String shippingAddress, String paymentMethod, Date createdTime, Date paymentTime, Date shippedTime, Date completedTime, String remark) {
        this.orderId = orderId;
        this.userId = userId;
        this.skuId = skuId;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = totalAmount;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.createdTime = createdTime;
        this.paymentTime = paymentTime;
        this.shippedTime = shippedTime;
        this.completedTime = completedTime;
        this.remark = remark;
    }
    public int getOrderId() {
        return orderId;
    }
    public int getUserId() {
        return userId;
    }
    public int getSkuId() {
        return skuId;
    }
    public int getQuantity() {
        return quantity;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    public String getStatus() {
        return status;
    }
    public String getShippingAddress() {
        return shippingAddress;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }
    public Date getCreatedTime() {
        return createdTime;
    }
    public Date getPaymentTime() {
        return paymentTime;
    }
    public Date getShippedTime() {
        return shippedTime;
    }
    public Date getCompletedTime() {
        return completedTime;
    }
    public String getRemark() {
        return remark;
    }
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public void setSkuId(int skuId) {
        this.skuId = skuId;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
    public void setPaymentTime(Date paymentTime) {
        this.paymentTime = paymentTime;
    }
    public void setShippedTime(Date shippedTime) {
        this.shippedTime = shippedTime;
    }
    public void setCompletedTime(Date completedTime) {
        this.completedTime = completedTime;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
    // toString方法，方便打印对象信息
    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", skuId=" + skuId +
                ", quantity=" + quantity +
                ", price=" + price +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", shippingAddress='" + shippingAddress + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", createdTime=" + createdTime +
                ", paymentTime=" + paymentTime +
                ", shippedTime=" + shippedTime +
                ", completedTime=" + completedTime +
                ", remark='" + remark + '\'' +
                '}';
    }
}
