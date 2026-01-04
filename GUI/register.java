/*
 * Created by JFormDesigner on Fri May 02 09:14:49 CST 2025
 */

package GUI;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import org.mindrot.jbcrypt.BCrypt;
import java.util.regex.Pattern;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
import src.User;
import src.UserJDBC;

/**
 * @author Nahida
 */
public class register {
    private void cancel(ActionEvent e) {
        register.dispose();
    }
    private void ok(ActionEvent e) {
        String password1 = new String(passwordField.getPassword());
        String password2 = new String(passwordField2.getPassword());
        String username = usernameField2.getText();
        String phone = PhoneField.getText().replaceAll("\\s+", ""); // 移除手机号中的空格
        String address = textField.getText();
        if (!password1.equals(password2)) {
            JOptionPane.showMessageDialog(register, "两次输入的密码不一致，请重新输入", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (username.isEmpty() || password1.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(register, "用户名、密码和电话不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-zA-Z])[0-9A-Za-z]{6,16}$";
        if (!Pattern.matches(passwordRegex, password1)) {
            JOptionPane.showMessageDialog(register, "密码必须包含数字和字母，且长度为6-16位", "格式错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String phoneRegex = "^((\\+86)|(0086))?1[3-9]\\d{9}$";
        if (!Pattern.matches(phoneRegex, phone)) {
            JOptionPane.showMessageDialog(register, "请输入正确的手机号格式", "格式错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(!isValidCodeRight()) {
            vcode.nextCode();
            jt_code.setText("");
            JOptionPane.showMessageDialog(register, "验证码错误", "注册失败", JOptionPane.ERROR_MESSAGE);
            return;
        }
            String hashedPassword = BCrypt.hashpw(password1, BCrypt.gensalt());
            User user = new User(username, hashedPassword, phone, address, BigDecimal.ZERO, "");
            String result = UserJDBC.addUser(user);
            if (result.startsWith("添加成功")) {
                JOptionPane.showMessageDialog(register, result, "注册成功", JOptionPane.INFORMATION_MESSAGE);
                register.dispose();
            } else {
                JOptionPane.showMessageDialog(register, result, "注册失败", JOptionPane.ERROR_MESSAGE);
            }

    }

    public boolean isValidCodeRight() {
        if(jt_code == null) {
            return false;
        }else if(vcode == null) {
            return true;
        }else return vcode.getCode().equalsIgnoreCase(jt_code.getText());
    }
    void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        register = new JDialog();
        label3 = new JLabel();
        label4 = new JLabel();
        label5 = new JLabel();
        label6 = new JLabel();
        label1 = new JLabel();
        PhoneField = new JFormattedTextField();
        usernameField2 = new JFormattedTextField();
        textField = new JFormattedTextField();
        passwordField = new JPasswordField();
        ok = new JButton();
        cancel = new JButton();
        passwordField2 = new JPasswordField();
        label7 = new JLabel();
        vcode = new ValidCode();
        label8 = new JLabel();
        jt_code = new JFormattedTextField();

        //======== register ========
        {
            register.setMinimumSize(new Dimension(560, 540));
            register.setTitle("\u6ce8\u518c");
            register.setPreferredSize(new Dimension(560, 540));
            register.setMaximumSize(new Dimension(580, 550));
            var registerContentPane = register.getContentPane();
            registerContentPane.setLayout(null);

            //---- label3 ----
            label3.setText("\u8d26\u6237\u540d");
            label3.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 18));
            label3.setHorizontalAlignment(SwingConstants.CENTER);
            registerContentPane.add(label3);
            label3.setBounds(85, 130, 60, 35);

            //---- label4 ----
            label4.setText("\u5bc6\u7801");
            label4.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 19));
            label4.setHorizontalAlignment(SwingConstants.CENTER);
            registerContentPane.add(label4);
            label4.setBounds(95, 180, 50, 35);

            //---- label5 ----
            label5.setText("\u7535\u8bdd");
            label5.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 19));
            label5.setHorizontalAlignment(SwingConstants.CENTER);
            registerContentPane.add(label5);
            label5.setBounds(95, 80, 50, 35);

            //---- label6 ----
            label6.setText("\u6536\u8d27\u5730\u5740\uff08\u53ef\u9009\uff09");
            label6.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 16));
            label6.setHorizontalAlignment(SwingConstants.CENTER);
            registerContentPane.add(label6);
            label6.setBounds(20, 265, 145, 60);

            //---- label1 ----
            label1.setText("\u6ce8\u518c\u4f1a\u5458");
            label1.setBackground(new Color(0x00f8f7fa, true));
            label1.setAutoscrolls(true);
            label1.setHorizontalAlignment(SwingConstants.CENTER);
            label1.setFont(label1.getFont().deriveFont(label1.getFont().getStyle() | Font.BOLD, label1.getFont().getSize() + 12f));
            registerContentPane.add(label1);
            label1.setBounds(10, 10, 535, 55);

            //---- PhoneField ----
            PhoneField.setHorizontalAlignment(SwingConstants.LEFT);
            PhoneField.setForeground(Color.black);
            registerContentPane.add(PhoneField);
            PhoneField.setBounds(155, 80, 260, 35);

            //---- usernameField2 ----
            usernameField2.setHorizontalAlignment(SwingConstants.LEFT);
            usernameField2.setForeground(Color.black);
            registerContentPane.add(usernameField2);
            usernameField2.setBounds(155, 130, 260, 35);

            //---- textField ----
            textField.setHorizontalAlignment(SwingConstants.LEFT);
            textField.setForeground(Color.black);
            registerContentPane.add(textField);
            textField.setBounds(155, 280, 260, 35);

            //---- passwordField ----
            passwordField.setHorizontalAlignment(SwingConstants.LEFT);
            registerContentPane.add(passwordField);
            passwordField.setBounds(155, 180, 260, 35);

            //---- ok ----
            ok.setText("\u786e\u5b9a");
            ok.setForeground(Color.black);
            ok.addActionListener(e -> ok(e));
            registerContentPane.add(ok);
            ok.setBounds(155, 420, 85, 35);

            //---- cancel ----
            cancel.setText("\u53d6\u6d88");
            cancel.setForeground(Color.black);
            cancel.addActionListener(e -> cancel(e));
            registerContentPane.add(cancel);
            cancel.setBounds(320, 420, 85, 35);

            //---- passwordField2 ----
            passwordField2.setHorizontalAlignment(SwingConstants.LEFT);
            registerContentPane.add(passwordField2);
            passwordField2.setBounds(155, 230, 260, 35);

            //---- label7 ----
            label7.setText("\u786e\u8ba4\u5bc6\u7801");
            label7.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 19));
            label7.setHorizontalAlignment(SwingConstants.CENTER);
            registerContentPane.add(label7);
            label7.setBounds(50, 230, 100, 35);
            registerContentPane.add(vcode);
            vcode.setBounds(160, 340, 94, 40);

            //---- label8 ----
            label8.setText("\u9a8c\u8bc1\u7801");
            label8.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 19));
            label8.setHorizontalAlignment(SwingConstants.CENTER);
            registerContentPane.add(label8);
            label8.setBounds(75, 345, 75, 35);

            //---- jt_code ----
            jt_code.setHorizontalAlignment(SwingConstants.LEFT);
            registerContentPane.add(jt_code);
            jt_code.setBounds(265, 340, 150, 40);

            {
                // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < registerContentPane.getComponentCount(); i++) {
                    Rectangle bounds = registerContentPane.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = registerContentPane.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                registerContentPane.setMinimumSize(preferredSize);
                registerContentPane.setPreferredSize(preferredSize);
            }
            register.pack();
            register.setLocationRelativeTo(register.getOwner());
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                FlatMTMaterialLighterIJTheme.setup();
            } catch (Exception e) {
                e.printStackTrace();
            }
            register registerDialog = new register();
            registerDialog.initComponents();
            JDialog dialog = registerDialog.register;
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        });
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    protected JDialog register;
    private JLabel label3;
    private JLabel label4;
    private JLabel label5;
    private JLabel label6;
    private JLabel label1;
    private JFormattedTextField PhoneField;
    private JFormattedTextField usernameField2;
    protected JFormattedTextField textField;
    private JPasswordField passwordField;
    private JButton ok;
    private JButton cancel;
    private JPasswordField passwordField2;
    private JLabel label7;
    private ValidCode vcode;
    private JLabel label8;
    private JFormattedTextField jt_code;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}