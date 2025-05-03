/*
 * Created by JFormDesigner on Fri May 02 09:14:49 CST 2025
 */

package GUI;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;

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
        String phone = PhoneField.getText();
        String address = textField.getText();

        if (!password1.equals(password2)) {
            JOptionPane.showMessageDialog(register, "两次输入的密码不一致，请重新输入", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (username.isEmpty() || password1.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(register, "用户名、密码和电话不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        User user = new User(username, password1, phone, address, BigDecimal.ZERO, "");
        String result = UserJDBC.addUser(user);

        if (result.startsWith("添加成功")) {
            JOptionPane.showMessageDialog(register, result, "成功", JOptionPane.INFORMATION_MESSAGE);
            register.dispose(); // 关闭注册窗口
        } else {
            JOptionPane.showMessageDialog(register, result, "错误", JOptionPane.ERROR_MESSAGE);
        }
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

        //======== register ========
        {
            register.setMinimumSize(new Dimension(540, 460));
            register.setTitle("\u6ce8\u518c");
            register.setPreferredSize(new Dimension(540, 460));
            register.setMaximumSize(new Dimension(580, 550));
            var registerContentPane = register.getContentPane();
            registerContentPane.setLayout(null);

            //---- label3 ----
            label3.setText("\u8d26\u6237\u540d");
            label3.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 18));
            label3.setHorizontalAlignment(SwingConstants.CENTER);
            registerContentPane.add(label3);
            label3.setBounds(75, 130, 60, 35);

            //---- label4 ----
            label4.setText("\u5bc6\u7801");
            label4.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 19));
            label4.setHorizontalAlignment(SwingConstants.CENTER);
            registerContentPane.add(label4);
            label4.setBounds(85, 180, 50, 35);

            //---- label5 ----
            label5.setText("\u7535\u8bdd");
            label5.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 19));
            label5.setHorizontalAlignment(SwingConstants.CENTER);
            registerContentPane.add(label5);
            label5.setBounds(85, 80, 50, 35);

            //---- label6 ----
            label6.setText("\u6536\u8d27\u5730\u5740\uff08\u53ef\u9009\uff09");
            label6.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 16));
            label6.setHorizontalAlignment(SwingConstants.CENTER);
            registerContentPane.add(label6);
            label6.setBounds(10, 270, 145, 60);

            //---- label1 ----
            label1.setText("\u6ce8\u518c\u4f1a\u5458");
            label1.setBackground(new Color(0x00f8f7fa, true));
            label1.setAutoscrolls(true);
            label1.setHorizontalAlignment(SwingConstants.CENTER);
            label1.setFont(label1.getFont().deriveFont(label1.getFont().getStyle() | Font.BOLD, label1.getFont().getSize() + 12f));
            registerContentPane.add(label1);
            label1.setBounds(190, 10, 125, 55);

            //---- PhoneField ----
            PhoneField.setHorizontalAlignment(SwingConstants.LEFT);
            PhoneField.setForeground(Color.black);
            registerContentPane.add(PhoneField);
            PhoneField.setBounds(145, 80, 260, 35);

            //---- usernameField2 ----
            usernameField2.setHorizontalAlignment(SwingConstants.LEFT);
            usernameField2.setForeground(Color.black);
            registerContentPane.add(usernameField2);
            usernameField2.setBounds(145, 130, 260, 35);

            //---- textField ----
            textField.setHorizontalAlignment(SwingConstants.LEFT);
            textField.setForeground(Color.black);
            registerContentPane.add(textField);
            textField.setBounds(145, 285, 260, 35);

            //---- passwordField ----
            passwordField.setHorizontalAlignment(SwingConstants.LEFT);
            registerContentPane.add(passwordField);
            passwordField.setBounds(145, 180, 260, 35);

            //---- ok ----
            ok.setText("\u786e\u5b9a");
            ok.setForeground(Color.black);
            ok.addActionListener(e -> ok(e));
            registerContentPane.add(ok);
            ok.setBounds(new Rectangle(new Point(145, 345), ok.getPreferredSize()));

            //---- cancel ----
            cancel.setText("\u53d6\u6d88");
            cancel.setForeground(Color.black);
            cancel.addActionListener(e -> cancel(e));
            registerContentPane.add(cancel);
            cancel.setBounds(new Rectangle(new Point(295, 345), cancel.getPreferredSize()));

            //---- passwordField2 ----
            passwordField2.setHorizontalAlignment(SwingConstants.LEFT);
            registerContentPane.add(passwordField2);
            passwordField2.setBounds(145, 235, 260, 35);

            //---- label7 ----
            label7.setText("\u786e\u8ba4\u5bc6\u7801");
            label7.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 19));
            label7.setHorizontalAlignment(SwingConstants.CENTER);
            registerContentPane.add(label7);
            label7.setBounds(35, 235, 100, 35);

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
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}