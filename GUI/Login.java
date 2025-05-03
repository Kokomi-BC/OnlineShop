package GUI;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
import src.User;
import src.UserJDBC;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Login extends JFrame {

    public static void main(String[] args) {
        FlatMTMaterialLighterIJTheme.setup();
        Login loginFrame = new Login();
    }
    public Login() {
        initComponents();
        login.setVisible(true);
    }

    private void onLoginSuccess() {
        System.out.println("登录成功，跳转到主界面");
        // 示例：打开新窗口或关闭当前窗口
    }

    private void loginin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(Login.this, "用户名或密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        User user = UserJDBC.getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            JOptionPane.showMessageDialog(Login.this, "登录成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            onLoginSuccess();
        } else {
            JOptionPane.showMessageDialog(Login.this, "用户名或密码错误", "错误", JOptionPane.ERROR_MESSAGE);
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
        hSpacer1 = new JPanel(null);
        hSpacer2 = new JPanel(null);
        button1 = new JButton();
        button2 = new JButton();

        //======== login ========
        {
            login.setMinimumSize(new Dimension(440, 300));
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
            label1.setBounds(225, 15, 125, 55);

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
            loginContentPane.add(hSpacer1);
            hSpacer1.setBounds(0, 30, 225, hSpacer1.getPreferredSize().height);
            loginContentPane.add(hSpacer2);
            hSpacer2.setBounds(345, 35, 225, 10);

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
    private JPanel hSpacer1;
    private JPanel hSpacer2;
    public JButton button1;
    private JButton button2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}