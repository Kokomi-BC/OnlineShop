package GUI;

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
import net.miginfocom.swing.MigLayout;
import src.*;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
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
import java.util.stream.Collectors;

import static src.CartJDBC.deleteCartItem;

public class Mainview extends JFrame {
    private JPanel cartPanel; // 定义类级别的变量
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final User currentUser;
    private static final String PRODUCT_CARD = "ProductList";
    private static final String CART_CARD = "Cart";
    private static final String PROFILE_CARD = "Profile";
    public Mainview(User user) {
        this.currentUser = user;
        setTitle("购物应用");
        setSize(800, 700);
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
        navPanel.add(buttonPanel, BorderLayout.CENTER);
        return navPanel;
    }

    private JPanel productItemsPanel;
    private JTextField searchField;
    private Timer searchTimer;
    private JLabel searchStatusLabel;

    private JPanel createProductListPanel() {
        UIManager.put("Component.arc",4);  // 统一组件圆角
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // 顶部搜索区域
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        // 标题与搜索框组合 - 使用FlatLaf边框
        JPanel titleSearchPanel = new JPanel(new BorderLayout(10, 0));
        titleSearchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(0, 0, 5, 0)
        ));
        JLabel titleLabel = new JLabel("Shop");
        titleLabel.setFont(UIManager.getFont("h1.font"));  // 使用预定义标题字体
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        titleSearchPanel.add(titleLabel, BorderLayout.WEST);
        // 搜索框使用FlatLaf特性
        JPanel searchBoxPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "输入内容搜索...");
        searchField.putClientProperty("JTextField.showClearButton", true);  // 显示清空按钮
        searchField.putClientProperty("JComponent.roundRect", true);  // 圆角样式
        searchBoxPanel.add(searchField, BorderLayout.CENTER);
        // 使用FlatLaf图标代替Emoji
        JButton searchIcon = new JButton(UIManager.getIcon("Component.searchIcon"));
        searchIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchIcon.setBorder(BorderFactory.createEmptyBorder());
        searchIcon.setContentAreaFilled(false);
        searchBoxPanel.add(searchIcon, BorderLayout.EAST);
        titleSearchPanel.add(searchBoxPanel, BorderLayout.CENTER);
        topPanel.add(titleSearchPanel, BorderLayout.NORTH);
        // 搜索状态标签使用次要文本颜色
        searchStatusLabel = new JLabel("", SwingConstants.CENTER);
        searchStatusLabel.setFont(UIManager.getFont("defaultFont"));
        searchStatusLabel.setForeground(UIManager.getColor("Component.secondaryForeground"));
        searchStatusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        topPanel.add(searchStatusLabel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        // 商品列表容器
        productItemsPanel = new JPanel();
        productItemsPanel.setLayout(new BoxLayout(productItemsPanel, BoxLayout.Y_AXIS));
        productItemsPanel.setOpaque(false);
        // 滚动面板优化
        JScrollPane scrollPane = new JScrollPane(productItemsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UIManager.getColor("Panel.background"));
        scrollPane.putClientProperty("JScrollPane.smoothScrolling", true);  // 启用平滑滚动
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        setupSearchListener();
        performSearch("");
        return mainPanel;
    }

    // 配置搜索监听器
    private void setupSearchListener() {
        searchTimer = new Timer(500, e -> {
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
        JPanel panel = new JPanel(new BorderLayout(10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        panel.setOpaque(true);
        panel.setBorder(createItemBorder(UIManager.getColor("Component.borderColor")));
        panel.setBackground(UIManager.getColor("Panel.background"));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        Color secondaryColor = getSafeColor("Component.secondaryForeground");
        String details = String.format("<html><div style='margin-top:3px; color:%s;'>类型：%s | 生产商：%s<br>生产日期：%s | 产地：%s</div></html>",
                colorToHex(secondaryColor), commodity.getType(), commodity.getManufacturer(),
                new SimpleDateFormat("yyyy-MM-dd").format(commodity.getProductionDate()), commodity.getOrigin());

        JLabel nameLabel = new JLabel(commodity.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        nameLabel.setForeground(UIManager.getColor("Component.foreground"));

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setOpaque(false);
        leftPanel.add(nameLabel, BorderLayout.NORTH);
        leftPanel.add(new JLabel(details), BorderLayout.CENTER);

        panel.add(leftPanel);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showProductDetails(commodity.getName());
            }
        });
        setupHoverEffect(panel);
        return panel;
    }
    private static String colorToHex(Color color) {
        if (color == null) {
            return "#000000";
        }
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    private Color getSafeColor(String key) {
        Color color = UIManager.getColor(key);
        if (color == null) {
            return UIManager.getColor("Label.foreground"); // 使用基础前景色作为后备
        }
        return color;
    }
    static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        public RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius * 2, radius * 2);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius, radius, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = radius;
            return insets;
        }
    }
    private Border createItemBorder(Color color) {
        return BorderFactory.createCompoundBorder(
                new RoundedBorder(color, 8),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        );
    }
    private void setupHoverEffect(JPanel panel) {
        Color defaultBg = UIManager.getColor("Panel.background");
        Color hoverBg = UIManager.getColor("Component.hoverBackground");
        Color pressedBg = UIManager.getColor("Component.pressedBackground");
        Color borderColor = UIManager.getColor("Component.focusColor");
        Border defaultBorder = createItemBorder(UIManager.getColor("Component.borderColor"));
        Border hoverBorder = createItemBorder(borderColor);
        Border pressedBorder = createItemBorder(borderColor.darker());

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                applyHoverStyle(panel, hoverBg, hoverBorder, Cursor.HAND_CURSOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                applyHoverStyle(panel, defaultBg, defaultBorder, Cursor.DEFAULT_CURSOR);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                applyHoverStyle(panel, pressedBg, pressedBorder, Cursor.HAND_CURSOR);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                applyHoverStyle(panel, hoverBg, hoverBorder, Cursor.HAND_CURSOR);
            }
        });
    }
    private void applyHoverStyle(JPanel panel, Color bgColor, Border border, int cursorType) {
        panel.setBackground(bgColor);
        panel.setBorder(border);
        panel.setCursor(Cursor.getPredefinedCursor(cursorType));
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
        List<Commodity> commodities = CommodityJDBC.searchCommodities(productName);
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
        JPanel quantityPanel = new JPanel();
        quantityPanel.setLayout(new BoxLayout(quantityPanel, BoxLayout.X_AXIS));
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
    // 截断过长文本
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
        checkoutButton.addActionListener(e -> {
            checkoutCartItem(currentUser.getId());
            updateCartDetails();
        });
        checkoutButton.setBackground(new Color(85, 183, 184));
        checkoutButton.setForeground(Color.WHITE);
        panel.add(totalLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(checkoutButton);
        return panel;
    }
    private void checkoutCartItem(int userid) {
        try {
            CartJDBC.checkout(userid);
            JOptionPane.showMessageDialog(this,
                    "购买成功！",
                    "结算成功",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            String errorMessage = ex.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("余额不足")) {
                    JOptionPane.showMessageDialog(this,
                            "账户余额不足",
                            "余额不足",
                            JOptionPane.ERROR_MESSAGE);
                } else if (errorMessage.contains("购物车为空")) {
                    JOptionPane.showMessageDialog(this,
                            "购物车中没有商品",
                            "购物车为空",
                            JOptionPane.WARNING_MESSAGE);
                } else if (errorMessage.contains("库存不足")) {
                    JOptionPane.showMessageDialog(this,
                            "部分商品库存不足，请调整数量后重试",
                            "库存不足",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "发生错误: " + errorMessage,
                            "系统错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "结账过程中发生未知错误",
                        "系统错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private JLabel totalLabel;
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
        profilePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 标题部分
        JLabel titleLabel = new JLabel("个人中心", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20));
        profilePanel.add(titleLabel, BorderLayout.NORTH);

        // 信息展示面板
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建可点击的信息行
        usernameLabel = createClickableRow(infoPanel, "用户名：", currentUser.getUsername(), "username");
        phoneLabel = createClickableRow(infoPanel, "电话：", currentUser.getPhone(), "phone");
        addressLabel = createClickableRow(infoPanel, "地址：", currentUser.getAddress(), "address");
        balanceLabel = createClickableRow(infoPanel, "余额：", currentUser.getBalance().toString(), "balance");
        remarkLabel = createClickableRow(infoPanel, "备注：", currentUser.getRemark(), "remark");
        createClickableRow(infoPanel, "密码：", "******", "password");
        // 注册时间（不可编辑）
        JPanel timePanel = createNonClickableRow("注册时间：",
                new SimpleDateFormat("yyyy-MM-dd").format(currentUser.getCreatedTime()));
        infoPanel.add(timePanel);

        profilePanel.add(new JScrollPane(infoPanel), BorderLayout.CENTER);
        return profilePanel;
    }

    private JLabel createClickableRow(JPanel parent, String labelText, String value, String fieldType) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        rowPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 标签样式
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setForeground(UIManager.getColor("Label.disabledForeground"));

        // 值标签
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.PLAIN, 14));

        // 添加交互效果
        rowPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ("balance".equals(fieldType)) {
                    showRechargeDialog();
                } else {
                    showEditDialog(fieldType);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rowPanel.setBackground(UIManager.getColor("List.hoverBackground"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rowPanel.setBackground(UIManager.getColor("Panel.background"));
            }
        });

        rowPanel.add(label, BorderLayout.WEST);
        rowPanel.add(valueLabel, BorderLayout.EAST);
        parent.add(rowPanel);
        return valueLabel;
    }

    private JPanel createNonClickableRow(String labelText, String value) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setForeground(UIManager.getColor("Label.disabledForeground"));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.PLAIN, 14));

        panel.add(label, BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.EAST);
        return panel;
    }

    private void showEditDialog(String fieldType) {
        JDialog dialog = new JDialog(this, "修改信息", true);
        dialog.setLayout(new MigLayout("insets 20, gap 15", "[][grow]", "[][][]"));

        // 输入组件
        JComponent inputField = createInputField(fieldType);
        JLabel tipLabel = new JLabel("");
        tipLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        tipLabel.setFont(tipLabel.getFont().deriveFont(12f));
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton confirmBtn = new JButton("保存更改");
        JButton cancelBtn = new JButton("取消");
        // 组件布局
        dialog.add(new JLabel("新" + getChineseFieldName(fieldType) + "："), "wrap");
        dialog.add(inputField, "w 250!, wrap");
        dialog.add(tipLabel, "gapleft 5, wrap");
        dialog.add(buttonPanel, "gaptop 15, span");

        buttonPanel.add(cancelBtn);
        buttonPanel.add(confirmBtn);
        // 事件处理
        confirmBtn.addActionListener(e -> handleConfirm(fieldType, inputField, dialog));
        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void handleConfirm(String fieldType, JComponent inputField, JDialog dialog) {
        try {
            String newValue = getInputValue(fieldType, inputField);
            if (newValue.trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "输入内容不能为空", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            updateUserField(fieldType, newValue);
            String result = UserJDBC.updateUser(currentUser);

            if (result.startsWith("修改成功")) {
                updateUIField(fieldType, newValue);
                refreshProfilePanel();
                dialog.dispose();
            }
            JOptionPane.showMessageDialog(dialog, result, "操作结果", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, "操作失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
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
        return switch (fieldType) {
            case "username" -> "用户名";
            case "password" -> "密码";
            case "phone" -> "电话";
            case "address" -> "地址";
            case "remark" -> "备注";
            default -> "";
        };
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

    public static void main() {
        SwingUtilities.invokeLater(() -> {
            FlatMTMaterialLighterIJTheme.setup();
            User user = UserJDBC.getUserByUsername("Kokomi");
            Mainview mainview = new Mainview(user);
            mainview.setVisible(true);
        });
    }
}