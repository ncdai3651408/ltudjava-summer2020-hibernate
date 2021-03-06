package vn.name.ChanhDai.QuanLySinhVien.view;

import vn.name.ChanhDai.QuanLySinhVien.utils.*;
import vn.name.ChanhDai.QuanLySinhVien.dao.SinhVienDAO;
import vn.name.ChanhDai.QuanLySinhVien.entity.SinhVien;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class SinhVienView {
    JFrame mainFrame;
    JTable tableSinhVien;
    JComboBox<SimpleComboBoxItem> comboBoxMaLop;
    JButton importCSVButton;

    FileChooserView fileChooserView;

    JTextField textFieldMaSinhVien;
    JTextField textFieldHoTen;
    JComboBox<String> comboBoxGioiTinh;
    JTextField textFieldCMND;
    JTextField textFieldMaLop;

    JRadioButton radioButtonUpdate;
    JRadioButton radioButtonCreate;
    JRadioButton radioButtonDelete;

    JButton buttonSubmit;

    public SinhVienView() {
        createUI();
        createImportCSVUI();

        new GetComboBoxMaLopThread(comboBoxMaLop, "all").start();
    }

    static class GetSinhVienThread extends Thread {
        JTable table;
        String maLop;

        public GetSinhVienThread(JTable table, String maLop) {
            this.table = table;
            this.maLop = maLop;
        }

        public void run() {
            List<SinhVien> list;
            if (maLop.equals("all")) {
                list = SinhVienDAO.getList();
                System.out.println("SinhVienView -> GetSinhVienThread -> Get All");
            } else {
                list = SinhVienDAO.getListByMaLop(maLop);
                System.out.println("SinhVienView -> GetSinhVienThread -> Get by Lop(" + maLop + ")");
            }

            SimpleTableModel model = (SimpleTableModel) table.getModel();
            model.clearRows();

            for (SinhVien sinhVien : list) {
                model.addRow(TableUtils.toRow(sinhVien));
            }

            model.fireTableDataChanged();
        }
    }

    static class GetComboBoxMaLopThread extends Thread {
        JComboBox<SimpleComboBoxItem> comboBox;
        String defaultValue;

        GetComboBoxMaLopThread(JComboBox<SimpleComboBoxItem> comboBox, String defaultValue) {
            this.comboBox = comboBox;
            this.defaultValue = defaultValue;
        }

        @Override
        public void run() {
            List<String> list = SinhVienDAO.getLopList();
            SimpleComboBoxModel model = (SimpleComboBoxModel) comboBox.getModel();

            model.removeAllElements();
            model.addElement(new SimpleComboBoxItem("all", "Tất Cả"));

            for (String item : list) {
                model.addElement(new SimpleComboBoxItem(item, item));
            }

            model.setSelectedItem(defaultValue);
        }
    }

    static class CreateSinhVienThread extends Thread {
        JTable tableTarget;
        JButton buttonSubmit;
        SinhVien sinhVien;

        public CreateSinhVienThread(JTable tableTarget, JButton buttonSubmit, SinhVien sinhVien) {
            this.tableTarget = tableTarget;
            this.buttonSubmit = buttonSubmit;
            this.sinhVien = sinhVien;
        }

        @Override
        public void run() {
            buttonSubmit.setEnabled(false);

            boolean success = SinhVienDAO.create(sinhVien);
            buttonSubmit.setEnabled(true);

            if (success) {
                SimpleTableModel tableModel = (SimpleTableModel) tableTarget.getModel();
                tableModel.addRow(TableUtils.toRow(sinhVien));
                tableModel.fireTableDataChanged();

                JOptionPane.showMessageDialog(null, "Thêm Sinh Viên thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(null, "Thêm Sinh Viên thất bại!", "Thông Báo", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class UpdateSinhVienThread extends Thread {
        JTable tableTarget;
        JButton buttonSubmit;
        SinhVien sinhVien;
        int rowIndex;

        public UpdateSinhVienThread(JTable tableTarget, JButton buttonSubmit, SinhVien sinhVien, int rowIndex) {
            this.tableTarget = tableTarget;
            this.buttonSubmit = buttonSubmit;
            this.sinhVien = sinhVien;
            this.rowIndex = rowIndex;
        }

        @Override
        public void run() {
            buttonSubmit.setEnabled(false);

            boolean success = SinhVienDAO.update(sinhVien);
            buttonSubmit.setEnabled(true);

            if (success) {
                SimpleTableModel tableModel = (SimpleTableModel) tableTarget.getModel();
                tableModel.updateRow(rowIndex, TableUtils.toRow(sinhVien));
                tableModel.fireTableDataChanged();

                JOptionPane.showMessageDialog(null, "Cập nhật thông tin Sinh Viên thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(null, "Cập nhật thông tin Sinh Viên thất bại!", "Thông Báo", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class DeleteSinhVienThread extends Thread {
        JTable tableTarget;
        JButton buttonSubmit;
        SinhVien sinhVien;
        int rowIndex;

        public DeleteSinhVienThread(JTable tableTarget, JButton buttonSubmit, SinhVien sinhVien, int rowIndex) {
            this.tableTarget = tableTarget;
            this.buttonSubmit = buttonSubmit;
            this.sinhVien = sinhVien;
            this.rowIndex = rowIndex;
        }

        @Override
        public void run() {
            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Bạn chắn chắn muốn xóa Sinh Viên " + sinhVien.getMaSinhVien() + "?",
                "Xác Nhận",
                JOptionPane.OK_CANCEL_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) return;

            buttonSubmit.setEnabled(false);

            boolean success = SinhVienDAO.delete(sinhVien.getMaSinhVien());
            buttonSubmit.setEnabled(true);

            if (success) {
                SimpleTableModel tableModel = (SimpleTableModel) tableTarget.getModel();
                tableModel.removeRow(rowIndex);
                tableModel.fireTableDataChanged();
                tableTarget.clearSelection();
//                if (tableModel.getRowCount() > 0) {
//                    tableTarget.setRowSelectionInterval(0, 0);
//                }

                JOptionPane.showMessageDialog(null, "Xóa Sinh Viên thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(null, "Xóa Sinh Viên thất bại!", "Thông Báo", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class ImportCSVThread extends Thread {
        JTable tableDraft;
        JTable tableTarget;
        JButton buttonImport;
        JComboBox<SimpleComboBoxItem> comboBoxMaLop;

        ImportCSVThread(JTable tableDraft, JTable tableTarget, JButton buttonImport, JComboBox<SimpleComboBoxItem> comboBoxMaLop) {
            this.tableDraft = tableDraft;
            this.tableTarget = tableTarget;
            this.buttonImport = buttonImport;
            this.comboBoxMaLop = comboBoxMaLop;
        }

        @Override
        public void run() {
            buttonImport.setEnabled(false);

            SimpleTableModel tableDraftModel = (SimpleTableModel) tableDraft.getModel();
            SimpleTableModel tableTargetModel = (SimpleTableModel) tableTarget.getModel();

            int desiredImportQuantity = tableDraftModel.getRowCount();
            int actualImportQuantity = 0;

            for (int i = 0; i < desiredImportQuantity; ++i) {
                SinhVien sinhVien = TableUtils.parseSinhVien(tableDraftModel.getRow(i));

                boolean success = SinhVienDAO.create(sinhVien);
                if (success) {
                    tableDraftModel.setValueAt(i, 5, "[ Thành Công ]");
                    tableTargetModel.addRow(TableUtils.toRow(sinhVien));
                    ++actualImportQuantity;
                } else {
                    tableDraftModel.setValueAt(i, 5, "[ Lỗi ]");
                }

                tableDraftModel.fireTableDataChanged();
                tableTargetModel.fireTableDataChanged();
            }

            buttonImport.setEnabled(true);
            JOptionPane.showMessageDialog(null, "Đã nhập Danh Sách Sinh Viên thành công (" + actualImportQuantity + "/" + desiredImportQuantity + " Sinh Viên)", "Kết Quả", JOptionPane.INFORMATION_MESSAGE);

            new GetComboBoxMaLopThread(comboBoxMaLop, "all").start();
        }
    }

    SinhVien getSeletedRow() {
        int rowIndex = tableSinhVien.getSelectedRow();
        if (rowIndex == -1) {
            return null;
        }

        SimpleTableModel tableModel = (SimpleTableModel) tableSinhVien.getModel();
        return TableUtils.parseSinhVien(tableModel.getRow(rowIndex));
    }

    void setFormValues(String maSinhVien, String hoTen, String gioiTinh, String cmnd, String maLop) {
        textFieldMaSinhVien.setText(maSinhVien);
        textFieldHoTen.setText(hoTen);
        comboBoxGioiTinh.setSelectedItem(gioiTinh);
        textFieldCMND.setText(cmnd);
        textFieldMaLop.setText(maLop);
    }

    void setFormValuesBySeletedRow() {
        SinhVien sinhVien = getSeletedRow();
        if (sinhVien == null) return;

        this.setFormValues(
            sinhVien.getMaSinhVien(),
            sinhVien.getHoTen(),
            sinhVien.getGioiTinh(),
            sinhVien.getCmnd(),
            sinhVien.getMaLop()
        );
    }

    void setFormEnabled(boolean enabled) {
        textFieldMaSinhVien.setEnabled(enabled);
        textFieldHoTen.setEnabled(enabled);
        comboBoxGioiTinh.setEnabled(enabled);
        textFieldCMND.setEnabled(enabled);
        textFieldMaLop.setEnabled(enabled);
    }

    void resetForm() {
        setFormValues("", "", "", "", "");
    }

    void createUI() {
        mainFrame = new JFrame();
        mainFrame.setTitle("Sinh Viên");

        BorderLayout layout = new BorderLayout();
        mainFrame.setLayout(layout);

        JLabel title = new JLabel("Sinh Viên");
        title.setFont(new Font("", Font.BOLD, 24));
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));

        headerPanel.add(ViewUtils.createButtonBack(mainFrame, "Trở về"));
        headerPanel.add(title);

        JLabel labelFilter = new JLabel("Xem Lớp");

        comboBoxMaLop = ViewUtils.createComboBox(new SimpleComboBoxItem("all", "Tất Cả"));
        comboBoxMaLop.setPreferredSize(new Dimension(144, 24));
        comboBoxMaLop.addActionListener(e -> {
            SimpleComboBoxItem item = (SimpleComboBoxItem) comboBoxMaLop.getSelectedItem();
            if (item != null) {
                String maLop = item.getValue();
                System.out.println("StudentView -> comboBoxMaLop -> " + maLop);
                new GetSinhVienThread(tableSinhVien, maLop).start();
            }
        });

        JButton buttonSaveCSVTemplateFile = new JButton("Lưu File CSV Mẫu");
        buttonSaveCSVTemplateFile.setPreferredSize(new Dimension(144, 24));
        buttonSaveCSVTemplateFile.setBackground(Color.decode("#eeeeee"));
        buttonSaveCSVTemplateFile.addActionListener(e -> {
            String header = "maLop,maSinhVien,hoTen,gioiTinh,cmnd";
            String content = "17HCB,1742001,Nguyễn Văn A,Nam,123456789";
            CSVUtils.saveCSVTemplateFile(header, content);
        });

        importCSVButton = new JButton("Nhập File CSV");
        importCSVButton.setPreferredSize(new Dimension(144, 24));

        JPanel topMenuPanel = new JPanel();
        topMenuPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        topMenuPanel.setBackground(Color.WHITE);
        BoxLayout topMenuPanelLayout = new BoxLayout(topMenuPanel, BoxLayout.X_AXIS);
        topMenuPanel.setLayout(topMenuPanelLayout);
        topMenuPanel.add(labelFilter);
        topMenuPanel.add(Box.createRigidArea(new Dimension(8, 0)));

        JPanel comboBoxMaLopWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        comboBoxMaLopWrapper.setBackground(Color.WHITE);
        comboBoxMaLopWrapper.add(comboBoxMaLop);

        topMenuPanel.add(comboBoxMaLopWrapper);

        topMenuPanel.add(Box.createHorizontalGlue());
        topMenuPanel.add(buttonSaveCSVTemplateFile);
        topMenuPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        topMenuPanel.add(importCSVButton);

        JPanel centerPanel = new JPanel();
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(topMenuPanel, BorderLayout.PAGE_START);

        Vector<String> columnNames = new Vector<>();
        columnNames.add("MSSV");
        columnNames.add("Họ Tên");
        columnNames.add("Giới Tính");
        columnNames.add("CMND");
        columnNames.add("Lớp");

        tableSinhVien = ViewUtils.createSimpleTable(new SimpleTableModel(columnNames, null));

        ListSelectionModel selectionModel = tableSinhVien.getSelectionModel();
        selectionModel.addListSelectionListener(e -> {
            if (radioButtonCreate.isSelected()) {
                resetForm();
            } else {
                setFormValuesBySeletedRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableSinhVien);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        scrollPane.setPreferredSize(new Dimension(720, scrollPane.getPreferredSize().height));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        formPanel.add(form);

        radioButtonUpdate = ViewUtils.createRadioButton("Sửa SV", 96, 24, SwingConstants.CENTER);
        radioButtonUpdate.setSelected(true);
        radioButtonUpdate.addActionListener(e -> {
            System.out.println("radioButtonUpdate " + radioButtonUpdate.isSelected());

            resetForm();
            setFormValuesBySeletedRow();

            setFormEnabled(true);
            textFieldMaSinhVien.setEnabled(false);
            textFieldHoTen.requestFocus();
        });

        radioButtonCreate = ViewUtils.createRadioButton("Thêm SV", 96, 24, SwingConstants.CENTER);
        radioButtonCreate.addActionListener(e -> {
            System.out.println("radioButtonCreate " + radioButtonCreate.isSelected());

            resetForm();
            setFormEnabled(true);
            textFieldMaSinhVien.requestFocus();
        });

        radioButtonDelete = ViewUtils.createRadioButton("Xóa SV", 96, 24, SwingConstants.CENTER);
        radioButtonDelete.addActionListener(e -> {
            System.out.println("radioButtonDelete " + radioButtonDelete.isSelected());

            resetForm();
            setFormEnabled(false);
            setFormValuesBySeletedRow();
        });

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioButtonUpdate);
        buttonGroup.add(radioButtonCreate);
        buttonGroup.add(radioButtonDelete);

        form.add(radioButtonUpdate, ViewUtils.createFormConstraints(0, 0, 1, 0, 0, 4, 0));
        form.add(radioButtonCreate, ViewUtils.createFormConstraints(1, 0, 1, 0, 0, 4, 0));
        form.add(radioButtonDelete, ViewUtils.createFormConstraints(2, 0, 1, 0, 0, 4, 0));

        form.add(new JLabel("MSSV"), ViewUtils.createFormConstraints(0, 1, 1, 4, 0, 4, 0));
        textFieldMaSinhVien = new JTextField();
        textFieldMaSinhVien.setEnabled(false);
        form.add(textFieldMaSinhVien, ViewUtils.createFormConstraints(1, 1, 2, 4, 0, 4, 0));

        form.add(new JLabel("Họ và Tên"), ViewUtils.createFormConstraints(0, 2, 1, 4, 0, 4, 0));
        textFieldHoTen = new JTextField();
        form.add(textFieldHoTen, ViewUtils.createFormConstraints(1, 2, 2, 4, 0, 4, 0));

        form.add(new JLabel("Giới Tính"), ViewUtils.createFormConstraints(0, 3, 1, 4, 0, 4, 0));
        String[] gioiTinhList = {"Nam", "Nữ", "Khác"};
        comboBoxGioiTinh = new JComboBox<>(gioiTinhList);
        form.add(comboBoxGioiTinh, ViewUtils.createFormConstraints(1, 3, 2, 4, 0, 4, 0));

        form.add(new JLabel("CMND"), ViewUtils.createFormConstraints(0, 4, 1, 4, 0, 4, 0));
        textFieldCMND = new JTextField();
        form.add(textFieldCMND, ViewUtils.createFormConstraints(1, 4, 2, 4, 0, 4, 0));

        form.add(new JLabel("Mã Lớp"), ViewUtils.createFormConstraints(0, 5, 1, 4, 0, 4, 0));
        textFieldMaLop = new JTextField();
        form.add(textFieldMaLop, ViewUtils.createFormConstraints(1, 5, 2, 4, 0, 4, 0));

        buttonSubmit = new JButton("Thực Hiện");
        form.add(buttonSubmit, ViewUtils.createFormConstraints(0, 6, 3, 4, 0, 0, 0));

        buttonSubmit.addActionListener(e -> {
            String maSinhVien = textFieldMaSinhVien.getText();
            String hoTen = textFieldHoTen.getText();
            String gioiTinh = (String) comboBoxGioiTinh.getSelectedItem();
            String cmnd = textFieldCMND.getText();
            String maLop = textFieldMaLop.getText();

            if (maSinhVien.equals("") || hoTen.equals("") || gioiTinh == null || gioiTinh.equals("") || cmnd.equals("") || maLop.equals("")) {
                JOptionPane.showMessageDialog(mainFrame, "Bạn chưa nhập đủ thông tin!", "Thông Báo", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SinhVien sinhVien = new SinhVien();
            sinhVien.setMaSinhVien(maSinhVien);
            sinhVien.setHoTen(hoTen);
            sinhVien.setGioiTinh(gioiTinh);
            sinhVien.setCmnd(cmnd);
            sinhVien.setMaLop(maLop);

            int rowSelectedIndex = tableSinhVien.getSelectedRow();

            if (radioButtonCreate.isSelected()) {

                // Create
                System.out.println("buttonSubmit -> Create");
                new CreateSinhVienThread(tableSinhVien, buttonSubmit, sinhVien).start();

            } else if (radioButtonUpdate.isSelected()) {

                // Update
                System.out.println("buttonSubmit -> Update");
                new UpdateSinhVienThread(tableSinhVien, buttonSubmit, sinhVien, rowSelectedIndex).start();

            } else if (radioButtonDelete.isSelected()) {

                // Delete
                System.out.println("buttonSubmit -> Delete");
                new DeleteSinhVienThread(tableSinhVien, buttonSubmit, sinhVien, rowSelectedIndex).start();
            }

        });

        sidebarPanel.add(formPanel);

        Container pane = mainFrame.getContentPane();
        pane.add(headerPanel, BorderLayout.PAGE_START);
        pane.add(centerPanel, BorderLayout.CENTER);
        pane.add(sidebarPanel, BorderLayout.LINE_END);

        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
    }

    void createImportCSVUI() {
        fileChooserView = new FileChooserView(importCSVButton) {
            @Override
            public Vector<String> getColumnNames() {
                Vector<String> columnNames = new Vector<>();
                columnNames.add("MSSV");
                columnNames.add("Họ tên");
                columnNames.add("Giới tính");
                columnNames.add("CMND");
                columnNames.add("Lớp");
                columnNames.add("Trạng thái");
                return columnNames;
            }

            @Override
            public Vector<String> parseTableRow(String[] str) {
                SinhVien sinhVien = CSVUtils.parseSinhVien(str);

                if (sinhVien != null) {
                    Vector<String> row = TableUtils.toRow(sinhVien);
                    row.add("[ Đang chờ ]");
                    return row;
                }

                return null;
            }

            @Override
            public void startImport(JTable tablePreview, JButton buttonImport) {
                new ImportCSVThread(tablePreview, tableSinhVien, buttonImport,  comboBoxMaLop).start();
            }

            @Override
            public void customTable(JTable table) {

            }
        };
    }

    public void setVisible(boolean visible) {
        mainFrame.setVisible(visible);
    }

    public void setVisible(boolean visible, String maLop) {
        setVisible(visible);

        if (visible) {
            SimpleComboBoxModel model = (SimpleComboBoxModel)comboBoxMaLop.getModel();
            model.setSelectedItem(maLop);
        }
    }
}
