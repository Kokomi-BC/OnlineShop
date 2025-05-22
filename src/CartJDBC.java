package src;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import static src.CommodityJDBC.getConnection;
public class CartJDBC {
    public static void increaseQuantity(int userId, int skuId) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            String selectSql = "SELECT c.cartid, c.num, s.stock " +
                    "FROM cart c " +
                    "JOIN skus s ON c.skuid = s.skuid " +
                    "WHERE c.userid = ? AND c.skuid = ? " +
                    "FOR UPDATE";
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, skuId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("购物车项不存在（用户ID: " + userId + ", SKU ID: " + skuId + "）");
                }
                int cartId = rs.getInt("cartid");
                int currentNum = rs.getInt("num");
                int stock = rs.getInt("stock");
                if (stock < currentNum + 1) {
                    conn.rollback();
                    throw new SQLException("库存不足（当前库存: " + stock + "，需求数量: " + (currentNum + 1) + "）");
                }
                try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE cart SET num = num + 1 WHERE cartid = ?")) {
                    updateStmt.setInt(1, cartId);
                    updateStmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
    public static class ConfirmDeleteException extends Exception {
        public ConfirmDeleteException(String message) {
            super(message);
        }
    }
    public static void decreaseQuantity(int userId, int skuId) throws Exception {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            String selectSql = "SELECT cartid, num FROM cart WHERE userid = ? AND skuid = ? FOR UPDATE";
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, skuId);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    throw new SQLException("购物车项不存在");
                }
                int currentNum = rs.getInt("num");
                if (currentNum == 1) {
                    throw new ConfirmDeleteException("需确认删除");
                } else {
                    String updateSql = "UPDATE cart SET num = num - 1 WHERE cartid = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, rs.getInt("cartid"));
                        updateStmt.executeUpdate();
                    }
                    conn.commit(); 
                }
            } catch (SQLException | ConfirmDeleteException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static void addItem(int userId, int skuId) throws SQLException {
        String querySkuSql = "SELECT commodityid, color, style, price, stock FROM skus WHERE skuid = ?";
        String queryCartSql = "SELECT num FROM cart WHERE userid = ? AND skuid = ?";
        String queryCommoditySql = "SELECT name, type, detail FROM commodities WHERE id = ?";
        String insertSql = "INSERT INTO cart (userid, skuid, name, color, style, price, type, detail, num) " +
                "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE num = IF(num < ?, num + 1, num)"; // 添加库存检查
        try (Connection conn = getConnection();
             PreparedStatement querySkuStmt = conn.prepareStatement(querySkuSql);
             PreparedStatement queryCartStmt = conn.prepareStatement(queryCartSql);
             PreparedStatement queryCommodityStmt = conn.prepareStatement(queryCommoditySql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            // 1. 查询SKU信息
            querySkuStmt.setInt(1, skuId);
            ResultSet skuRs = querySkuStmt.executeQuery();
            if (!skuRs.next()) {
                throw new SQLException("未找到对应的SKU ID: " + skuId);
            }

            int commodityid = skuRs.getInt("commodityid");
            String color = skuRs.getString("color");
            String style = skuRs.getString("style");
            BigDecimal price = skuRs.getBigDecimal("price");
            int stock = skuRs.getInt("stock");

            // 2. 查询当前购物车中的数量
            queryCartStmt.setInt(1, userId);
            queryCartStmt.setInt(2, skuId);
            ResultSet cartRs = queryCartStmt.executeQuery();
            int currentCartQuantity = cartRs.next() ? cartRs.getInt("num") : 0;

            // 3. 检查库存是否足够
            if (currentCartQuantity >= stock) {
                throw new SQLException("库存不足，当前库存: " + stock);
            }

            // 4. 查询商品信息
            queryCommodityStmt.setInt(1, commodityid);
            ResultSet commodityRs = queryCommodityStmt.executeQuery();
            if (!commodityRs.next()) {
                throw new SQLException("未找到对应的商品 ID: " + commodityid);
            }
            String name = commodityRs.getString("name");
            String type = commodityRs.getString("type");
            String detail = commodityRs.getString("detail");
            // 5. 执行插入/更新操作（带库存检查）
            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, skuId);
            insertStmt.setString(3, name);
            insertStmt.setString(4, color);
            insertStmt.setString(5, style);
            insertStmt.setBigDecimal(6, price);
            insertStmt.setString(7, type);
            insertStmt.setString(8, detail);
            insertStmt.setInt(9, stock); // 用于ON DUPLICATE KEY UPDATE的库存检查
            int affectedRows = insertStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("添加商品到购物车失败");
            }
        } catch (SQLException e) {
            throw new SQLException("添加商品到购物车时出错: " + e.getMessage(), e);
        }
    }
    // 删除购物车商品
    public static void removeItem(int cartId) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM cart WHERE cartid = ?")) {
            stmt.setInt(1, cartId);
            stmt.executeUpdate();
        }
    }

    // 计算总金额
    public BigDecimal calculateTotalAmount(int userId) throws SQLException {
        BigDecimal total = BigDecimal.ZERO;
        String sql = "SELECT SUM(c.num * s.price) AS total FROM cart c JOIN skus s ON c.skuid = s.skuid WHERE userid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                total = rs.getBigDecimal("total");
            }
        }
        return total != null ? total : BigDecimal.ZERO;
    }

    // 结算购物车
    public static void checkout(int userId) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. 获取用户信息（带锁）
                String address;
                BigDecimal balance;
                try (PreparedStatement userStmt = conn.prepareStatement(
                        "SELECT balance, address FROM users WHERE id = ? FOR UPDATE")) {
                    userStmt.setInt(1, userId);
                    ResultSet rs = userStmt.executeQuery();
                    if (!rs.next()) throw new SQLException("用户不存在");
                    balance = rs.getBigDecimal("balance");
                    address = rs.getString("address"); // 直接使用局部变量
                }
                List<CartItem> items = new ArrayList<>();
                BigDecimal totalAmount = BigDecimal.ZERO;
                String cartSql = "SELECT c.skuid, c.num, s.price, s.stock "
                        + "FROM cart c JOIN skus s ON c.skuid = s.skuid "
                        + "WHERE userid = ? FOR UPDATE";
                try (PreparedStatement cartStmt = conn.prepareStatement(cartSql)) {
                    cartStmt.setInt(1, userId);
                    ResultSet rs = cartStmt.executeQuery();
                    while (rs.next()) {
                        int skuId = rs.getInt("skuid");
                        int quantity = rs.getInt("num");
                        BigDecimal price = rs.getBigDecimal("price");
                        int stock = rs.getInt("stock");
                        if (quantity > stock) {
                            throw new SQLException("商品 " + skuId + " 库存不足");
                        }
                        items.add(new CartItem(skuId, quantity, price));
                        totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(quantity)));
                    }
                }
                if (items.isEmpty()) {
                    throw new SQLException("购物车为空，请添加商品");
                }
                // 3. 验证余额
                if (balance.compareTo(totalAmount) < 0) {
                    throw new SQLException("余额不足，当前余额：" + balance + "，需支付：" + totalAmount);
                }
                // 4. 扣减余额
                try (PreparedStatement updateUser = conn.prepareStatement(
                        "UPDATE users SET balance = balance - ? WHERE id = ?")) {
                    updateUser.setBigDecimal(1, totalAmount);
                    updateUser.setInt(2, userId);
                    updateUser.executeUpdate();
                }
                // 5. 创建主订单
                int orderId;
                try (PreparedStatement orderStmt = conn.prepareStatement(
                        "INSERT INTO orders (userid, total_amount, status, shipping_address,payment_method) "
                                + "VALUES (?, ?, '已支付', ?,'默认方式')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    orderStmt.setInt(1, userId);
                    orderStmt.setBigDecimal(2, totalAmount);
                    orderStmt.setString(3, address);
                    orderStmt.executeUpdate();
                    try (ResultSet rs = orderStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            orderId = rs.getInt(1);
                        } else {
                            throw new SQLException("创建订单失败");
                        }
                    }
                }
                // 6. 插入订单明细并扣减库存
                String detailSql = "INSERT INTO order_details (orderid, skuid, quantity, price) VALUES (?, ?, ?, ?)";
                String updateSkuSql = "UPDATE skus SET stock = stock - ? WHERE skuid = ?";
                try (PreparedStatement detailStmt = conn.prepareStatement(detailSql);
                     PreparedStatement updateSkuStmt = conn.prepareStatement(updateSkuSql)) {
                    for (CartItem item : items) {
                        // 插入明细
                        detailStmt.setInt(1, orderId);
                        detailStmt.setInt(2, item.skuId);
                        detailStmt.setInt(3, item.quantity);
                        detailStmt.setBigDecimal(4, item.price);
                        detailStmt.addBatch();
                        // 扣减库存
                        updateSkuStmt.setInt(1, item.quantity);
                        updateSkuStmt.setInt(2, item.skuId);
                        updateSkuStmt.addBatch();
                    }
                    // 批量执行
                    detailStmt.executeBatch();
                    updateSkuStmt.executeBatch();
                }
                // 7. 清空购物车
                try (PreparedStatement clearCart = conn.prepareStatement(
                        "DELETE FROM cart WHERE userid = ?")) {
                    clearCart.setInt(1, userId);
                    clearCart.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
    public static void deleteCartItem(int userId, int skuId) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            String deleteSql = "DELETE FROM cart WHERE userid = ? AND skuid = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, userId);
                deleteStmt.setInt(2, skuId);
                deleteStmt.executeUpdate();
            }
            conn.commit();
        }
    }
    public static List<CartItemDetail> getCartDetailsByUserId(int userId) throws SQLException {
        // 检查用户是否存在
        String userCheckSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(userCheckSql)) {
            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                throw new SQLException("用户ID " + userId + " 不存在");
            }
        }

        //查询购物车详情
        List<CartItemDetail> details = new ArrayList<>();
        String sql = "SELECT c.cartid, c.userid, c.skuid, c.num, "
                + "s.price, com.name AS product_name,s.stock, s.style,s.color, com.manufacturer "
                + "FROM cart c "
                + "JOIN skus s ON c.skuid = s.skuid "
                + "JOIN commodities com ON s.commodityid = com.id "
                + "WHERE c.userid = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CartItemDetail item = new CartItemDetail(
                        rs.getInt("cartid"),
                        rs.getInt("userid"),
                        rs.getInt("skuid"),
                        rs.getInt("num"),
                        rs.getInt("stock"),
                        rs.getBigDecimal("price"),
                        rs.getString("product_name"),
                        rs.getString("style"),
                        rs.getString("color"),
                        rs.getString("manufacturer")
                );
                details.add(item);
            }
        }
        return details;
    }

    // 临时存储购物车项信息
    private static class CartItem {
        int skuId;
        int quantity;
        BigDecimal price;
        CartItem(int skuId, int quantity, BigDecimal price) {
            this.skuId = skuId;
            this.quantity = quantity;
            this.price = price;
        }
    }
}