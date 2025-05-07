package src;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static src.CommodityJDBC.handleSQLException;
import static src.CommodityJDBC.rollbackTransaction;
public class OrderJBDC {
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
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }
    private static void setOrderParameters(PreparedStatement pstmt, Order o) throws SQLException {
        pstmt.setInt(1, o.getUserId());
        pstmt.setBigDecimal(2, o.getTotalAmount());
        pstmt.setString(3, o.getShippingAddress());
        pstmt.setString(4, o.getPaymentMethod());
        pstmt.setString(5, o.getRemark());
    }
    public static boolean addOrder(Order order) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            int orderId;
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO orders (userId, totalAmount, shippingAddress, paymentMethod, remark) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                setOrderParameters(pstmt, order);
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                        order.setOrderId(orderId); // 回填生成的订单ID到对象
                    } else {
                        throw new SQLException("创建订单失败，未获取到ID");
                    }
                }
            }
            List<OrderDetail> details = order.getDetails();
            if (details != null && !details.isEmpty()) {
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO order_details (orderid, skuid, quantity, price) " +
                                "VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    for (OrderDetail detail : details) {
                        pstmt.setInt(1, orderId);  // 关联订单ID
                        pstmt.setInt(2, detail.getSkuId());
                        pstmt.setInt(3, detail.getQuantity());
                        pstmt.setBigDecimal(4, detail.getPrice());
                        pstmt.addBatch();
                    }
                    int[] results = pstmt.executeBatch();
                    for (int i = 0; i < results.length; i++) {
                        if (results[i] == Statement.EXECUTE_FAILED) {
                            throw new BatchUpdateException(
                                    "订单明细插入失败（第" + (i + 1) + "条记录失败）",
                                    results
                            );
                        }
                    }
                    // 回填生成的明细ID到对象
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        int index = 0;
                        while (rs.next() && index < details.size()) {
                            details.get(index++).setDetailId(rs.getInt(1));
                        }
                    }
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            rollbackTransaction(conn);
            handleSQLException(e);
            return false;
        } finally {
            closeConnection(conn);
        }
    }
    // 根据订单ID获取完整订单信息（包含明细）
    public static Order getOrderById(int orderId) {
        String orderSql = "SELECT * FROM orders WHERE orderid = ?";
        String detailSql = "SELECT * FROM order_details WHERE orderid = ?";
        try (Connection conn = getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(orderSql);
             PreparedStatement detailStmt = conn.prepareStatement(detailSql)) {
            orderStmt.setInt(1, orderId);
            try (ResultSet rs = orderStmt.executeQuery()) {
                if (!rs.next()) return null;
                Order order = new Order();
                order.setOrderId(rs.getInt("orderid"));
                order.setUserId(rs.getInt("userid"));
                order.setTotalAmount(rs.getBigDecimal("total_amount"));
                order.setStatus(rs.getString("status"));
                order.setShippingAddress(rs.getString("shipping_address"));
                order.setPaymentMethod(rs.getString("payment_method"));
                order.setCreatedTime(rs.getObject("created_time", LocalDateTime.class));
                order.setPaymentTime(rs.getObject("payment_time", LocalDateTime.class));
                order.setShippedTime(rs.getObject("shipped_time", LocalDateTime.class));
                order.setCompletedTime(rs.getObject("completed_time", LocalDateTime.class));
                order.setRemark(rs.getString("remark"));
                // 查询关联明细
                detailStmt.setInt(1, orderId);
                try (ResultSet detailRs = detailStmt.executeQuery()) {
                    List<OrderDetail> details = new ArrayList<>();
                    while (detailRs.next()) {
                        OrderDetail detail = new OrderDetail();
                        detail.setDetailId(detailRs.getInt("detail_id"));
                        detail.setOrderId(detailRs.getInt("orderid"));
                        detail.setSkuId(detailRs.getInt("skuid"));
                        detail.setQuantity(detailRs.getInt("quantity"));
                        detail.setPrice(detailRs.getBigDecimal("price"));
                        details.add(detail);
                    }
                    order.setDetails(details);
                }
                return order;
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return null;
        }
    }
    // 根据明细ID获取单个订单明细
    public static OrderDetail getOrderDetail(int detailId) {
        String sql = "SELECT * FROM order_details WHERE detail_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, detailId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) return null;

                OrderDetail detail = new OrderDetail();
                detail.setDetailId(rs.getInt("detail_id"));
                detail.setOrderId(rs.getInt("orderid"));
                detail.setSkuId(rs.getInt("skuid"));
                detail.setQuantity(rs.getInt("quantity"));
                detail.setPrice(rs.getBigDecimal("price"));
                return detail;
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return null;
        }
    }
    private static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.setAutoCommit(true); // 恢复自动提交
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("关闭连接失败: " + e.getMessage());
        }
    }
}
