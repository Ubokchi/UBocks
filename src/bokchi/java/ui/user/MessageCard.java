// src/bokchi/java/ui/user/MessageCard.java
package bokchi.java.ui.user;

import javax.swing.*;
import java.awt.*;

public class MessageCard extends JPanel {
    public MessageCard(String message) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(220, 140));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(0xE5E5E5)));

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setForeground(Color.GRAY);

        add(label, BorderLayout.CENTER);
    }
}