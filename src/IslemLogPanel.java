import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class IslemLogPanel extends JPanel {

    public IslemLogPanel(AnaEkran anaEkran) {
        setBackground(anaEkran.getMainBg());
        setBorder(new EmptyBorder(30, 40, 30, 40));
        setLayout(new BorderLayout(0, 20));

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        JLabel baslik = new JLabel("⏳ İşlem Geçmişi");
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 28));
        baslik.setForeground(anaEkran.getHeaderTextColor());
        topPanel.add(baslik, BorderLayout.CENTER);

        JPanel aramaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        aramaPanel.setOpaque(false);

        JComboBox<String> cmbIslemTipiFiltre = anaEkran.getCmbIslemTipiFiltre();

        JTextField txtArama = new JTextField(15);
        txtArama.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "🔍 Seri No / Ad / Açıklama...");
        txtArama.putClientProperty(FlatClientProperties.STYLE, anaEkran.getInputStyle());
        txtArama.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtArama.setPreferredSize(new Dimension(200, 45));

        JButton btnLogPdf = new JButton("📄 PDF");
        btnLogPdf.setBackground(new Color(231, 76, 60));
        btnLogPdf.setForeground(Color.WHITE);
        btnLogPdf.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogPdf.setPreferredSize(new Dimension(110, 45));
        btnLogPdf.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);

        JButton btnLogExcel = new JButton("📊 Excel");
        btnLogExcel.setBackground(new Color(39, 174, 96));
        btnLogExcel.setForeground(Color.WHITE);
        btnLogExcel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogExcel.setPreferredSize(new Dimension(110, 45));
        btnLogExcel.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);

        btnLogExcel.addActionListener(e -> anaEkran.islemExcelRaporAl());
        btnLogPdf.addActionListener(e -> anaEkran.islemPdfRaporAl());

        cmbIslemTipiFiltre.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) anaEkran.islemLoglariniFiltrele(txtArama.getText());
        });

        txtArama.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { anaEkran.islemLoglariniFiltrele(txtArama.getText()); }
        });

        aramaPanel.add(cmbIslemTipiFiltre);
        aramaPanel.add(txtArama);
        aramaPanel.add(btnLogExcel);
        aramaPanel.add(btnLogPdf);

        topPanel.add(aramaPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JTable tablo = new JTable(anaEkran.getIslemTabloModeli());
        tablo.setRowHeight(45);
        tablo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tablo.setToolTipText("💡 Yazının tamamını okumak için hücreye basılı tutun.");

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
        tablo.getTableHeader().setBackground(anaEkran.isDarkMode() ? new Color(35, 45, 55) : new Color(44, 62, 80));
        tablo.getTableHeader().setForeground(Color.WHITE);

        tablo.getColumnModel().getColumn(0).setPreferredWidth(190);
        tablo.getColumnModel().getColumn(1).setPreferredWidth(150);
        tablo.getColumnModel().getColumn(2).setPreferredWidth(230);
        tablo.getColumnModel().getColumn(3).setPreferredWidth(160);
        tablo.getColumnModel().getColumn(4).setPreferredWidth(80);
        tablo.getColumnModel().getColumn(5).setPreferredWidth(600);

        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBorder(new EmptyBorder(0, 10, 0, 10));

                if (column == 4 || column == 0 || column == 1) l.setHorizontalAlignment(JLabel.CENTER);
                else l.setHorizontalAlignment(JLabel.LEFT);

                // Dinamik Veri Görselleştirme: Logların okunabilirliğini artırmak için
                // işlem tiplerine (Giriş, Çıkış, Taşıma) göre hücredeki metin renklerini koşullu olarak (Conditional Formatting) değiştiriyorum.
                if (column == 1) {
                    String tip = value.toString();
                    if(tip.equals("STOK GİRİŞİ")) l.setForeground(anaEkran.isDarkMode() ? new Color(46, 204, 113) : new Color(39, 174, 96));
                    else if(tip.equals("STOK ÇIKIŞI")) l.setForeground(anaEkran.isDarkMode() ? new Color(255, 100, 100) : new Color(231, 76, 60));
                    else if(tip.equals("YENİ KAYIT")) l.setForeground(anaEkran.isDarkMode() ? new Color(90, 180, 255) : new Color(52, 152, 219));
                    else if(tip.equals("TAŞIMA")) l.setForeground(anaEkran.isDarkMode() ? new Color(245, 176, 65) : new Color(243, 156, 18));
                    else if(tip.equals("DÜZENLEME")) l.setForeground(anaEkran.isDarkMode() ? new Color(180, 130, 220) : new Color(155, 89, 182));
                    else if(tip.equals("RAF EKLENDİ")) l.setForeground(anaEkran.isDarkMode() ? new Color(26, 188, 156) : new Color(22, 160, 133));
                    else if(tip.equals("RAF SİLME")) l.setForeground(anaEkran.isDarkMode() ? new Color(170, 180, 180) : new Color(127, 140, 141));
                    else l.setForeground(anaEkran.getHeaderTextColor());
                } else {
                    if (isSelected) l.setForeground(anaEkran.isDarkMode() ? Color.WHITE : Color.BLACK);
                    else l.setForeground(anaEkran.getHeaderTextColor());
                }
                return l;
            }
        };

        for (int i = 0; i < tablo.getColumnCount(); i++) tablo.getColumnModel().getColumn(i).setCellRenderer(paddedRenderer);

        anaEkran.addCellClickBubble(tablo);

        JScrollPane scrollPane = new JScrollPane(tablo);
        scrollPane.getViewport().setBackground(anaEkran.getTableBg());
        add(scrollPane, BorderLayout.CENTER);
    }
}