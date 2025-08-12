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

    private ItemVO result; // OK 눌렀을 때만

    public ItemEditDialog(Window owner, ItemVO origin) {
        super(owner, origin == null ? "상품 등록" : "상품 수정", ModalityType.APPLICATION_MODAL);
        setSize(360, 280);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.anchor = GridBagConstraints.WEST;

        int y = 0;
        c.gridx = 0; c.gridy = y; form.add(new JLabel("Type"), c);
        c.gridx = 1; form.add(cbType, c); y++;

        c.gridx = 0; c.gridy = y; form.add(new JLabel("Name"), c);
        c.gridx = 1; form.add(tfName, c); y++;

        c.gridx = 0; c.gridy = y; form.add(new JLabel("Price"), c);
        c.gridx = 1; form.add(spPrice, c); y++;

        c.gridx = 0; c.gridy = y; form.add(new JLabel("Stock"), c);
        c.gridx = 1; form.add(spStock, c); y++;

        c.gridx = 1; c.gridy = y; form.add(ckActive, c); y++;
        c.gridx = 1; c.gridy = y; form.add(ckStamp, c); y++;

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnCancel);
        buttons.add(btnOk);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        // 초기값 (수정 모드)
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

        // 리스너
        btnOk.addActionListener(e -> onOk());
        btnCancel.addActionListener(e -> { result = null; dispose(); });
        getRootPane().setDefaultButton(btnOk);
    }

    private void onOk() {
        String name = tfName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이름을 입력하세요.");
            tfName.requestFocus(); return;
        }
        ItemVO vo = new ItemVO();
        vo.setType((ItemType) cbType.getSelectedItem());
        vo.setName(name);
        vo.setPrice((Integer) spPrice.getValue());
        vo.setStock((Integer) spStock.getValue());
        vo.setActive(ckActive.isSelected());
        vo.setStampEligible(ckStamp.isSelected());
        // 정책: DRINK가 아니면 자동으로 적립 불가
        if (vo.getType() != ItemType.DRINK) vo.setStampEligible(false);
        result = vo;
        dispose();
    }

    // OK이면 ItemVO, 취소면 null
    public ItemVO showDialog() {
        setVisible(true);
        return result;
    }
}