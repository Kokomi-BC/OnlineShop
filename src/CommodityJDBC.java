package src;
import javax.swing.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
public class CommodityJDBC {
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
    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }
    public static int addCommodity(Commodity commodity) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM commodities WHERE name = ?")) {
                checkStmt.setString(1, commodity.getName());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("商品已存在，无法添加同名商品: " + commodity.getName());
                        JOptionPane.showMessageDialog(null, "商品已存在，无法添加同名商品", "错误", JOptionPane.ERROR_MESSAGE);
                        return -1;
                    }
                }
            }
            int commodityId;
                try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO commodities (name, type, detail, image_base64, production_date, manufacturer, origin, remark) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                setCommodityParameter(pstmt, commodity);
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        commodityId = rs.getInt(1);
                    } else {
                        throw new SQLException("创建商品失败，未获取到ID");
                    }
                }
            }

            conn.commit();
            return commodityId;
        } catch (SQLException e) {
            rollbackTransaction(conn);
            handleSQLException(e);
            return -1;
        } finally {
            closeConnection(conn);
        }
    }

    public static Commodity getCommodityById(int id) {
        String sql = "SELECT c.*, cs.skuid, cs.commodityid, cs.color, cs.style, cs.price, cs.stock " +
                "FROM commodities c " +
                "LEFT JOIN skus cs ON c.id = cs.commodityid " +
                "WHERE c.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            Commodity commodity = null;
            List<CommoditySKU> skus = new ArrayList<>();
            while (rs.next()) {
                if (commodity == null) {
                    commodity = new Commodity();
                    commodity.setId(rs.getInt("id"));
                    commodity.setName(rs.getString("name"));
                    commodity.setType(rs.getString("type"));
                    commodity.setDetail(rs.getString("detail"));
                    commodity.setImageBase64(rs.getString("image_base64"));
                    commodity.setProductionDate(rs.getDate("production_date"));
                    commodity.setManufacturer(rs.getString("manufacturer"));
                    commodity.setOrigin(rs.getString("origin"));
                    commodity.setRemark(rs.getString("remark"));
                }
                if (rs.getObject("skuid") != null) {
                    CommoditySKU sku = new CommoditySKU();
                    sku.setSkuId(rs.getInt("skuid"));
                    sku.setCommodityid(rs.getInt("commodityid"));
                    sku.setColor(rs.getString("color"));
                    sku.setStyle(rs.getString("style"));
                    sku.setPrice(BigDecimal.valueOf(rs.getDouble("price")));
                    sku.setStock(rs.getInt("stock"));
                    if (sku.getColor() != null || sku.getStyle() != null) {
                        skus.add(sku);
                    }
                }
            }
            if (commodity == null) {
                System.out.println("查询失败：未找到ID为 " + id + " 的商品");
                return null;
            }
            commodity.setSkus(skus);
            return commodity;
        } catch (SQLException e) {
            handleSQLException(e);
            return null;
        }
    }
    public static CommoditySKU getCommoditySKUById(int skuId) {
        String sql = "SELECT skuid, commodityid, color, style, price, stock " +
                "FROM skus WHERE skuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, skuId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                CommoditySKU sku = new CommoditySKU();
                sku.setSkuId(rs.getInt("skuid"));
                sku.setCommodityid(rs.getInt("commodityid"));
                sku.setColor(rs.getString("color"));
                sku.setStyle(rs.getString("style"));
                sku.setPrice(BigDecimal.valueOf(rs.getDouble("price")));
                sku.setStock(rs.getInt("stock"));

                if (rs.next()) {
                    System.err.println("警告：发现重复SKUID记录: " + skuId);
                }
                return sku;
            } else {
                System.out.println("未找到SKUID为 " + skuId + " 的记录");
                return null;
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return null;
        }
    }
    public static boolean updateSkuStock(int skuid, int delta) throws SQLException {
        String sql = "UPDATE skus SET stock = stock + ? WHERE skuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, delta);
            pstmt.setInt(2, skuid);
            return pstmt.executeUpdate() > 0;
        }
    }
    public static List<CommoditySKU> getCommodityskuById(int id) {
        String sql = "SELECT skuid, color, style, price, stock FROM skus WHERE commodityid  = ?";
        List<CommoditySKU> skus = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CommoditySKU sku = new CommoditySKU();
                    sku.setSkuId(rs.getInt("skuid"));
                    sku.setCommodityid(id);
                    sku.setColor(rs.getString("color"));
                    sku.setStyle(rs.getString("style"));
                    sku.setPrice(BigDecimal.valueOf(rs.getDouble("price")));
                    sku.setStock(rs.getInt("stock"));
                    skus.add(sku);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }

        return skus;
    }
    public static List<Commodity> getCommodityByName(String name) {
        String sql = "SELECT c.*, cs.skuid, cs.commodityid, cs.color, cs.style, cs.price, cs.stock " +
                "FROM commodities c " +
                "LEFT JOIN skus cs ON c.id = cs.commodityid " +
                "WHERE c.name LIKE ?";
        Map<Integer, Commodity> commodityMap = new LinkedHashMap<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int commodityId = rs.getInt("id");
                Commodity commodity = commodityMap.computeIfAbsent(commodityId, _ -> {
                    Commodity c = new Commodity();
                    c.setId(commodityId);
                    try {
                        c.setName(rs.getString("name"));
                        c.setType(rs.getString("type"));
                        c.setDetail(rs.getString("detail"));
                        c.setImageBase64(rs.getString("image_base64"));
                        c.setProductionDate(rs.getDate("production_date"));
                        c.setManufacturer(rs.getString("manufacturer"));
                        c.setOrigin(rs.getString("origin"));
                        c.setRemark(rs.getString("remark"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    c.setSkus(new ArrayList<>()); // 初始化SKU列表
                    return c;
                });
                // 处理SKU信息
                if (rs.getObject("skuid") != null) {
                    CommoditySKU sku = new CommoditySKU();
                    sku.setSkuId(rs.getInt("skuid"));
                    sku.setCommodityid(rs.getInt("commodityid"));
                    sku.setColor(rs.getString("color"));
                    sku.setStyle(rs.getString("style"));
                    sku.setPrice(BigDecimal.valueOf(rs.getDouble("price")));
                    sku.setStock(rs.getInt("stock"));
                    // 根据条件添加SKU
                    if (sku.getColor() != null || sku.getStyle() != null) {
                        commodity.getSkus().add(sku);
                    }
                }
            }
            List<Commodity> result = new ArrayList<>(commodityMap.values());
            if (result.isEmpty()) {
                System.out.println("未找到名称包含 [" + name + "] 的商品");
            }
            return result;
        } catch (SQLException e) {
            handleSQLException(e);
            return Collections.emptyList();
        }
    }
    //模糊搜索
    public static List<Commodity> searchCommodities(String keyword) {
        // 构建包含多个字段的模糊查询SQL
        String sql = "SELECT c.*, cs.skuid, cs.commodityid, cs.color, cs.style, cs.price, cs.stock " +
                "FROM commodities c " +
                "LEFT JOIN skus cs ON c.id = cs.commodityid " +
                "WHERE c.name LIKE ? " +
                "   OR c.type LIKE ? " +
                "   OR c.detail LIKE ? " +
                "   OR c.manufacturer LIKE ? " +
                "   OR c.origin LIKE ? " +
                "   OR c.remark LIKE ?";

        Map<Integer, Commodity> commodityMap = new LinkedHashMap<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 为所有6个参数设置相同的搜索关键字（两端添加通配符）
            for (int i = 1; i <= 6; i++) {
                pstmt.setString(i, "%" + keyword + "%");
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int commodityId = rs.getInt("id");
                    // 使用 computeIfAbsent 创建或获取已有商品对象
                    Commodity commodity = commodityMap.computeIfAbsent(commodityId, _ -> {
                        Commodity c = new Commodity();
                        // 设置商品基础信息（精简后的设置方式）
                        c.setId(commodityId);
                        try {
                            c.setName(rs.getString("name"));
                        c.setType(rs.getString("type"));
                        c.setDetail(rs.getString("detail"));
                        c.setImageBase64(rs.getString("image_base64"));
                        c.setProductionDate(rs.getDate("production_date"));
                        c.setManufacturer(rs.getString("manufacturer"));
                        c.setOrigin(rs.getString("origin"));
                        c.setRemark(rs.getString("remark"));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        c.setSkus(new ArrayList<>());
                        return c;
                    });
                    // 处理SKU信息
                    if (rs.getObject("skuid") != null) {
                        CommoditySKU sku = new CommoditySKU();
                        sku.setSkuId(rs.getInt("skuid"));
                        sku.setCommodityid(rs.getInt("commodityid"));
                        sku.setColor(rs.getString("color"));
                        sku.setStyle(rs.getString("style"));
                        sku.setPrice(BigDecimal.valueOf(rs.getDouble("price")));
                        sku.setStock(rs.getInt("stock"));
                        // 当颜色或样式有值时才添加到列表
                        if (sku.getColor() != null || sku.getStyle() != null) {
                            commodity.getSkus().add(sku);
                        }
                    }
                }
            }

            List<Commodity> result = new ArrayList<>(commodityMap.values());
            if (result.isEmpty()) {
                System.out.println("未找到包含 [" + keyword + "] 的商品");
            }
            return result;
        } catch (SQLException e) {
            handleSQLException(e);
            return Collections.emptyList();
        }
    }

    private static void setCommodityParameter(PreparedStatement pstmt, Commodity c) throws SQLException {
        pstmt.setString(1, c.getName());
        pstmt.setString(2, c.getType());
        pstmt.setString(3, c.getDetail());
        pstmt.setString(4, c.getImageBase64());
        if (c.getProductionDate() != null) {
            pstmt.setDate(5, new java.sql.Date(c.getProductionDate().getTime()));
        } else {
            pstmt.setNull(5, Types.DATE);
        }
        pstmt.setString(6, c.getManufacturer());
        pstmt.setString(7, c.getOrigin());
        pstmt.setString(8, c.getRemark());
    }

    private static boolean commodityExists(Connection conn, String name) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 1 FROM commodities WHERE name = ?")) {
            pstmt.setString(1, name);
            return pstmt.executeQuery().next();
        }
    }
    static void rollbackTransaction(Connection conn) {
        try {
            if (conn != null) conn.rollback();
        } catch (SQLException ex) {
            System.err.println("回滚失败: " + ex.getMessage());
        }
    }
    static void handleSQLException(SQLException e) {
        System.err.println("SQL错误[" + e.getErrorCode() + "]: " + e.getMessage());
    }
    public static boolean addSKU(int commodityId, CommoditySKU sku) {
        // 参数校验
        if (sku == null || sku.getPrice() == null) {
            System.out.println("SKU或价格不能为空");
            return false;
        }
        String sql = "INSERT INTO skus (commodityid, color, style, price, stock) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commodityId);
            pstmt.setString(2, sku.getColor());  // 允许null（触发数据库默认值）
            pstmt.setString(3, sku.getStyle());  // 允许null（触发数据库默认值）
            pstmt.setDouble(4, sku.getPrice().doubleValue());
            Integer stock = sku.getStock();
            pstmt.setInt(5, stock != null ? stock : 0);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            handleSQLException(e);
            return false;
        }
    }
    public static boolean deleteSKU(int skuId) {
        String sql = "DELETE FROM skus WHERE skuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, skuId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            handleSQLException(e);
            return false;
        }
    }
    public static boolean updateSKU(int skuId, CommoditySKU sku) {
        if (sku == null) {
            System.out.println("SKU不能为空");
            return false;
        }
        List<String> updates = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (sku.getColor() != null) {
            updates.add("color = ?");
            params.add(sku.getColor());
        }
        if (sku.getStyle() != null) {
            updates.add("style = ?");
            params.add(sku.getStyle());
        }
        if (sku.getPrice() != null) {
            updates.add("price = ?");
            params.add(sku.getPrice());
        }
        if (sku.getStock() != null) {
            updates.add("stock = ?");
            params.add(sku.getStock());
        }
        if (updates.isEmpty()) {
            System.out.println("未提供有效更新字段");
            return false;
        }
        String sql = "UPDATE skus SET " + String.join(", ", updates) + " WHERE skuid = ?";
        params.add(skuId);
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            handleSQLException(e);
            return false;
        }
    }
    public static boolean updateCommodity(int commodityId, Commodity commodity) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            if (!commodityExistsById(conn, commodityId)) {
                System.out.println("错误：商品ID " + commodityId + " 不存在！");
                return false;
            }
            if (commodityId <= 0) {
                System.out.println("错误：无效商品ID");
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE commodities SET name=?, type=?, detail=?, image_base64=?, production_date=?, manufacturer=?, origin=?, remark=? WHERE id=?")) {
                setCommodityParameter2(pstmt, commodity);
                pstmt.setInt(9, commodityId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("更新商品失败，未影响任何行");
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
    private static void setCommodityParameter2(PreparedStatement pstmt, Commodity c) throws SQLException {
        pstmt.setString(1, c.getName());
        pstmt.setString(2, c.getType());
        pstmt.setString(3, c.getDetail());
        pstmt.setString(4, c.getImageBase64());
        if (c.getProductionDate() != null) {
            pstmt.setDate(5, new java.sql.Date(c.getProductionDate().getTime()));
        } else {
            pstmt.setNull(5, Types.DATE);
        }
        pstmt.setString(6, c.getManufacturer());
        pstmt.setString(7, c.getOrigin());
        pstmt.setString(8, c.getRemark());
    }
    public static boolean deleteCommodity(int commodityId) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            if (!commodityExistsById(conn, commodityId)) {
                System.out.println("错误：商品ID " + commodityId + " 不存在！");
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM commodities WHERE id=?")) {
                pstmt.setInt(1, commodityId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("删除商品失败，未影响任何行");
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
    private static boolean commodityExistsById(Connection conn, int commodityId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM commodities WHERE id=?")) {
            pstmt.setInt(1, commodityId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    private static boolean commodityExistsExcludingId(Connection conn, String name, int excludeId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM commodities WHERE name=? AND id!=?")) {
            pstmt.setString(1, name);
            pstmt.setInt(2, excludeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }


    static void closeConnection(Connection conn) {
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