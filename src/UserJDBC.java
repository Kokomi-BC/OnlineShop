package src;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserJDBC {
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

    public static String addUser(User user) {
        String checkUsernameSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password, phone, address, balance, remark) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkUsernameSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            checkStmt.setString(1, user.getUsername());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return "用户名已存在";
            }

            insertStmt.setString(1, user.getUsername());
            insertStmt.setString(2, user.getPassword());
            insertStmt.setString(3, user.getPhone());
            insertStmt.setString(4, user.getAddress());
            insertStmt.setBigDecimal(5, user.getBalance());
            insertStmt.setString(6, user.getRemark());

            int affectedRows = insertStmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        return "添加成功，用户名"+ user.getUsername()+"用户ID: " + generatedId;
                    }
                }
                return "添加成功";
            } else {
                return "添加失败";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "数据库错误: " + e.getMessage();
        }
    }

    // 根据用户名查询用户
    public static User getUserByUsername(String username) {
        String sql = "SELECT id, username, password, phone, address, balance, remark FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getBigDecimal("balance"),
                        rs.getString("remark")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 根据ID查询用户
    public static User getUserById(int id) {
        String sql = "SELECT id, username, password, phone, address, balance, remark FROM users WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getBigDecimal("balance"),
                        rs.getString("remark")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取所有用户
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, phone, address, balance, remark FROM users";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getBigDecimal("balance"),
                        rs.getString("remark")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // 删除用户
    public static boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}