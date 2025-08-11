package bokchi.java.ui.user;

import javax.swing.*;
import java.awt.*;

public class RewardChoiceDialog extends JDialog {
    private boolean useReward = false;

    public RewardChoiceDialog(Window owner, int balance, int threshold) {
        super(owner, "보상 사용", ModalityType.APPLICATION_MODAL);
        setSize(320,180);
        setLocationRelativeTo(owner);

        String msg = "잔여 스탬프: " + balance + " / 보상 임계치: " + threshold + "\n보상을 지금 사용할까요?";
        JLabel label = new JLabel("<html>" + msg.replace("\n","<br>") + "</html>");
        JButton btnUse = new JButton("지금 사용");
        JButton btnSave = new JButton("적립만");

        btnUse.addActionListener(e -> { useReward = true; dispose(); });
        btnSave.addActionListener(e -> { useReward = false; dispose(); });

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        center.add(label, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnSave);
        south.add(btnUse);

        setLayout(new BorderLayout());
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    public boolean showDialog() {
        setVisible(true);
        return useReward;
    }
}