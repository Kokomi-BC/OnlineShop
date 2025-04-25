package src;

public class Cart {
    private int cartId; // 购物车ID
    private int userId; // 客户ID
    private int skuId; // 商品SKUID
    private int num; // 数量

    public Cart(int cartId, int userId, int skuId, int num) {
        this.cartId = cartId;
        this.userId = userId;
        this.skuId = skuId;
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
    @Override
    public String toString() {
        return "Cart{" +
                "cartId=" + cartId +
                ", userId=" + userId +
                ", skuId=" + skuId +
                ", num=" + num +
                '}';
    }
}