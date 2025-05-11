package src;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static src.CommodityJDBC.*;
public class OrderJBDC {
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
                        order.setOrderId(orderId);
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

    public static List<Order> getOrdersByUserId(int userId) {
        String orderSql = "SELECT * FROM orders WHERE userid = ?";
        String detailSql = "SELECT * FROM order_details WHERE orderid = ?";
        List<Order> orders = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {

            orderStmt.setInt(1, userId);
            try (ResultSet rs = orderStmt.executeQuery()) {
                while (rs.next()) {
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

                    try (PreparedStatement detailStmt = conn.prepareStatement(detailSql)) {
                        detailStmt.setInt(1, order.getOrderId());
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
                    }
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return orders;
    }

    public static int getTotalQuantityByUserId(int userId) {
        String sql = "SELECT SUM(od.quantity) AS total_quantity " +
                "FROM order_details od " +
                "JOIN orders o ON od.orderid = o.orderid " +
                "WHERE o.status IN ('已支付', '已发货') AND o.userid = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_quantity");
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return 0;
    }
    public static List<Orderinfo> getinfoById(int userId) {
        String sql = "SELECT " +
                "o.orderid AS orderid, " +
                "c.name AS commodityName, " +
                "s.style AS style, " +
                "s.color AS color, " +
                "od.price * od.quantity AS amount, " +
                "od.quantity AS quantity, " +
                "o.payment_method AS paymentMethod, " +
                "o.status AS status, " +
                "o.shipping_address AS shippingAddress, " +
                "o.created_time AS createdTime, " +
                "o.remark AS remark " +
                "FROM orders o " +
                "INNER JOIN order_details od ON o.orderid = od.orderid " +
                "INNER JOIN skus s ON od.skuid = s.skuid " +
                "INNER JOIN commodities c ON s.commodityid = c.id " +
                "WHERE o.userid = ?";
        List<Orderinfo> orders = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Orderinfo orderInfo = new Orderinfo();
                orderInfo.setOrderid(rs.getInt("orderid"));
                orderInfo.setPaymentMethod(rs.getString("paymentMethod"));
                orderInfo.setStatus(rs.getString("status"));
                orderInfo.setShippingAddress(rs.getString("shippingAddress"));
                // 处理时间戳（支持Java 8+的LocalDateTime）
                Timestamp timestamp = rs.getTimestamp("createdTime");
                if (timestamp != null) {
                    orderInfo.setCreatedTime(timestamp.toLocalDateTime());
                }
                // 设置商品明细信息
                orderInfo.setCommodityName(rs.getString("commodityName"));
                orderInfo.setStyle(rs.getString("style"));
                orderInfo.setColor(rs.getString("color"));
                // 处理金额（使用BigDecimal保证精度）
                BigDecimal amount = rs.getBigDecimal("amount");
                orderInfo.setAmount(amount != null ? amount.doubleValue() : 0.0);
                // 设置其他字段
                orderInfo.setQuantity(rs.getInt("quantity"));
                orderInfo.setRemark(rs.getString("remark"));
                orders.add(orderInfo);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        } finally {
            closeConnection(conn);
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                handleSQLException(e);
            }
        }
        return orders;
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
