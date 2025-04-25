package src;
import java.math.BigDecimal;
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
            throw new RuntimeException("驱动加载失败", e);
        }
    }

    private final ThreadLocal<String> address = new ThreadLocal<>();

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }
    public void increaseQuantity(int userId, int skuId) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // 1. 锁定购物车项和SKU
            String selectSql = "SELECT c.cartid, c.num, s.stock " +
                    "FROM cart c " +
                    "JOIN skus s ON c.skuid = s.skuid " +
                    "WHERE c.userid = ? AND c.skuid = ? " +
                    "FOR UPDATE";

            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, skuId);
                ResultSet rs = stmt.executeQuery();

                // 2. 检查是否存在购物车项
                if (!rs.next()) {
                    throw new SQLException("购物车项不存在（用户ID: " + userId + ", SKU ID: " + skuId + "）");
                }

                // 3. 提取关键数据
                int cartId = rs.getInt("cartid");
                int currentNum = rs.getInt("num");
                int stock = rs.getInt("stock");

                // 4. 检查库存
                if (stock < currentNum + 1) {
                    conn.rollback();
                    throw new SQLException("库存不足（当前库存: " + stock + "，需求数量: " + (currentNum + 1) + "）");
                }

                // 5. 更新数量
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

    public static void decreaseQuantity(int userId, int skuId) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // 1. 锁定购物车项
            String selectSql = "SELECT cartid, num FROM cart " +
                    "WHERE userid = ? AND skuid = ? " +
                    "FOR UPDATE";

            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, skuId);
                ResultSet rs = stmt.executeQuery();

                // 2. 检查是否存在购物车项
                if (!rs.next()) {
                    throw new SQLException("购物车项不存在（用户ID: " + userId + ", SKU ID: " + skuId + "）");
                }

                // 3. 提取关键数据
                int cartId = rs.getInt("cartid");
                int currentNum = rs.getInt("num");

                // 4. 更新逻辑
                if (currentNum == 1) {
                    // 删除操作
                    try (PreparedStatement deleteStmt = conn.prepareStatement(
                            "DELETE FROM cart WHERE cartid = ?")) {
                        deleteStmt.setInt(1, cartId);
                        deleteStmt.executeUpdate();
                    }
                } else {
                    // 减少数量
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE cart SET num = num - 1 WHERE cartid = ?")) {
                        updateStmt.setInt(1, cartId);
                        updateStmt.executeUpdate();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // 添加商品到购物车
    public static void addItem(int userId, int skuId) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement skuStmt = conn.prepareStatement("SELECT stock FROM skus WHERE skuid = ? FOR UPDATE")) {
                skuStmt.setInt(1, skuId);
                ResultSet rs = skuStmt.executeQuery();
                if (!rs.next()) throw new SQLException("SKU不存在");
                int stock = rs.getInt("stock");
                if (stock < 1) throw new SQLException("库存不足");

                // 检查是否已存在购物车项
                try (PreparedStatement cartStmt = conn.prepareStatement("SELECT cartid, num FROM cart WHERE userid = ? AND skuid = ? FOR UPDATE")) {
                    cartStmt.setInt(1, userId);
                    cartStmt.setInt(2, skuId);
                    ResultSet cartRs = cartStmt.executeQuery();
                    if (cartRs.next()) {
                        int cartId = cartRs.getInt("cartid");
                        int currentNum = cartRs.getInt("num");
                        if (stock < currentNum + 1) {
                            conn.rollback();
                            throw new SQLException("库存不足，无法添加更多");
                        }
                        // 增加数量
                        try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE cart SET num = num + 1 WHERE cartid = ?")) {
                            updateStmt.setInt(1, cartId);
                            updateStmt.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO cart (userid, skuid, num) VALUES (?, ?, 1)")) {
                            insertStmt.setInt(1, userId);
                            insertStmt.setInt(2, skuId);
                            insertStmt.executeUpdate();
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // 删除购物车商品
    public void removeItem(int cartId) throws SQLException {
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
    public void checkout(int userId) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal balance;
                try (PreparedStatement userStmt = conn.prepareStatement("SELECT balance, address FROM users WHERE id = ? FOR UPDATE")) {
                    userStmt.setInt(1, userId);
                    ResultSet rs = userStmt.executeQuery();
                    if (!rs.next()) throw new SQLException("用户不存在");
                    balance = rs.getBigDecimal("balance");
                    address.set(rs.getString("address"));
                }
                // 2. 获取购物车项及总金额
                List<CartItem> items = new ArrayList<>();
                BigDecimal totalAmount = BigDecimal.ZERO;
                String cartSql = "SELECT c.cartid, c.skuid, c.num, s.price, s.stock FROM cart c JOIN skus s ON c.skuid = s.skuid WHERE userid = ? FOR UPDATE";
                try (PreparedStatement cartStmt = conn.prepareStatement(cartSql)) {
                    cartStmt.setInt(1, userId);
                    ResultSet rs = cartStmt.executeQuery();
                    while (rs.next()) {
                        int skuId = rs.getInt("skuid");
                        int num = rs.getInt("num");
                        BigDecimal price = rs.getBigDecimal("price");
                        int stock = rs.getInt("stock");
                        if (num > stock) {
                            throw new SQLException("商品 " + skuId + " 库存不足");
                        }
                        items.add(new CartItem(skuId, num, price));
                        totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(num)));
                    }
                }
                if (balance.compareTo(totalAmount) < 0) {
                    throw new SQLException("余额不足");
                }
                // 4. 扣减余额
                try (PreparedStatement updateUser = conn.prepareStatement("UPDATE users SET balance = balance - ? WHERE id = ?")) {
                    updateUser.setBigDecimal(1, totalAmount);
                    updateUser.setInt(2, userId);
                    updateUser.executeUpdate();
                }
                // 5. 扣减库存并生成订单
                for (CartItem item : items) {
                    try (PreparedStatement updateSku = conn.prepareStatement("UPDATE skus SET stock = stock - ? WHERE skuid = ?")) {
                        updateSku.setInt(1, item.quantity);
                        updateSku.setInt(2, item.skuId);
                        updateSku.executeUpdate();
                    }

                    try (PreparedStatement insertOrder = conn.prepareStatement(
                            "INSERT INTO orders (userid, skuid, quantity, price, total_amount, status, shipping_address) VALUES (?, ?, ?, ?, ?, '已支付', ?)")) {
                        insertOrder.setInt(1, userId);
                        insertOrder.setInt(2, item.skuId);
                        insertOrder.setInt(3, item.quantity);
                        insertOrder.setBigDecimal(4, item.price);
                        insertOrder.setBigDecimal(5, item.price.multiply(BigDecimal.valueOf(item.quantity)));
                        insertOrder.setString(6, address.get()); // 从之前查询的address
                        insertOrder.executeUpdate();
                    }
                }

                // 6. 清空购物车
                try (PreparedStatement clearCart = conn.prepareStatement("DELETE FROM cart WHERE userid = ?")) {
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

    public List<CartItemDetail> getCartDetailsByUserId(int userId) throws SQLException {
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
                + "s.price, com.name AS product_name,s.stock, s.style, com.manufacturer "
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
