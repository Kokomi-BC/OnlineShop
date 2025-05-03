package src;

public class Cart {
    private int cartId; // 购物车ID
    private int userId; // 客户ID
    private int skuId; // 商品SKUID
    private String name; // 商品名称
    private String type; // 商品类别
    private String detail; // 商品详情
    private String color; // 颜色
    private String style; // 款式
    private int num; // 数量

    public Cart(int cartId, int userId, int skuId, String name, String type, String detail, String color, String style, int num) {
        this.cartId = cartId;
        this.userId = userId;
        this.skuId = skuId;
        this.name = name;
        this.type = type;
        this.detail = detail;
        this.color = color;
        this.style = style;
        this.num = num;
    }

    public int getCartId() {
        return cartId;
    }
    public int getUserId() {
        return userId;
    }
    public int getSkuId() {
        return skuId;
    }
    public int getNum() {
        return num;
    }
    public void setCartId(int cartId) {
        this.cartId = cartId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public void setSkuId(int skuId) {
        this.skuId = skuId;
    }
    public void setNum(int num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "cartId=" + cartId +
                ", userId=" + userId +
                ", skuId=" + skuId +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", detail='" + detail + '\'' +
                ", color='" + color + '\'' +
                ", style='" + style + '\'' +
                ", num=" + num +
                '}';
    }
}