package GUI;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
import src.User;
import src.UserJDBC;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.mindrot.jbcrypt.BCrypt;

public class Login extends JFrame {

    public static void main(String[] args) {
        FlatMTMaterialLighterIJTheme.setup();
        UIManager.put("Button.arc", 20); // 圆角按钮
        UIManager.put("Component.arc", 20); // 圆角组件
        UIManager.put("TextComponent.arc", 10); // 文本框圆角
        UIManager.put("Component.hoverBackground", new Color(0xE3F2FD));  // 浅蓝色调
        UIManager.put("Component.pressedBackground", new Color(0xBBDEFB));
        UIManager.put("Component.focusColor", new Color(0x93E1FF));  // Material Blue
        UIManager.put("Component.hoverEffect", true);
        UIManager.put("Component.hoverFadeTime", 200);
        Login loginFrame = new Login();
    }
    public Login() {
        initComponents();
        login.setVisible(true);
    }

    private void onLoginSuccess(User user) {
        System.out.println("登录成功，跳转到主界面");
        login.dispose();
        Mainview mainview = new Mainview(user);
        mainview.setVisible(true);
    }
    private void loginin(ActionEvent e) {
        String username = usernameField.getText();
        String inputPassword = new String(passwordField.getPassword()); // 改个更明确的变量名
        if (username.isEmpty() || inputPassword.isEmpty()) {
            JOptionPane.showMessageDialog(Login.this, "用户名或密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            User user = UserJDBC.getUserByUsername(username);
            if (user != null && BCrypt.checkpw(inputPassword, user.getPassword())) {
                if ("admin".equals(user.getPermission())) {
                    onAdminLoginSuccess(user);
                } else {
                    JOptionPane.showMessageDialog(Login.this, "登录成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    onLoginSuccess(user);
                }
            } else {
                showAuthFailedMessage();
            }
        } catch (IllegalArgumentException ex) {
            System.err.println("密码哈希值格式错误: " + ex.getMessage());
            showAuthFailedMessage();
        }
    }
    // 统一认证失败提示方法
    private void showAuthFailedMessage() {
        JOptionPane.showMessageDialog(Login.this,
                "用户名或密码错误",
                "认证失败",
                JOptionPane.ERROR_MESSAGE);
    }


    // 新增管理员登录成功后的处理方法
    private void onAdminLoginSuccess(User user) {
        Object[] options = {"购物界面", "管理界面"};
        int choice = JOptionPane.showOptionDialog(
                Login.this,
                "请选择要进入的界面",
                "选择界面",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == 0) { // 购物界面
            System.out.println("管理员选择进入购物界面");
            login.dispose();
            Mainview mainview = new Mainview(user);
            mainview.setVisible(true);
        } else if (choice == 1) { // 管理界面
            System.out.println("管理员选择进入管理界面");
            login.dispose();
            Manager managerView = new Manager(user);
            managerView.setVisible(true);
        }
    }

    private void register(ActionEvent e) {
        register registerDialog = new register();
        registerDialog.initComponents();
        JDialog dialog = registerDialog.register;
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
        login.setVisible(false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                login.setVisible(true);
            }
        });
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        login = new JFrame();
        label1 = new JLabel();
        label2 = new JLabel();
        label3 = new JLabel();
        passwordField = new JPasswordField();
        usernameField = new JFormattedTextField();
        button1 = new JButton();
        button2 = new JButton();

        //======== login ========
        {
            login.setMinimumSize(new Dimension(600, 410));
            login.setMaximumSize(new Dimension(880, 600));
            login.setPreferredSize(new Dimension(600, 410));
            login.setBackground(Color.white);
            login.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            login.setTitle("\u5546\u57ce");
            login.setType(Window.Type.POPUP);
            login.setLocationByPlatform(true);
            login.setFocusTraversalPolicyProvider(true);
            var loginContentPane = login.getContentPane();
            loginContentPane.setLayout(null);

            //---- label1 ----
            label1.setText("\u767b\u5f55");
            label1.setBackground(new Color(0x00f8f7fa, true));
            label1.setAutoscrolls(true);
            label1.setHorizontalAlignment(SwingConstants.CENTER);
            label1.setFont(label1.getFont().deriveFont(label1.getFont().getStyle() | Font.BOLD, label1.getFont().getSize() + 12f));
            loginContentPane.add(label1);
            label1.setBounds(0, 10, 570, 70);

            //---- label2 ----
            label2.setText("\u8d26\u6237");
            label2.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 19));
            label2.setHorizontalAlignment(SwingConstants.CENTER);
            loginContentPane.add(label2);
            label2.setBounds(70, 90, 50, 30);

            //---- label3 ----
            label3.setText("\u5bc6\u7801");
            label3.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 19));
            label3.setHorizontalAlignment(SwingConstants.CENTER);
            loginContentPane.add(label3);
            label3.setBounds(70, 170, 50, 35);

            //---- passwordField ----
            passwordField.setHorizontalAlignment(SwingConstants.LEFT);
            loginContentPane.add(passwordField);
            passwordField.setBounds(155, 170, 305, 35);

            //---- usernameField ----
            usernameField.setHorizontalAlignment(SwingConstants.LEFT);
            loginContentPane.add(usernameField);
            usernameField.setBounds(155, 90, 305, 35);

            //---- button1 ----
            button1.setText("\u767b\u5f55");
            button1.setForeground(new Color(0x333333));
            button1.addActionListener(e -> loginin(e));
            loginContentPane.add(button1);
            button1.setBounds(165, 250, 85, 32);

            //---- button2 ----
            button2.setText("\u6ce8\u518c");
            button2.setForeground(new Color(0x333333));
            button2.addActionListener(e -> register(e));
            loginContentPane.add(button2);
            button2.setBounds(330, 250, 86, 32);

            {
                // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < loginContentPane.getComponentCount(); i++) {
                    Rectangle bounds = loginContentPane.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = loginContentPane.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                loginContentPane.setMinimumSize(preferredSize);
                loginContentPane.setPreferredSize(preferredSize);
            }
            login.pack();
            login.setLocationRelativeTo(null);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    public JFrame login;
    private JLabel label1;
    private JLabel label2;
    private JLabel label3;
    private JPasswordField passwordField;
    private JFormattedTextField usernameField;
    public JButton button1;
    private JButton button2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}