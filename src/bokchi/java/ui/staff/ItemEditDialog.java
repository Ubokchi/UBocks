package bokchi.java.ui.staff;

import bokchi.java.model.ItemVO;
import bokchi.java.model.enums.ItemType;

import javax.swing.*;
import java.awt.*;

public class ItemEditDialog extends JDialog {
    private final JComboBox<ItemType> cbType = new JComboBox<>(ItemType.values());
    private final JTextField tfName = new JTextField(16);
    private final JSpinner spPrice = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 100));
    private final JSpinner spStock = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));
    private final JCheckBox ckActive = new JCheckBox("판매중");
    private final JCheckBox ckStamp = new JCheckBox("스탬프 적립 대상(DRINK만)");

    private final JButton btnOk = new JButton("확인");
    private final JButton btnCancel = new JButton("취소");

    private ItemVO result; // OK일 때만

    public ItemEditDialog(Window owner, ItemVO origin) {
        super(owner, origin == null ? "상품 등록" : "상품 수정", ModalityType.APPLICATION_MODAL);
        setSize(380, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        // 폼
        JPanel form = new JPanel(new GridBagLayout());
        int y = 0;

        form.add(new JLabel("Type"), gbc(0, y, GridBagConstraints.WEST, 0.0));
        form.add(cbType,           gbcFill(1, y++, 1.0));

        form.add(new JLabel("Name"), gbc(0, y, GridBagConstraints.WEST, 0.0));
        form.add(tfName,            gbcFill(1, y++, 1.0));

        form.add(new JLabel("Price"), gbc(0, y, GridBagConstraints.WEST, 0.0));
        form.add(spPrice,            gbcFill(1, y++, 1.0));

        form.add(new JLabel("Stock"), gbc(0, y, GridBagConstraints.WEST, 0.0));
        form.add(spStock,            gbcFill(1, y++, 1.0));

        // 체크박스는 오른쪽 컬럼만
        form.add(ckActive, gbc(1, y++, GridBagConstraints.WEST, 0.0));
        form.add(ckStamp,  gbc(1, y++, GridBagConstraints.WEST, 0.0));

        // 버튼
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnCancel);
        buttons.add(btnOk);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        // 초기값 세팅
        if (origin != null) {
            cbType.setSelectedItem(origin.getType());
            tfName.setText(origin.getName());
            spPrice.setValue(origin.getPrice());
            spStock.setValue(origin.getStock());
            ckActive.setSelected(origin.isActive());
            ckStamp.setSelected(origin.isStampEligible());
        } else {
            ckActive.setSelected(true);
            ckStamp.setSelected(true);
        }
        syncStampCheckbox(); // 타입에 따라 스탬프 체크박스 활성/비활성

        // 타입 변경 시 스탬프 체크박스 동기화
        cbType.addActionListener(e -> syncStampCheckbox());

        // 리스너
        btnOk.addActionListener(e -> onOk());
        btnCancel.addActionListener(e -> { result = null; dispose(); });
        getRootPane().setDefaultButton(btnOk);
    }

    private void syncStampCheckbox() {
        ItemType t = (ItemType) cbType.getSelectedItem();
        boolean isDrink = (t == ItemType.DRINK);
        ckStamp.setEnabled(isDrink);
        if (!isDrink) ckStamp.setSelected(false);
    }

    private void onOk() {
        String name = tfName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이름을 입력하세요.");
            tfName.requestFocus();
            return;
        }

        ItemVO vo = new ItemVO();
        vo.setType((ItemType) cbType.getSelectedItem());
        vo.setName(name);
        vo.setPrice((Integer) spPrice.getValue());
        vo.setStock((Integer) spStock.getValue());
        vo.setActive(ckActive.isSelected());
        vo.setStampEligible(ckStamp.isSelected());

        // DRINK가 아니면 적립 불가
        if (vo.getType() != ItemType.DRINK) {
            vo.setStampEligible(false);
        }

        result = vo;
        dispose();
    }

    // OK이면 ItemVO 반환, 취소면 null
    public ItemVO showDialog() {
        setVisible(true);
        return result;
    }

    // GBC 헬퍼 -> 뭔지 잘 모르겠음
    // gbc 재사용으로 인한 상태 누수가 생겨서 오류가 발생했음.
    // -> 그래서 컴포넌트 만들 때마다 새 gbc를 만들기 위한 gbc 헬퍼를 만든 것
    private static GridBagConstraints gbc(int x, int y, int anchor, double weightx) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y;
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = anchor;
        g.weightx = weightx;
        return g;
    }
    private static GridBagConstraints gbcFill(int x, int y, double weightx) {
        GridBagConstraints g = gbc(x, y, GridBagConstraints.WEST, weightx);
        g.fill = GridBagConstraints.HORIZONTAL;
        return g;
    }
}