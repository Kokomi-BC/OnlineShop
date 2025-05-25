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
                    "INSERT INTO orders (userId, total_amount, shipping_address, payment_method, remark) " +
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

    public static boolean updateOrder(Order order) {
        String sql = "UPDATE orders SET userid = ?, total_amount = ?, status = ?, shipping_address = ?, payment_method = ?, payment_time = ?, shipped_time = ?, completed_time = ?, remark = ? WHERE orderid = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, order.getUserId());
            pstmt.setBigDecimal(2, order.getTotalAmount());
            pstmt.setString(3, order.getStatus());
            pstmt.setString(4, order.getShippingAddress());
            pstmt.setString(5, order.getPaymentMethod());
            pstmt.setObject(6, order.getPaymentTime());
            pstmt.setObject(7, order.getShippedTime());
            pstmt.setObject(8, order.getCompletedTime());
            pstmt.setString(9, order.getRemark());
            pstmt.setInt(10, order.getOrderId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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

    public static List<Orderinfo> getAllOrderinfos() {
        List<Orderinfo> orders = new ArrayList<>();
        String sql = "SELECT " +
                "o.orderid AS orderid, " +
                "od.detail_id AS detailid, " +
                "c.name AS commodityName, " +
                "c.id AS commodityid, " +
                "s.skuid AS skuid, " +
                "s.style AS style, " +
                "s.color AS color, " +
                "od.price * od.quantity AS amount, " +
                "od.quantity AS quantity, " +
                "o.payment_method AS paymentMethod, " +
                "o.status AS status, " +
                "o.shipping_address AS shippingAddress, " +
                "o.created_time AS createdTime, " +
                "o. shipped_time AS  shipped_time, " +
                "o. payment_time AS  payment_time, " +
                "o.completed_time AS completedTime, " +
                "o.remark AS remark " +
                "FROM orders o " +
                "INNER JOIN order_details od ON o.orderid = od.orderid " +
                "INNER JOIN skus s ON od.skuid = s.skuid " +
                "INNER JOIN commodities c ON s.commodityid = c.id";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Orderinfo orderInfo = new Orderinfo();
                orderInfo.setOrderid(rs.getInt("orderid"));
                orderInfo.setCommodityName(rs.getString("commodityName"));
                orderInfo.setCommodityid(rs.getInt("commodityid"));
                orderInfo.setDetailid(rs.getInt("detailid"));
                orderInfo.setStyle(rs.getString("style"));
                orderInfo.setColor(rs.getString("color"));
                BigDecimal amount = rs.getBigDecimal("amount");
                orderInfo.setAmount(amount != null ? amount.doubleValue() : 0.0);
                orderInfo.setQuantity(rs.getInt("quantity"));
                orderInfo.setPaymentMethod(rs.getString("paymentMethod"));
                orderInfo.setStatus(rs.getString("status"));
                orderInfo.setRemark(rs.getString("remark"));
                orderInfo.setShippingAddress(rs.getString("shippingAddress"));
                Timestamp timestamp = rs.getTimestamp("createdTime");
                if (timestamp != null) {
                    orderInfo.setCreatedTime(timestamp.toLocalDateTime());
                }
                Timestamp completedTimestamp = rs.getTimestamp("completedTime");
                if (completedTimestamp != null) {
                    orderInfo.setCompletedTime(completedTimestamp.toLocalDateTime());
                }
                Timestamp Shipped_timeTimestamp = rs.getTimestamp("Shipped_time");
                if (Shipped_timeTimestamp != null) {
                    orderInfo.setShipped_time(Shipped_timeTimestamp.toLocalDateTime());
                }
                Timestamp Payment_timeTimestamp = rs.getTimestamp("Payment_time");
                if (Payment_timeTimestamp != null) {
                    orderInfo.setPayment_time(Payment_timeTimestamp.toLocalDateTime());
                }
                orders.add(orderInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }


    public static List<Orderinfo> getinfoById(int userId) {
        String sql = "SELECT " +
                "o.orderid AS orderid, " +
                "od.detail_id AS detailid, " +
                "c.name AS commodityName, " +
                "c.id AS commodityid, " +
                "s.skuid AS skuid, " +
                "s.style AS style, " +
                "s.color AS color, " +
                "od.price * od.quantity AS amount, " +
                "od.quantity AS quantity, " +
                "o.payment_method AS paymentMethod, " +
                "o.status AS status, " +
                "o.shipping_address AS shippingAddress, " +
                "o.created_time AS createdTime, " +
                "o. shipped_time AS  shipped_time, " +
                "o. payment_time AS  payment_time, " +
                "o.completed_time AS completedTime, " +
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
                orderInfo.setDetailid(rs.getInt("detailid"));
                orderInfo.setPaymentMethod(rs.getString("paymentMethod"));
                orderInfo.setStatus(rs.getString("status"));
                orderInfo.setShippingAddress(rs.getString("shippingAddress"));
                Timestamp timestamp = rs.getTimestamp("createdTime");
                if (timestamp != null) {
                    orderInfo.setCreatedTime(timestamp.toLocalDateTime());
                }
                orderInfo.setCommodityName(rs.getString("commodityName"));
                orderInfo.setCommodityid(rs.getInt("commodityid"));
                orderInfo.setSkuid(rs.getInt("skuid"));
                orderInfo.setStyle(rs.getString("style"));
                orderInfo.setColor(rs.getString("color"));
                BigDecimal amount = rs.getBigDecimal("amount");
                orderInfo.setAmount(amount != null ? amount.doubleValue() : 0.0);
                orderInfo.setQuantity(rs.getInt("quantity"));
                orderInfo.setRemark(rs.getString("remark"));
                Timestamp completedTimestamp = rs.getTimestamp("completedTime");
                if (completedTimestamp != null) {
                    orderInfo.setCompletedTime(completedTimestamp.toLocalDateTime());
                }
                Timestamp Shipped_timeTimestamp = rs.getTimestamp("Shipped_time");
                if (Shipped_timeTimestamp != null) {
                    orderInfo.setShipped_time(Shipped_timeTimestamp.toLocalDateTime());
                }
                Timestamp Payment_timeTimestamp = rs.getTimestamp("Payment_time");
                if (Payment_timeTimestamp != null) {
                    orderInfo.setPayment_time(Payment_timeTimestamp.toLocalDateTime());
                }
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

    public static Orderinfo getInfoByDetailId(int detailId) {
        String sql = "SELECT " +
                "o.orderid AS orderid, " +
                "od.detail_id AS detailid, " +
                "c.name AS commodityName, " +
                "c.id AS commodityid, " +
                "s.skuid AS skuid, " +
                "s.style AS style, " +
                "s.color AS color, " +
                "od.price * od.quantity AS amount, " +
                "od.quantity AS quantity, " +
                "o.payment_method AS paymentMethod, " +
                "o.status AS status, " +
                "o.shipping_address AS shippingAddress, " +
                "o.created_time AS createdTime, " +
                "o. shipped_time AS  shipped_time, " +
                "o. payment_time AS  payment_time, " +
                "o.completed_time AS completedTime, " +
                "o.remark AS remark " +
                "FROM orders o " +
                "INNER JOIN order_details od ON o.orderid = od.orderid " +
                "INNER JOIN skus s ON od.skuid = s.skuid " +
                "INNER JOIN commodities c ON s.commodityid = c.id " +
                "WHERE od.detail_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, detailId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Orderinfo orderInfo = new Orderinfo();
                    orderInfo.setOrderid(rs.getInt("orderid"));
                    orderInfo.setDetailid(rs.getInt("detailid"));
                    orderInfo.setPaymentMethod(rs.getString("paymentMethod"));
                    orderInfo.setStatus(rs.getString("status"));
                    orderInfo.setShippingAddress(rs.getString("shippingAddress"));
                    Timestamp timestamp = rs.getTimestamp("createdTime");
                    if (timestamp != null) {
                        orderInfo.setCreatedTime(timestamp.toLocalDateTime());
                    }
                    orderInfo.setCommodityName(rs.getString("commodityName"));
                    orderInfo.setCommodityid(rs.getInt("commodityid"));
                    orderInfo.setSkuid(rs.getInt("skuid"));
                    orderInfo.setStyle(rs.getString("style"));
                    orderInfo.setColor(rs.getString("color"));
                    BigDecimal amount = rs.getBigDecimal("amount");
                    orderInfo.setAmount(amount != null ? amount.doubleValue() : 0.0);
                    orderInfo.setQuantity(rs.getInt("quantity"));
                    orderInfo.setRemark(rs.getString("remark"));
                    Timestamp completedTimestamp = rs.getTimestamp("completedTime");
                    if (completedTimestamp != null) {
                        orderInfo.setCompletedTime(completedTimestamp.toLocalDateTime());
                    }
                    Timestamp Shipped_timeTimestamp = rs.getTimestamp("Shipped_time");
                    if (Shipped_timeTimestamp != null) {
                        orderInfo.setShipped_time(Shipped_timeTimestamp.toLocalDateTime());
                    }
                    Timestamp Payment_timeTimestamp = rs.getTimestamp("Payment_time");
                    if (Payment_timeTimestamp != null) {
                        orderInfo.setPayment_time(Payment_timeTimestamp.toLocalDateTime());
                    }
                    return orderInfo;
                } else {
                    System.out.println("未找到明细ID: " + detailId);
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            handleSQLException(e);
        }
        return null;
    }
    public static boolean updateOrderInfo(Orderinfo orderInfo) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            String updateDetailSql = "UPDATE order_details SET quantity = ?, price = ? ,skuid=? WHERE detail_id = ? AND orderid = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateDetailSql)) {
                BigDecimal price = BigDecimal.valueOf(orderInfo.getAmount() / orderInfo.getQuantity());
                pstmt.setInt(1, orderInfo.getQuantity());
                pstmt.setBigDecimal(2, price);
                pstmt.setInt(3, orderInfo.getSkuid());
                pstmt.setInt(4, orderInfo.getDetailid());
                pstmt.setInt(5, orderInfo.getOrderid());
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }
            }
            String updateTotalSql = "UPDATE orders SET total_amount = (SELECT SUM(price * quantity) FROM order_details WHERE orderid = ?) WHERE orderid = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateTotalSql)) {
                pstmt.setInt(1, orderInfo.getOrderid());
                pstmt.setInt(2, orderInfo.getOrderid());
                pstmt.executeUpdate();
            }

            StringBuilder updateOrderSql = new StringBuilder("UPDATE orders SET ");
            List<Object> params = new ArrayList<>();
            List<String> setClauses = new ArrayList<>();

            // 处理状态相关字段
            if (orderInfo.getStatus() != null) {
                setClauses.add("status = ?");
                params.add(orderInfo.getStatus());
                // 根据状态自动更新时间字段
                switch (orderInfo.getStatus()) {
                    case "已支付":
                        setClauses.add("payment_time = COALESCE(payment_time, NOW())");
                        break;
                    case "已发货":
                        setClauses.add("shipped_time = COALESCE(shipped_time, NOW())");
                        break;
                    case "已完成":
                        if (orderInfo.getCompletedTime() != null) {
                            setClauses.add("completed_time = ?");
                            params.add(orderInfo.getCompletedTime());
                        }
                        else {
                            setClauses.add("completed_time = NOW()");
                        }
                        break;
                    case "已取消": {
                        setClauses.add("payment_time = null");
                        setClauses.add("shipped_time = null");
                        setClauses.add("completed_time = null");
                        setClauses.add("payment_method = null");
                        break;
                    }
                }
            }

            if (orderInfo.getShippingAddress() != null) {
                setClauses.add("shipping_address = ?");
                params.add(orderInfo.getShippingAddress());
            }
            if (orderInfo.getPaymentMethod() != null) {
                setClauses.add("payment_method = ?");
                params.add(orderInfo.getPaymentMethod());
            }
            if (orderInfo.getRemark() != null) {
                setClauses.add("remark = ?");
                params.add(orderInfo.getRemark());
            }

            if (setClauses.isEmpty()) {
                conn.commit();
                return true;
            }
            updateOrderSql.append(String.join(", ", setClauses));
            updateOrderSql.append(" WHERE orderid = ?");
            params.add(orderInfo.getOrderid());
            try (PreparedStatement pstmt = conn.prepareStatement(updateOrderSql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    Object param = params.get(i);
                    if (param instanceof String) {
                        pstmt.setString(i + 1, (String) param);
                    } else if (param instanceof LocalDateTime) {
                        pstmt.setTimestamp(i + 1, Timestamp.valueOf((LocalDateTime) param));
                    } else if (param instanceof Integer) {
                        pstmt.setInt(i + 1, (Integer) param);
                    }
                }
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            handleSQLException(e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
