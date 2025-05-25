package src;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import static src.CommodityJDBC.getConnection;
public class UserJDBC {
    public static String addUser(User user) {
        String checkUsernameSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        String checkPhoneSql = "SELECT COUNT(*) FROM users WHERE phone = ?";
        String insertSql = "INSERT INTO users (username, password, phone, address, balance, remark) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement checkUsernameStmt = conn.prepareStatement(checkUsernameSql);
             PreparedStatement checkPhoneStmt = conn.prepareStatement(checkPhoneSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            // 检查用户名是否已存在
            checkUsernameStmt.setString(1, user.getUsername());
            ResultSet usernameRs = checkUsernameStmt.executeQuery();
            if (usernameRs.next() && usernameRs.getInt(1) > 0) {
                return "用户名已存在";
            }
            checkPhoneStmt.setString(1, user.getPhone());
            ResultSet phoneRs = checkPhoneStmt.executeQuery();
            if (phoneRs.next() && phoneRs.getInt(1) > 0) {
                return "该号码已注册";
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
                        return "添加成功" ;
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
    public static String updateUser(User user) {
        StringBuilder updateSqlBuilder = new StringBuilder("UPDATE users SET username = ?, phone = ?, address = ?, balance = ?, remark = ?");
        boolean passwordProvided = user.getPassword() != null;

        if (passwordProvided) {
            updateSqlBuilder.append(", password = ?");
        }
        updateSqlBuilder.append(" WHERE id = ?");
        try (Connection conn = getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSqlBuilder.toString())) {
            int paramIndex = 1;
            updateStmt.setString(paramIndex++, user.getUsername());
            updateStmt.setString(paramIndex++, user.getPhone());
            updateStmt.setString(paramIndex++, user.getAddress());
            updateStmt.setBigDecimal(paramIndex++, user.getBalance());
            updateStmt.setString(paramIndex++, user.getRemark());

            if (passwordProvided) {
                updateStmt.setString(paramIndex++, user.getPassword());
            }
            updateStmt.setInt(paramIndex, user.getId());
            int affectedRows = updateStmt.executeUpdate();
            if (affectedRows > 0) {
                return "修改成功";
            } else {
                return "修改失败，未找到指定用户";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "数据库错误: " + e.getMessage();
        }
    }

    // 根据用户名查询用户
    public static User getUserByUsername(String username) {
        String sql = "SELECT id, username, password, phone, address, balance, remark, permission, created_time, modified_time FROM users WHERE username = ?";

        return getUser(username, sql);
    }
    // 根据手机号查询用户
    public static User getUserByUserphone(String phone) {
        String sql = "SELECT id, username, password, phone, address, balance, remark, permission, created_time, modified_time FROM users WHERE phone = ?";

        return getUser(phone, sql);
    }

    private static User getUser(String phone, String sql) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getBigDecimal("balance"),
                        rs.getString("remark"),
                        rs.getString("permission"),
                        rs.getTimestamp("created_time"),
                        rs.getTimestamp("modified_time")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 根据ID查询用户
    public static User getUserById(int id) {
        String sql = "SELECT id, username, password, phone, address, balance, remark, permission, created_time, modified_time FROM users WHERE id = ?";

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
                        rs.getString("remark"),
                        rs.getString("permission"),
                        rs.getTimestamp("created_time"),
                        rs.getTimestamp("modified_time")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, phone, address, balance, remark, permission FROM users";
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
                        rs.getString("remark"),
                        rs.getString("permission") // 添加权限字段
                );
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    public static String setAdminPermission(int userId) {
        String checkAdminSql = "SELECT permission FROM users WHERE id = ?";
        String updatePermissionSql = "UPDATE users SET permission = 'admin' WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement checkAdminStmt = conn.prepareStatement(checkAdminSql);
             PreparedStatement updateStmt = conn.prepareStatement(updatePermissionSql)) {
            checkAdminStmt.setInt(1, userId);
            ResultSet rs = checkAdminStmt.executeQuery();
            if (rs.next()) {
                String permission = rs.getString("permission");
                if ("admin".equals(permission)) {
                    return "用户ID " + userId + " 已经是管理员，无需再次设置";
                } else {
                    updateStmt.setInt(1, userId);
                    int affectedRows = updateStmt.executeUpdate();
                    if (affectedRows > 0) {
                        return "用户ID " + userId + " 的权限已成功设置为管理员";
                    } else {
                        return "设置失败，未找到指定用户";
                    }
                }
            } else {
                return "设置失败，未找到指定用户";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "数据库错误: " + e.getMessage();
        }
    }

    public static String revokeAdminPermission(int userId) {
        String checkAdminSql = "SELECT permission FROM users WHERE id = ?";
        String updatePermissionSql = "UPDATE users SET permission = '' WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement checkAdminStmt = conn.prepareStatement(checkAdminSql);
             PreparedStatement updateStmt = conn.prepareStatement(updatePermissionSql)) {

            // 检查用户权限
            checkAdminStmt.setInt(1, userId);
            ResultSet rs = checkAdminStmt.executeQuery();

            if (rs.next()) {
                String permission = rs.getString("permission");
                if ("admin".equals(permission)) {
                    updateStmt.setInt(1, userId);
                    int affectedRows = updateStmt.executeUpdate();
                    if (affectedRows > 0) {
                        return "用户ID " + userId + " 的管理员权限已成功撤销";
                    } else {
                        return "撤销失败，未找到指定用户";
                    }
                } else {
                    return "用户ID " + userId + " 不是管理员，无需撤销";
                }
            } else {
                return "撤销失败，未找到指定用户";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "数据库错误: " + e.getMessage();
        }
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