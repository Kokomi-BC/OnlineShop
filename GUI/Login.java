package GUI;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
import com.formdev.flatlaf.util.SystemInfo;
import src.User;
import src.UserJDBC;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.mindrot.jbcrypt.BCrypt;

public class Login extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatMTMaterialLighterIJTheme.setup();
            UIManager.put("Button.arc", 20);
            UIManager.put("Component.arc", 20);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("Component.arrowType", "chevron");
            UIManager.put("TitlePane.unifiedBackground", true);
            UIManager.put("Component.hoverBackground", new Color(0xE3F2FD));
            UIManager.put("Component.pressedBackground", new Color(0xBBDEFB));
            UIManager.put("Component.focusColor", new Color(0x55B7B8));
            UIManager.put("Component.hoverEffect", true);
            UIManager.put("Component.hoverFadeTime", 200);
            if( SystemInfo.isLinux ) {
                JFrame.setDefaultLookAndFeelDecorated( true );
                JDialog.setDefaultLookAndFeelDecorated( true );
            }
            Login loginFrame = new Login();
        });
    }
    public Login() {
        initComponents();
        login.setVisible(true);
        login.setResizable(false);
    }
    private void onLoginSuccess(User user) {
        System.out.println("登录成功，跳转到主界面");
        login.dispose();
        Mainview mainview = new Mainview(user);
        mainview.setVisible(true);
    }
    private void loginin(ActionEvent e) {
        String input = usernameField.getText();
        String inputPassword = new String(passwordField.getPassword());
        if (input.isEmpty() || inputPassword.isEmpty()) {
            JOptionPane.showMessageDialog(Login.this, "用户名或密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!isValidCodeRight()) {
            JOptionPane.showMessageDialog(Login.this, "验证码错误", "错误", JOptionPane.ERROR_MESSAGE);
            vcode.nextCode();
            jt_code.setText("");
            return;
        }

        try {
            User user = UserJDBC.getUserByUsername(input); // 按用户名查询
            User user1 = UserJDBC.getUserByUserphone(input); // 按手机号查询
            boolean isValidUser = (user != null && BCrypt.checkpw(inputPassword, user.getPassword()));
            boolean isValidPhoneUser = (user1 != null && BCrypt.checkpw(inputPassword, user1.getPassword()));
            if (isValidUser) {
                handleLoginSuccess(user);
            } else if (isValidPhoneUser) {
                handleLoginSuccess(user1);
            } else {
                showAuthFailedMessage();
            }
        }
        catch (IllegalArgumentException ex) {
            System.err.println("密码哈希值格式错误: " + ex.getMessage());
            showAuthFailedMessage();
        }
    }

    // 统一处理登录成功的逻辑
    private void handleLoginSuccess(User user) {
        if ("admin".equals(user.getPermission())) {
            onAdminLoginSuccess(user);
        } else {
            JOptionPane.showMessageDialog(Login.this, "登录成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            onLoginSuccess(user);
        }
    }
    // 统一认证失败提示方法
    private void showAuthFailedMessage() {
        vcode.nextCode();
        jt_code.setText("");
        JOptionPane.showMessageDialog(Login.this,
                "用户名或密码错误",
                "认证失败",
                JOptionPane.ERROR_MESSAGE);
    }

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
        dialog.setResizable(false);
        login.setVisible(false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                login.setVisible(true);
            }
        });
    }

    public boolean isValidCodeRight() {
        if(jt_code == null) {
            return false;
        }else if(vcode == null) {
            return true;
        }else return vcode.getCode().equalsIgnoreCase(jt_code.getText());
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
        jt_code = new JFormattedTextField();
        label4 = new JLabel();
        vcode = new ValidCode();

        //======== login ========
        {
            login.setMinimumSize(new Dimension(600, 410));
            login.setMaximumSize(new Dimension(620, 500));
            login.setPreferredSize(new Dimension(600, 420));
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
            label1.setBounds(15, 10, 570, 70);

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
            label3.setBounds(70, 160, 50, 35);

            //---- passwordField ----
            passwordField.setHorizontalAlignment(SwingConstants.LEFT);
            loginContentPane.add(passwordField);
            passwordField.setBounds(155, 160, 305, 35);

            //---- usernameField ----
            usernameField.setHorizontalAlignment(SwingConstants.LEFT);
            loginContentPane.add(usernameField);
            usernameField.setBounds(155, 90, 305, 35);

            //---- button1 ----
            button1.setText("\u767b\u5f55");
            button1.setForeground(new Color(0x333333));
            button1.addActionListener(e -> loginin(e));
            loginContentPane.add(button1);
            button1.setBounds(165, 315, 85, 35);

            //---- button2 ----
            button2.setText("\u6ce8\u518c");
            button2.setForeground(new Color(0x333333));
            button2.addActionListener(e -> register(e));
            loginContentPane.add(button2);
            button2.setBounds(335, 315, 85, 35);

            //---- jt_code ----
            jt_code.setHorizontalAlignment(SwingConstants.LEFT);
            loginContentPane.add(jt_code);
            jt_code.setBounds(280, 230, 180, 35);

            //---- label4 ----
            label4.setText("\u9a8c\u8bc1\u7801");
            label4.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 19));
            label4.setHorizontalAlignment(SwingConstants.CENTER);
            loginContentPane.add(label4);
            label4.setBounds(55, 230, 100, 35);
            loginContentPane.add(vcode);
            vcode.setBounds(160, 225, 95, 40);

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
    private JFormattedTextField jt_code;
    private JLabel label4;
    private ValidCode vcode;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on


}