package src;

import java.util.List;

public class example2 {
    public static void main(String[] args) {
        // 注册用户
        UserJDBC.addUser(new User(0, "Aice", "123456", 1000.00));

        // 用户登录
        User user = UserJDBC.getUserByUsername("Aice");
        if (user == null) {
            System.out.println("用户不存在");
            return;
        }

        // 添加商品到购物车（商品ID=1，数量=2）
        String result = CartJDBC.addToCart(user.getId(), 1, 2);
        System.out.println(result); // 输出添加结果

        // 查看购物车
        List<CartItem> cart = CartJDBC.getCartByUser(user.getId());
        cart.forEach(item -> {
            System.out.println("购物车项: " + item);
        });

        // 更新余额
        UserJDBC.updateBalance(user.getId(), 800.00);
    }
}