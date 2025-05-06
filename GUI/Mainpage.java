/*
 * Created by JFormDesigner on Sun May 04 23:25:49 CST 2025
 */

package GUI;

import java.awt.*;
import javax.swing.*;

/**
 * @author Nahida
 */
public class Mainpage {
    public Mainpage() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        shop = new JPanel();
        tabbedPane3 = new JTabbedPane();
        first = new JPanel();
        chart = new JPanel();
        user = new JPanel();

        //======== shop ========
        {
            shop.setLayout(new CardLayout());

            //======== tabbedPane3 ========
            {
                tabbedPane3.setMinimumSize(new Dimension(700, 500));
                tabbedPane3.setPreferredSize(new Dimension(700, 500));

                //======== first ========
                {
                    first.setLayout(null);

                    {
                        // compute preferred size
                        Dimension preferredSize = new Dimension();
                        for(int i = 0; i < first.getComponentCount(); i++) {
                            Rectangle bounds = first.getComponent(i).getBounds();
                            preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                            preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                        }
                        Insets insets = first.getInsets();
                        preferredSize.width += insets.right;
                        preferredSize.height += insets.bottom;
                        first.setMinimumSize(preferredSize);
                        first.setPreferredSize(preferredSize);
                    }
                }
                tabbedPane3.addTab("\u4e3b\u9875", first);

                //======== chart ========
                {
                    chart.setLayout(new CardLayout());
                }
                tabbedPane3.addTab("\u8d2d\u7269\u8f66", chart);

                //======== user ========
                {
                    user.setLayout(new CardLayout());
                }
                tabbedPane3.addTab("\u6211\u7684", user);
            }
            shop.add(tabbedPane3, "card1");
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel shop;
    private JTabbedPane tabbedPane3;
    private JPanel first;
    private JPanel chart;
    private JPanel user;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
