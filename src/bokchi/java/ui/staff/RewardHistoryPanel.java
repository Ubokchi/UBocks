package bokchi.java.ui.staff;

import bokchi.java.dao.jdbc.JdbcRewardHistoryDaoImple;
import bokchi.java.model.RewardHistoryVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class RewardHistoryPanel extends JPanel {

    private final JTextField tfUserId = new JTextField(8);
    private final JTextField tfFrom = new JTextField(10);  // YYYY-MM-DD
    private final JTextField tfTo   = new JTextField(10);  // YYYY-MM-DD
    private final JTextField tfRecent = new JTextField("20", 4);

    private final JButton btnSearch = new JButton("검색");
    private final JButton btnRecent = new JButton("최근 N건");
    private final JButton btnAll    = new JButton("전체");

    private final JTable table;
    private final DefaultTableModel model;

    public RewardHistoryPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createTitledBorder("리워드 이력"));

        // 상단 검색 바
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        north.add(new JLabel("회원ID:"));
        north.add(tfUserId);
        north.add(new JLabel("기간:"));
        north.add(tfFrom);
        north.add(new JLabel("~"));
        north.add(tfTo);
        north.add(btnSearch);
        north.add(new JLabel("최근"));
        north.add(tfRecent);
        north.add(new JLabel("건"));
        north.add(btnRecent);
        north.add(btnAll);
        add(north, BorderLayout.NORTH);

        // 테이블
        model = new DefaultTableModel(
                new Object[]{"ID", "회원ID", "주문ID", "증감(±)", "사유", "발생시각"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 조회 전용
            }
        };

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 버튼 동작
        btnSearch.addActionListener(e -> doSearch());
        btnRecent.addActionListener(e -> loadRecent());
        btnAll.addActionListener(e -> loadAll());

        // 초기: 비움
    }

    private void doSearch() {
        String uidText = tfUserId.getText().trim();
        if (uidText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "회원ID를 입력하세요.");
            tfUserId.requestFocus();
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(uidText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "회원ID는 숫자여야 합니다.");
            tfUserId.requestFocus();
            return;
        }

        String fromText = tfFrom.getText().trim();
        String toText   = tfTo.getText().trim();

        JdbcRewardHistoryDaoImple dao = JdbcRewardHistoryDaoImple.getInstance();
        List<RewardHistoryVO> list;

        try {
            if (!fromText.isEmpty() && !toText.isEmpty()) {
                LocalDate from = LocalDate.parse(fromText);
                LocalDate to   = LocalDate.parse(toText);
                list = dao.findByUserIdBetween(userId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());
            } else {
                list = dao.findByUserId(userId);
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "기간은 YYYY-MM-DD 형식으로 입력하세요.");
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "조회 중 오류: " + ex.getMessage());
            return;
        }

        fillTable(list);
    }

    private void loadRecent() {
        String uidText = tfUserId.getText().trim();
        if (uidText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "회원ID를 입력하세요.");
            tfUserId.requestFocus();
            return;
        }

        int userId;
        int limit;
        try {
            userId = Integer.parseInt(uidText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "회원ID는 숫자여야 합니다.");
            tfUserId.requestFocus();
            return;
        }
        try {
            limit = Integer.parseInt(tfRecent.getText().trim());
            if (limit <= 0) limit = 20;
        } catch (NumberFormatException ex) {
            limit = 20;
        }

        try {
            var dao = JdbcRewardHistoryDaoImple.getInstance();
            var list = dao.findRecentByUserId(userId, limit);
            fillTable(list);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "조회 중 오류: " + ex.getMessage());
        }
    }

    private void loadAll() {
        try {
            var dao = JdbcRewardHistoryDaoImple.getInstance();
            var list = dao.findAll();
            fillTable(list);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "조회 중 오류: " + ex.getMessage());
        }
    }

    private void fillTable(List<RewardHistoryVO> list) {
        model.setRowCount(0);
        if (list == null) return;
        for (var vo : list) {
            model.addRow(new Object[]{
                    vo.getHistoryId(),
                    vo.getUserId(),
                    vo.getOrderId(),
                    vo.getDelta(),            // +적립 / -사용 / ±조정
                    vo.getReason(),           // EARN / REDEEM / ADJUST
                    vo.getCreatedAt()         // LocalDateTime
            });
        }
    }
}