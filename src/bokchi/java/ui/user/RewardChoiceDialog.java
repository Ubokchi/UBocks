// src/bokchi/java/ui/user/RewardCartChoiceDialog.java
package bokchi.java.ui.user;

import bokchi.java.dao.jdbc.JdbcItemDaoImple;
import bokchi.java.model.ItemVO;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

public class RewardChoiceDialog extends JDialog {
    private boolean useReward = false;
    private Integer selectedItemId = null;

    public static class Entry {
        public final int itemId;
        public final String label; // ex) "아메리카노 x3"
        public Entry(int itemId, String label) { this.itemId = itemId; this.label = label; }
        @Override public String toString() { return label; }
    }

    public RewardChoiceDialog(Window owner,
                                  java.util.List<CartPanel.CartLine> cartLines,
                                  int balance, int threshold) {
        super(owner, "무료 쿠폰 적용 대상 선택", ModalityType.APPLICATION_MODAL);
        setSize(420, 260);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10,10));

        boolean canRedeem = balance >= threshold;

        JLabel top = new JLabel(String.format(
                "잔여 스탬프: %d / 필요: %d   %s",
                balance, threshold, canRedeem ? "→ 사용 가능" : "→ 사용 불가"
        ));
        top.setBorder(BorderFactory.createEmptyBorder(12,12,0,12));
        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8,8));
        center.setBorder(BorderFactory.createEmptyBorder(0,12,0,12));

        JComboBox<Entry> combo = new JComboBox<>();
        combo.setEnabled(canRedeem);

        if (canRedeem) {
            var itemDao = JdbcItemDaoImple.getInstance();
            List<Entry> eligible = new ArrayList<>();
            for (var line : cartLines) {
                if (line.unitPrice <= 0 || line.qty <= 0) continue; // 무료/0원/수량 0 제외
                ItemVO vo = itemDao.findById(line.itemId);
                if (vo != null && vo.isStampEligible() && vo.isActive()) {
                    eligible.add(new Entry(line.itemId, line.name + " x" + line.qty));
                }
            }
            if (eligible.isEmpty()) {
                center.add(new JLabel("장바구니에 무료 적용 가능한 상품이 없습니다."), BorderLayout.CENTER);
            } else {
                for (Entry e : eligible) combo.addItem(e);
                center.add(new JLabel("무료로 적용할 상품을 선택하세요:"), BorderLayout.NORTH);
                center.add(combo, BorderLayout.CENTER);
            }
        } else {
            JTextArea ta = new JTextArea("스탬프가 임계치에 도달하면 장바구니에서 1잔을 무료로 선택할 수 있어요.");
            ta.setEditable(false);
            ta.setOpaque(false);
            center.add(ta, BorderLayout.CENTER);
        }
        add(center, BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnSkip = new JButton("적립만");
        JButton btnUse = new JButton("지금 사용");
        btnUse.setEnabled(canRedeem);
        south.add(btnSkip);
        south.add(btnUse);
        add(south, BorderLayout.SOUTH);

        btnSkip.addActionListener(e -> { useReward = false; selectedItemId = null; dispose(); });
        btnUse.addActionListener(e -> {
            if (!canRedeem) return;
            var sel = (Entry) combo.getSelectedItem();
            if (sel == null) {
                JOptionPane.showMessageDialog(this, "무료 적용 대상을 선택하세요.");
                return;
            }
            useReward = true;
            selectedItemId = sel.itemId;
            dispose();
        });
    }

    public boolean showDialog() { setVisible(true); return useReward; }
    public Integer getSelectedItemId() { return selectedItemId; }
}