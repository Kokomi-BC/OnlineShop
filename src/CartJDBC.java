package src;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartJDBC {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/commodities";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "root";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load MySQL JDBC driver");
        }
    }

    // 获取数据库连接
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    // 添加商品到购物车（带库存验证）
    public static String addToCart(int userId, int commodifyId, int quantity) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // 开启事务

            // 1. 检查库存
            String checkStockSql = "SELECT num FROM commodities WHERE id = ? FOR UPDATE";
            PreparedStatement checkStmt = conn.prepareStatement(checkStockSql);
            checkStmt.setInt(1, commodifyId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                conn.rollback();
                return "商品不存在";
            }

            int stock = rs.getInt("num");
            if (stock < quantity) {
                conn.rollback();
                return "库存不足，当前库存: " + stock;
            }

            // 2. 检查购物车是否已有该商品
            String checkCartSql = "SELECT quantity FROM cart WHERE user_id = ? AND commodify_id = ?";
            PreparedStatement checkCartStmt = conn.prepareStatement(checkCartSql);
            checkCartStmt.setInt(1, userId);
            checkCartStmt.setInt(2, commodifyId);
            ResultSet cartRs = checkCartStmt.executeQuery();

            if (cartRs.next()) {
                // 已有该商品，更新数量
                int existingQty = cartRs.getInt("quantity");
                String updateSql = "UPDATE cart SET quantity = ? WHERE user_id = ? AND commodify_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, existingQty + quantity);
                updateStmt.setInt(2, userId);
                updateStmt.setInt(3, commodifyId);
                updateStmt.executeUpdate();
            } else {
                // 新商品，添加到购物车
                String addCartSql = "INSERT INTO cart (user_id, commodify_id, quantity) VALUES (?, ?, ?)";
                PreparedStatement addStmt = conn.prepareStatement(addCartSql);
                addStmt.setInt(1, userId);
                addStmt.setInt(2, commodifyId);
                addStmt.setInt(3, quantity);
                addStmt.executeUpdate();
            }

            conn.commit();
            return "添加成功";
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return "操作失败: " + e.getMessage();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 获取用户购物车
    public static List<CartItem> getCartByUser(int userId) {
        List<CartItem> cartItems = new ArrayList<>();
        String sql = "SELECT * FROM cart WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                CartItem item = new CartItem(
                        rs.getInt("user_id"),
                        rs.getInt("commodify_id"),
                        rs.getInt("quantity")
                );
                item.setCartId(rs.getInt("cart_id"));
                cartItems.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cartItems;
    }

    // 从购物车移除商品
    public static boolean removeFromCart(int cartId) {
        String sql = "DELETE FROM cart WHERE cart_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cartId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 更新购物车商品数量
    public static String updateCartItemQuantity(int cartId, int newQuantity) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1. 获取商品ID和当前数量
            String getItemSql = "SELECT commodify_id, quantity FROM cart WHERE cart_id = ?";
            PreparedStatement getItemStmt = conn.prepareStatement(getItemSql);
            getItemStmt.setInt(1, cartId);
            ResultSet rs = getItemStmt.executeQuery();

            if (!rs.next()) {
                conn.rollback();
                return "购物车项不存在";
            }

            int commodifyId = rs.getInt("commodify_id");
            int currentQty = rs.getInt("quantity");

            // 2. 检查库存是否足够
            String checkStockSql = "SELECT num FROM commodities WHERE id = ? FOR UPDATE";
            PreparedStatement checkStmt = conn.prepareStatement(checkStockSql);
            checkStmt.setInt(1, commodifyId);
            ResultSet stockRs = checkStmt.executeQuery();

            if (!stockRs.next()) {
                conn.rollback();
                return "商品不存在";
            }

            int stock = stockRs.getInt("num");
            if (stock < newQuantity) {
                conn.rollback();
                return "库存不足，当前库存: " + stock;
            }

            // 3. 更新数量
            String updateSql = "UPDATE cart SET quantity = ? WHERE cart_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, newQuantity);
            updateStmt.setInt(2, cartId);
            updateStmt.executeUpdate();

            conn.commit();
            return "更新成功";
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return "操作失败: " + e.getMessage();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 清空用户购物车
    public static boolean clearCart(int userId) {
        String sql = "DELETE FROM cart WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}