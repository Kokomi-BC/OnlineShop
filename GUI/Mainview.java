package GUI;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
import org.mindrot.jbcrypt.BCrypt;
import src.*;
import net.miginfocom.swing.MigLayout; // 用于 Swing 项目
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import static src.CartJDBC.deleteCartItem;
import static src.OrderJBDC.getTotalQuantityByUserId;
import static src.OrderJBDC.getinfoById;
import static src.UserJDBC.getUserById;

public class Mainview extends JFrame {
    private JPanel cartPanel; // 定义类级别的变量
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private static User currentUser;
    private static final String PRODUCT_CARD = "ProductList";
    private static final String CART_CARD = "Cart";
    private static final String PROFILE_CARD = "Profile";
    public Mainview(User user) {
        currentUser = user;
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
            btn.addActionListener(_ -> {
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
        searchIcon.setContentAreaFilled(false);
        searchBoxPanel.add(searchIcon, BorderLayout.EAST);
        titleSearchPanel.add(searchBoxPanel, BorderLayout.CENTER);
        topPanel.add(titleSearchPanel, BorderLayout.NORTH);
        searchStatusLabel = new JLabel("", SwingConstants.CENTER);
        searchStatusLabel.setFont(UIManager.getFont("defaultFont"));
        searchStatusLabel.setForeground(UIManager.getColor("Component.secondaryForeground"));
        searchStatusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        topPanel.add(searchStatusLabel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        productItemsPanel = new JPanel();
        productItemsPanel.setLayout(new BoxLayout(productItemsPanel, BoxLayout.Y_AXIS));
        productItemsPanel.setOpaque(false);
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
                searchTimer.restart();
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
        Color secondaryColor = getSafeColor();
        String productionDate = (commodity.getProductionDate() != null)
                ? new SimpleDateFormat("yyyy-MM-dd").format(commodity.getProductionDate())
                : "未知";

        String details = String.format("<html><div style='margin-top:3px; color:%s;'>类型：%s | 生产商：%s<br>生产日期：%s | 产地：%s</div></html>",
                colorToHex(secondaryColor), commodity.getType(), commodity.getManufacturer(),
                productionDate, commodity.getOrigin());
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
                showProductDetails(commodity.getId());
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
    private Color getSafeColor() {
        Color color = UIManager.getColor("Component.secondaryForeground");
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
    private void addInfoRow(JPanel panel, String label, String value) {
        panel.add(new JLabel(label));
        panel.add(new JLabel(value != null ? value : "无"));
    }
    private void applyHoverStyle(JPanel panel, Color bgColor, Border border, int cursorType) {
        panel.setBackground(bgColor);
        panel.setBorder(border);
        panel.setCursor(Cursor.getPredefinedCursor(cursorType));
    }
    private void showProductDetails(int productId) {
        if (productId == -1) {
            JOptionPane.showMessageDialog(this, "商品不存在", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Commodity commodity = CommodityJDBC.getCommodityById(productId);
        if (commodity == null) {
            JOptionPane.showMessageDialog(this, "无法加载商品详情", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<CommoditySKU> skuDetails = CommodityJDBC.getCommodityskuById(productId);
        if (skuDetails.isEmpty()) {
            JOptionPane.showMessageDialog(this, "该商品没有可用的SKU", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
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
            skuButton.addActionListener(_ -> handleAddToCart(sku, parentDialog));
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
            if (sku != null && sku.getStock() <= 0) {
                throw new IllegalArgumentException("该商品已售罄");
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
                    .toList();
            for (CartItemDetail item : sortedItems) {
                total = total.add(item.getTotalPrice());
                cartPanel.add(createCartItemPanel(item));
            }
            totalLabel.setText(String.format("¥%.2f 元", total.doubleValue()));
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
        deleteButton.addActionListener(_ -> removeCartItem(item.getCartId()));
        JPanel quantityPanel = new JPanel();
        quantityPanel.setLayout(new BoxLayout(quantityPanel, BoxLayout.X_AXIS));
        JButton minusButton = new JButton("-");
        minusButton.setMargin(new Insets(0, 4, 0, 4));
        minusButton.addActionListener(_ -> {
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
        JLabel quantityLabel = new JLabel(String.valueOf(item.getQuantity()));
        quantityLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        JButton plusButton = new JButton("+");
        plusButton.setMargin(new Insets(0, 4, 0, 4));
        plusButton.addActionListener(_ ->
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
                truncateString(commodity.getName()));
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
                    showProductDetails(commodity.getId());
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
    private String truncateString(String text) {
        if (text.length() > 24) {
            return text.substring(0, 24 - 3) + "...";
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
        checkoutButton.addActionListener(_ -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(panel);
            JLabel messageLabel = new JLabel("<html><div style='text-align:center; padding:10px;'>"
                    + "确认支付 <span style='color:#FF5722; font-weight:bold;'>"
                    + totalLabel.getText() + "</span> 吗？</div></html>");
            messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            int option = JOptionPane.showConfirmDialog(
                    parentWindow,  // 使用正确的父组件
                    messageLabel,
                    "确认支付",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (option == JOptionPane.OK_OPTION) {
                boolean isCheckoutSuccessful = checkoutCartItem(currentUser.getId());
                updateCartDetails();
                if (isCheckoutSuccessful) {
                    JOptionPane.showMessageDialog(
                            parentWindow,
                            "<html><div style='padding:10px;'>支付成功！</div></html>",
                            "提示",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });

        panel.add(totalLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(checkoutButton);
        return panel;
    }

    private boolean checkoutCartItem(int userid) {
        try {
            CartJDBC.checkout(userid);
            Window parentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            JOptionPane.showMessageDialog(parentWindow,
                    "<html><div style='padding:10px;'>购买成功！</div></html>",
                    "结算成功",
                    JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
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
            return false;

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
    private JLabel pendingOrdersLabel;
    private JPanel createProfilePanel() {
        profilePanel = new JPanel(new BorderLayout(10, 10));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel("个人中心", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20));
        profilePanel.add(titleLabel, BorderLayout.NORTH);
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        usernameLabel = createClickableRow(infoPanel, "用户名：", currentUser.getUsername(), "username");
        pendingOrdersLabel = createClickableRow(infoPanel, "待收货数量：", String.valueOf(getTotalQuantityByUserId(currentUser.getId())), "orders");
        phoneLabel = createClickableRow(infoPanel, "电话：", currentUser.getPhone(), "phone");
        addressLabel = createClickableRow(infoPanel, "收货地址：", currentUser.getAddress(), "address");
        balanceLabel = createClickableRow(infoPanel, "余额：", currentUser.getBalance().toString(), "balance");
        remarkLabel = createClickableRow(infoPanel, "备注：", currentUser.getRemark(), "remark");
        createClickableRow(infoPanel, "密码：", "******", "password");
        JPanel timePanel = createNonClickableRow(
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
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setForeground(UIManager.getColor("Label.disabledForeground"));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.PLAIN, 14));
        rowPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ("balance".equals(fieldType)) {
                    showRechargeDialog();
                } else if ("orders".equals(fieldType)) {
                    showOrderDetailsWindow();
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

    private JPanel createNonClickableRow(String value) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JLabel label = new JLabel("注册时间：");
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
        JComponent inputField = createInputField(fieldType);
        JLabel tipLabel = new JLabel("");
        tipLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        tipLabel.setFont(tipLabel.getFont().deriveFont(12f));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton confirmBtn = new JButton("保存更改");
        JButton cancelBtn = new JButton("取消");
        dialog.add(new JLabel("修改" + getChineseFieldName(fieldType)), "wrap");
        dialog.add(inputField, "w 250!, wrap");
        dialog.add(tipLabel, "gapleft 5, wrap");
        dialog.add(buttonPanel, "gaptop 15, span");
        buttonPanel.add(cancelBtn);
        buttonPanel.add(confirmBtn);
        confirmBtn.addActionListener(_ -> handleConfirm(fieldType, inputField, dialog));
        cancelBtn.addActionListener(_ -> dialog.dispose());
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    private static final String PASSWORD_REGEX = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";
    private static final String PHONE_REGEX = "^(?:(?:\\+|00)86)?1[3-9]\\d{9}$";
    private void handleConfirm(String fieldType, JComponent inputField, JDialog dialog) {
        try {
            String newValue = getInputValue(fieldType, inputField);
            if (newValue == null) {
                return; // 密码错误时getInputValue已弹窗，此处直接终止
            }
            newValue = newValue.trim();
            if (newValue.isEmpty()) {
                showValidationError(dialog, fieldType, "不能为空");
                return;
            }
            if (!validateField(fieldType, newValue)) {
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
    private boolean validateField(String fieldType, String value) {
        switch (fieldType) {
            case "password":
                if (!value.matches(PASSWORD_REGEX)) {
                    showValidationError(null, fieldType, "需6-16位字母数字组合");
                    return false;
                }
                break;
            case "phone":
                if (!value.matches(PHONE_REGEX)) {
                    showValidationError(null, fieldType, "请输入有效的手机号");
                    return false;
                }
                break;
        }
        return true;
    }
    private void showValidationError(Component parent, String fieldType, String reason) {
        String message = String.format("%s格式错误：%s",
                getChineseFieldName(fieldType),
                reason);

        JOptionPane.showMessageDialog(parent,
                message,
                "输入错误",
                JOptionPane.ERROR_MESSAGE);
    }
    private void showRechargeDialog() {
        JDialog dialog = new JDialog(this, "账户充值", true);
        dialog.setLayout(new BorderLayout(10, 10));
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
                0.0, // 初始值
                0.00, // 最小值
                100000.0, // 最大值
                0.1 // 步长
        );
        JSpinner amountSpinner = new JSpinner(spinnerModel);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(
                amountSpinner,
                "#,##0.00" // 数字格式
        );
        amountSpinner.setEditor(editor);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        contentPanel.add(new JLabel("充值金额（元）"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        contentPanel.add(amountSpinner, gbc);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton confirmBtn = new JButton("确认");
        JButton cancelBtn = new JButton("取消");
        confirmBtn.setPreferredSize(new Dimension(80, 25));
        cancelBtn.setPreferredSize(confirmBtn.getPreferredSize());
        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        confirmBtn.addActionListener(_ -> {
            try {
                double amount = (Double) amountSpinner.getValue();
                BigDecimal rechargeAmount = BigDecimal.valueOf(amount)
                        .setScale(2, RoundingMode.HALF_UP);
                currentUser.setBalance(currentUser.getBalance().add(rechargeAmount));
                String result = UserJDBC.updateUser(currentUser);
                if (result.startsWith("修改成功")) {
                    refreshProfilePanel();
                    balanceLabel.setText(String.format("¥%.2f", currentUser.getBalance()));
                    dialog.dispose();
                }

                JOptionPane.showMessageDialog(
                        dialog,
                        result,
                        "提示",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "充值失败：" + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        cancelBtn.addActionListener(_ -> dialog.dispose());
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        amountSpinner.requestFocusInWindow();
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String getChineseFieldName(String fieldType) {
        return switch (fieldType) {
            case "username" -> "用户名";
            case "password" -> "密码";
            case "phone" -> "手机号";
            case "address" -> "收货地址";
            case "balance" -> "余额";
            case "remark" -> "备注";
            default -> "";
        };
    }

    private JComponent createInputField(String fieldType) {
        return switch (fieldType) {
            case "password" -> {
                JPanel panel = new JPanel(new GridLayout(2, 1));
                JPanel row1 = new JPanel(new BorderLayout(5, 0)); // 水平间距5px
                row1.add(new JLabel("输入密码："), BorderLayout.WEST);
                row1.add(new JPasswordField());
                JPanel row2 = new JPanel(new BorderLayout(5, 0));
                row2.add(new JLabel("确认密码："), BorderLayout.WEST);
                row2.add(new JPasswordField());

                panel.add(row1);
                panel.add(row2);
                yield panel;
            }
            default -> new JTextField(currentUser.getFieldValue(fieldType));
        };
    }
    private String getInputValue(String fieldType, JComponent field) {
        if (fieldType.equals("password")) {
            JPanel mainPanel = (JPanel) field;
            JPanel row1 = (JPanel) mainPanel.getComponent(0);
            JPasswordField password1 = (JPasswordField) row1.getComponent(1); // 输入框是行面板的第二个组件
            JPanel row2 = (JPanel) mainPanel.getComponent(1);
            JPasswordField password2 = (JPasswordField) row2.getComponent(1);
            String pw1 = new String(password1.getPassword());
            String pw2 = new String(password2.getPassword());
            if (!pw1.equals(pw2)) {
                JOptionPane.showMessageDialog(mainPanel, "两次密码输入不一致", "错误", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            return pw1;
        } else {
            return ((JTextField) field).getText();
        }
    }

    private void updateUserField(String fieldType, String value) {
        switch (fieldType) {
            case "username" -> currentUser.setUsername(value);
            case "password" -> currentUser.setPassword(BCrypt.hashpw(value, BCrypt.gensalt()));
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
        if (currentUser == null) {
            usernameLabel.setText("未登录");
            phoneLabel.setText("");
            addressLabel.setText("");
            remarkLabel.setText("");
            balanceLabel.setText("0.00");
            pendingOrdersLabel.setText("0");
        } else {
            currentUser=getUserById(currentUser.getId());
            usernameLabel.setText(currentUser.getUsername());
            String phone = currentUser.getPhone();
            phoneLabel.setText(phone != null && !phone.isEmpty() ? phone : "未设置");
            String address = currentUser.getAddress();
            addressLabel.setText(address != null && !address.isEmpty() ? address : "未设置");
            String remark = currentUser.getRemark();
            remarkLabel.setText(remark != null && !remark.isEmpty() ? remark : "无");
            balanceLabel.setText(currentUser.getBalance().toPlainString());
            int pendingOrdersCount = getTotalQuantityByUserId(currentUser.getId());
            pendingOrdersLabel.setText(String.valueOf(pendingOrdersCount));
        }
        profilePanel.revalidate();
        profilePanel.repaint();
    }

    private void showOrderDetailsWindow() {
        JFrame orderFrame = new JFrame("订单详情");
        orderFrame.setPreferredSize(new Dimension(800, 600));
        JPanel mainPanel = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));
        JLabel titleLabel = new JLabel("我的订单（待收货：" + getTotalQuantityByUserId(currentUser.getId()) + "）");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16));
        mainPanel.add(titleLabel, "wrap, gapbottom 15");
        List<Orderinfo> orders = getinfoById(currentUser.getId());
        JTable orderTable = getJTable(orders);
        mainPanel.add(new JScrollPane(orderTable), "grow, wrap");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton refreshBtn = new JButton("刷新");
        JButton closeBtn = new JButton("关闭");
        refreshBtn.addActionListener(_ -> refreshOrderData(orderTable));
        closeBtn.addActionListener(_ -> orderFrame.dispose());
        buttonPanel.add(refreshBtn);
        buttonPanel.add(closeBtn);
        mainPanel.add(buttonPanel, "gaptop 15");
        orderFrame.add(mainPanel);
        orderFrame.pack();
        orderFrame.setLocationRelativeTo(this);
        orderFrame.setVisible(true);
    }

    private JTable getJTable(List<Orderinfo> orders) {
        OrderTableModel model = new OrderTableModel(orders);
        JTable orderTable = new JTable(model);
        orderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = orderTable.rowAtPoint(e.getPoint());
                if (viewRow != -1) {
                    int modelRow = orderTable.convertRowIndexToModel(viewRow);
                    Orderinfo selectedOrder = model.getOrderAt(modelRow);
                    showOrderDetailsDialog(selectedOrder); // 显示详情弹窗
                }
            }
        });
        return orderTable;
    }

    private void refreshOrderData(JTable table) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                List<Orderinfo> orders = getinfoById(currentUser.getId());
                ((OrderTableModel) table.getModel()).setOrders(orders);
                return null;
            }
        };
        worker.execute();
    }
    static class OrderTableModel extends AbstractTableModel {
        private List<Orderinfo> orders;
        private final String[] columnNames = {"订单id","商品id" ,"商品名称", "款式","颜色","金额","数量","支付方式", "状态", "收货地址","下单时间","完成时间","订单备注"};
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        public OrderTableModel(List<Orderinfo> orders) {
            this.orders = new ArrayList<>(orders);
        }
        public void setOrders(List<Orderinfo> orders) {
            this.orders = new ArrayList<>(orders);
            fireTableDataChanged();
        }
        public Orderinfo getOrderAt(int row) {
            return orders.get(row);
        }
        @Override public int getRowCount() { return orders.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int column) { return columnNames[column]; }
        @Override
        public Object getValueAt(int row, int column) {
            Orderinfo order = orders.get(row);
            return switch (column) {
                case 0 -> order.getOrderid();
                case 1 -> order.getCommodityid();
                case 2 -> order.getCommodityName();
                case 3 -> order.getStyle();
                case 4 -> order.getColor();
                case 5 -> order.getAmount();
                case 6 -> order.getQuantity();
                case 7 -> order.getPaymentMethod();
                case 8 -> order.getStatus();
                case 9 -> order.getShippingAddress();
                case 10 -> order.getCreatedTime().format(formatter);
                case 11 -> order.getCreatedTime().format(formatter);
                case 12 -> order.getRemark();
                default -> null;
            };
        }
    }
    private void showOrderDetailsDialog(Orderinfo order) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        JDialog dialog = new JDialog();
        dialog.setTitle("订单详情 - " + order.getOrderid());
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        JPanel mainPanel = new JPanel(new MigLayout("wrap 1, insets 0", "[grow]", "[grow][]"));
        JPanel contentPanel = new JPanel(new MigLayout("wrap 2, insets 30",
                "[right]15[350,grow,fill]",
                "[]10[]10[]10[]10[]10[]10[]10[]10[]10[]10[]10[]10[]10[]10[grow]"));
        Font labelFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 14f);
        Font valueFont = UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f);
        addFormRow(contentPanel, "订单ID:", formatValue(order.getOrderid()), labelFont, valueFont);
        addFormRow(contentPanel, "商品ID:", formatValue(order.getCommodityid()), labelFont, valueFont);
        addFormRow(contentPanel, "商品名称:", formatValue(order.getCommodityName()), labelFont, valueFont);
        addFormRow(contentPanel, "款式:", formatValue(order.getStyle()), labelFont, valueFont);
        addFormRow(contentPanel, "颜色:", formatValue(order.getColor()), labelFont, valueFont);
        addFormRow(contentPanel, "单价:", formatCurrency(order.getAmount()), labelFont, valueFont);
        addFormRow(contentPanel, "下单数量:", formatValue(order.getQuantity()), labelFont, valueFont);
        BigDecimal amount = BigDecimal.valueOf(order.getAmount());
        BigDecimal quantity = BigDecimal.valueOf(order.getQuantity());
        BigDecimal total = amount.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        addFormRow(contentPanel, "总价:", formatCurrency(total.doubleValue()), labelFont, valueFont);
        addFormRow(contentPanel, "支付方式:", formatValue(order.getPaymentMethod()), labelFont, valueFont);
        addFormRow(contentPanel, "状态:", formatStatus(order.getStatus()), labelFont, valueFont);
        addFormRow(contentPanel, "收货地址:", formatAddress(order.getShippingAddress()), labelFont, valueFont);
        addFormRow(contentPanel, "下单时间:", order.getCreatedTime().format(formatter), labelFont, valueFont);
        addFormRow(contentPanel, "完成时间:", order.getCreatedTime().format(formatter), labelFont, valueFont);
        addFormRow(contentPanel, "备注:", formatRemarks(order.getRemark()), labelFont, valueFont);
        JLabel moreLabel = createLinkLabel();
        moreLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dialog.dispose();
                showProductDetails(order.getCommodityid());
            }
        });
        contentPanel.add(moreLabel, "span 2, center, gaptop 30");
        JPanel buttonPanel = new JPanel(new MigLayout("insets 10, align right"));
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(_ -> dialog.dispose());
        closeButton.setFont(labelFont.deriveFont(Font.PLAIN));
        closeButton.setFocusPainted(false);
        buttonPanel.add(closeButton, "gapright 20");
        mainPanel.add(new JScrollPane(contentPanel), "grow");
        mainPanel.add(buttonPanel, "growx, right");
        dialog.add(mainPanel);
        dialog.setMinimumSize(new Dimension(600, 750)); // 稍微增加最小高度
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
    private String formatValue(Object value) {
        if (value == null) {
            return "<html><i style='color:#999;'>无</i></html>";
        }
        return value.toString();
    }
    private String formatStatus(String status) {
        if (status == null || status.isEmpty()) {
            return "<html><i style='color:#999;'>无状态信息</i></html>";
        }
        return status;
    }
    private String formatCurrency(double amount) {
        return String.format("<html>¥ <b>%.2f</b></html>", amount);
    }
    private String formatAddress(String address) {
        return String.format("<html><div style='width: 320px;'>%s</div></html>",
                address.replaceAll(",", "<br/>"));
    }
    private String formatRemarks(String remarks) {
        if (remarks == null || remarks.isEmpty()) {
            return "<html><i style='color:#666;'>无备注信息</i></html>";
        }
        return String.format("<html><div style='width: 320px; color:#666;'>%s</div></html>", remarks);
    }

    private void addFormRow(JPanel panel, String label, Object value, Font labelFont, Font valueFont) {
        JLabel lbl = new JLabel(String.format("<html><b>%s</b></html>", label));
        lbl.setFont(labelFont);
        panel.add(lbl, "gap para");

        JLabel val = value instanceof JLabel ? (JLabel) value : new JLabel(value.toString());
        val.setFont(valueFont);
        panel.add(val);
    }
    private JLabel createLinkLabel() {
        JLabel label = new JLabel("<html><u>显示更多商品详情</u></html>");
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setForeground(new Color(0x0693E3));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                label.setText("<html><u style='color:#0679c4;'>显示更多商品详情</u></html>");
            }
            @Override
            public void mouseExited(MouseEvent e) {
                label.setText("<html><u style='color:#0693E3;'>显示更多商品详情</u></html>");
            }
        });
        return label;
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