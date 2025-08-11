package bokchi.java.ui.user;

import javax.swing.*;
import java.awt.*;

public class ReceiptDialog extends JDialog {
    public ReceiptDialog(Window owner, String text) {
        super(owner, "영수증", ModalityType.APPLICATION_MODAL);
        setSize(420, 480);
        setLocationRelativeTo(owner);
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        add(new JScrollPane(area));
    }
}