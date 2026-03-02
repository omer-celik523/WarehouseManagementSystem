import com.formdev.flatlaf.FlatClientProperties;
import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AnaEkran extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContent;
    private List<JButton> menuButtons = new ArrayList<>();

    private RafManager rafManager;
    private UrunManager urunManager;
    private UrunDao urunDao;
    private RafDao rafDao;

    private DefaultTableModel tabloModeli;
    private DefaultTableModel rafTabloModeli;
    private JLabel lblToplamUrun, lblToplamRaf, lblBosKapasite, lblDolulukOrani;
    private DefaultTableModel sonUrunlerModel;
    private DefaultTableModel kritikStokModel; 
    private JComboBox<String> cmbKategoriFiltre; 

    private final Color MAIN_BG = new Color(245, 248, 250);
    private final Color SIDEBAR_BG = new Color(255, 255, 255);
    private final Color ACTIVE_BTN_BG = new Color(52, 152, 219);
    private final Color INACTIVE_BTN_BG = new Color(255, 255, 255);
    private final Color TEXT_COLOR = new Color(80, 90, 100);
    private final Color HEADER_TEXT_COLOR = new Color(44, 62, 80);

    private final String INPUT_STYLE = "arc: 10; padding: 5,10,5,10; background: #FFFFFF; foreground: #000000; borderColor: #BDC3C7; focusedBorderColor: #3498DB;";
    // İnatçı gri sağ buton için EditableBackground eklendi
    private final String COMBO_STYLE = INPUT_STYLE + " buttonBackground: #FFFFFF; buttonEditableBackground: #FFFFFF; buttonArrowColor: #2C3E50; buttonHoverArrowColor: #3498DB;";

    public AnaEkran() {
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("Button.background", Color.WHITE);
        UIManager.put("Label.foreground", Color.BLACK);
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", Color.BLACK);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", Color.BLACK);
        
        // Editable (Yazılabilir) ComboBox için o gri butonu beyaz yapma garantisi
        UIManager.put("ComboBox.buttonBackground", Color.WHITE);
        UIManager.put("ComboBox.buttonEditableBackground", Color.WHITE);
        
        UIManager.put("FileChooser.background", Color.WHITE);
        UIManager.put("List.background", Color.WHITE);
        UIManager.put("List.foreground", Color.BLACK);
        UIManager.put("Tree.background", Color.WHITE);
        UIManager.put("Tree.foreground", Color.BLACK);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.foreground", Color.BLACK);
        UIManager.put("ScrollPane.background", Color.WHITE);
        UIManager.put("Viewport.background", Color.WHITE);

        this.rafManager = new RafManager();
        this.urunManager = new UrunManager(this.rafManager);
        this.urunDao = new UrunDao();
        this.rafDao = new RafDao();

        setTitle("WMS Pro - Tam Kapsamlı Depo Yönetimi");
        setSize(1350, 800); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(330, 0));
        sidebar.setBorder(new EmptyBorder(40, 20, 30, 20));

        JLabel logo = new JLabel("📦 WMS Pro");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        logo.setForeground(new Color(41, 128, 185));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 50)));

        JButton btnDashboard = createMenuButton("📊 Ana Panel", "DASHBOARD_SAYFASI");
        JButton btnUrunler = createMenuButton("🛒 Ürün İşlemleri", "URUNLER_SAYFASI");
        JButton btnListeleme = createMenuButton("📋 Envanter Listesi", "LISTELEME_SAYFASI");
        JButton btnRaflar = createMenuButton("🗄️ Raf İşlemleri", "RAFLAR_SAYFASI");
        JButton btnCikis = createMenuButton("🚪 Çıkış Yap", null);

        sidebar.add(btnDashboard);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(btnUrunler);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(btnListeleme);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(btnRaflar);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnCikis);

        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)),
                new EmptyBorder(40, 25, 30, 25)));

        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(MAIN_BG);

        mainContent.add(createDashboardPanel(), "DASHBOARD_SAYFASI");
        mainContent.add(createUrunlerPanel(), "URUNLER_SAYFASI");
        mainContent.add(createListelemePanel(), "LISTELEME_SAYFASI");
        mainContent.add(createRaflarPanel(), "RAFLAR_SAYFASI");

        add(mainContent, BorderLayout.CENTER);
        setActiveButton(btnDashboard);

        btnCikis.addActionListener(e -> {
            int cevap = JOptionPane.showConfirmDialog(this, "Çıkmak istediğinize emin misiniz?", "Çıkış", JOptionPane.YES_NO_OPTION);
            if (cevap == JOptionPane.YES_OPTION) {
                this.dispose();
                new LoginEkran().setVisible(true);
            }
        });

        sistemiYenile();
    }

    private String modernInputAl(String baslik, String mesaj) {
        JLabel lbl = new JLabel("<html><body style='width: 500px; font-family: sans-serif; font-size: 18px; font-weight: bold; padding-bottom: 10px;'>" + mesaj.replace("\n", "<br>") + "</body></html>");
        lbl.setForeground(Color.BLACK);

        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        txt.setPreferredSize(new Dimension(500, 60));
        txt.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(txt, BorderLayout.CENTER);

        int result = JOptionPane.showOptionDialog(this, panel, baslik,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"Onayla", "İptal"}, "Onayla");

        if (result == JOptionPane.OK_OPTION && !txt.getText().trim().isEmpty()) {
            return txt.getText().trim();
        }
        return null;
    }

    private String modernSecimAl(String baslik, String mesaj, Object[] secenekler) {
        JLabel lbl = new JLabel("<html><body style='width: 500px; font-family: sans-serif; font-size: 18px; font-weight: bold; padding-bottom: 10px;'>" + mesaj.replace("\n", "<br>") + "</body></html>");
        lbl.setForeground(Color.BLACK);

        JComboBox<Object> cb = new JComboBox<>(secenekler);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        cb.setPreferredSize(new Dimension(500, 60));
        cb.putClientProperty(FlatClientProperties.STYLE, COMBO_STYLE);

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(cb, BorderLayout.CENTER);

        int result = JOptionPane.showOptionDialog(this, panel, baslik,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"Seç", "İptal"}, "Seç");

        if (result == JOptionPane.OK_OPTION && cb.getSelectedItem() != null) {
            return cb.getSelectedItem().toString();
        }
        return null;
    }

    private Integer getGecerliPozitifSayi(String baslik, String mesaj, Integer maxKapasite) {
        int hak = 7;
        while(hak > 0) {
            String input = modernInputAl(baslik, mesaj);
            if(input == null) return null;
            try {
                int sayi = Integer.parseInt(input);
                if(sayi <= 0) {
                    JOptionPane.showMessageDialog(this, "Lütfen 0'dan büyük bir sayı giriniz!", "Hatalı Değer", JOptionPane.ERROR_MESSAGE);
                } else if (maxKapasite != null && sayi > maxKapasite) {
                    JOptionPane.showMessageDialog(this, "Girdiğiniz miktar maksimum sınırı (" + maxKapasite + ") aşıyor!", "Sınır Aşıldı", JOptionPane.ERROR_MESSAGE);
                } else {
                    return sayi;
                }
            } catch(Exception e) {
                JOptionPane.showMessageDialog(this, "Lütfen geçerli bir sayı giriniz!", "Geçersiz Format", JOptionPane.ERROR_MESSAGE);
            }
            hak--;
        }
        JOptionPane.showMessageDialog(this, "Çok fazla hatalı deneme yaptınız!\nİşlem iptal edildi.", "Güvenlik İptali", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    private Integer getGecerliRafIndex(String baslik, String mesaj) {
        int hak = 7;
        while(hak > 0) {
            String input = modernInputAl(baslik, mesaj);
            if(input == null) return null;
            try {
                int rafSira = Integer.parseInt(input);
                if(rafSira >= 1 && rafSira <= rafManager.getRafSayisi()) {
                    return rafSira - 1;
                }
                JOptionPane.showMessageDialog(this, "HATA! Lütfen 1 ile " + rafManager.getRafSayisi() + " arasında geçerli bir raf numarası giriniz.", "Geçersiz Raf", JOptionPane.ERROR_MESSAGE);
            } catch(Exception e) {
                JOptionPane.showMessageDialog(this, "Lütfen geçerli bir sayı giriniz!", "Geçersiz Format", JOptionPane.ERROR_MESSAGE);
            }
            hak--;
        }
        JOptionPane.showMessageDialog(this, "Çok fazla hatalı deneme yaptınız!\nİşlem iptal edildi.", "Güvenlik İptali", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    private Urun getGecerliUrun(String baslik, String mesaj) {
        int hak = 7;
        while(hak > 0) {
            String seriNo = modernInputAl(baslik, mesaj);
            if(seriNo == null) return null;
            Urun u = urunManager.urunBul(seriNo);
            if(u != null) return u;
            JOptionPane.showMessageDialog(this, "Ürün bulunamadı! Lütfen geçerli bir seri numarası giriniz.", "Bulunamadı", JOptionPane.ERROR_MESSAGE);
            hak--;
        }
        JOptionPane.showMessageDialog(this, "Çok fazla hatalı deneme yaptınız!\nİşlem iptal edildi.", "Güvenlik İptali", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    private void setActiveButton(JButton activeButton) {
        for (JButton btn : menuButtons) {
            btn.setBackground(INACTIVE_BTN_BG);
            btn.setForeground(TEXT_COLOR);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        }
        activeButton.setBackground(ACTIVE_BTN_BG);
        activeButton.setForeground(Color.WHITE);
        activeButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 30));
        panel.setBackground(MAIN_BG);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel ustPanel = new JPanel(new BorderLayout(0, 20));
        ustPanel.setOpaque(false);
        JLabel baslik = new JLabel("Ana Panel - Genel Bakış");
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 32));
        baslik.setForeground(HEADER_TEXT_COLOR);
        ustPanel.add(baslik, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 25, 0));
        statsPanel.setOpaque(false);
        lblToplamUrun = new JLabel("0 Çeşit");
        lblToplamRaf = new JLabel("0 Raf");
        lblBosKapasite = new JLabel("0 Birim");
        lblDolulukOrani = new JLabel("% 0");

        statsPanel.add(new GradientCard("Sistemdeki Ürün Çeşidi", lblToplamUrun, new Color(52, 152, 219), new Color(116, 185, 255)));
        statsPanel.add(new GradientCard("Toplam Raf Sayısı", lblToplamRaf, new Color(39, 174, 96), new Color(85, 239, 196)));
        statsPanel.add(new GradientCard("Toplam Boş Kapasite", lblBosKapasite, new Color(231, 76, 60), new Color(255, 118, 117)));
        statsPanel.add(new GradientCard("Depo Doluluk Oranı", lblDolulukOrani, new Color(155, 89, 182), new Color(210, 180, 222)));

        ustPanel.add(statsPanel, BorderLayout.CENTER);
        panel.add(ustPanel, BorderLayout.NORTH);

        JPanel altPanel = new JPanel(new GridLayout(1, 2, 30, 0)); 
        altPanel.setOpaque(false);

        JPanel solTabloPanel = new JPanel(new BorderLayout(0, 10));
        solTabloPanel.setOpaque(false);
        JLabel solBaslik = new JLabel("Son Eklenenler (Son 5)");
        solBaslik.setFont(new Font("Segoe UI", Font.BOLD, 20));
        solBaslik.setForeground(HEADER_TEXT_COLOR);
        solTabloPanel.add(solBaslik, BorderLayout.NORTH);

        String[] solKolonlar = {"Ürün Adı", "Kategori", "Stok"};
        sonUrunlerModel = new DefaultTableModel(null, solKolonlar);
        JTable sonUrunlerTablo = createStyledTable(sonUrunlerModel, new Color(236, 240, 241));
        solTabloPanel.add(new JScrollPane(sonUrunlerTablo), BorderLayout.CENTER);

        JPanel sagTabloPanel = new JPanel(new BorderLayout(0, 10));
        sagTabloPanel.setOpaque(false);
        JLabel sagBaslik = new JLabel("🚨 Kritik Stok Uyarıları");
        sagBaslik.setFont(new Font("Segoe UI", Font.BOLD, 20));
        sagBaslik.setForeground(new Color(231, 76, 60)); 
        sagTabloPanel.add(sagBaslik, BorderLayout.NORTH);

        String[] sagKolonlar = {"Ürün Adı", "Kalan Stok", "Kritik Eşik"};
        kritikStokModel = new DefaultTableModel(null, sagKolonlar);
        JTable kritikStokTablo = createStyledTable(kritikStokModel, new Color(250, 219, 216)); 
        sagTabloPanel.add(new JScrollPane(kritikStokTablo), BorderLayout.CENTER);

        altPanel.add(solTabloPanel);
        altPanel.add(sagTabloPanel);

        panel.add(altPanel, BorderLayout.CENTER);
        return panel;
    }

    private JTable createStyledTable(DefaultTableModel model, Color headerColor) {
        JTable tablo = new JTable(model);
        tablo.setRowHeight(35);
        tablo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tablo.setBackground(Color.WHITE);
        tablo.setForeground(HEADER_TEXT_COLOR);
        tablo.setGridColor(new Color(230, 240, 250));
        tablo.setEnabled(false);
        tablo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        tablo.getTableHeader().setBackground(headerColor);
        tablo.getTableHeader().setForeground(HEADER_TEXT_COLOR);
        ((DefaultTableCellRenderer) tablo.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
        return tablo;
    }

    private JPanel createUrunlerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MAIN_BG);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel baslik = new JLabel("Ürün Operasyon Merkezi");
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 28));
        baslik.setForeground(HEADER_TEXT_COLOR);
        panel.add(baslik, BorderLayout.NORTH);

        JPanel islemGrid = new JPanel(new GridLayout(2, 2, 30, 30));
        islemGrid.setOpaque(false);
        islemGrid.setBorder(new EmptyBorder(50, 50, 50, 50));

        JButton btnYeniKayit = createBigActionButton("➕ Yeni Ürün Kaydı Ekle", "Depoya yepyeni bir ürün tanımlayın", new Color(46, 204, 113));
        JButton btnStokEkle = createBigActionButton("📦 Mevcut Stoğa Ekle", "Sistemde var olan ürünün adetini artırın", new Color(52, 152, 219));
        JButton btnUrunCikar = createBigActionButton("➖ Ürün Çıkar / Sil", "Depodan ürün çıkışı yapın", new Color(231, 76, 60));
        JButton btnUrunTasi = createBigActionButton("🔄 Ürün Taşı", "Ürünleri raflar arasında transfer edin", new Color(241, 196, 15));

        btnYeniKayit.addActionListener(e -> {
            if (rafManager.getRafSayisi() == 0) {
                JOptionPane.showMessageDialog(this, "Depoda hiç raf yok! Lütfen Raf İşlemleri menüsünden raf ekleyiniz.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            } else {
                yeniKayitFormuAc();
            }
        });

        btnStokEkle.addActionListener(e -> mevcutStogaEkleFormuAc());
        btnUrunCikar.addActionListener(e -> urunCikarFormuAc());
        btnUrunTasi.addActionListener(e -> urunTasiFormuAc());

        islemGrid.add(btnYeniKayit);
        islemGrid.add(btnStokEkle);
        islemGrid.add(btnUrunCikar);
        islemGrid.add(btnUrunTasi);

        panel.add(islemGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createListelemePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(MAIN_BG);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel baslik = new JLabel("Envanter Listesi");
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 28));
        baslik.setForeground(HEADER_TEXT_COLOR);
        topPanel.add(baslik, BorderLayout.WEST);

        JPanel aramaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        aramaPanel.setOpaque(false);

        cmbKategoriFiltre = new JComboBox<>();
        cmbKategoriFiltre.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cmbKategoriFiltre.setPreferredSize(new Dimension(250, 45)); 
        cmbKategoriFiltre.putClientProperty(FlatClientProperties.STYLE, COMBO_STYLE);

        JTextField txtArama = new JTextField(15);
        txtArama.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "🔍 Seri No / Ad Ara...");
        txtArama.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtArama.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtArama.setPreferredSize(new Dimension(300, 45)); 

        JButton btnAra = new JButton("Filtrele");
        btnAra.setBackground(new Color(52, 152, 219));
        btnAra.setForeground(Color.WHITE);
        btnAra.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnAra.setPreferredSize(new Dimension(110, 45)); 
        btnAra.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);

        btnAra.addActionListener(e -> listeyiFiltrele(txtArama.getText()));

        JButton btnTumunuGoster = new JButton("📋 Tümünü Listele");
        btnTumunuGoster.setBackground(new Color(155, 89, 182));
        btnTumunuGoster.setForeground(Color.WHITE);
        btnTumunuGoster.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnTumunuGoster.setPreferredSize(new Dimension(190, 45)); 
        btnTumunuGoster.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        btnTumunuGoster.addActionListener(e -> {
            txtArama.setText("");
            if (cmbKategoriFiltre.getItemCount() > 0) cmbKategoriFiltre.setSelectedIndex(0);
            tabloyuGuncelle();
        });

        JButton btnPdfAktar = new JButton("📄 PDF Rapor Al");
        btnPdfAktar.setBackground(new Color(231, 76, 60));
        btnPdfAktar.setForeground(Color.WHITE);
        btnPdfAktar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnPdfAktar.setPreferredSize(new Dimension(170, 45)); 
        btnPdfAktar.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        btnPdfAktar.addActionListener(e -> pdfRaporAl());

        aramaPanel.add(cmbKategoriFiltre);
        aramaPanel.add(txtArama);
        aramaPanel.add(btnAra);
        aramaPanel.add(btnTumunuGoster);
        aramaPanel.add(btnPdfAktar);

        topPanel.add(aramaPanel, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);

        String[] kolonlar = {"Kategori", "Ürün Adı", "Seri Numarası", "Stok", "Raf Dağılımı", "Kayıt Tarihi"};
        tabloModeli = new DefaultTableModel(null, kolonlar);
        JTable tablo = new JTable(tabloModeli);
        tablo.setRowHeight(40);
        tablo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        tablo.setBackground(Color.WHITE);
        tablo.setForeground(HEADER_TEXT_COLOR);
        tablo.setGridColor(new Color(230, 240, 250));
        tablo.setSelectionBackground(new Color(214, 234, 248));
        tablo.setSelectionForeground(Color.BLACK);

        tablo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        tablo.getTableHeader().setBackground(new Color(52, 152, 219));
        tablo.getTableHeader().setForeground(Color.WHITE);

        tablo.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablo.getColumnModel().getColumn(0).setPreferredWidth(180); 
        tablo.getColumnModel().getColumn(1).setPreferredWidth(250); 
        tablo.getColumnModel().getColumn(2).setPreferredWidth(150); 
        tablo.getColumnModel().getColumn(3).setPreferredWidth(100);  
        tablo.getColumnModel().getColumn(4).setPreferredWidth(350); 
        tablo.getColumnModel().getColumn(5).setPreferredWidth(180); 

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tablo.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tablo.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tablo.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(tablo);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void listeyiFiltrele(String arananKelime) {
        String kelime = arananKelime.toLowerCase().trim();
        String secilenKategori = cmbKategoriFiltre.getSelectedItem() != null ? cmbKategoriFiltre.getSelectedItem().toString() : "Tüm Kategoriler";

        tabloModeli.setRowCount(0);
        for (Urun u : urunManager.getUrunler()) {
            boolean kelimeUyuyor = u.getSeriNo().toLowerCase().contains(kelime) || u.getAd().toLowerCase().contains(kelime);
            boolean kategoriUyuyor = secilenKategori.equals("Tüm Kategoriler") || u.getKategori().equals(secilenKategori);

            if (kelimeUyuyor && kategoriUyuyor) {
                tabloModeli.addRow(new Object[]{u.getKategori(), u.getAd(), u.getSeriNo(), u.getMiktar(), u.getRafKodlariString(), u.getEklenmeTarihi()});
            }
        }
    }

    private JPanel createRaflarPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 30));
        panel.setBackground(MAIN_BG);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel baslik = new JLabel("Raf Düzeni ve Ayarları");
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 28));
        baslik.setForeground(HEADER_TEXT_COLOR);
        panel.add(baslik, BorderLayout.NORTH);

        JPanel tabloPanel = new JPanel(new BorderLayout());
        tabloPanel.setOpaque(false);

        String[] kolonlar = {"Raf Adı", "Toplam Kapasite", "Boş Kapasite", "Doluluk Oranı"};
        rafTabloModeli = new DefaultTableModel(null, kolonlar);
        JTable rafTablo = new JTable(rafTabloModeli);

        rafTablo.setRowHeight(40);
        rafTablo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        rafTablo.setBackground(Color.WHITE);
        rafTablo.setForeground(HEADER_TEXT_COLOR);
        rafTablo.setGridColor(new Color(230, 240, 250));
        rafTablo.setEnabled(false);

        rafTablo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        rafTablo.getTableHeader().setBackground(new Color(39, 174, 96));
        rafTablo.getTableHeader().setForeground(Color.WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < 4; i++) rafTablo.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        tabloPanel.add(new JScrollPane(rafTablo), BorderLayout.CENTER);

        JPanel islemGrid = new JPanel(new GridLayout(1, 2, 40, 40));
        islemGrid.setOpaque(false);
        islemGrid.setBorder(new EmptyBorder(30, 50, 30, 50));

        JButton btnRafEkle = createBigActionButton("🗄️ Yeni Raf Ekle", "Depoya yeni kapasite alanları tanımlayın", new Color(46, 204, 113));
        JButton btnRafSil = createBigActionButton("🗑️ Raf Sil", "Mevcut boş rafları sistemden kaldırın", new Color(231, 76, 60));

        btnRafEkle.addActionListener(e -> cokluRafEkleFormuAc());
        btnRafSil.addActionListener(e -> rafSilFormuAc());

        islemGrid.add(btnRafEkle);
        islemGrid.add(btnRafSil);

        JPanel splitPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        splitPanel.setOpaque(false);
        splitPanel.add(tabloPanel);
        splitPanel.add(islemGrid);

        panel.add(splitPanel, BorderLayout.CENTER);
        return panel;
    }

    private void yeniKayitFormuAc() {
        JDialog dialog = new JDialog(this, true); 
        dialog.setUndecorated(true); 
        dialog.setSize(550, 750); 
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(Color.WHITE);
        titleBar.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("📦 Yeni Ürün Kaydı");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(HEADER_TEXT_COLOR);

        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        closeButton.setForeground(Color.GRAY);
        closeButton.setBackground(Color.WHITE);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dialog.dispose());

        titleBar.add(titleLabel, BorderLayout.WEST);
        titleBar.add(closeButton, BorderLayout.EAST);

        ComponentDragger dragger = new ComponentDragger(dialog);
        titleBar.addMouseListener(dragger);
        titleBar.addMouseMotionListener(dragger);

        dialog.add(titleBar, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(12, 1, 5, 5));
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        formPanel.setBackground(Color.WHITE);

        JTextField txtAd = new JTextField(); txtAd.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE); txtAd.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        JTextField txtSeriNo = new JTextField(); txtSeriNo.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE); txtSeriNo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        JTextField txtMiktar = new JTextField(); txtMiktar.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE); txtMiktar.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        
        JComboBox<String> cmbKategori = new JComboBox<>();
        cmbKategori.setEditable(true); 
        cmbKategori.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        cmbKategori.putClientProperty(FlatClientProperties.STYLE, COMBO_STYLE);
        
        // --- İNATÇI GRİ YERİN KESİN ÇÖZÜMÜ ---
        // ComboBox'ın yazılabilir iç kısmının arka planını zorla beyaza boyuyoruz
        cmbKategori.getEditor().getEditorComponent().setBackground(Color.WHITE);
        // ------------------------------------

        for (String kat : urunDao.tumKategorileriGetir()) cmbKategori.addItem(kat);
        if (cmbKategori.getItemCount() == 0) cmbKategori.addItem("Genel");

        JTextField txtKritik = new JTextField(); 
        txtKritik.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtKritik.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JComboBox<String> cmbRaflar = new JComboBox<>();
        cmbRaflar.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cmbRaflar.putClientProperty(FlatClientProperties.STYLE, COMBO_STYLE);
        for (int i = 0; i < rafManager.getRafSayisi(); i++) cmbRaflar.addItem((i + 1) + ". Raf (Boş: " + rafManager.getRafKapasitesi(i) + ")");

        Font lblFont = new Font("Segoe UI", Font.BOLD, 15);
        JLabel lblKategori = new JLabel("Kategori (Seç veya Yeni Yaz):"); lblKategori.setForeground(HEADER_TEXT_COLOR); lblKategori.setFont(lblFont);
        JLabel lblAd = new JLabel("Ürün Adı:"); lblAd.setForeground(HEADER_TEXT_COLOR); lblAd.setFont(lblFont);
        JLabel lblSeri = new JLabel("Seri Numarası:"); lblSeri.setForeground(HEADER_TEXT_COLOR); lblSeri.setFont(lblFont);
        JLabel lblMiktar = new JLabel("Miktar:"); lblMiktar.setForeground(HEADER_TEXT_COLOR); lblMiktar.setFont(lblFont);
        JLabel lblKritik = new JLabel("Kritik Stok Uyarı Sınırı (Adet):"); lblKritik.setForeground(HEADER_TEXT_COLOR); lblKritik.setFont(lblFont);
        JLabel lblRaf = new JLabel("Hedef Raf:"); lblRaf.setForeground(HEADER_TEXT_COLOR); lblRaf.setFont(lblFont);

        formPanel.add(lblKategori); formPanel.add(cmbKategori);
        formPanel.add(lblAd); formPanel.add(txtAd);
        formPanel.add(lblSeri); formPanel.add(txtSeriNo);
        formPanel.add(lblMiktar); formPanel.add(txtMiktar);
        formPanel.add(lblKritik); formPanel.add(txtKritik);
        formPanel.add(lblRaf); formPanel.add(cmbRaflar);

        JButton btnKaydet = new JButton("Kaydet");
        btnKaydet.setBackground(new Color(46, 204, 113));
        btnKaydet.setForeground(Color.WHITE);
        btnKaydet.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnKaydet.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        btnKaydet.setPreferredSize(new Dimension(300, 50));

        btnKaydet.addActionListener(e -> {
            try {
                String kategori = cmbKategori.getSelectedItem() != null ? cmbKategori.getSelectedItem().toString().trim() : "Genel";
                String ad = txtAd.getText();
                String seriNo = txtSeriNo.getText();

                if (ad.isEmpty() || seriNo.isEmpty() || kategori.isEmpty()) throw new Exception("Tüm alanları doldurunuz!");

                int miktar, kritik;
                try {
                    miktar = Integer.parseInt(txtMiktar.getText());
                    kritik = Integer.parseInt(txtKritik.getText()); 
                    if (miktar <= 0 || kritik < 0) throw new Exception("Değerler geçerli olmalı!");
                } catch(NumberFormatException ex) {
                    throw new Exception("Lütfen miktar ve kritik sınır için geçerli bir sayı giriniz!");
                }

                int rafIndex = cmbRaflar.getSelectedIndex();
                if (urunManager.urunBul(seriNo) != null) throw new Exception("Bu seri numarası zaten var!");
                if (miktar > rafManager.getToplamKapasite()) throw new Exception("Depoda bu kadar boş yer yok!");

                int rafKapasite = rafManager.getRafKapasitesi(rafIndex);

                if (miktar > rafKapasite) {
                    int cevap = JOptionPane.showConfirmDialog(dialog, "Seçilen rafta yeterli yer yok!\nÜrünü parçalı dağıtmak ister misiniz?", "Yetersiz", JOptionPane.YES_NO_OPTION);
                    if (cevap == JOptionPane.YES_OPTION) {
                        dialog.dispose();
                        parcaliEklemeBaslat("Yeni Kayıt", ad, seriNo, miktar, null, kategori, kritik); 
                    }
                    return;
                }

                Urun yeniUrun = new Urun(ad, seriNo, miktar, rafIndex, kategori, kritik);
                urunManager.getUrunler().add(yeniUrun);
                rafManager.kapasiteGuncelle(rafIndex, -miktar);

                urunDao.veritabaniniGuncelle(urunManager.getUrunler());
                sistemiYenile();

                JOptionPane.showMessageDialog(this, "Ürün Başarıyla Eklendi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel altPanel = new JPanel();
        altPanel.setBackground(Color.WHITE);
        altPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        altPanel.add(btnKaydet);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(altPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private static class ComponentDragger extends MouseAdapter {
        private final Window window;
        private Point mouseDownCompCoords = null;

        public ComponentDragger(Window window) {
            this.window = window;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            mouseDownCompCoords = e.getPoint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point currCoords = e.getLocationOnScreen();
            window.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
        }
    }

    private void parcaliEklemeBaslat(String baslik, String ad, String seriNo, int miktar, Urun mevcut, String kat, int kritik) {
        int kalan = miktar;
        Urun islemUrunu = mevcut;

        if (islemUrunu == null) {
            islemUrunu = new Urun(ad, seriNo, miktar, 0, kat, kritik);
            islemUrunu.getRafDagilimi().clear();
        } else {
            islemUrunu.setMiktar(islemUrunu.getMiktar() + miktar);
        }

        while (kalan > 0) {
            Integer secilenRaf = getGecerliRafIndex(baslik, "Kalan: " + kalan + "\nHangi rafa ekleyeceksiniz? (1-" + rafManager.getRafSayisi() + "):");
            if (secilenRaf == null) break;
            int bosYer = rafManager.getRafKapasitesi(secilenRaf);
            if (bosYer <= 0) continue;
            Integer eklenecek = getGecerliPozitifSayi(baslik, "Bu rafta " + bosYer + " boş yer var. Kaç adet koyacaksınız?", Math.min(bosYer, kalan));
            if (eklenecek == null) break;

            rafManager.kapasiteGuncelle(secilenRaf, -eklenecek);
            islemUrunu.rafVeMiktarEkle(secilenRaf, eklenecek);
            kalan -= eklenecek;
        }

        if (mevcut == null && !islemUrunu.getRafDagilimi().isEmpty()) {
            islemUrunu.setRafIndex(islemUrunu.getRafDagilimi().keySet().iterator().next());
            urunManager.getUrunler().add(islemUrunu);
        }
        urunDao.veritabaniniGuncelle(urunManager.getUrunler());
        sistemiYenile();
    }
    
    private void parcaliEklemeBaslat(String baslik, String ad, String seriNo, int miktar, Urun mevcut) {
        parcaliEklemeBaslat(baslik, ad, seriNo, miktar, mevcut, mevcut.getKategori(), mevcut.getKritikEsik());
    }

    private void mevcutStogaEkleFormuAc() {
        Urun u = getGecerliUrun("Mevcut Stoğa Ekle", "Stok eklenecek ürünün Seri Numarasını giriniz:");
        if (u == null) return;

        Integer miktar = getGecerliPozitifSayi("Mevcut Stoğa Ekle", "Ürüne kaç adet eklenecek?", null);
        if (miktar == null) return;

        Integer rafIndex = getGecerliRafIndex("Mevcut Stoğa Ekle", "Hangi rafa eklenecek? (1-" + rafManager.getRafSayisi() + "):");
        if (rafIndex == null) return;

        if (miktar > rafManager.getRafKapasitesi(rafIndex)) {
            int cevap = JOptionPane.showConfirmDialog(this, "Seçilen rafta yer yok! Parçalı eklemek ister misiniz?", "Yetersiz", JOptionPane.YES_NO_OPTION);
            if(cevap == JOptionPane.YES_OPTION) parcaliEklemeBaslat("Mevcut Stoğa Ekle", u.getAd(), u.getSeriNo(), miktar, u);
            return;
        }

        rafManager.kapasiteGuncelle(rafIndex, -miktar);
        u.setMiktar(u.getMiktar() + miktar);
        u.rafVeMiktarEkle(rafIndex, miktar);

        urunDao.veritabaniniGuncelle(urunManager.getUrunler());
        sistemiYenile();
        JOptionPane.showMessageDialog(this, "Stok başarıyla güncellendi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
    }

    private void urunCikarFormuAc() {
        Urun u = getGecerliUrun("Çıkar / Sil", "Çıkarılacak ürün Seri No:");
        if(u == null) return;
        if(u.getRafDagilimi().isEmpty()) return;

        Object[] mevcutRaflar = u.getRafDagilimi().keySet().stream().map(r -> (r + 1) + ". Raf").toArray();
        String secilenRafStr = modernSecimAl("Çıkar / Sil", "Hangi raftan çıkaracaksınız?", mevcutRaflar);
        if (secilenRafStr == null) return;
        int secilenRaf = Integer.parseInt(secilenRafStr.split("\\.")[0]) - 1;

        int raftakiMiktar = u.getRafDagilimi().get(secilenRaf);
        Integer miktar = getGecerliPozitifSayi("Çıkar / Sil", "Bu rafta " + raftakiMiktar + " adet var. Çıkarılacak miktar:", raftakiMiktar);
        if(miktar == null) return;

        rafManager.kapasiteGuncelle(secilenRaf, +miktar);
        u.raftanUrunEksilt(secilenRaf, miktar);
        u.setMiktar(u.getMiktar() - miktar);

        if (u.getMiktar() == 0) urunManager.getUrunler().remove(u);
        
        urunDao.veritabaniniGuncelle(urunManager.getUrunler());
        sistemiYenile();
        JOptionPane.showMessageDialog(this, "İşlem başarılı.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void urunTasiFormuAc() {
        Urun u = getGecerliUrun("Taşı", "Taşınacak ürün Seri No:");
        if(u == null) return;
        if(u.getRafDagilimi().isEmpty()) return;

        Object[] mevcutRaflar = u.getRafDagilimi().keySet().stream().map(r -> (r + 1) + ". Raf").toArray();
        String eskiRafStr = modernSecimAl("Taşı", "Hangi raftaki ürünleri taşıyacaksınız?", mevcutRaflar);
        if (eskiRafStr == null) return;
        int eskiRaf = Integer.parseInt(eskiRafStr.split("\\.")[0]) - 1;

        int raftakiMiktar = u.getRafDagilimi().get(eskiRaf);
        Integer tasinacakMiktar = getGecerliPozitifSayi("Taşı", "Kaçını taşıyacaksınız?", raftakiMiktar);
        if (tasinacakMiktar == null) return;

        Integer yeniRaf = getGecerliRafIndex("Taşı", "Hedef raf (1-" + rafManager.getRafSayisi() + "):");
        if (yeniRaf == null) return;

        if (rafManager.getRafKapasitesi(yeniRaf) < tasinacakMiktar) {
            JOptionPane.showMessageDialog(this, "Hedef rafta yeterli yer yok!", "Hata", JOptionPane.ERROR_MESSAGE); return;
        }

        rafManager.kapasiteGuncelle(eskiRaf, +tasinacakMiktar);
        rafManager.kapasiteGuncelle(yeniRaf, -tasinacakMiktar);
        u.rafTasimaGuncellemesi(eskiRaf, yeniRaf, tasinacakMiktar);

        urunDao.veritabaniniGuncelle(urunManager.getUrunler());
        sistemiYenile();
        JOptionPane.showMessageDialog(this, "Taşıma başarılı!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
    }

    private void rafSilFormuAc() {
        if(rafManager.getRafSayisi() == 0) return;
        Integer silinecekAdet = getGecerliPozitifSayi("Raf Sil", "Kaç adet raf silmek istiyorsunuz?", rafManager.getRafSayisi());
        if (silinecekAdet == null) return;

        for (int i = 0; i < silinecekAdet; i++) {
            Object[] mevcutRaflar = new Object[rafManager.getRafSayisi()];
            for(int j = 0; j < rafManager.getRafSayisi(); j++) mevcutRaflar[j] = (j + 1) + ". Raf";
            String secilenRafStr = modernSecimAl("Raf Sil", "Silmek istediğiniz rafı seçiniz:", mevcutRaflar);
            if (secilenRafStr == null) return;
            
            int silinecekIndex = Integer.parseInt(secilenRafStr.split("\\.")[0]) - 1;
            if (urunManager.rafDoluMu(silinecekIndex)) {
                JOptionPane.showMessageDialog(this, "Seçtiğiniz Raf DOLU!", "Uyarı", JOptionPane.WARNING_MESSAGE); return;
            }
            if (rafManager.rafSil(silinecekIndex)) {
                urunManager.rafSilindiktenSonraGuncelle(silinecekIndex);
                sistemiYenile();
            }
        }
    }

    private void cokluRafEkleFormuAc() {
        Integer eklenecekSayi = getGecerliPozitifSayi("Yeni Raf Ekle", "Kaç adet raf eklemek istiyorsunuz?", null);
        if(eklenecekSayi == null) return;

        RafDao rDao = new RafDao();
        int mevcutSayi = rafManager.getRafSayisi();

        for (int i = 0; i < eklenecekSayi; i++) {
            Integer kap = getGecerliPozitifSayi("Yeni Raf Ekle", (mevcutSayi + i + 1) + ". Raf kapasitesi:", null);
            if(kap == null) return;
            rDao.rafEkle(new Raf(kap));
        }
        sistemiYenile();
        JOptionPane.showMessageDialog(this, "Raflar eklendi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
    }

    private void sistemiYenile() {
        this.rafManager = new RafManager();
        this.urunManager = new UrunManager(this.rafManager);
        
        if (cmbKategoriFiltre != null) {
            String mevcutSecim = cmbKategoriFiltre.getSelectedItem() != null ? cmbKategoriFiltre.getSelectedItem().toString() : "Tüm Kategoriler";
            cmbKategoriFiltre.removeAllItems();
            cmbKategoriFiltre.addItem("Tüm Kategoriler");
            for (String kat : urunDao.tumKategorileriGetir()) cmbKategoriFiltre.addItem(kat);
            cmbKategoriFiltre.setSelectedItem(mevcutSecim);
        }

        dashboardGuncelle();
        tabloyuGuncelle();
        
        if (rafTabloModeli != null) {
            rafTabloModeli.setRowCount(0);
            for (Object[] row : rafDao.getRafDetaylari()) rafTabloModeli.addRow(row);
        }
    }

    private void dashboardGuncelle() {
        lblToplamUrun.setText(urunManager.getUrunler().size() + " Çeşit");
        lblToplamRaf.setText(rafManager.getRafSayisi() + " Raf");
        int bosKapasite = rafManager.getToplamKapasite();
        lblBosKapasite.setText(bosKapasite + " Birim");

        int toplamUrunMiktari = 0;
        for (Urun u : urunManager.getUrunler()) toplamUrunMiktari += u.getMiktar();

        int tamKapasite = bosKapasite + toplamUrunMiktari;
        int yuzde = (tamKapasite > 0) ? (toplamUrunMiktari * 100 / tamKapasite) : 0;
        lblDolulukOrani.setText("% " + yuzde);
        if (yuzde > 85) lblDolulukOrani.getParent().setBackground(new Color(231, 76, 60));

        if (sonUrunlerModel != null) {
            sonUrunlerModel.setRowCount(0);
            List<Urun> tumUrunler = urunManager.getUrunler();
            int limit = Math.min(5, tumUrunler.size());
            if (limit == 0) sonUrunlerModel.addRow(new Object[]{"Kayıt Yok", "", ""});
            else {
                for (int i = tumUrunler.size() - 1; i >= tumUrunler.size() - limit; i--) {
                    Urun u = tumUrunler.get(i);
                    sonUrunlerModel.addRow(new Object[]{u.getAd(), u.getKategori(), u.getMiktar() + " Adet"});
                }
            }
        }

        if (kritikStokModel != null) {
            kritikStokModel.setRowCount(0);
            boolean alarmVarMi = false;
            for (Urun u : urunManager.getUrunler()) {
                if (u.getMiktar() <= u.getKritikEsik()) {
                    kritikStokModel.addRow(new Object[]{u.getAd(), u.getMiktar() + " Adet", "Sınır: " + u.getKritikEsik()});
                    alarmVarMi = true;
                }
            }
            if (!alarmVarMi) {
                kritikStokModel.addRow(new Object[]{"Kritik stok uyarısı yok.", "", ""});
            }
        }
    }

    private void tabloyuGuncelle() {
        if (tabloModeli != null) {
            tabloModeli.setRowCount(0);
            for(Urun u : urunManager.getUrunler()) {
                tabloModeli.addRow(new Object[]{u.getKategori(), u.getAd(), u.getSeriNo(), u.getMiktar(), u.getRafKodlariString(), u.getEklenmeTarihi()});
            }
        }
    }

    private void pdfRaporAl() {
        if (urunManager.getUrunler().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Depoda ürün yok!", "Uyarı", JOptionPane.WARNING_MESSAGE); return;
        }

        FileDialog fileDialog = new FileDialog(this, "PDF Raporunu Kaydet", FileDialog.SAVE);
        fileDialog.setFile("WMS_Pro_Envanter_Raporu.pdf");
        fileDialog.setVisible(true);

        String dir = fileDialog.getDirectory(), file = fileDialog.getFile();
        if (dir != null && file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(new File(dir, file)));
                document.open();
                
                Paragraph baslik = new Paragraph("WMS Pro - Guncel Envanter Raporu\n\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20));
                baslik.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(baslik);

                PdfPTable table = new PdfPTable(6); 
                table.setWidthPercentage(100);
                table.addCell("Kategori");
                table.addCell("Urun Adi");
                table.addCell("Seri Numarasi");
                table.addCell("Stok");
                table.addCell("Raf Dagilimi");
                table.addCell("Kayit Tarihi");

                for (Urun u : urunManager.getUrunler()) {
                    table.addCell(u.getKategori());
                    table.addCell(u.getAd());
                    table.addCell(u.getSeriNo());
                    table.addCell(String.valueOf(u.getMiktar()));
                    table.addCell(u.getRafKodlariString());
                    table.addCell(u.getEklenmeTarihi());
                }
                document.add(table); document.close();
                JOptionPane.showMessageDialog(this, "Rapor Alındı!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JButton createMenuButton(String text, String pageName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setForeground(TEXT_COLOR);
        btn.setBackground(INACTIVE_BTN_BG);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMargin(new Insets(10, 15, 10, 15));
        btn.setMaximumSize(new Dimension(300, 65));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        if (pageName != null) {
            btn.addActionListener(e -> { cardLayout.show(mainContent, pageName); setActiveButton(btn); });
            menuButtons.add(btn);
        }
        return btn;
    }

    private JButton createBigActionButton(String title, String subtitle, Color bg) {
        JButton btn = new JButton("<html><center><font size='5' color='#FFFFFF'>" + title + "</font><br><font size='3' color='#F0F0F0'>" + subtitle + "</font></center></html>");
        btn.setBackground(bg); btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        return btn;
    }

    private class GradientCard extends JPanel {
        private Color color1, color2;
        public GradientCard(String title, JLabel valueLabel, Color c1, Color c2) {
            this.color1 = c1; this.color2 = c2;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); setBorder(new EmptyBorder(25, 25, 25, 25)); setOpaque(false);
            JLabel lblTitle = new JLabel(title); lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblTitle.setForeground(new Color(255, 255, 255, 230));
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32)); valueLabel.setForeground(Color.WHITE);
            add(lblTitle); add(Box.createRigidArea(new Dimension(0, 10))); add(valueLabel);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g; g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setPaint(new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2));
            g2d.fill(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
        }
    }
}