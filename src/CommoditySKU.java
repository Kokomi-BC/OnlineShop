package src;

import java.math.BigDecimal;

public class CommoditySKU {
    private Integer skuId; 
    private Integer Commodityid;
    private String color;
    private String style;
    private Double price;
    private Integer stock;

    public CommoditySKU() {
        this.skuId = -1;
        this.Commodityid = null; // 初始化为null，表示未关联
    }
    public CommoditySKU(String color, String style, int num, double price) {
        this();
        this.style = style;
        this.color = color;
        this.stock = num;
        this.price = price;
    }
    public CommoditySKU(int num,double price) {
        this();
        this.stock=num;
        this.price=price;
    }
    public CommoditySKU(Integer commodityid) {
        if (commodityid == null) {
            throw new IllegalArgumentException("Commodityid不能为null");
        }
        this.Commodityid = commodityid;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public void setStyle(String style) {
        this.style = style;
    }
    public int getSkuId() {
        return skuId;
    }
    public void setSkuId(int skuId) {
        this.skuId = skuId;
    }
    public Integer getCommodityid() {return Commodityid;}
    public void setCommodityid(Integer commodityid) {
        if (commodityid == null) {
            throw new IllegalArgumentException("commodityid不能为null");
        }
        if (this.Commodityid != null && !this.Commodityid.equals(commodityid)) {
            throw new IllegalArgumentException("SKU的商品ID不能修改");
        }
        this.Commodityid = commodityid;
    }
    public BigDecimal getPrice() {
        return BigDecimal.valueOf(price);
    }
    public void setPrice(Double price) {
        this.price = price;
    }
    public Integer getStock() {
        return stock;
    }
    public void setStock(int stock) {
        this.stock = stock;
    }
    public String getStyle() {
        return style;
    }
    public String getColor() {
        return color;
    }

    public void setPrice(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            throw new IllegalArgumentException("价格不能为null");
        }
        this.price = bigDecimal.doubleValue();
    }
}
