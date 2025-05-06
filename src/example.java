import GUI.Login;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
import javax.swing.*;
import java.awt.*;
public static void UImanage(){
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
}


public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        FlatMTMaterialLighterIJTheme.setup();
        UImanage();
        Login loginFrame = new Login();
    });
}
