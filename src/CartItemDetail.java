package src;
import java.math.BigDecimal;
public class CartItemDetail {
    private int cartId;
    private int userId;
    private int skuId;
    private int quantity;
    private int stock;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String productName;
    private String style;
    private String color;
    private String manufacturer;
    private CommoditySKU cachedSku;
    private Commodity cachedCommodity;

    public CommoditySKU getCachedSku() {
        return cachedSku;
    }

    public void setCachedSku(CommoditySKU cachedSku) {
        this.cachedSku = cachedSku;
    }

    public Commodity getCachedCommodity() {
        return cachedCommodity;
    }

    public void setCachedCommodity(Commodity cachedCommodity) {
        this.cachedCommodity = cachedCommodity;
    }

    public CartItemDetail(int cartId, int userId, int skuId, int quantity,int stock,
                          BigDecimal price, String productName,
                          String style, String color, String manufacturer) {
        this.cartId = cartId;
        this.userId = userId;
        this.skuId = skuId;
        this.quantity = quantity;
        this.price = price;
        this.stock=stock;
        this.totalPrice = price.multiply(BigDecimal.valueOf(quantity));
        this.productName = productName;
        this.style = style;
        this.color =color;
        this.manufacturer = manufacturer;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getStyle() {
        return style;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getSkuId() {
        return skuId;
    }

    public int getUserId() {
        return userId;
    }

    public int getCartId() {
        return cartId;
    }

    public int getStock() {
        return 0;
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal gettotalPrice() {
        return totalPrice;
    }

    public void settotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
