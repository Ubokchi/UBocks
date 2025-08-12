package bokchi.java.ui.staff;

import bokchi.java.dao.jdbc.JdbcItemDaoImple;
import bokchi.java.model.ItemVO;
import bokchi.java.model.enums.ItemType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ItemManagePanel extends JPanel {
    private final JdbcItemDaoImple itemDao = JdbcItemDaoImple.getInstance();

    private final JTextField tfSearch = new JTextField(16);
    private final JButton btnSearch = new JButton("검색");
    private final JButton btnRefresh = new JButton("새로고침");

    private final JButton btnAdd = new JButton("추가");
    private final JButton btnEdit = new JButton("수정");
    private final JButton btnDelete = new JButton("삭제");
    private final JButton btnToggleActive = new JButton("활성/비활성");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Type", "Name", "Price", "Stock", "Active", "Stamp"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) {
            return switch (c) {
                case 0,3,4 -> Integer.class;
                case 5,6 -> Boolean.class;
                default -> String.class;
            };
        }
    };
    private final JTable table = new JTable(model);

    public ItemManagePanel() {
        super(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        // 상단: 검색/새로고침
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        top.add(new JLabel("이름"));
        top.add(tfSearch);
        top.add(btnSearch);
        top.add(Box.createHorizontalStrut(12));
        top.add(btnRefresh);

        // 하단: CRUD 버튼들
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        bottom.add(btnAdd);
        bottom.add(btnEdit);
        bottom.add(btnDelete);
        bottom.add(btnToggleActive);

        // 테이블
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // 리스너
        btnRefresh.addActionListener(e -> loadAll());
        btnSearch.addActionListener(e -> search());
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnToggleActive.addActionListener(e -> onToggleActive());

        // 초기 로드
        loadAll();
    }

    private void loadAll() {
        model.setRowCount(0);
        List<ItemVO> list = itemDao.findAll();
        for (ItemVO v : list) addRow(v);
    }

    private void search() {
        String q = tfSearch.getText().trim();
        model.setRowCount(0);
        if (q.isEmpty()) { loadAll(); return; }
        // 판매중만 검색하고 싶다면 searchActiveByText 사용, 전체 검색이면 findAll 후 필터
        for (ItemVO v : itemDao.searchActiveByText(q)) addRow(v);
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "검색 결과가 없습니다.");
        }
    }

    private void addRow(ItemVO v) {
        model.addRow(new Object[]{
                v.getItemId(),
                v.getType().name(),
                v.getName(),
                v.getPrice(),
                v.getStock(),
                v.isActive(),
                v.isStampEligible()
        });
    }

    private Integer getSelectedItemId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return (Integer) model.getValueAt(row, 0);
    }

    private void onAdd() {
        ItemEditDialog dlg = new ItemEditDialog(SwingUtilities.getWindowAncestor(this), null);
        ItemVO vo = dlg.showDialog();
        if (vo == null) return; // cancel
        int inserted = itemDao.insert(vo);
        if (inserted > 0) {
            JOptionPane.showMessageDialog(this, "등록되었습니다.");
            loadAll();
        } else {
            JOptionPane.showMessageDialog(this, "등록 실패");
        }
    }

    private void onEdit() {
        Integer id = getSelectedItemId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "수정할 항목을 선택하세요.");
            return;
        }
        ItemVO origin = itemDao.findById(id);
        if (origin == null) {
            JOptionPane.showMessageDialog(this, "해당 상품을 찾을 수 없습니다.");
            return;
        }
        ItemEditDialog dlg = new ItemEditDialog(SwingUtilities.getWindowAncestor(this), origin);
        ItemVO edited = dlg.showDialog();
        if (edited == null) return; // cancel
        edited.setItemId(id);
        int updated = itemDao.update(edited);
        if (updated > 0) {
            JOptionPane.showMessageDialog(this, "수정되었습니다.");
            loadAll();
        } else {
            JOptionPane.showMessageDialog(this, "수정 실패");
        }
    }

    private void onDelete() {
        Integer id = getSelectedItemId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "삭제할 항목을 선택하세요.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "정말 삭제할까요?", "확인", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int deleted = itemDao.delete(id);
        if (deleted > 0) {
            JOptionPane.showMessageDialog(this, "삭제되었습니다.");
            loadAll();
        } else {
            JOptionPane.showMessageDialog(this, "삭제 실패");
        }
    }

    private void onToggleActive() {
        Integer id = getSelectedItemId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "대상 항목을 선택하세요.");
            return;
        }
        ItemVO v = itemDao.findById(id);
        if (v == null) {
            JOptionPane.showMessageDialog(this, "해당 상품을 찾을 수 없습니다.");
            return;
        }
        v.setActive(!v.isActive());
        int updated = itemDao.update(v);
        if (updated > 0) {
            JOptionPane.showMessageDialog(this, v.isActive() ? "활성화되었습니다." : "비활성화되었습니다.");
            loadAll();
        } else {
            JOptionPane.showMessageDialog(this, "상태 변경 실패");
        }
    }
}