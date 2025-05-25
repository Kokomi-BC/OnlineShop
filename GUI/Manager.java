package GUI;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
import org.mindrot.jbcrypt.BCrypt;
import src.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;


import static src.CommodityJDBC.getCommodityskuById;
import static src.CommodityJDBC.updateSkuStock;

public class Manager extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private static final String PRODUCT_MANAGE = "ProductManagement"; // 商品管理卡片标识
    private static final String ORDER_MANAGE = "OrderManagement";     // 订单管理卡片标识
    private static final String USER_MANAGE = "UserManagement";       // 用户管理卡片标识
    private static User currentUser;
    public Manager(User user) {
        currentUser = user;
        setTitle("管理系统");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.add(createProductManagementPanel(), PRODUCT_MANAGE);  // 商品管理
        mainPanel.add(createOrderManagementPanel(), ORDER_MANAGE);      // 订单管理
        mainPanel.add(createUserManagementPanel(), USER_MANAGE);        // 用户管理
        JPanel navigationPanel = createNavigationPanel();
        setLayout(new BorderLayout());
        add(navigationPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        String[] buttons = {"商品管理", "订单管理", "用户管理"};

        for (String text : buttons) {
            JButton btn = getJButton(text);
            buttonPanel.add(btn);
        }

        navPanel.add(buttonPanel, BorderLayout.CENTER);
        return navPanel;
    }

    private JButton getJButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btn.addActionListener(_ -> {
            switch (text) {
                case "商品管理":
                    cardLayout.show(mainPanel, PRODUCT_MANAGE);
                    break;
                case "订单管理":
                    cardLayout.show(mainPanel, ORDER_MANAGE);
                    break;
                case "用户管理":
                    cardLayout.show(mainPanel, USER_MANAGE);
                    break;
            }
        });
        return btn;
    }

    // ======================= 商品管理面板 =======================
    private JPanel productItemsPanel;
    private JTextField searchField;
    private Timer searchTimer;
    private JLabel searchStatusLabel;
    private JPanel createProductManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JPanel titleSearchPanel = new JPanel(new BorderLayout(10, 0));
        titleSearchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(0, 0, 5, 0)
        ));
        JLabel titleLabel = new JLabel("首页");
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        titleSearchPanel.add(titleLabel, BorderLayout.WEST);
        JPanel searchBoxPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "键入以开始搜索...");
        searchField.putClientProperty("JTextField.showClearButton", true);  // 显示清空按钮
        searchField.putClientProperty("JComponent.roundRect", true);  // 圆角样式
        searchBoxPanel.add(searchField, BorderLayout.CENTER);
        JButton searchIcon = new JButton(UIManager.getIcon("Component.searchIcon"));
        searchIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchIcon.setBorder(BorderFactory.createEmptyBorder());
        searchBoxPanel.add(searchIcon, BorderLayout.EAST);
        titleSearchPanel.add(searchBoxPanel, BorderLayout.CENTER);
        topPanel.add(titleSearchPanel, BorderLayout.NORTH);
        searchStatusLabel = new JLabel("", SwingConstants.CENTER);
        searchStatusLabel.setFont(UIManager.getFont("defaultFont"));
        searchStatusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        topPanel.add(searchStatusLabel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        productItemsPanel = new JPanel();
        productItemsPanel.setLayout(new BoxLayout(productItemsPanel, BoxLayout.Y_AXIS));
        productItemsPanel.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(productItemsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.putClientProperty("JScrollPane.smoothScrolling", true);  // 启用平滑滚动
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("+");
        addButton.setFont(new Font("Arial", Font.BOLD, 20));
        addButton.setToolTipText("新建商品");
        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));  // 添加手型光标
        bottomPanel.add(addButton);
        addButton.addActionListener(_ -> {
            showAddProductDialog(); // 调用新建商品对话框的方法
        });
        bottomPanel.add(addButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        setupSearchListener();
        performSearch("");
        return mainPanel;
    }

    private void showAddProductDialog() {
        JDialog dialog = new JDialog(this, "新建商品", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JTextField nameField = new JTextField();
        addFormRow(formPanel, "商品名称：", nameField);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"日用品","平板","手机" ,"电脑","数码产品", "服装", "药品","家具"});
        typeCombo.setEditable(true);
        addFormRow(formPanel, "商品类型：", typeCombo);
        JPanel datePanel = new JPanel(new BorderLayout());
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new Date()); // 默认当前日期
        datePanel.add(dateSpinner);
        addFormRow(formPanel, "发布日期：", datePanel);
        JTextField manufacturerField = new JTextField();
        addFormRow(formPanel, "生产厂家：", manufacturerField);
        JTextField originField = new JTextField();
        addFormRow(formPanel, "商品产地：", originField);
        JTextArea detailArea = new JTextArea(3, 20);
        JScrollPane detailScroll = new JScrollPane(detailArea);
        addFormRow(formPanel, "商品详情：", detailScroll);
        JTextField remarkField = new JTextField();
        addFormRow(formPanel, "备注：", remarkField);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton confirmButton = new JButton("确认");
        JButton cancelButton = new JButton("取消");
        confirmButton.addActionListener(_ -> handleFormSubmission(
                dialog, nameField, typeCombo, dateSpinner,
                manufacturerField, originField, detailArea, remarkField
        ));
        cancelButton.addActionListener(_ -> dialog.dispose());
        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }




    private void addFormRow(JPanel panel, String label, Component field) {
        JLabel jlabel = new JLabel(label);
        jlabel.setFont(jlabel.getFont().deriveFont(Font.BOLD));
        panel.add(jlabel);
        panel.add(field);
    }
    private void handleFormSubmission(JDialog dialog,
                                      JTextField nameField,
                                      JComboBox<String> typeCombo,
                                      JSpinner dateSpinner,
                                      JTextField manufacturerField,
                                      JTextField originField,
                                      JTextArea detailArea,
                                      JTextField remarkField) {
        Commodity commodity = new Commodity();
        commodity.setName(nameField.getText().trim());
        commodity.setType((String)typeCombo.getSelectedItem());
        commodity.setProductionDate((Date)dateSpinner.getValue());
        commodity.setManufacturer(manufacturerField.getText().trim());
        commodity.setOrigin(originField.getText().trim());
        commodity.setDetail(detailArea.getText().trim());
        commodity.setRemark(remarkField.getText().trim());
        if (commodity.getName().isEmpty()) {
            showError("商品名称不能为空");
            return;
        }
        int id=CommodityJDBC.addCommodity(commodity);
        if (id>=0) {
            refreshProductItems(CommodityJDBC.searchCommodities(""));
            dialog.dispose();
            commodity.setId(id);
            showCommodityDetails(commodity.getId());
        } else {
            showError("保存商品信息失败");
        }
    }

    private void setupSearchListener() {
        searchTimer = new Timer(500, _ -> {
            String keyword = searchField.getText().trim();
            performSearch(keyword);
        });
        searchTimer.setRepeats(false);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { triggerSearch(); }
            @Override
            public void removeUpdate(DocumentEvent e) { triggerSearch(); }
            @Override
            public void changedUpdate(DocumentEvent e) { triggerSearch(); }
            private void triggerSearch() {
                searchTimer.restart(); // 重置计时器以延迟搜索
            }
        });
    }
    private void performSearch(String keyword) {
        showSearchStatus("搜索中...", new Color(153, 153, 153));
        new SwingWorker<List<Commodity>, Void>() {
            @Override
            protected List<Commodity> doInBackground() {
                return CommodityJDBC.searchCommodities(keyword);
            }
            @Override
            protected void done() {
                try {
                    List<Commodity> commodities = get();
                    if (commodities.isEmpty()) {
                        showSearchStatus("未找到名称包含 \"" + keyword + "\" 的商品", new Color(15, 152, 197));
                    } else {
                        searchStatusLabel.setText(""); // 清空状态提示
                    }
                    refreshProductItems(commodities);
                } catch (Exception ex) {
                    showSearchStatus("搜索失败，请稍后重试", new Color(186, 60, 56));
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void showSearchStatus(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            searchStatusLabel.setText(text);
            searchStatusLabel.setForeground(color);
        });
    }
    private JPanel createEmptyPanel() {
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
        String message = "<html><div style='text-align:center;'>"
                + "<p style='font-size:16px; color:#666; margin-bottom:8px;'>没有找到相关商品</p>"
                + "<p style='font-size:13px; color:#999;'>尝试其他关键词或浏览其他分类</p></div></html>";
        emptyPanel.add(new JLabel(message, SwingConstants.CENTER), BorderLayout.CENTER);
        return emptyPanel;
    }

    private void refreshProductItems(List<Commodity> commodities) {
        productItemsPanel.removeAll();
        if (commodities.isEmpty()) {
            JPanel emptyPanel = createEmptyPanel();
            productItemsPanel.add(emptyPanel);
        } else {
            commodities.forEach(commodity -> {
                JPanel itemPanel = createItemPanel(commodity);
                productItemsPanel.add(itemPanel);
                productItemsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            });
        }
        productItemsPanel.revalidate();
        productItemsPanel.repaint();
    }

    private JPanel createItemPanel(Commodity commodity) {
        final Color baseBg = new Color(255, 255, 255);
        final Color hoverBg = new Color(220, 247, 255);
        final Color pressedBg = new Color (147, 225, 255);
        class CustomPanel extends JPanel {
            private boolean isHovered = false;
            private boolean isPressed = false;

            public CustomPanel() {
                super(new BorderLayout(10, 5));
            }

            public void setHovered(boolean hovered) {
                this.isHovered = hovered;
            }

            public void setPressed(boolean pressed) {
                this.isPressed = pressed;
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0, 0, 0, 0));
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    Color currentBg = isPressed ? pressedBg :
                            isHovered ? hoverBg : baseBg;
                    g2.setColor(currentBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                } finally {
                    g2.dispose();
                }
            }
        }
        CustomPanel panel = new CustomPanel();
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                panel.setHovered(true);
                panel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setCursor(Cursor.getDefaultCursor());
                panel.setHovered(false);
                panel.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                panel.setPressed(true);
                panel.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                panel.setPressed(false);
                panel.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showCommodityDetails(commodity.getId());
            }
        });
        panel.setOpaque(true);
        panel.setBorder(createItemBorder(UIManager.getColor("Component.borderColor")));
        panel.setBackground(UIManager.getColor("Panel.background"));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        Color secondaryColor = getSafeColor();
        String productionDate = (commodity.getProductionDate() != null)
                ? new SimpleDateFormat("yyyy-MM-dd").format(commodity.getProductionDate())
                : "未知";
        String details = String.format("<html><div style='margin-top:3px; color:%s;'>类型：%s | 生产商：%s<br>发布日期：%s | 产地：%s</div></html>",
                colorToHex(secondaryColor), commodity.getType(), commodity.getManufacturer(),
                productionDate, commodity.getOrigin());
        JLabel nameLabel = new JLabel(commodity.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setOpaque(false);
        leftPanel.add(nameLabel, BorderLayout.NORTH);
        leftPanel.add(new JLabel(details), BorderLayout.CENTER);
        panel.add(leftPanel);
        JPanel buttonPanel = createActionButtons(commodity);
        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
    }
    private JPanel createActionButtons(Commodity commodity) {
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        buttonPanel.setOpaque(false);
        JButton editButton = createFlatButton("修改", _ -> showEditDialog(commodity));
        JButton deleteButton = createFlatButton("删除", _ -> confirmAndDelete(commodity));
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        return buttonPanel;
    }
    private Border createItemBorder(Color color) {
        return BorderFactory.createCompoundBorder(
                new Mainview.RoundedBorder(color, 8),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        );
    }
    private JButton createFlatButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setContentAreaFilled(true);
        button.setForeground(Color.BLACK);
        if ("删除".equals(text)) {
            button.setBackground(Color.decode("#FEF6F6"));
            button.putClientProperty("JButton.hoverBackground", Color.decode("#EFC3CA").darker());
        } else {
            button.setBackground(Color.decode("#F4FEFF"));
            button.putClientProperty("JButton.hoverBackground", Color.decode("#FFECA1").darker());
        }

        button.setFocusable(false);
        button.addActionListener(listener);
        return button;
    }
    private void confirmAndDelete(Commodity commodity) {
        int result = JOptionPane.showConfirmDialog(
                this,
                "确定要删除商品 [" + commodity.getName() + "] 吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            if (CommodityJDBC.deleteCommodity(commodity.getId())) {
                refreshProductItems(CommodityJDBC.searchCommodities(""));
            } else {
                showError("删除商品失败");
            }
        }
    }

    private void showEditDialog(Commodity original) {
        JDialog dialog = new JDialog(this, "编辑商品", true);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                showCommodityDetails(original.getId());
            }
        });
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JTextField nameField = new JTextField();
        addFormRow(formPanel, "商品名称：", nameField);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"日用品","平板","手机" ,"电脑","数码产品", "服装", "药品","家具"});
        typeCombo.setEditable(true);
        addFormRow(formPanel, "商品类型：", typeCombo);
        JPanel datePanel = new JPanel(new BorderLayout());
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new Date());
        datePanel.add(dateSpinner);
        addFormRow(formPanel, "发布日期：", datePanel);
        JTextField manufacturerField = new JTextField();
        addFormRow(formPanel, "生产厂家：", manufacturerField);
        JTextField originField = new JTextField();
        addFormRow(formPanel, "商品产地：", originField);
        JTextArea detailArea = new JTextArea(3, 20);
        JTextField detailScroll = new JTextField();
        addFormRow(formPanel, "商品详情：", detailScroll);
        JTextField remarkField = new JTextField();
        addFormRow(formPanel, "备注：", remarkField);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton confirmButton =new JButton("确认");
        JButton cancelButton = new JButton("取消");
        nameField.setText(original.getName());
        typeCombo.setSelectedItem(original.getType());
        dateSpinner.setValue(original.getProductionDate());
        manufacturerField.setText(original.getManufacturer());
        originField.setText(original.getOrigin());
        detailArea.setText(original.getDetail());
        remarkField.setText(original.getRemark());
        cancelButton.addActionListener(_ -> dialog.dispose());
        confirmButton.addActionListener(_ -> {
            Commodity updated = buildCommodityFromFields(
                    original.getId(), // 必须使用原始ID
                    nameField,
                    typeCombo,
                    dateSpinner,
                    manufacturerField,
                    originField,
                    detailArea,
                    remarkField
            );
            if (updated == null) return;
            if (CommodityJDBC.updateCommodity(original.getId(), updated)) {
                refreshProductItems(CommodityJDBC.searchCommodities(""));
                dialog.dispose();
            } else {
                showError("更新失败，请检查商品是否存在");
            }
        });
        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private Commodity buildCommodityFromFields(int id, JTextField nameField, JComboBox<String> typeCombo,
                                               JSpinner dateSpinner, JTextField manufacturerField,
                                               JTextField originField, JTextArea detailArea,
                                               JTextField remarkField) {
        if (nameField.getText().trim().isEmpty()) {
            showError("商品名称不能为空");
            nameField.requestFocus();
            return null;
        }
        Commodity commodity = new Commodity();
        commodity.setId(id);
        commodity.setName(nameField.getText().trim());
        commodity.setType(typeCombo.getEditor().getItem().toString().trim());
        commodity.setProductionDate((Date)dateSpinner.getValue());
        commodity.setManufacturer(manufacturerField.getText().trim());
        commodity.setOrigin(originField.getText().trim());
        commodity.setDetail(detailArea.getText().trim());
        commodity.setRemark(remarkField.getText().trim());
        return commodity;
    }

    private static String colorToHex(Color color) {
        if (color == null) {
            return "#000000";
        }
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    private Color getSafeColor() {
        Color color = UIManager.getColor("Component.secondaryForeground");
        if (color == null) {
            return UIManager.getColor("Label.foreground"); // 使用基础前景色作为后备
        }
        return color;
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        panel.add(new JLabel(label));
        panel.add(new JLabel(value != null ? value : "无"));
    }

    private void showCommodityDetails(int commodityId) {
        if (commodityId == -1) {
            JOptionPane.showMessageDialog(this, "商品不存在", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Commodity commodity = CommodityJDBC.getCommodityById(commodityId);
        if (commodity == null) {
            JOptionPane.showMessageDialog(this, "无法加载商品详情", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JDialog dialog = new JDialog(this, "商品详情", true);
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JPanel infoPanel = createInfoPanel(commodity);
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        SkuTableModel tableModel = new SkuTableModel();
        JTable skuTable = new JTable(tableModel);
        skuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshSkuTable(tableModel, commodityId);
        mainPanel.add(new JScrollPane(skuTable), BorderLayout.CENTER);
        JPanel buttonPanel = createButtonPanel(dialog, skuTable, commodityId, tableModel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createInfoPanel(Commodity commodity) {
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        addInfoRow(infoPanel, "商品名称：", commodity.getName());
        addInfoRow(infoPanel, "商品类型：", commodity.getType());
        addInfoRow(infoPanel, "生产日期：", String.valueOf(commodity.getProductionDate()));
        addInfoRow(infoPanel, "制造商：", commodity.getManufacturer());

        JTextArea detailArea = new JTextArea(commodity.getDetail());
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        return infoPanel;
    }

    private JPanel createButtonPanel(JDialog parent, JTable skuTable, int commodityId, SkuTableModel tableModel) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnAdd = new JButton("添加");
        JButton btnEdit = new JButton("修改");
        JButton btnDelete = new JButton("删除");
        JButton btnCancel = new JButton("确定");
        btnAdd.addActionListener(_ -> handleAddSku(commodityId, parent, tableModel));
        btnEdit.addActionListener(_ -> {
            int selectedRow = skuTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(parent, "请先选择一个SKU", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CommoditySKU selectedSku = tableModel.getSkuAt(selectedRow);
            handleEditSku(selectedSku, parent, tableModel);
        });
        btnDelete.addActionListener(_ -> {
            int selectedRow = skuTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(parent, "请先选择一个SKU", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CommoditySKU sku = tableModel.getSkuAt(selectedRow);
            if (CommodityJDBC.deleteSKU(sku.getSkuId())) {
                refreshSkuTable(tableModel, commodityId);
            }
        });
        btnCancel.addActionListener(_ -> parent.dispose());

        panel.add(btnAdd);
        panel.add(btnEdit);
        panel.add(btnDelete);
        panel.add(btnCancel);
        return panel;
    }
    private static class SkuTableModel extends AbstractTableModel {
        private final String[] COLUMNS = {"SKU ID", "颜色", "款式", "价格", "库存"};
        private List<CommoditySKU> skus = new ArrayList<>();
        public void setSkus(List<CommoditySKU> skus) {
            this.skus = skus;
            fireTableDataChanged();
        }
        public CommoditySKU getSkuAt(int row) {
            return skus.get(row);
        }
        @Override
        public int getRowCount() { return skus.size(); }
        @Override
        public int getColumnCount() { return COLUMNS.length; }
        @Override
        public Object getValueAt(int row, int column) {
            CommoditySKU sku = skus.get(row);
            return switch (column) {
                case 0 -> sku.getSkuId();
                case 1 -> sku.getColor();
                case 2 -> sku.getStyle();
                case 3 -> sku.getPrice();
                case 4 -> sku.getStock();
                default -> null;
            };
        }
        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }
    }

    private void refreshSkuTable(SkuTableModel tableModel, int commodityId) {
        List<CommoditySKU> skus = getCommodityskuById(commodityId);
        tableModel.setSkus(skus);
    }
    private void handleAddSku(int commodityId, JDialog parent, SkuTableModel tableModel) {
        JDialog addDialog = new JDialog(parent, "添加SKU", true);
        JPanel panel = createSkuFormPanel(null);

        JButton btnSubmit = new JButton("提交");
        btnSubmit.addActionListener(_ -> {
            CommoditySKU newSku = parseSkuFromForm(panel);
            if (newSku != null && CommodityJDBC.addSKU(commodityId, newSku)) {
                refreshSkuTable(tableModel, commodityId);
                addDialog.dispose();
            }
        });

        panel.add(btnSubmit);
        addDialog.setContentPane(panel);
        addDialog.pack();
        addDialog.setLocationRelativeTo(parent);
        addDialog.setVisible(true);
    }
    private void handleEditSku(CommoditySKU sku, JDialog parent, SkuTableModel tableModel) {
        JDialog editDialog = new JDialog(parent, "修改SKU", true);
        JPanel panel = createSkuFormPanel(sku);
        JButton btnSave = new JButton("保存");
        btnSave.addActionListener(_ -> {
            CommoditySKU updatedSku = parseSkuFromForm(panel);
            if (updatedSku != null && CommodityJDBC.updateSKU(sku.getSkuId(), updatedSku)) {
                refreshSkuTable(tableModel, sku.getCommodityid());
                editDialog.dispose();
            }
        });

        panel.add(btnSave);
        editDialog.setContentPane(panel);
        editDialog.pack();
        editDialog.setLocationRelativeTo(parent);
        editDialog.setVisible(true);
    }
    private JPanel createSkuFormPanel(CommoditySKU sku) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);  // 统一间距
        gbc.anchor = GridBagConstraints.WEST;
        JTextField colorField = createStyledTextField(sku != null ? sku.getColor() : "");
        JTextField styleField = createStyledTextField(sku != null ? sku.getStyle() : "");
        JFormattedTextField priceField = createNumberField(sku != null ? sku.getPrice() : 0.0);
        JFormattedTextField stockField = createNumberField(sku != null ? sku.getStock() : 0);
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("颜色："), gbc);
        gbc.gridx = 1;
        panel.add(colorField, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("款式："), gbc);
        gbc.gridx = 1;
        panel.add(styleField, gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("价格："), gbc);
        gbc.gridx = 1;
        panel.add(createCurrencyField(priceField), gbc);
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("库存："), gbc);
        gbc.gridx = 1;
        panel.add(createUnitField(stockField), gbc);
        panel.putClientProperty("colorField", colorField);
        panel.putClientProperty("styleField", styleField);
        panel.putClientProperty("priceField", priceField);
        panel.putClientProperty("stockField", stockField);

        return panel;
    }

    private JTextField createStyledTextField(String text) {
        JTextField tf = new JTextField(text);
        tf.setColumns(18);
        return tf;
    }

    private JFormattedTextField createNumberField(Number value) {
        NumberFormat format = NumberFormat.getNumberInstance();
        JFormattedTextField tf = new JFormattedTextField(format);
        tf.setValue(value);
        tf.setHorizontalAlignment(JTextField.RIGHT);
        tf.setColumns(15);
        return tf;
    }

    private JComponent createCurrencyField(JFormattedTextField field) {
        JPanel wrapper = new JPanel(new BorderLayout(5, 0));
        wrapper.add(new JLabel("￥"), BorderLayout.WEST);
        wrapper.add(field, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent createUnitField(JFormattedTextField field) {
        JPanel wrapper = new JPanel(new BorderLayout(5, 0));
        wrapper.add(field, BorderLayout.CENTER);
        wrapper.add(new JLabel("件"), BorderLayout.EAST);
        return wrapper;
    }

    private CommoditySKU parseSkuFromForm(JPanel panel) {
        try {
            CommoditySKU sku = new CommoditySKU();
            JTextField colorField = (JTextField) panel.getClientProperty("colorField");
            JTextField styleField = (JTextField) panel.getClientProperty("styleField");
            JFormattedTextField priceField = (JFormattedTextField) panel.getClientProperty("priceField");
            JFormattedTextField stockField = (JFormattedTextField) panel.getClientProperty("stockField");
            sku.setColor(colorField.getText().trim());
            sku.setStyle(styleField.getText().trim());
            sku.setPrice(((Number)priceField.getValue()).doubleValue());
            sku.setStock(((Number)stockField.getValue()).intValue());
            if (sku.getColor().isEmpty()) sku.setColor(null);
            if (sku.getStyle().isEmpty()) sku.setStyle(null);
            if (sku.getPrice().doubleValue() <= 0 || sku.getStock() < 0) {
                throw new IllegalArgumentException("价格必须大于0，库存不能为负数");
            }

            return sku;
        } catch (NullPointerException e) {
            showError("请填写所有必填字段");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("输入格式错误：" + e.getMessage());
        }
        return null;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "输入错误", JOptionPane.ERROR_MESSAGE);
    }

    // ======================= 订单管理面板 =======================
    private JPanel createOrderManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        String[] columns = {"订单ID","明细id" ,"商品名称","颜色","款式", "数量", "金额", "状态", "创建时间","支付时间","发货时间","完成时间","订单备注"};
        model.setColumnIdentifiers(columns);
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setAutoCreateRowSorter(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int row = table.rowAtPoint(e.getPoint());
                    int detailId = (int) table.getValueAt(row, 1);
                    showOrderDetailDialog(detailId,model);
                }
            }
        });
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(_ -> refreshOrderTable(model));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        refreshOrderTable(model);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        return mainPanel;
    }

    private void refreshOrderTable(DefaultTableModel model) {
        model.setRowCount(0);
        List<Orderinfo> orders = OrderJBDC.getAllOrderinfos();
        for (Orderinfo o : orders) {
            model.addRow(new Object[]{
                    o.getOrderid(),
                    o.getDetailid(),
                    o.getCommodityName() != null ? o.getCommodityName() : "",
                    o.getColor() != null ? o.getColor() : "",
                    o.getStyle() != null ? o.getStyle() : "",
                    o.getQuantity(),
                    o.getAmount(),
                    o.getStatus() != null ? o.getStatus() : "",
                    o.getCreatedTime() != null ? o.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "",
                    o.getPayment_time() != null ? o.getPayment_time().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "",
                    o.getShipped_time() != null ? o.getShipped_time().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "",
                    o.getCompletedTime() != null ? o.getCompletedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "",
                    o.getRemark()
            });
        }

    }

    private void showOrderDetailDialog(int detailId, DefaultTableModel model) {
        Orderinfo order = OrderJBDC.getInfoByDetailId(detailId);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(createOrderManagementPanel()), true);
        dialog.setTitle("订单详情 - " + detailId);
        dialog.setSize(550, 550); // 调整对话框尺寸
        dialog.setResizable(false);
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel();
        GroupLayout layout = new GroupLayout(formPanel);
        formPanel.setLayout(layout);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel statusLabel = new JLabel("订单状态:");
        JLabel paymentLabel = new JLabel("支付方式:");
        JLabel addressLabel = new JLabel("收货地址:");
        JLabel remarkLabel = new JLabel("备注信息:");
        JLabel currentSkuLabel = new JLabel("当前SKU:");
        JLabel changeSkuLabel = new JLabel("更换SKU:");
        JLabel currentSkuValue = new JLabel(order.getColor() + " - " + order.getStyle());
        List<CommoditySKU> availableSkus = getCommodityskuById(order.getCommodityid())
                .stream()
                .filter(sku -> sku.getStock() > 0)
                .toList();

        JComboBox<CommoditySKU> skuCombo = new JComboBox<>(new Vector<>(availableSkus));
        skuCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CommoditySKU sku) {
                    setText(String.format("%s - %s (库存: %d | 价格: ¥%.2f)",
                            sku.getColor(), sku.getStyle(), sku.getStock(), sku.getPrice()));
                }
                return this;
            }
        });
        for (int i = 0; i < skuCombo.getItemCount(); i++) {
            if (skuCombo.getItemAt(i).getSkuId() == order.getSkuid()) {
                skuCombo.setSelectedIndex(i);
                break;
            }
        }
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"待支付", "已支付", "已发货", "已完成", "已取消"});
        statusCombo.setSelectedItem(order.getStatus());
        JTextField paymentField = new JTextField(order.getPaymentMethod(), 20);
        JTextArea addressArea = new JTextArea(order.getShippingAddress(), 3, 20);
        addressArea.setLineWrap(true);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        JTextArea remarkArea = new JTextArea(order.getRemark(), 3, 20);
        remarkArea.setLineWrap(true);
        JScrollPane remarkScroll = new JScrollPane(remarkArea);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(currentSkuLabel)
                                        .addComponent(changeSkuLabel)
                                        .addComponent(statusLabel)
                                        .addComponent(paymentLabel)
                                        .addComponent(addressLabel)
                                        .addComponent(remarkLabel))
                                .addGap(30)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(currentSkuValue)
                                        .addComponent(skuCombo)
                                        .addComponent(statusCombo)
                                        .addComponent(paymentField)
                                        .addComponent(addressScroll)
                                        .addComponent(remarkScroll))
                        )
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(currentSkuLabel)
                                .addComponent(currentSkuValue))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(changeSkuLabel)
                                .addComponent(skuCombo))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(statusLabel)
                                .addComponent(statusCombo))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(paymentLabel)
                                .addComponent(paymentField))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(addressLabel)
                                .addComponent(addressScroll))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(remarkLabel)
                                .addComponent(remarkScroll))
        );
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveBtn = new JButton("保存修改");
        JButton cancelBtn = new JButton("取消");
        saveBtn.addActionListener(_ -> {
            if (paymentField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "支付方式不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            CommoditySKU selectedSku = (CommoditySKU) skuCombo.getSelectedItem();
            if (selectedSku.getStock() < order.getQuantity()) {
                JOptionPane.showMessageDialog(dialog,
                        String.format("库存不足！当前库存：%d，订单需要：%d",
                                selectedSku.getStock(), order.getQuantity()),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            order.setSkuid(selectedSku.getSkuId());
            order.setColor(selectedSku.getColor());
            order.setStyle(selectedSku.getStyle());
            order.setAmount(selectedSku.getPrice().doubleValue());
            order.setStatus((String) statusCombo.getSelectedItem());
            order.setPaymentMethod(paymentField.getText().trim());
            order.setShippingAddress(addressArea.getText().trim());
            order.setRemark(remarkArea.getText().trim());
            try {
                boolean success = OrderJBDC.updateOrderInfo(order);
                if (success) {
                    updateSkuStock(order.getSkuid(), +order.getQuantity());
                    updateSkuStock(selectedSku.getSkuId(), -order.getQuantity());
                    JOptionPane.showMessageDialog(dialog, "更新成功！");
                    refreshOrderTable(model);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "更新失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "数据库错误：" + ex.getMessage(),
                        "严重错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(_ -> dialog.dispose());
        saveBtn.setPreferredSize(new Dimension(100, 30));
        cancelBtn.setPreferredSize(new Dimension(100, 30));
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    // ======================= 用户管理面板 =======================
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        String[] columnNames = {"ID", "用户名", "电话", "地址", "余额", "权限", "备注"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xDDDDDD)),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)
        ));

        JButton refreshBtn = new JButton("刷新");
        JButton addBtn = new JButton("新增用户");
        JButton editBtn = new JButton("编辑用户");
        JButton deleteBtn = new JButton("删除用户");
        JButton jumpBtn = new JButton("切换至购物界面");
        JButton exitBtn = new JButton("退出登录");
        JButton adminBtn = new JButton("设管理员");
        JButton adminrev = new JButton("撤销权限");
        refreshBtn.addActionListener(_ -> refreshUserTable(model));
        addBtn.addActionListener(_ -> showUserEditDialog(null,model));
        editBtn.addActionListener(_ -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int userId = (int) table.getValueAt(row, 0);
                User user = UserJDBC.getUserById(userId);
                showUserEditDialog(user,model);
            }
        });
        deleteBtn.addActionListener(_ -> deleteSelectedUser(table, model));
        jumpBtn.addActionListener(_ ->{
            dispose();
            Mainview mainview = new Mainview(currentUser);
            mainview.setVisible(true);
        });
        exitBtn.addActionListener(_ ->{
            dispose();
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
            Login loginFrame = new Login();
        });
        adminBtn.addActionListener(_ -> setAdminPermission(table, model));
        adminrev.addActionListener(_ ->  revokeAdminPermission(table, model));
        toolBar.add(refreshBtn);
        toolBar.add(Box.createHorizontalStrut(16));
        toolBar.addSeparator();
        toolBar.add(addBtn);
        toolBar.add(Box.createHorizontalStrut(8));
        toolBar.add(editBtn);
        toolBar.add(Box.createHorizontalStrut(8));
        toolBar.add(deleteBtn);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(adminBtn);
        toolBar.add(Box.createHorizontalStrut(8));
        toolBar.add(adminrev);
        toolBar.add(Box.createHorizontalStrut(8));
        toolBar.add(jumpBtn);
        toolBar.add(Box.createHorizontalStrut(8));
        toolBar.add(exitBtn);
        refreshUserTable(model);
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    private void refreshUserTable(DefaultTableModel model) {
        model.setRowCount(0);
        List<User> users = UserJDBC.getAllUsers();
        for (User user : users) {
            model.addRow(new Object[]{
                    user.getId(),
                    user.getUsername(),
                    user.getPhone(),
                    user.getAddress(),
                    user.getBalance().setScale(2, RoundingMode.HALF_UP),
                    user.getPermission(),
                    user.getRemark()
            });
        }
    }
    private static final String PHONE_REGEX = "^((\\+86)|(0086))?1[3-9]\\d{9}$";
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-zA-Z])[0-9A-Za-z]{6,16}$";
    private void showUserEditDialog(User user, DefaultTableModel model) {
        JDialog dialog = new JDialog(this, user == null ? "新增用户" : "编辑用户", true);
        dialog.setLayout(new BorderLayout(15, 15));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("用户信息"),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField phoneField = new JTextField(20);
        JTextField addressField = new JTextField(20);
        JTextField balanceField = new JTextField(20);
        JTextField remarkField = new JTextField(20);
        if (user != null) {
            usernameField.setText(user.getUsername());
            phoneField.setText(user.getPhone());
            addressField.setText(user.getAddress());
            balanceField.setText(user.getBalance().toString());
            remarkField.setText(user.getRemark());
        }
        addFormRow(formPanel, gbc, 0, "用户名：", true, usernameField);
        addFormRow(formPanel, gbc, 1, user == null ? "密码：" : "新密码：", user == null, passwordField);
        addFormRow(formPanel, gbc, 2, "电话：", true, phoneField);
        addFormRow(formPanel, gbc, 3, "地址：", true, addressField);
        addFormRow(formPanel, gbc, 4, "余额：", true, balanceField);
        addFormRow(formPanel, gbc, 5, "备注：", false, remarkField);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton confirmBtn = new JButton("确认");
        JButton cancelBtn = new JButton("取消");

        confirmBtn.addActionListener(_ -> {
            if (!validateField(usernameField, "用户名不能为空")) return;
            if (user == null && !validateField(passwordField, "密码不能为空")) return;
            if (!validateField(phoneField, "电话不能为空")) return;
            if (!validateField(addressField, "地址不能为空")) return;
            if (!validateNumberField(balanceField, "余额格式错误")) return;

            // 格式验证增强
            String password = new String(passwordField.getPassword()).trim();
            String phone = phoneField.getText().trim();
            // 新增用户时密码格式验证
            if (user == null && !validateRegex(password, PASSWORD_REGEX,
                    "密码必须包含数字和字母，长度6-16位")) {
                return;
            }
            // 更新时密码修改验证
            if (user != null && !password.isEmpty() &&
                    !validateRegex(password, PASSWORD_REGEX,
                            "新密码必须包含数字和字母，长度6-16位")) {
                return;
            }
            // 手机号格式验证
            if (!validateRegex(phone, PHONE_REGEX,
                    "请输入有效的手机号（支持+86/0086开头）")) {
                return;
            }
            try {
                String finalPassword = password.isEmpty() ?
                        (user != null ? user.getPassword() : null) :  // 保留原密码
                        BCrypt.hashpw(password, BCrypt.gensalt());   // 加密新密码
                User newUser = new User(
                        user != null ? user.getId() : 0,
                        usernameField.getText().trim(),
                        finalPassword,  // 使用处理后的密码
                        phone,
                        addressField.getText().trim(),
                        new BigDecimal(balanceField.getText().trim()),
                        remarkField.getText().trim(),
                        user != null ? user.getPermission() : "user"
                );
                String result = user == null ?
                        UserJDBC.addUser(newUser) :
                        UserJDBC.updateUser(newUser);
                if (result.endsWith("成功")) {
                    refreshUserTable(model);
                    JOptionPane.showMessageDialog(this, result);
                    dialog.dispose();
                } else {
                    showError(result);
                }
            } catch (NumberFormatException ex) {
                showError("请输入有效的数字格式");
            }
        });
        cancelBtn.addActionListener(_ -> dialog.dispose());
        buttonPanel.add(cancelBtn);
        buttonPanel.add(confirmBtn);
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    private boolean validateRegex(String input, String regex, String errorMessage) {
        if (Pattern.matches(regex, input)) {
            return true;
        }
        showError(errorMessage);
        return false;
    }
    private void addFormRow(JPanel panel, GridBagConstraints gbc, int yPos, String label, boolean required, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = yPos;
        JLabel lbl = new JLabel(required ? label + " *" : label);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        field.putClientProperty("JComponent.required", required);
        panel.add(field, gbc);
    }

    private boolean validateField(JComponent field, String errorMsg) {
        String text = field instanceof JPasswordField ?
                new String(((JPasswordField) field).getPassword()).trim() :
                ((JTextField) field).getText().trim();

        if (text.isEmpty()) {
            showError(errorMsg);
            field.requestFocus();
            return false;
        }
        return true;
    }
    private boolean validateNumberField(JTextField field, String errorMsg) {
        if (!validateField(field, "余额不能为空")) return false;
        try {
            new BigDecimal(field.getText().trim());
            return true;
        } catch (NumberFormatException e) {
            showError(errorMsg);
            field.requestFocus();
            return false;
        }
    }
    private void deleteSelectedUser(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确认删除该用户？", "删除确认", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int userId = (int) table.getValueAt(row, 0);
                if (UserJDBC.deleteUser(userId)) {
                    model.removeRow(row);
                    refreshUserTable(model);
                } else {
                    showError("删除用户失败");
                }
            }
        }
    }
    private void setAdminPermission(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int userId = (int) table.getValueAt(row, 0);
            String result = UserJDBC.setAdminPermission(userId);
            if (result.contains("成功")) {
                refreshUserTable(model);
            }
            JOptionPane.showMessageDialog(this, result);
        }
    }
    private void revokeAdminPermission(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int userId = (int) table.getValueAt(row, 0);
            String result = UserJDBC.revokeAdminPermission(userId);
            if (result.contains("成功")) {
                refreshUserTable(model);
            }
            JOptionPane.showMessageDialog(this, result);
        }
    }
    public static void main() {
        SwingUtilities.invokeLater(() -> {
            FlatMTMaterialLighterIJTheme.setup();
            User user = UserJDBC.getUserByUsername("Kokomi");
            Manager mainview = new Manager(user);
            mainview.setVisible(true);
        });
    }
}