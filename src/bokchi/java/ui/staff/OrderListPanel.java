package bokchi.java.ui.staff;

import bokchi.java.dao.jdbc.JdbcOrderDaoImple;
import bokchi.java.model.OrderVO;
import bokchi.java.model.enums.OrderStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 주문 조회 + 상태 변경 패널 */
public class OrderListPanel extends JPanel {

    private final JTable table;
    private final DefaultTableModel model;

    // 상단바
    private final JButton btnReload = new JButton("새로고침");
    private final JComboBox<OrderStatus> cbStatus =
            new JComboBox<>(OrderStatus.values());
    private final JButton btnChange = new JButton("상태 변경");

    private final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public OrderListPanel() {
        super(new BorderLayout(8, 8));

        // 테이블 모델
        model = new DefaultTableModel(new Object[]{
                "주문번호", "사용자ID", "상태", "총액(원)", "주문시각"
        }, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return false;
            }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0,1 -> Integer.class;
                    case 3 -> Integer.class;
                    default -> String.class;
                };
            }
        };

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 숫자 컬럼 우측정렬
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setCellRenderer(right);

        // 상단 바
        JPanel top = new JPanel(new BorderLayout());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        left.add(btnReload);                 

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightPanel.add(new JLabel("새 상태:"));
        rightPanel.add(cbStatus);
        rightPanel.add(btnChange);            

        top.add(left, BorderLayout.WEST);
        top.add(rightPanel, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 이벤트
        btnReload.addActionListener(e -> reloadFromDb());
        btnChange.addActionListener(e -> changeStatus());

        // 최초 로드
        reloadFromDb();
    }

    /** DB에서 주문 목록을 다시 불러와 테이블 갱신 */
    private void reloadFromDb() {
        try {
            var dao = JdbcOrderDaoImple.getInstance();
            List<OrderVO> list = dao.findAll();

            model.setRowCount(0);
            for (OrderVO vo : list) {
                model.addRow(new Object[]{
                        vo.getOrderId(),
                        vo.getUserId() == null ? 0 : vo.getUserId(),
                        vo.getStatus().name(),
                        vo.getTotalAmount(),
                        vo.getOrderTime() == null ? "" : FMT.format(vo.getOrderTime())
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "주문 목록을 불러오는 중 오류: " + ex.getMessage());
        }
    }

    /** 선택된 주문의 상태를 콤보박스 선택값으로 변경 */
    private void changeStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "상태를 변경할 주문을 선택하세요.");
            return;
        }
        int orderId = (Integer) model.getValueAt(row, 0);
        OrderStatus newStatus = (OrderStatus) cbStatus.getSelectedItem();
        if (newStatus == null) {
            JOptionPane.showMessageDialog(this, "변경할 상태를 선택하세요.");
            return;
        }

        // 이미 같은 상태면 패스
        String cur = (String) model.getValueAt(row, 2);
        if (cur.equals(newStatus.name())) {
            JOptionPane.showMessageDialog(this, "이미 " + newStatus.name() + " 상태입니다.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "주문 #" + orderId + " 상태를 " + cur + " → " + newStatus.name() + " 로 변경할까요?",
                "확인", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            var dao = JdbcOrderDaoImple.getInstance();
            int updated = dao.updateStatus(orderId, newStatus.name());
            if (updated == 1) {
                // 테이블 즉시 반영
                model.setValueAt(newStatus.name(), row, 2);
                JOptionPane.showMessageDialog(this, "상태가 변경되었습니다.");
            } else {
                JOptionPane.showMessageDialog(this, "상태 변경 실패(대상 없음).");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "상태 변경 중 오류: " + ex.getMessage());
        }
    }
}