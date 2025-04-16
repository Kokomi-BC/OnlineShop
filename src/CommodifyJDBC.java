package src;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommodifyJDBC {
    // 数据库连接信息
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/commodities";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "mm050808";

    // 加载数据库驱动
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("驱动加载失败");
        }
    }

    // 获取数据库连接
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    // 添加商品
    public static boolean addCommodify(Commodify comm) {
        // 1. 先检查ID是否已存在
        String checkIdSql = "SELECT COUNT(*) FROM commodities WHERE id = ?";
        // 2. 插入商品的SQL
        String insertSql = "INSERT INTO commodities (id, name, type, detail, price, num, state) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             // 检查ID的PreparedStatement
             PreparedStatement checkStmt = conn.prepareStatement(checkIdSql);
             // 插入商品的PreparedStatement
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            // 检查ID是否已存在
            checkStmt.setInt(1, comm.getId());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("商品ID " + comm.getId() + " 已存在，添加失败！");
                return false;
            }

            // 插入新商品
            insertStmt.setInt(1, comm.getId());
            insertStmt.setString(2, comm.getName());
            insertStmt.setString(3, comm.getType());
            insertStmt.setString(4, comm.getDetail());
            insertStmt.setDouble(5, comm.getPrice());
            insertStmt.setInt(6, comm.getNum());
            insertStmt.setString(7, comm.getState());

            int affectedRows = insertStmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("商品添加成功！");
                return true;
            } else {
                System.out.println("商品添加失败！");
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库错误: " + e.getMessage());
            return false;
        }
    }

    // 根据ID查询商品
    public static Commodify getCommodifyById(int id) {
        String sql = "SELECT * FROM commodities WHERE id = ?";
        Commodify comm = null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                comm = new Commodify(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("detail"),
                        rs.getDouble("price"),
                        rs.getInt("num"),
                        rs.getString("state")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comm;
    }

    // 查询所有商品
    public static List<Commodify> getAllCommodifies() {
        String sql = "SELECT * FROM commodities";
        List<Commodify> list = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Commodify comm = new Commodify(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("detail"),
                        rs.getDouble("price"),
                        rs.getInt("num"),
                        rs.getString("state")
                );
                list.add(comm);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 更新商品信息
    public static boolean updateCommodify(Commodify comm) {
        String sql = "UPDATE commodities SET name=?, type=?, detail=?, price=?, num=?, state=? WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, comm.getName());
            pstmt.setString(2, comm.getType());
            pstmt.setString(3, comm.getDetail());
            pstmt.setDouble(4, comm.getPrice());
            pstmt.setInt(5, comm.getNum());
            pstmt.setString(6, comm.getState());
            pstmt.setInt(7, comm.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 根据ID删除商品
    public static boolean deleteCommodify(int id) {
        // 先删除购物车中引用该商品的记录
        String deleteCartSql = "DELETE FROM cart WHERE commodify_id = ?";
        // 再删除商品
        String deleteCommodifySql = "DELETE FROM commodities WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement deleteCartStmt = conn.prepareStatement(deleteCartSql);
             PreparedStatement deleteCommodifyStmt = conn.prepareStatement(deleteCommodifySql)) {

            // 1. 删除购物车记录
            deleteCartStmt.setInt(1, id);
            deleteCartStmt.executeUpdate();

            // 2. 删除商品
            deleteCommodifyStmt.setInt(1, id);
            return deleteCommodifyStmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 根据名称模糊查询商品
    public static List<Commodify> searchCommodifiesByName(String keyword) {
        String sql = "SELECT * FROM commodities WHERE name LIKE ?";
        List<Commodify> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Commodify comm = new Commodify(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("detail"),
                        rs.getDouble("price"),
                        rs.getInt("num"),
                        rs.getString("state")
                );
                list.add(comm);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 更新商品库存
    public static boolean updateCommodifyNum(int id, int newNum) {
        String sql = "UPDATE commodities SET num=? WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newNum);
            pstmt.setInt(2, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean updateCommodifyPartial(int id, String field, Object value) {
        // 验证字段名，防止SQL注入
        if (!isValidField(field)) {
            System.out.println("无效的字段名: " + field);
            return false;
        }

        String sql = "UPDATE commodities SET " + field + " = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 根据值的类型设置参数
            if (value instanceof String) {
                pstmt.setString(1, (String) value);
            } else if (value instanceof Double) {
                pstmt.setDouble(1, (Double) value);
            } else if (value instanceof Integer) {
                pstmt.setInt(1, (Integer) value);
            } else {
                System.out.println("不支持的数据类型");
                return false;
            }

            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 验证字段名是否合法，防止SQL注入
    private static boolean isValidField(String field) {
        return field.matches("name|type|detail|price|num|state");
    }
}
