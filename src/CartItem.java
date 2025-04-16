package src;

public class CartItem {
    private int cartId;
    private int userId;
    private int commodifyId;
    private int quantity;

    public CartItem() {}

    public CartItem(int userId, int commodifyId, int quantity) {
        this.userId = userId;
        this.commodifyId = commodifyId;
        this.quantity = quantity;
    }

    // Getter & Setter 方法
    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCommodifyId() {
        return commodifyId;
    }

    public void setCommodifyId(int commodifyId) {
        this.commodifyId = commodifyId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // toString() 方法
    @Override
    public String toString() {
        return "CartItem{" +
                "cartId=" + cartId +
                ", userId=" + userId +
                ", commodifyId=" + commodifyId +
                ", quantity=" + quantity +
                '}';
    }
}