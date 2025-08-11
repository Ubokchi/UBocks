package bokchi.java.ui.user;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ItemGridPanel extends JPanel {
    private final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
    private final JScrollPane scroll = new JScrollPane(flow);
    private final List<JPanel> cards = new ArrayList<>();

    public ItemGridPanel() {
        super(new BorderLayout());
        flow.setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        setOpaque(false);
    }

    public void addCard(JPanel card) {
        cards.add(card);
        flow.add(card);
    }

    public void clear() {
        cards.clear();
        flow.removeAll();
    }

    public void refresh() {
        revalidate();
        repaint();
    }

    public void filterByText(String q) {
        String query = q == null ? "" : q.toLowerCase();
        flow.removeAll();

        for (JPanel c : cards) {
            if (c instanceof ItemCard itemCard) {
                boolean match = itemCard.getTitle().toLowerCase().contains(query);
                if (match) flow.add(c);
            } else {
                flow.add(c);
            }
        }

        refresh();
    }
}