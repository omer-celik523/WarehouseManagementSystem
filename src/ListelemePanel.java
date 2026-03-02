import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ListelemePanel extends JPanel {

    public ListelemePanel(AnaEkran anaEkran) {
        setBackground(anaEkran.getMainBg());
        setBorder(new EmptyBorder(30, 40, 30, 40));
        setLayout(new BorderLayout(0, 20));

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        JLabel baslik = new JLabel("Stok Listesi");
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 28));
        baslik.setForeground(anaEkran.getHeaderTextColor());
        topPanel.add(baslik, BorderLayout.CENTER);

        JPanel aramaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        aramaPanel.setOpaque(false);

        JComboBox<String> cmbKategoriFiltre = anaEkran.getCmbKategoriFiltre();

        JTextField txtArama = new JTextField(15);
        txtArama.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "🔍 Seri No / Ad Ara...");
        txtArama.putClientProperty(FlatClientProperties.STYLE, anaEkran.getInputStyle());
        txtArama.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtArama.setPreferredSize(new Dimension(200, 45));

        JButton btnPdfAktar = new JButton("📄 PDF");
        btnPdfAktar.setBackground(new Color(231, 76, 60));
        btnPdfAktar.setForeground(Color.WHITE);
        btnPdfAktar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnPdfAktar.setPreferredSize(new Dimension(110, 45));
        btnPdfAktar.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);

        JButton btnExcelAktar = new JButton("📊 Excel");
        btnExcelAktar.setBackground(new Color(39, 174, 96));
        btnExcelAktar.setForeground(Color.WHITE);
        btnExcelAktar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnExcelAktar.setPreferredSize(new Dimension(110, 45));
        btnExcelAktar.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);

        cmbKategoriFiltre.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) anaEkran.listeyiFiltrele(txtArama.getText());
        });

        // Dinamik Filtreleme (Real-time Search): Kullanıcı arama kutusuna her harf girdiğinde 'KeyReleased'
        // eventi tetiklenir ve tablo anlık olarak filtrelenir. Ekstra bir 'Ara' butonuna basmaya gerek kalmaz (Modern UX).
        txtArama.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { anaEkran.listeyiFiltrele(txtArama.getText()); }
        });

        btnPdfAktar.addActionListener(e -> anaEkran.pdfRaporAl());
        btnExcelAktar.addActionListener(e -> anaEkran.excelRaporAl());

        aramaPanel.add(cmbKategoriFiltre);
        aramaPanel.add(txtArama);
        aramaPanel.add(btnExcelAktar);
        aramaPanel.add(btnPdfAktar);

        topPanel.add(aramaPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JTable tablo = new JTable(anaEkran.getTabloModeli());
        tablo.setRowHeight(40);
        tablo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tablo.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tablo.setFillsViewportHeight(true);
        tablo.setShowGrid(true);
        tablo.setShowVerticalLines(true);
        tablo.setShowHorizontalLines(true);
        tablo.setGridColor(anaEkran.getTableGridColor());
        tablo.setIntercellSpacing(new Dimension(1, 1));
        tablo.setBackground(anaEkran.getTableBg());
        tablo.setForeground(anaEkran.getHeaderTextColor());
        tablo.setSelectionBackground(anaEkran.isDarkMode() ? new Color(9, 71, 113) : new Color(214, 234, 248));
        tablo.setSelectionForeground(anaEkran.isDarkMode() ? Color.WHITE : Color.BLACK);

        tablo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        tablo.getTableHeader().setBackground(anaEkran.isDarkMode() ? new Color(41, 128, 185) : new Color(52, 152, 219));
        tablo.getTableHeader().setForeground(Color.WHITE);

        tablo.getColumnModel().getColumn(0).setPreferredWidth(160);
        tablo.getColumnModel().getColumn(1).setPreferredWidth(250);
        tablo.getColumnModel().getColumn(2).setPreferredWidth(150);
        tablo.getColumnModel().getColumn(3).setPreferredWidth(80);
        tablo.getColumnModel().getColumn(4).setPreferredWidth(120);
        tablo.getColumnModel().getColumn(5).setPreferredWidth(140);
        tablo.getColumnModel().getColumn(6).setPreferredWidth(300);
        tablo.getColumnModel().getColumn(7).setPreferredWidth(180);

        // Custom Cell Rendering: JTable standart haliyle verileri dümdüz ekrana basar.
        // Ben DefaultTableCellRenderer sınıfını Override ederek (Çok biçimlilik) tablonun içindeki
        // hücrelerin sağına soluna padding (boşluk) ekledim ve sayısal değerleri merkeze hizaladım.
        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBorder(new EmptyBorder(0, 10, 0, 10));

                // Miktar, Fiyat, Toplam Değer gibi sayısal alanları (sütun 3,4,5,6) okumayı kolaylaştırmak için ortalıyoruz.
                if (column == 3 || column == 4 || column == 5 || column == 6) l.setHorizontalAlignment(JLabel.CENTER);
                else l.setHorizontalAlignment(JLabel.LEFT);
                return l;
            }
        };

        for (int i = 0; i < tablo.getColumnCount(); i++) {
            tablo.getColumnModel().getColumn(i).setCellRenderer(paddedRenderer);
        }

        anaEkran.addCellClickBubble(tablo);

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem itemDuzenle = new JMenuItem("✏️ Seçili Ürünü Düzenle");
        itemDuzenle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        itemDuzenle.setBackground(anaEkran.getSidebarBg());
        itemDuzenle.setForeground(anaEkran.getHeaderTextColor());
        popupMenu.add(itemDuzenle);

        // Tabloya sağ tıklayınca açılan Context Menu (Pop-up) entegrasyonu.
        tablo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    // Tıklanan koordinata göre hangi satırda olduğumuzu bulup o satırı seçili hale getiriyoruz.
                    int r = tablo.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < tablo.getRowCount()) {
                        tablo.setRowSelectionInterval(r, r);
                    } else {
                        tablo.clearSelection();
                    }
                    int rowindex = tablo.getSelectedRow();
                    if (rowindex < 0) return;
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        itemDuzenle.addActionListener(e -> {
            int row = tablo.getSelectedRow();
            if (row >= 0) {
                String seriNo = tablo.getValueAt(row, 2).toString();
                Urun secilenUrun = anaEkran.getUrunManager().urunBul(seriNo);
                if (secilenUrun != null) anaEkran.urunDuzenleFormuAc(secilenUrun);
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablo);
        scrollPane.getViewport().setBackground(anaEkran.getTableBg());
        add(scrollPane, BorderLayout.CENTER);
    }
}