package src;
import java.sql.*;
import java.sql.Date;
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
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }
    // 添加商品基本信息（默认款式）
    /*
    public static boolean addCommodity(Commodity commodity) throws SQLException {
        String sql = "INSERT INTO commodities (id, name, type, detail, production_date, manufacturer, origin, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // 开启事务
            if (commodityExists(conn, commodity.getId())) {
                System.out.println("错误：商品ID已存在");
                return false;
            }
            if (commodityExists(conn, commodity.getName())) {
                System.out.println("警告：商品名称 '"+commodity.getName()+"' 已存在！");
                System.out.print("是否要继续添加？(y/n): ");
                Scanner scanner = new Scanner(System.in);
                String choice = scanner.nextLine().trim().toLowerCase();
                if (!choice.equals("y")) {
                    System.out.println("用户取消添加商品");
                    return false;
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setCommodityParameters(pstmt, commodity);
                int affectedRows = pstmt.executeUpdate();
                conn.commit(); // 提交事务
                return affectedRows > 0;
            } catch (SQLException e) {
                conn.rollback(); // 回滚事务
                handleSQLException(e);
                return false;
            }
        }
    }
*/
    public static boolean addCommodity(Commodity commodity, List<CommoditySKU> skus) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            // 插入商品基本信息（使用RETURN_GENERATED_KEYS获取自增ID）
            int commodityId;
            if (commodityExists(conn, commodity.getName())) {
                System.out.println("警告：商品名称 '"+commodity.getName()+"' 已存在！");
                System.out.print("是否要继续添加？(y/n): ");
                Scanner scanner = new Scanner(System.in);
                String choice = scanner.nextLine().trim().toLowerCase();
                if (!choice.equals("y")) {
                    System.out.println("用户取消添加商品");

                    return false;
                }
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO commodities (name, type, detail, production_date, manufacturer, origin, remark) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)",
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

            // 插入SKU数据（关联获取到的商品ID）
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO skus (commodityid, color, style, price, stock) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {

                for (CommoditySKU sku : skus) {
                    pstmt.setInt(1, commodityId);  // 关联商品ID
                    pstmt.setString(2, sku.getColor());
                    pstmt.setString(3, sku.getStyle());
                    pstmt.setBigDecimal(4, sku.getPrice());
                    pstmt.setInt(5, sku.getStock());
                    pstmt.addBatch();
                }
                int[] results = pstmt.executeBatch();
                for (int i = 0; i < results.length; i++) {
                    if (results[i] == Statement.EXECUTE_FAILED) {
                        throw new BatchUpdateException(
                                "SKU批量插入失败（第" + (i+1) + "条记录失败）",
                                results
                        );
                    }
                }
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    int index = 0;
                    while (rs.next() && index < skus.size()) {
                        skus.get(index++).setSkuId(rs.getInt(1));
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
    // 带事务的完整商品添加
    public static boolean addCommodityWithSKUs(Commodity commodity, List<CommoditySKU> skus) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            // 检查商品ID是否已存在
            if (commodityExists(conn, commodity.getId())) {
                System.out.println("错误：商品ID已存在");
                return false;
            }
            // 2. 插入商品基本信息（commodityid 存在）
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO commodities (id, name, type, detail, production_date, manufacturer, origin, remark) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                setCommodityParameters(pstmt, commodity);
                pstmt.executeUpdate();
            }
            // 3. 再插入SKU数据（此时 commodityid 已存在，外键约束不会报错）
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO skus (commodityid, color, style, price, stock) " +
                            "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                for (CommoditySKU sku : skus) {
                    pstmt.setInt(1, commodity.getId());
                    pstmt.setString(2, sku.getColor());
                    pstmt.setString(3, sku.getStyle());
                    pstmt.setBigDecimal(4, sku.getPrice());
                    pstmt.setInt(5, sku.getStock());
                    pstmt.addBatch();
                }
                int[] updateCounts = pstmt.executeBatch();
                for (int count : updateCounts) {
                    if (count == Statement.EXECUTE_FAILED) {
                        throw new SQLException("SKU插入失败");
                    }
                }
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    int index = 0;
                    while (rs.next() && index < skus.size()) {
                        skus.get(index++).setSkuId(rs.getInt(1));
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
    // 查询商品详情（包含SKU）
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
                    sku.setPrice(rs.getDouble("price"));
                    sku.setStock(rs.getInt("stock"));
                    if (sku.getColor() != null || sku.getStyle() != null) {
                        skus.add(sku);
                    }
                }
            }
            // 改为控制台输出代替抛异常
            if (commodity == null) {
                System.out.println("查询失败：未找到ID为 " + id + " 的商品");
                return null;  // 明确返回null
            }
            commodity.setSkus(skus);
            return commodity;
        } catch (SQLException e) {
            handleSQLException(e);
            return null;  // 数据库异常也返回null
        }
    }
    public static CommoditySKU getCommoditySKUById(int skuId) {
        String sql = "SELECT skuid, commodityid, color, style, price, stock " +
                "FROM skus WHERE skuid = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 设置查询参数
            pstmt.setInt(1, skuId);
            ResultSet rs = pstmt.executeQuery();

            // 处理查询结果
            if (rs.next()) {
                CommoditySKU sku = new CommoditySKU();
                // 映射基础字段
                sku.setSkuId(rs.getInt("skuid"));
                sku.setCommodityid(rs.getInt("commodityid"));
                sku.setColor(rs.getString("color"));
                sku.setStyle(rs.getString("style"));
                sku.setPrice(rs.getDouble("price"));
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
    }// 从数据库获取 SKU 信息
    public static List<CommoditySKU> getCommodityskuByName(String name) {
        String sql = "SELECT skuid, commodityid, color, style, price, stock FROM skus WHERE commodityid IN " +
                "(SELECT id FROM commodities WHERE name = ?)";
        List<CommoditySKU> skus = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CommoditySKU sku = new CommoditySKU();
                    sku.setSkuId(rs.getInt("skuid"));
                    sku.setCommodityid(rs.getInt("commodityid"));
                    sku.setColor(rs.getString("color"));
                    sku.setStyle(rs.getString("style"));
                    sku.setPrice(rs.getDouble("price"));
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
                Commodity commodity = commodityMap.computeIfAbsent(commodityId, k -> {
                    Commodity c = new Commodity();
                    c.setId(commodityId);
                    try {
                        c.setName(rs.getString("name"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        c.setType(rs.getString("type"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        c.setDetail(rs.getString("detail"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        c.setProductionDate(rs.getDate("production_date"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        c.setManufacturer(rs.getString("manufacturer"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        c.setOrigin(rs.getString("origin"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
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
                    sku.setPrice(rs.getDouble("price"));
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
    // 辅助方法
    private static void setCommodityParameters(PreparedStatement pstmt, Commodity c) throws SQLException {
        pstmt.setInt(1, c.getId());
        pstmt.setString(2, c.getName());
        pstmt.setString(3, c.getType());
        pstmt.setString(4, c.getDetail());
        if (c.getProductionDate() != null) {
            pstmt.setDate(5, new java.sql.Date(c.getProductionDate().getTime()));
        } else {
            pstmt.setNull(5, Types.DATE);
        }
        pstmt.setString(6, c.getManufacturer());
        pstmt.setString(7, c.getOrigin());
        pstmt.setString(8, c.getRemark());
    }
    private static void setCommodityParameter(PreparedStatement pstmt, Commodity c) throws SQLException {
        pstmt.setString(1, c.getName());
        pstmt.setString(2, c.getType());
        pstmt.setString(3, c.getDetail());
        if (c.getProductionDate() != null) {
            pstmt.setDate(4, new java.sql.Date(c.getProductionDate().getTime()));
        } else {
            pstmt.setNull(4, Types.DATE);
        }
        pstmt.setString(5, c.getManufacturer());
        pstmt.setString(6, c.getOrigin());
        pstmt.setString(7, c.getRemark());
    }

    private static boolean commodityExists(Connection conn, int id) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 1 FROM commodities WHERE id = ?")) {
            pstmt.setInt(1, id);
            return pstmt.executeQuery().next();
        }
    }
    private static boolean commodityExists(Connection conn, String name) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 1 FROM commodities WHERE name = ?")) {
            pstmt.setString(1, name);
            return pstmt.executeQuery().next();
        }
    }
    // 事务管理
    private static void rollbackTransaction(Connection conn) {
        try {
            if (conn != null) conn.rollback();
        } catch (SQLException ex) {
            System.err.println("回滚失败: " + ex.getMessage());
        }
    }
    private static void handleSQLException(SQLException e) {
        System.err.println("SQL错误[" + e.getErrorCode() + "]: " + e.getMessage());
    }
    public static boolean updateSkuStock(int commodityId, String color, String style, int newStock) {
        String sql = "UPDATE skus SET stock = stock + ? " +
                "WHERE commodityid = ? " +
                "AND (color = ? OR (? IS NULL AND color IS NULL)) " +
                "AND (style = ? OR (? IS NULL AND style IS NULL))";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newStock);
            pstmt.setInt(2, commodityId);
            pstmt.setString(3, color);
            pstmt.setString(4, color); // 重复绑定 null 判断参数
            pstmt.setString(5, style);
            pstmt.setString(6, style);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException(e);
            return false;
        }
    }
    public static boolean updateSkuPrice(int commodityid, String color, String style, double newPrice) {
        String sql = "UPDATE skus SET price = ? "  // 修正表名
                + "WHERE commodityid = ? AND color <=> ? AND style <=> ?"; // 使用字符串比较

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, commodityid);
            pstmt.setString(3, color);
            pstmt.setString(4, style);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException(e);
            return false;
        }
    }

    public static boolean updateCommodifyPartial(int id, String field, Object value) {
        if (!isValidField(field)) {
            System.out.println("无效字段: " + field);
            return false;
        }

        String sql = String.format("UPDATE commodities SET %s = ? WHERE id = ?", field);

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 根据字段类型设置参数
            int paramIndex = 1;
            switch (field) {
                case "production_date":
                    if (value instanceof Date) {
                        pstmt.setDate(paramIndex++, new java.sql.Date(((Date) value).getTime()));
                    } else {
                        throw new IllegalArgumentException("production_date需要Date类型");
                    }
                    break;
                default:
                    pstmt.setObject(paramIndex++, value);
            }
            pstmt.setInt(paramIndex, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException(e);
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("参数类型错误: " + e.getMessage());
            return false;
        }
    }

    private static boolean isValidField(String field) {
        return Set.of("name", "type", "detail", "production_date",
                "manufacturer", "origin", "remark").contains(field);
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
    public static List<Map<String, Object>> getSkuDetailsBycommodityid(int commodityid) {
        List<Map<String, Object>> skuDetails = new ArrayList<>();
        String sql = "SELECT color, style, stock FROM skus WHERE commodityid = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commodityid);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> sku = new HashMap<>();
                    sku.put("color", rs.getString("color"));
                    sku.put("style", rs.getString("style"));
                    sku.put("stock", rs.getInt("stock"));
                    skuDetails.add(sku);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return skuDetails;
    }
}