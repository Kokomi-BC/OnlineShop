package src;
import java.math.BigDecimal;
public class OrderDetail {
    private Integer detailId;
    private Integer orderId;
    private Integer skuId;
    private Integer quantity;
    private BigDecimal price;
    // Getters
    public Integer getDetailId() { return detailId; }
    public Integer getOrderId() { return orderId; }
    public Integer getSkuId() { return skuId; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    // Setters
    public void setDetailId(Integer detailId) { this.detailId = detailId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public void setSkuId(Integer skuId) { this.skuId = skuId; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
