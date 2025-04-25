package src;


import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class example2 {
public static void main(String[] args) throws SQLException {
 String news =UserJDBC.addUser(new User(1,"kokomi","12345678","12345678","湖南省株洲市天元区", BigDecimal.valueOf(19999.9)));
       System.out.println(news);
       try {
            CartJDBC.addItem(1, 1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        CartJDBC cartJDBC = new CartJDBC();
        List<CartItemDetail> items ;
        try {
            items = cartJDBC.getCartDetailsByUserId(1);
            if (items == null || items.isEmpty()) {
                System.out.println("购物车为空");
            } else {
                items.forEach(item -> {
                    System.out.println("商品名称: " + item.getProductName());
                    System.out.println("款式: " + item.getStyle());
                    System.out.println("单价: ￥" + item.getPrice());
                    System.out.println("数量: " + item.getQuantity());
                    System.out.println("库存: " + item.getStock());
                    System.out.println("总价: ￥" + item.getTotalPrice());
                    System.out.println("生产商: " + item.getManufacturer());
                    System.out.println("-----------------------");
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询购物车失败: " + e.getMessage(), e);
        }
    CartJDBC.decreaseQuantity( 1,1);


    }
}