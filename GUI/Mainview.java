package GUI;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
import src.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import static src.CartJDBC.deleteCartItem;

public class Mainview extends JFrame {
    private JPanel cartPanel; // 定义类级别的变量
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private DefaultListModel<String> productListModel;
    protected JList<String> productList;
    private final User currentUser;
    private static final String PRODUCT_CARD = "ProductList";
    private static final String CART_CARD = "Cart";
    private static final String PROFILE_CARD = "Profile";
    public Mainview(User user) {
        this.currentUser = user; // 保存当前登录用户
        setTitle("购物应用");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.add(createProductListPanel(), PRODUCT_CARD);
        mainPanel.add(createCartPanel(), CART_CARD);
        mainPanel.add(createProfilePanel(), PROFILE_CARD);
        JPanel navigationPanel = createNavigationPanel();
        setLayout(new BorderLayout());
        add(navigationPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }
    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JLabel logoLabel = new JLabel("电商平台", SwingConstants.LEFT);
        logoLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        logoLabel.setForeground(new Color(0, 120, 215));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        String[] buttons = {"商品列表", "购物车", "个人中心"};
        for (String text : buttons) {
            JButton btn = new JButton(text);
            btn.setPreferredSize(new Dimension(120, 40));
            btn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            // 添加动作监听器
            btn.addActionListener(e -> {
                switch (text) {
                    case "商品列表":
                        cardLayout.show(mainPanel, PRODUCT_CARD);
                        break;
                    case "购物车":
                        cardLayout.show(mainPanel, CART_CARD);
                        updateCartDetails();
                        break;
                    case "个人中心":
                        cardLayout.show(mainPanel, PROFILE_CARD);
                        refreshProfilePanel();
                        break;
                }
            });
            buttonPanel.add(btn);
        }
        navPanel.add(logoLabel, BorderLayout.WEST);
        navPanel.add(buttonPanel, BorderLayout.CENTER);
        return navPanel;
    }

    private JPanel productItemsPanel;
    private JTextField searchField;
    private Timer searchTimer;
    private JLabel searchStatusLabel;

    private JPanel createProductListPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部搜索区域
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); // 底部增加间距

        // 标题与搜索框组合
        JPanel titleSearchPanel = new JPanel(new BorderLayout(15, 0));
        titleSearchPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        // 标题样式优化
        JLabel titleLabel = new JLabel("商品列表");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(51, 51, 51));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 8, 0));
        titleSearchPanel.add(titleLabel, BorderLayout.WEST);

        // 带图标的搜索框
        JPanel searchBoxPanel = new JPanel(new BorderLayout());
        searchField = createSearchField();
        searchBoxPanel.add(searchField, BorderLayout.CENTER);
        // 搜索图标
        JLabel searchIcon = new JLabel(new ImageIcon("search_icon.png")); // 替换实际图标路径
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        searchBoxPanel.add(searchIcon, BorderLayout.EAST);
        titleSearchPanel.add(searchBoxPanel, BorderLayout.CENTER);
        topPanel.add(titleSearchPanel, BorderLayout.NORTH);
        // 搜索状态提示
        searchStatusLabel = new JLabel("", SwingConstants.CENTER);
        searchStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchStatusLabel.setForeground(new Color(153, 153, 153));
        searchStatusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        topPanel.add(searchStatusLabel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        // 商品列表容器
        productItemsPanel = new JPanel();
        productItemsPanel.setLayout(new BoxLayout(productItemsPanel, BoxLayout.Y_AXIS));
        // 滚动面板优化
        JScrollPane scrollPane = new JScrollPane(productItemsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        setupSearchListener();
        performSearch("");
        return mainPanel;
    }

    // 配置搜索监听器
    private void setupSearchListener() {
        searchTimer = new Timer(300, e -> {
            String keyword = searchField.getText().trim();
            performSearch(keyword);
        });
        searchTimer.setRepeats(false); // 确保只触发一次
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
    }private JTextField createSearchField() {
        JTextField field = new JTextField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(204, 204, 204)),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        field.setForeground(new Color(51, 51, 51));
        field.putClientProperty("JTextField.placeholderText", "输入商品名称搜索..."); // Java 11+ 支持

        // 自定义焦点效果
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 122, 255)),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(204, 204, 204)),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
        });
        return field;
    }
    private void performSearch(String keyword) {
        showSearchStatus("搜索中...", new Color(153, 153, 153));
        new SwingWorker<List<Commodity>, Void>() {
            @Override
            protected List<Commodity> doInBackground() throws SQLException {
                return CommodityJDBC.getCommodityByName(keyword);
            }
            @Override
            protected void done() {
                try {
                    List<Commodity> commodities = get();
                    if (commodities.isEmpty()) {
                        showSearchStatus("未找到名称包含 \"" + keyword + "\" 的商品", new Color(255, 59, 48));
                    } else {
                        searchStatusLabel.setText(""); // 清空状态提示
                    }
                    refreshProductItems(commodities);
                } catch (Exception ex) {
                    showSearchStatus("搜索失败，请稍后重试", new Color(255, 59, 48));
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

    private void refreshProductItems(List<Commodity> commodities) {
        productItemsPanel.removeAll();
        if (commodities.isEmpty()) {
            // 空状态提示面板
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));

            JLabel emptyLabel = new JLabel(
                    "<html><div style='text-align:center;'>"
                            + "<p style='font-size:16px; color:#666; margin-bottom:8px;'>没有找到相关商品</p>"
                            + "<p style='font-size:13px; color:#999;'>尝试其他关键词或浏览其他分类</p>"
                            + "</div></html>",
                    SwingConstants.CENTER
            );
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            productItemsPanel.add(emptyPanel);
        } else {
            for (Commodity commodity : commodities) {
                JPanel itemPanel = createItemPanel(commodity);
                productItemsPanel.add(itemPanel);
                productItemsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        productItemsPanel.revalidate();
        productItemsPanel.repaint();
    }
    private JPanel createItemPanel(Commodity commodity) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120)); // 固定高度
        JLabel nameLabel = new JLabel(commodity.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        String details = String.format("<html>"
                        + "<div style='color:#666; margin-top:5px;'>"
                        + "类型：%s | 生产商：%s<br>"
                        + "生产日期：%s | 产地：%s"
                        + "</div></html>",
                commodity.getType(),
                commodity.getManufacturer(),
                new SimpleDateFormat("yyyy-MM-dd").format(commodity.getProductionDate()),
                commodity.getOrigin()
        );
        setupHoverEffect(panel);
        JLabel detailLabel = new JLabel(details);
        detailLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showProductDetails(commodity.getName());
            }
        });
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.add(nameLabel, BorderLayout.NORTH);
        leftPanel.add(detailLabel, BorderLayout.CENTER);
        panel.add(leftPanel, BorderLayout.CENTER);
        return panel;
    }
    private void setupHoverEffect(JPanel panel) {
        Color defaultBg = Color.WHITE;
        Color hoverBg = new Color(245, 251, 255); // 浅蓝色背景
        Color borderColor = new Color(52, 190, 232);
        panel.setBackground(defaultBg);
        panel.setOpaque(true);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(hoverBg);
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 1, 2, 1, borderColor),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(defaultBg);
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 1, 2, 1, borderColor.darker()),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 1, 2, 1, borderColor),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
        });
    }
    private void showProductDetails(String productName) {
        int productId = getProductIdByName(productName);
        if (productId == -1) {
            JOptionPane.showMessageDialog(this, "商品不存在", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Commodity commodity = CommodityJDBC.getCommodityById(productId);
        if (commodity == null) {
            JOptionPane.showMessageDialog(this, "无法加载商品详情", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 获取商品的所有SKU信息
        List<CommoditySKU> skuDetails = CommodityJDBC.getCommodityskuByName(productName);
        if (skuDetails.isEmpty()) {
            JOptionPane.showMessageDialog(this, "该商品没有可用的SKU", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 创建详情面板
        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        addInfoRow(infoPanel, "商品名称：", commodity.getName());
        addInfoRow(infoPanel, "商品类型：", commodity.getType());
        addInfoRow(infoPanel, "生产日期：", String.valueOf(commodity.getProductionDate()));
        addInfoRow(infoPanel, "制造商：", commodity.getManufacturer());
        JTextArea detailArea = new JTextArea(commodity.getDetail());
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailsPanel.add(infoPanel, BorderLayout.NORTH);
        detailsPanel.add(new JScrollPane(detailArea), BorderLayout.CENTER);
        JDialog dialog = new JDialog(this, "商品详情", true);
        detailsPanel.add(createSkuPanel(skuDetails, dialog), BorderLayout.SOUTH);
        dialog.setContentPane(detailsPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl);
        panel.add(new JLabel(value));
    }
    private JScrollPane createSkuPanel(List<CommoditySKU> skuDetails, JDialog parentDialog) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(skuDetails.size(), 1));
        for (CommoditySKU sku : skuDetails) {
            String color = sku.getColor();
            String style = sku.getStyle();
            Integer stock = sku.getStock();
            BigDecimal price = sku.getPrice();
            String skuText = String.format("颜色：%s | 样式：%s | 库存：%d | 价格：%.2f",
                    color, style, stock, price);
            JButton skuButton = new JButton(skuText);
            skuButton.addActionListener(e -> handleAddToCart(sku, parentDialog));
            panel.add(skuButton);
        }
        return new JScrollPane(panel);
    }

    private void handleAddToCart(CommoditySKU sku, JDialog dialogToClose) {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "用户未登录", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            addProductToCart(sku.getSkuId());
            updateCartDetails();
            dialogToClose.dispose(); 
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "未知错误：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProductToCart(int skuId) {
        try {
            CommoditySKU sku = CommodityJDBC.getCommoditySKUById(skuId);
            //  检查库存
            if (sku.getStock() <= 0) {
                throw new IllegalArgumentException("该商品已无库存");
            }
            CartJDBC.addItem(currentUser.getId(), skuId);
            updateCartDetails();
        } catch (SQLException ex) {
            String errorMessage = "添加商品到购物车失败";
            if (ex.getMessage() != null && ex.getMessage().contains("库存不足")) {
                errorMessage = "库存不足，无法添加更多数量";
            }
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, errorMessage, "错误", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "警告", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "未知错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    private int getProductIdByName(String productName) {
        List<Commodity> commodities = CommodityJDBC.getCommodityByName(productName);
        if (commodities != null && !commodities.isEmpty()) {
            return commodities.get(0).getId(); // 返回第一个商品的ID
        } else {
            System.out.println("未找到商品名称包含: " + productName);
            throw new IllegalArgumentException("未找到商品: " + productName);
        }
    }
    private void updateCartDetails() {
        try {
            List<CartItemDetail> cartItems = CartJDBC.getCartDetailsByUserId(currentUser.getId());
            BigDecimal total = BigDecimal.ZERO;
            cartPanel.removeAll();
            List<CartItemDetail> sortedItems = cartItems.stream()
                    .sorted(Comparator.comparing(item -> {
                        CommoditySKU sku = Optional.ofNullable(item.getCachedSku())
                                .orElseGet(() -> {
                                    CommoditySKU newSku = CommodityJDBC.getCommoditySKUById(item.getSkuId());
                                    item.setCachedSku(newSku);
                                    return newSku;
                                });
                        Commodity commodity = Optional.ofNullable(item.getCachedCommodity())
                                .orElseGet(() -> {
                                    if (sku == null) return null;
                                    Commodity newCommodity = CommodityJDBC.getCommodityById(sku.getCommodityid());
                                    item.setCachedCommodity(newCommodity);
                                    return newCommodity;
                                });

                        return (commodity != null) ? commodity.getName() : "";
                    }))
                    .collect(Collectors.toList());
            for (CartItemDetail item : sortedItems) {
                total = total.add(item.getTotalPrice());
                cartPanel.add(createCartItemPanel(item));
            }
            totalLabel.setText(String.format("总计：¥%.2f 元", total.doubleValue()));
            cartPanel.revalidate();
            cartPanel.repaint();
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, cartPanel);
            JViewport viewport = scrollPane.getViewport();
            Point prevPosition = viewport.getViewPosition();
            SwingUtilities.invokeLater(() -> viewport.setViewPosition(prevPosition));
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "无法更新购物车详情：" + ex.getMessage(),
                    "数据库错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private JPanel createCartItemPanel(CartItemDetail item) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, 60));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(52, 184, 232)),
                BorderFactory.createEmptyBorder(4, 10, 4, 5)
        ));
        CommoditySKU sku = CommodityJDBC.getCommoditySKUById(item.getSkuId());
        Commodity commodity = sku != null ? CommodityJDBC.getCommodityById(sku.getCommodityid()) : null;
        JPanel clickablePanel = new JPanel(new BorderLayout(5, 0));
        clickablePanel.setBackground(Color.WHITE);
        clickablePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // 信息显示内容
        String displayContent = buildDisplayContent(commodity, sku);
        JLabel infoLabel = new JLabel(displayContent);
        infoLabel.setVerticalAlignment(SwingConstants.TOP);
        clickablePanel.add(infoLabel, BorderLayout.CENTER);
        clickablePanel.addMouseListener(createProductMouseAdapter(commodity, sku));
        JPanel actionPanel = createActionPanelWithParams(
                item,
                commodity != null ? commodity.getName() : "未知商品",
                sku != null ? sku.getColor() : "未知颜色",
                sku != null ? sku.getStyle() : "未知样式"
        );

        panel.add(clickablePanel, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createActionPanelWithParams(CartItemDetail item, String productName, String color, String style) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        JButton deleteButton = new JButton("×");
        deleteButton.setPreferredSize(new Dimension(30, 20));
        deleteButton.addActionListener(e -> removeCartItem(item.getCartId()));

        // 数量控制组件
        JPanel quantityPanel = new JPanel();
        quantityPanel.setLayout(new BoxLayout(quantityPanel, BoxLayout.X_AXIS));

        // 减少按钮
        JButton minusButton = new JButton("-");
        minusButton.setMargin(new Insets(0, 4, 0, 4));
        minusButton.addActionListener(e -> {
            try {
                decreaseCartItemQuantity(
                        item.getUserId(),
                        item.getSkuId(),
                        productName,
                        color,
                        style
                );
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel,
                        "操作失败: " + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        // 当前数量显示
        JLabel quantityLabel = new JLabel(String.valueOf(item.getQuantity()));
        quantityLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        // 增加按钮
        JButton plusButton = new JButton("+");
        plusButton.setMargin(new Insets(0, 4, 0, 4));
        plusButton.addActionListener(e ->
                increaseCartItemQuantity(item.getUserId(), item.getSkuId()));

        quantityPanel.add(minusButton);
        quantityPanel.add(quantityLabel);
        quantityPanel.add(plusButton);

        panel.add(deleteButton, BorderLayout.EAST);
        panel.add(quantityPanel, BorderLayout.WEST);

        return panel;
    }

    // 商品信息显示构建
    private String buildDisplayContent(Commodity commodity, CommoditySKU sku) {
        if (commodity == null || sku == null) {
            return "<html><div style='color: red; font-size: 12px;'>商品信息加载失败</div></html>";
        }

        String mainLine = String.format("<span style='font-size: 12px;'>%s</span>",
                truncateString(commodity.getName(), 24));

        String detailLine = String.format("<span style='font-size: 11px; color: #666;'>%s | %s | %s元</span>",
                sku.getColor() != null ? sku.getColor() : "未知颜色",
                sku.getStyle() != null ? sku.getStyle() : "未知样式",
                sku.getPrice() != null ? sku.getPrice().setScale(2, RoundingMode.HALF_UP) : "未知价格");

        return "<html><div style='padding: 2px 0;'>" + mainLine + "<br/>" + detailLine + "</div></html>";
    }
    // 带参数的鼠标适配器
    private MouseAdapter createProductMouseAdapter(Commodity commodity, CommoditySKU sku) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() instanceof JButton) return;
                if (commodity != null && sku != null) {
                    showProductDetails(commodity.getName());
                } else {
                    JOptionPane.showMessageDialog(null,
                            "商品信息加载失败",
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                ((JComponent) e.getSource()).setBackground(new Color(245, 245, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ((JComponent) e.getSource()).setBackground(Color.WHITE);
            }
        };
    }
    // 辅助方法：截断过长文本
    private String truncateString(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 3) + "...";
        }
        return text;
    }
    private JPanel createCartPanel() {
        cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));
        cartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Reduced padding
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Reduced padding
        JLabel label = new JLabel("购物车", SwingConstants.CENTER);
        label.setFont(new Font("微软雅黑", Font.BOLD, 16));
        headerPanel.add(label);
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(headerPanel, BorderLayout.NORTH);
        containerPanel.add(new JScrollPane(cartPanel), BorderLayout.CENTER);
        JPanel checkoutPanel = createCheckoutPanel();
        containerPanel.add(checkoutPanel, BorderLayout.SOUTH);
        return containerPanel;
    }

    private JPanel createCheckoutPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        totalLabel = new JLabel("总计：0.00 元");
        JButton checkoutButton = new JButton("去结算");
        checkoutButton.setBackground(new Color(85, 183, 184));
        checkoutButton.setForeground(Color.WHITE);
        panel.add(totalLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(checkoutButton);
        return panel;
    }
    private JLabel totalLabel;
    // 增加购物车商品数量
    private void increaseCartItemQuantity(int userId, int skuId) {
        try {
            CartJDBC.increaseQuantity(userId, skuId);
            updateCartDetails(); // 更新购物车详情
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数量已达到上限", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    // 减少购物车商品数量
    private void decreaseCartItemQuantity(int userId, int skuId, String productName, String color, String style) throws SQLException {
        try {
            CartJDBC.decreaseQuantity(userId, skuId);
            updateCartDetails(); // 更新界面
        } catch (CartJDBC.ConfirmDeleteException ex) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "确定要删除商品："+productName+" "+color+" "+style+"？",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION  // 使用 YES/NO 选项
            );
            if (option == JOptionPane.YES_OPTION) {
                try {
                    deleteCartItem(userId,skuId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                updateCartDetails();
                JOptionPane.showMessageDialog(this, "商品已删除"); // 删除成功提示
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "减少数量失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void removeCartItem(int cartId) {
        try {
            CartJDBC.removeItem(cartId);
            updateCartDetails(); // 更新购物车详情
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "删除商品失败", "错误", JOptionPane.ERROR_MESSAGE);
            updateCartDetails();        }
    }
    private JPanel profilePanel;
    private JLabel usernameLabel;
    private JLabel phoneLabel;
    private JLabel addressLabel;
    private JLabel balanceLabel;
    private JLabel remarkLabel;

    private JPanel createProfilePanel() {
        profilePanel = new JPanel(new BorderLayout(10, 10));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("个人中心", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        profilePanel.add(titleLabel, BorderLayout.NORTH);
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        usernameLabel = createInfoRow(infoPanel, "用户名：", currentUser.getUsername(), "修改", e -> showEditDialog("username"));
        createInfoRow(infoPanel, "密码：", "******", "修改", e -> showEditDialog("password"));
        phoneLabel = createInfoRow(infoPanel, "电话：", currentUser.getPhone(), "修改", e -> showEditDialog("phone"));
        addressLabel = createInfoRow(infoPanel, "地址：", currentUser.getAddress(), "修改", e -> showEditDialog("address"));
        balanceLabel = createInfoRow(infoPanel, "余额：", currentUser.getBalance().toString(), "充值", e -> showRechargeDialog());
        remarkLabel = createInfoRow(infoPanel, "备注：", currentUser.getRemark(), "修改", e -> showEditDialog("remark"));
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.add(new JLabel("注册时间：" + new SimpleDateFormat("yyyy-MM-dd").format(currentUser.getCreatedTime())));
        infoPanel.add(timePanel);
        profilePanel.add(new JScrollPane(infoPanel), BorderLayout.CENTER);
        return profilePanel;
    }

    private JLabel createInfoRow(JPanel parent, String label, String value, String btnText, ActionListener listener) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JLabel valueLabel = new JLabel(value);
        row.add(new JLabel(label));
        row.add(valueLabel);
        JButton actionBtn = new JButton(btnText);
        actionBtn.setPreferredSize(new Dimension(80, 25));
        actionBtn.addActionListener(listener);
        row.add(actionBtn);
        parent.add(row);
        return valueLabel;
    }

    private void showEditDialog(String fieldType) {
        JDialog dialog = new JDialog(this, "修改信息", true);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.setSize(300, 150);
        JLabel label = new JLabel("新" + getChineseFieldName(fieldType) + "：");
        JComponent inputField = createInputField(fieldType); // 创建输入字段
        JButton confirmBtn = new JButton("确认");
        JButton cancelBtn = new JButton("取消");
        // 添加组件到对话框
        dialog.add(label);
        dialog.add((Component) inputField);
        dialog.add(confirmBtn);
        dialog.add(cancelBtn);
        // 确认按钮的事件处理
        confirmBtn.addActionListener(e -> {
            try {
                // 获取输入值并进行验证
                String newValue = getInputValue(fieldType, inputField);
                // 验证输入是否为空
                if (newValue == null || newValue.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "字段 '" + getChineseFieldName(fieldType) + "' 不能为空，请重新输入。",
                            "输入错误", JOptionPane.ERROR_MESSAGE);
                    return; // 如果输入为空，直接返回，不再继续执行
                }
                // 更新用户字段
                updateUserField(fieldType, newValue);

                // 调用数据库更新方法
                String result = UserJDBC.updateUser(currentUser);

                // 检查更新结果
                if (result.startsWith("修改成功")) {
                    updateUIField(fieldType, newValue); // 更新UI
                    refreshProfilePanel(); // 刷新面板
                    dialog.dispose(); // 关闭对话框
                }

                // 显示提示信息
                JOptionPane.showMessageDialog(dialog, result, "提示", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                // 处理异常情况
                JOptionPane.showMessageDialog(dialog, "修改失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 取消按钮的事件处理
        cancelBtn.addActionListener(e -> dialog.dispose());
        // 刷新个人资料面板
        refreshProfilePanel();
        // 设置对话框大小、位置并显示
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    private void showRechargeDialog() {
        JDialog dialog = new JDialog(this, "账户充值", true);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        JTextField amountField = new JTextField();
        JButton confirmBtn = new JButton("确认");
        JButton cancelBtn = new JButton("取消");
        dialog.add(new JLabel("充值金额："));
        dialog.add(amountField);
        dialog.add(confirmBtn);
        dialog.add(cancelBtn);
        confirmBtn.addActionListener(e -> {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText());
                currentUser.setBalance(currentUser.getBalance().add(amount));
                String result = UserJDBC.updateUser(currentUser);
                refreshProfilePanel();
                if (result.startsWith("修改成功")) {
                    balanceLabel.setText(currentUser.getBalance().toString());
                    refreshProfilePanel();
                    dialog.dispose();
                }
                JOptionPane.showMessageDialog(dialog, result, "提示", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "请输入有效数字", "错误", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "充值失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String getChineseFieldName(String fieldType) {
        switch (fieldType) {
            case "username": return "用户名";
            case "password": return "密码";
            case "phone": return "电话";
            case "address": return "地址";
            case "remark": return "备注";
            default: return "";
        }
    }

    private JComponent createInputField(String fieldType) {
        JComponent field = switch (fieldType) {
            case "password" -> new JPasswordField();
            default -> new JTextField(currentUser.getFieldValue(fieldType));
        };
        return field;
    }

    private String getInputValue(String fieldType, JComponent field) {
        return fieldType.equals("password")
                ? new String(((JPasswordField) field).getPassword())
                : ((JTextField) field).getText();
    }

    private void updateUserField(String fieldType, String value) {
        switch (fieldType) {
            case "username" -> currentUser.setUsername(value);
            case "password" -> currentUser.setPassword(value);
            case "phone" -> currentUser.setPhone(value);
            case "address" -> currentUser.setAddress(value);
            case "remark" -> currentUser.setRemark(value);
        }

    }

    private void updateUIField(String fieldType, String value) {
        switch (fieldType) {
            case "username" -> usernameLabel.setText(value);
            case "phone" -> phoneLabel.setText(value);
            case "address" -> addressLabel.setText(value);
            case "remark" -> remarkLabel.setText(value);
        }
        refreshProfilePanel();
    }
    private void refreshProfilePanel() {
        usernameLabel.setText(currentUser.getUsername());
        phoneLabel.setText(currentUser.getPhone());
        addressLabel.setText(currentUser.getAddress());
        remarkLabel.setText(currentUser.getRemark());
        balanceLabel.setText(currentUser.getBalance().toPlainString());
        profilePanel.revalidate();
        profilePanel.repaint();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatMTMaterialLighterIJTheme.setup();
            UIManager.put("Button.arc", 20); // 圆角按钮
            UIManager.put("Component.arc", 20); // 圆角组件
            UIManager.put("TextComponent.arc", 10); // 文本框圆角
            User user = UserJDBC.getUserByUsername("Kokomi");
            Mainview mainview = new Mainview(user);
            mainview.setVisible(true);
        });
    }
}