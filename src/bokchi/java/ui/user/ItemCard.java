package bokchi.java.ui.user;

import javax.swing.*;

import bokchi.java.model.ItemVO;

import java.awt.*;
import java.awt.event.ActionListener;

public class ItemCard extends JPanel {
    private final ItemVO item; // ★ DB VO 객체 그대로 저장

    // DB ItemVO 기반 생성자
    public ItemCard(ItemVO item, ActionListener onAdd) {
        this.item = item;

        setLayout(new BorderLayout(8,6));
        setPreferredSize(new Dimension(220, 140));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E5E5)),
                BorderFactory.createEmptyBorder(10,10,10,10)
        ));

        // (옵션) 이미지 자리 — 현재는 placeholder
        JLabel thumb = new JLabel("IMG", SwingConstants.CENTER);
        thumb.setPreferredSize(new Dimension(72, 72));
        thumb.setOpaque(true);
        thumb.setBackground(new Color(0xF0F0F0));
        thumb.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));

        // 텍스트 구성
        JLabel lbTitle = new JLabel(item.getName());
        lbTitle.setFont(lbTitle.getFont().deriveFont(Font.BOLD));

        JLabel lbSub = new JLabel(item.getDescription() != null ? item.getDescription() : "");
        lbSub.setForeground(new Color(0x666666));

        JLabel lbPrice = new JLabel(item.getPrice() + " 원");
        lbPrice.setForeground(new Color(0x006241));
        lbPrice.setFont(lbPrice.getFont().deriveFont(Font.BOLD));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.add(lbTitle);
        text.add(Box.createVerticalStrut(2));
        text.add(lbSub);
        text.add(Box.createVerticalStrut(6));
        text.add(lbPrice);

        JButton btnAdd = new JButton("담기");
        btnAdd.addActionListener(onAdd);

        JPanel center = new JPanel(new BorderLayout(8,0));
        center.setOpaque(false);
        center.add(thumb, BorderLayout.WEST);
        center.add(text, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
        add(btnAdd, BorderLayout.SOUTH);
    }

    public ItemVO getItem() { return item; }
    public String getTitle() { return item.getName(); }
    public int getPrice() { return item.getPrice(); }
}