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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// AnaEkran sınıfımız MVC (Model-View-Controller) mimarisindeki "Controller" (Orkestratör) görevini üstleniyor.
// Tüm paneller arası iletişimi ve veri akışını merkezi olarak buradan yönetiyoruz.
public class AnaEkran extends JFrame {

    // Arayüzün RAM dostu olması için CardLayout kullanıyorum. Her menüde yeni bir JFrame açıp işletim
    // sistemini yormak yerine, panelleri üst üste dizip (Single Page Application - SPA mantığı)
    // sadece istenileni görünür kılıyorum.
    private CardLayout cardLayout;
    private JPanel mainContent;
    private List<JButton> menuButtons = new ArrayList<>();

    // Veritabanı ve İş Mantığı (Business Logic) sınıflarını (Dependency Injection benzeri bir yapıyla) sisteme dahil ediyorum.
    private RafManager rafManager;
    private UrunManager urunManager;
    private UrunDao urunDao;
    private RafDao rafDao;
    private IslemDao islemDao;

    private DefaultTableModel tabloModeli, islemTabloModeli, rafTabloModeli;
    private JComboBox<String> cmbKategoriFiltre, cmbIslemTipiFiltre;

    private DashboardPanel dashboardPanel;

    private boolean isDarkMode;
    private String aktifSayfa = "DASHBOARD_SAYFASI"; // Varsayılan başlangıç sayfamız
    private int oncekiKritikSayisi = -1; // Kritik stok bildirimi spam'ini önlemek için state tutuyoruz.

    // Global UI Tema Renklerimiz
    private Color MAIN_BG, SIDEBAR_BG, ACTIVE_BTN_BG, INACTIVE_BTN_BG, TEXT_COLOR, HEADER_TEXT_COLOR, TABLE_BG, TABLE_GRID_COLOR;
    private String INPUT_STYLE, COMBO_STYLE;

    public AnaEkran() {
        this(false);
    }

    public AnaEkran(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;

        // Nesne yönelimli programlama (OOP) ilkeleri gereği nesneleri constructor içinde ilklendiriyorum.
        this.rafManager = new RafManager();
        this.urunManager = new UrunManager(this.rafManager);
        this.urunDao = new UrunDao();
        this.rafDao = new RafDao();
        this.islemDao = new IslemDao();

        setTitle("WMS Pro - Tam Kapsamlı Depo Yönetimi");
        setSize(1450, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        temaRenkleriniAyarla();

        getContentPane().removeAll();
        menuButtons.clear();
        setLayout(new BorderLayout());

        // Yan menü (Sidebar) tasarımı
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(330, 0));
        sidebar.setBorder(new EmptyBorder(40, 20, 30, 20));

        JLabel logo = new JLabel("📦 WMS Pro");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        logo.setForeground(ACTIVE_BTN_BG);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 50)));

        // Menü butonlarımızı oluşturup CardLayout key'leri ile bağlıyoruz
        sidebar.add(createMenuButton("📊 Ana Panel", "DASHBOARD_SAYFASI"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(createMenuButton("🛒 Ürün İşlemleri", "URUNLER_SAYFASI"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(createMenuButton("📋 Stok Listesi", "LISTELEME_SAYFASI"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(createMenuButton("⏳ İşlem Geçmişi", "ISLEM_LOG_SAYFASI"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(createMenuButton("🗄️ Raf İşlemleri", "RAFLAR_SAYFASI"));

        sidebar.add(Box.createVerticalGlue()); // Butonları yukarı iter, alt kısmı boş bırakır.

        // Dinamik Tema Değişimi (Dark/Light Mode)
        JButton btnTemaToggle = createMenuButton(isDarkMode ? "☀️ Aydınlık Mod" : "🌙 Karanlık Mod", null);
        btnTemaToggle.addActionListener(e -> {
            isDarkMode = !isDarkMode;
            initUI(); // Ekranı yeni tema renkleriyle baştan çizer
        });
        sidebar.add(btnTemaToggle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton btnCikis = createMenuButton("🚪 Çıkış Yap", null);
        btnCikis.addActionListener(e -> {
            int cevap = JOptionPane.showConfirmDialog(this, "Çıkmak istediğinize emin misiniz?", "Çıkış", JOptionPane.YES_NO_OPTION);
            if (cevap == JOptionPane.YES_OPTION) {
                this.dispose(); // Belleği (RAM) sızıntılardan korumak için ekranı yok ediyoruz.
                new LoginEkran().setVisible(true);
            }
        });
        sidebar.add(btnCikis);

        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, isDarkMode ? TABLE_GRID_COLOR : new Color(230, 230, 230)),
                new EmptyBorder(40, 25, 30, 25)));

        getContentPane().add(sidebar, BorderLayout.WEST);

        // --- ORTAK BİLEŞENLER (Modeller) ---
        // Tablolar ve Filtreler alt panellerden de erişilebilsin diye burada Singleton gibi tek bir referansla üretiliyor.
        cmbKategoriFiltre = new JComboBox<>();
        cmbKategoriFiltre.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cmbKategoriFiltre.setPreferredSize(new Dimension(170, 45));
        cmbKategoriFiltre.putClientProperty(FlatClientProperties.STYLE, COMBO_STYLE);

        String[] islemTipleri = {"Tüm İşlemler", "YENİ KAYIT", "STOK GİRİŞİ", "STOK ÇIKIŞI", "TAŞIMA", "DÜZENLEME", "RAF EKLENDİ", "RAF SİLME"};
        cmbIslemTipiFiltre = new JComboBox<>(islemTipleri);
        cmbIslemTipiFiltre.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cmbIslemTipiFiltre.setPreferredSize(new Dimension(170, 45));
        cmbIslemTipiFiltre.putClientProperty(FlatClientProperties.STYLE, COMBO_STYLE);

        String[] kolonlar = {"Kategori", "Ürün Adı", "Seri Numarası", "Stok", "Birim Fiyat", "Toplam Değer", "Raf Dağılımı", "Kayıt Tarihi"};
        tabloModeli = new DefaultTableModel(null, kolonlar) {
            @Override public boolean isCellEditable(int row, int column) { return false; } // Tablodan manuel veri değişimini engelliyoruz
        };

        String[] logKolonlar = {"Tarih / Saat", "İşlem Tipi", "Ürün Adı", "Seri Numarası", "Miktar", "Açıklama (Sistem + Not)"};
        islemTabloModeli = new DefaultTableModel(null, logKolonlar) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        String[] rafKolonlar = {"Raf Kodu", "Toplam Kapasite", "Boş Kapasite", "Doluluk Oranı"};
        rafTabloModeli = new DefaultTableModel(null, rafKolonlar);

        // --- CARD LAYOUT VE PANELLERİN EKLENMESİ ---
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(MAIN_BG);

        // Panelleri oluşturup referansları AnaEkran (this) üzerinden gönderiyoruz (Dependency Injection).
        dashboardPanel = new DashboardPanel(this);

        mainContent.add(dashboardPanel, "DASHBOARD_SAYFASI");
        mainContent.add(new UrunlerPanel(this, MAIN_BG, HEADER_TEXT_COLOR), "URUNLER_SAYFASI");
        mainContent.add(new ListelemePanel(this), "LISTELEME_SAYFASI");
        mainContent.add(new IslemLogPanel(this), "ISLEM_LOG_SAYFASI");
        mainContent.add(new RaflarPanel(this, rafTabloModeli, MAIN_BG, TABLE_BG, TABLE_GRID_COLOR, HEADER_TEXT_COLOR), "RAFLAR_SAYFASI");

        getContentPane().add(mainContent, BorderLayout.CENTER);

        cardLayout.show(mainContent, aktifSayfa);
        for (JButton btn : menuButtons) {
            if (aktifSayfa.equals(btn.getActionCommand())) {
                setActiveButton(btn);
                break;
            }
        }

        revalidate();
        repaint();
        sistemiYenile(); // Başlangıçta tüm verileri güncelleyip tabloları doldurur.
    }

    // FlatLaf Kütüphanesi ile modern UI görünümü sağlama metodu
    private void temaRenkleriniAyarla() {
        if (isDarkMode) {
            try { UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf"); } catch(Exception e){}
            MAIN_BG = new Color(30, 30, 30);
            SIDEBAR_BG = new Color(43, 43, 43);
            TABLE_BG = new Color(43, 43, 43);
            TABLE_GRID_COLOR = new Color(70, 70, 70);
            ACTIVE_BTN_BG = new Color(41, 128, 185);
            INACTIVE_BTN_BG = new Color(43, 43, 43);
            TEXT_COLOR = new Color(200, 200, 200);
            HEADER_TEXT_COLOR = new Color(240, 240, 240);
            INPUT_STYLE = "arc: 10; background: #1E1E1E; foreground: #FFFFFF; borderColor: #555555; focusedBorderColor: #3498DB;";
            COMBO_STYLE = INPUT_STYLE + " buttonBackground: #1E1E1E; buttonEditableBackground: #1E1E1E; buttonArrowColor: #FFFFFF; buttonHoverArrowColor: #3498DB;";
        } else {
            try { UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf"); } catch(Exception e){}
            MAIN_BG = new Color(245, 248, 250);
            SIDEBAR_BG = new Color(255, 255, 255);
            TABLE_BG = Color.WHITE;
            TABLE_GRID_COLOR = new Color(180, 190, 200);
            ACTIVE_BTN_BG = new Color(52, 152, 219);
            INACTIVE_BTN_BG = new Color(255, 255, 255);
            TEXT_COLOR = new Color(80, 90, 100);
            HEADER_TEXT_COLOR = new Color(44, 62, 80);
            INPUT_STYLE = "arc: 10; background: #FFFFFF; foreground: #000000; borderColor: #BDC3C7; focusedBorderColor: #3498DB;";
            COMBO_STYLE = INPUT_STYLE + " buttonBackground: #FFFFFF; buttonEditableBackground: #FFFFFF; buttonArrowColor: #2C3E50; buttonHoverArrowColor: #3498DB;";
        }
        UIManager.put("OptionPane.background", SIDEBAR_BG);
        UIManager.put("Panel.background", SIDEBAR_BG);
        UIManager.put("Label.foreground", HEADER_TEXT_COLOR);
    }

    // DRY (Don't Repeat Yourself) Prensibi: Projedeki tüm JTable'lar standart bir estetiğe sahip olsun diye tek metotta üretiyorum.
    public JTable createStyledTable(DefaultTableModel model, Color headerColor) {
        JTable tablo = new JTable(model); tablo.setRowHeight(35); tablo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tablo.setBackground(TABLE_BG); tablo.setForeground(HEADER_TEXT_COLOR); tablo.setShowGrid(true); tablo.setShowVerticalLines(true); tablo.setShowHorizontalLines(true);
        tablo.setGridColor(TABLE_GRID_COLOR); tablo.setIntercellSpacing(new Dimension(1, 1)); tablo.setEnabled(true);
        tablo.setSelectionBackground(isDarkMode ? new Color(9, 71, 113) : new Color(214, 234, 248)); tablo.setSelectionForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        tablo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15)); tablo.getTableHeader().setBackground(headerColor); tablo.getTableHeader().setForeground(isDarkMode ? Color.WHITE : HEADER_TEXT_COLOR);
        ((DefaultTableCellRenderer) tablo.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
        return tablo;
    }

    // UX (Kullanıcı Deneyimi) Dokunuşu: Hücrelere sığmayan uzun açıklamalar için Tooltip (Baloncuk) mantığını basılı tutma (Long Press) eventiyle yazdım.
    // Event-Driven programlama ve Timer sınıfı kullanarak arayüzü kilitlemeden (Asenkron) işlem yaptırdım.
    public void addCellClickBubble(JTable table) {
        MouseAdapter longPressAdapter = new MouseAdapter() {
            private Timer pressTimer;
            @Override public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (row >= 0 && col >= 0) {
                        pressTimer = new Timer(400, evt -> {
                            Object value = table.getValueAt(row, col);
                            if (value != null && !value.toString().trim().isEmpty()) {
                                String text = value.toString();
                                JPopupMenu bubble = new JPopupMenu();
                                bubble.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 2));
                                bubble.setBackground(SIDEBAR_BG);
                                JTextArea ta = new JTextArea(text);
                                ta.setWrapStyleWord(true); ta.setLineWrap(true); ta.setEditable(false);
                                ta.setFont(new Font("Segoe UI", Font.BOLD, 16)); ta.setBackground(SIDEBAR_BG);
                                ta.setForeground(HEADER_TEXT_COLOR); ta.setBorder(new EmptyBorder(15, 15, 15, 15));
                                int cols = Math.max(20, Math.min(45, text.length()));
                                int rows = Math.min(10, (text.length() / cols) + 1);
                                ta.setColumns(cols); ta.setRows(rows);
                                bubble.add(ta); bubble.show(table, e.getX(), e.getY() + 15);
                            }
                        });
                        pressTimer.setRepeats(false); pressTimer.start();
                    }
                }
            }
            @Override public void mouseReleased(MouseEvent e) { if (pressTimer != null) pressTimer.stop(); }
            @Override public void mouseExited(MouseEvent e) { if (pressTimer != null) pressTimer.stop(); }
            @Override public void mouseDragged(MouseEvent e) { if (pressTimer != null) pressTimer.stop(); }
        };
        table.addMouseListener(longPressAdapter); table.addMouseMotionListener(longPressAdapter);
    }

    // Sistem loglarına kullanıcıların isteğe bağlı not düşmesi için oluşturduğum modern Input paneli.
    private String kullaniciNotuAl(String islemAdi) {
        JTextArea txtNot = new JTextArea(4, 30);
        txtNot.setLineWrap(true); txtNot.setWrapStyleWord(true); txtNot.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtNot.setBackground(isDarkMode ? new Color(30, 30, 30) : Color.WHITE); txtNot.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        txtNot.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblSayac = new JLabel("Kalan Karakter: 200"); lblSayac.setFont(new Font("Segoe UI", Font.BOLD, 12)); lblSayac.setForeground(new Color(150, 150, 150));
        txtNot.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) { if (txtNot.getText().length() >= 200) e.consume(); }
            @Override public void keyReleased(KeyEvent e) {
                int kalan = 200 - txtNot.getText().length(); lblSayac.setText("Kalan Karakter: " + Math.max(0, kalan));
                if(kalan < 20) lblSayac.setForeground(new Color(231, 76, 60)); else lblSayac.setForeground(new Color(150, 150, 150));
            }
        });

        JPanel panel = new JPanel(new BorderLayout(5, 10)); panel.setBackground(SIDEBAR_BG); panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel baslik = new JLabel("<html><b style='font-size:14px; color:" + (isDarkMode ? "#E0E0E0" : "#2C3E50") + "'>" + islemAdi + " İçin İşlem Açıklaması (İsteğe Bağlı)</b><br><span style='color:" + (isDarkMode ? "#AAAAAA" : "#555555") + "'>Bu işlem için sistem geçmişine yansıyacak bir not yazabilirsiniz.</span></html>");
        panel.add(baslik, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(txtNot); scroll.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 2, true));
        panel.add(scroll, BorderLayout.CENTER); panel.add(lblSayac, BorderLayout.SOUTH);

        Object[] butonlar = {"İşlemi Tamamla"};
        JOptionPane.showOptionDialog(this, panel, "Not Ekle", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, butonlar, butonlar[0]);
        return txtNot.getText().replace("\n", " ").trim();
    }

    private String modernInputAl(String baslik, String mesaj) {
        JLabel lbl = new JLabel("<html><body style='width: 500px; font-family: sans-serif; font-size: 18px; font-weight: bold; padding-bottom: 10px; color:" + (isDarkMode ? "#E0E0E0" : "#000000") + "'>" + mesaj.replace("\n", "<br>") + "</body></html>");
        JTextField txt = new JTextField(); txt.setFont(new Font("Segoe UI", Font.BOLD, 22)); txt.setPreferredSize(new Dimension(500, 60)); txt.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        JPanel panel = new JPanel(new BorderLayout(0, 15)); panel.setBackground(SIDEBAR_BG); panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(lbl, BorderLayout.NORTH); panel.add(txt, BorderLayout.CENTER);
        int result = JOptionPane.showOptionDialog(this, panel, baslik, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"Onayla", "İptal"}, "Onayla");
        if (result == JOptionPane.OK_OPTION && !txt.getText().trim().isEmpty()) { return txt.getText().trim(); }
        return null;
    }

    private String modernSecimAl(String baslik, String mesaj, Object[] secenekler) {
        JLabel lbl = new JLabel("<html><body style='width: 500px; font-family: sans-serif; font-size: 18px; font-weight: bold; padding-bottom: 10px; color:" + (isDarkMode ? "#E0E0E0" : "#000000") + "'>" + mesaj.replace("\n", "<br>") + "</body></html>");
        JComboBox<Object> cb = new JComboBox<>(secenekler); cb.setFont(new Font("Segoe UI", Font.BOLD, 20)); cb.setPreferredSize(new Dimension(500, 60)); cb.putClientProperty(FlatClientProperties.STYLE, COMBO_STYLE);
        JPanel panel = new JPanel(new BorderLayout(0, 15)); panel.setBackground(SIDEBAR_BG); panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(lbl, BorderLayout.NORTH); panel.add(cb, BorderLayout.CENTER);
        int result = JOptionPane.showOptionDialog(this, panel, baslik, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"Seç", "İptal"}, "Seç");
        if (result == JOptionPane.OK_OPTION && cb.getSelectedItem() != null) { return cb.getSelectedItem().toString(); }
        return null;
    }

    // Veri doğrulama (Validation) işlemleri için gizli 8 Hak Sınırı olan Error Handling
    private Integer getGecerliPozitifSayi(String baslik, String mesaj, Integer maxKapasite) {
        int hak = 8;
        while(hak > 0) {
            String input = modernInputAl(baslik, mesaj);
            if(input == null) return null; // İptale basarsa direkt çıkar

            try {
                int sayi = Integer.parseInt(input);
                if(sayi <= 0) {
                    JOptionPane.showMessageDialog(this, "Lütfen 0'dan büyük bir sayı giriniz!", "Hatalı Değer", JOptionPane.ERROR_MESSAGE);
                }
                else if (maxKapasite != null && sayi > maxKapasite) {
                    JOptionPane.showMessageDialog(this, "Girdiğiniz miktar maksimum sınırı (" + maxKapasite + ") aşıyor!", "Sınır Aşıldı", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    return sayi;
                }
            } catch(Exception e) {
                JOptionPane.showMessageDialog(this, "Lütfen geçerli bir tamsayı giriniz!", "Geçersiz Format", JOptionPane.ERROR_MESSAGE);
            }
            hak--;
        }
        // 8 hak biterse sessizce fişi çeker
        JOptionPane.showMessageDialog(this, "Çok fazla hatalı deneme yaptınız, ana menüye yönlendiriliyorsunuz.", "İşlem Kesildi", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    private Integer hedefRafSecimiAl(String baslik, String mesaj) {
        if (rafManager.getRafSayisi() == 0) { JOptionPane.showMessageDialog(this, "Sistemde kayıtlı raf bulunmuyor!", "Hata", JOptionPane.ERROR_MESSAGE); return null; }
        Object[] rafSecenekleri = new Object[rafManager.getRafSayisi()];
        for(int i=0; i < rafManager.getRafSayisi(); i++) rafSecenekleri[i] = (i + 1) + ". Raf (Boş: " + rafManager.getRafKapasitesi(i) + ")";
        String secim = modernSecimAl(baslik, mesaj, rafSecenekleri);
        if(secim == null) return null;
        return Integer.parseInt(secim.split("\\.")[0]) - 1;
    }

    private Urun getGecerliUrun(String baslik, String mesaj) {
        int hak = 8;
        while(hak > 0) {
            String seriNo = modernInputAl(baslik, mesaj);
            if(seriNo == null) return null;

            Urun u = urunManager.urunBul(seriNo);
            if(u != null) return u;

            JOptionPane.showMessageDialog(this, "Ürün bulunamadı! Lütfen geçerli bir seri numarası giriniz.", "Bulunamadı", JOptionPane.ERROR_MESSAGE);
            hak--;
        }
        JOptionPane.showMessageDialog(this, "Çok fazla hatalı deneme yaptınız, ana menüye yönlendiriliyorsunuz.", "İşlem Kesildi", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    private void setActiveButton(JButton activeButton) {
        for (JButton btn : menuButtons) { btn.setBackground(INACTIVE_BTN_BG); btn.setForeground(TEXT_COLOR); btn.setFont(new Font("Segoe UI", Font.PLAIN, 20)); }
        activeButton.setBackground(ACTIVE_BTN_BG); activeButton.setForeground(Color.WHITE); activeButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
    }

    private JButton createMenuButton(String t, String p) {
        JButton b = new JButton(t); b.setFont(new Font("Segoe UI", Font.BOLD, 20)); b.setForeground(TEXT_COLOR); b.setBackground(INACTIVE_BTN_BG);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(300, 65)); b.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        if (p != null) {
            b.setActionCommand(p);
            b.addActionListener(e -> {
                cardLayout.show(mainContent, p);
                aktifSayfa = p;
                setActiveButton(b);
            });
            menuButtons.add(b);
        }
        return b;
    }

    public void islemLoglariniFiltrele(String arananKelime) {
        String kelime = arananKelime.toLowerCase().trim();
        String secilenTip = cmbIslemTipiFiltre != null && cmbIslemTipiFiltre.getSelectedItem() != null ? cmbIslemTipiFiltre.getSelectedItem().toString() : "Tüm İşlemler";

        if(islemTabloModeli != null) {
            islemTabloModeli.setRowCount(0);
            List<IslemLog> tumLoglar = islemDao.tumLoglariGetir();

            for (IslemLog log : tumLoglar) {
                boolean kelimeUyuyor = log.getUrunAdi().toLowerCase().contains(kelime) ||
                        log.getSeriNo().toLowerCase().contains(kelime) ||
                        log.getAciklama().toLowerCase().contains(kelime);

                boolean tipUyuyor = secilenTip.equals("Tüm İşlemler") || log.getIslemTipi().equalsIgnoreCase(secilenTip);

                if (kelimeUyuyor && tipUyuyor) {
                    islemTabloModeli.addRow(new Object[]{
                            log.getTarih(), log.getIslemTipi(), log.getUrunAdi(),
                            log.getSeriNo(), log.getMiktarDegisimi(), log.getAciklama()
                    });
                }
            }
        }
    }

    private void tabloyuGuncelle() {
        if (tabloModeli != null) {
            tabloModeli.setRowCount(0);
            for(Urun u : urunManager.getUrunler()) {
                double toplamUrunDegeri = u.getMiktar() * u.getBirimFiyat();
                tabloModeli.addRow(new Object[]{
                        u.getKategori(),
                        u.getAd(),
                        u.getSeriNo(),
                        u.getMiktar(),
                        String.format("%,.2f ₺", u.getBirimFiyat()),
                        String.format("%,.2f ₺", toplamUrunDegeri),
                        u.getRafKodlariString(),
                        u.getEklenmeTarihi()
                });
            }
        }
    }

    public void listeyiFiltrele(String arananKelime) {
        String kelime = arananKelime.toLowerCase().trim();
        String secilenKategori = cmbKategoriFiltre.getSelectedItem() != null ? cmbKategoriFiltre.getSelectedItem().toString() : "Tüm Kategoriler";

        if(tabloModeli != null) {
            tabloModeli.setRowCount(0);
            for (Urun u : urunManager.getUrunler()) {
                String urunKat = u.getKategori() != null ? u.getKategori().trim() : "";
                boolean kelimeUyuyor = u.getSeriNo().toLowerCase().contains(kelime) || u.getAd().toLowerCase().contains(kelime);
                boolean kategoriUyuyor = secilenKategori.equals("Tüm Kategoriler") || urunKat.equalsIgnoreCase(secilenKategori.trim());

                if (kelimeUyuyor && kategoriUyuyor) {
                    double toplamUrunDegeri = u.getMiktar() * u.getBirimFiyat();
                    tabloModeli.addRow(new Object[]{
                            u.getKategori(),
                            u.getAd(),
                            u.getSeriNo(),
                            u.getMiktar(),
                            String.format("%,.2f ₺", u.getBirimFiyat()),
                            String.format("%,.2f ₺", toplamUrunDegeri),
                            u.getRafKodlariString(),
                            u.getEklenmeTarihi()
                    });
                }
            }
        }
    }

    //CRUD OPERASYON EKRANLARI (Create, Read, Update, Delete)

    public void yeniKayitTetikle() {
        if (rafManager.getRafSayisi() == 0) {
            JOptionPane.showMessageDialog(this, "Depoda hiç raf yok! Önce raf ekleyiniz.", "Uyarı", JOptionPane.WARNING_MESSAGE);
        } else {
            yeniKayitFormuAc();
        }
    }

    // Modal Pencere mantığıyla oluşturulmuş Custom Form
    private void yeniKayitFormuAc() {
        JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true); // Windows pencere çerçevesini kaldırıp kendi tasarımımızı ekliyoruz
        dialog.setSize(550, 800);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(SIDEBAR_BG);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(SIDEBAR_BG);
        titleBar.setBorder(new EmptyBorder(10, 20, 10, 20));
        JLabel titleLabel = new JLabel("📦 Yeni Ürün Kaydı");
        titleLabel.setForeground(HEADER_TEXT_COLOR);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JButton closeButton = new JButton("X"); closeButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        closeButton.setForeground(HEADER_TEXT_COLOR);
        closeButton.setBorderPainted(false); closeButton.setContentAreaFilled(false);
        closeButton.addActionListener(e -> dialog.dispose());
        titleBar.add(titleLabel, BorderLayout.WEST); titleBar.add(closeButton, BorderLayout.EAST);
        ComponentDragger dragger = new ComponentDragger(dialog); // Çerçevesiz ekranı Mouse ile sürüklemek için Custom Class
        titleBar.addMouseListener(dragger); titleBar.addMouseMotionListener(dragger);
        dialog.add(titleBar, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(14, 1, 5, 5));
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        formPanel.setBackground(SIDEBAR_BG);

        JTextField txtAd = new JTextField(); txtAd.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtAd.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextField txtSeriNo = new JTextField(); txtSeriNo.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtSeriNo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextField txtMiktar = new JTextField(); txtMiktar.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtMiktar.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextField txtFiyat = new JTextField(); txtFiyat.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtFiyat.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JComboBox<String> cmbKategori = new JComboBox<>(); cmbKategori.setEditable(true); cmbKategori.putClientProperty(FlatClientProperties.STYLE, COMBO_STYLE);
        cmbKategori.setFont(new Font("Segoe UI", Font.BOLD, 18));
        for (String kat : urunDao.tumKategorileriGetir()) cmbKategori.addItem(kat);

        JTextField txtKritik = new JTextField(); txtKritik.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtKritik.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JComboBox<String> cmbRaflar = new JComboBox<>(); cmbRaflar.putClientProperty(FlatClientProperties.STYLE, COMBO_STYLE);
        cmbRaflar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        for (int i = 0; i < rafManager.getRafSayisi(); i++) cmbRaflar.addItem((i + 1) + ". Raf (Boş: " + rafManager.getRafKapasitesi(i) + ")");

        Font lblFont = new Font("Segoe UI", Font.BOLD, 16);

        JLabel l1 = new JLabel("Kategori:"); l1.setForeground(HEADER_TEXT_COLOR); l1.setFont(lblFont); formPanel.add(l1); formPanel.add(cmbKategori);
        JLabel l2 = new JLabel("Ürün Adı:"); l2.setForeground(HEADER_TEXT_COLOR); l2.setFont(lblFont); formPanel.add(l2); formPanel.add(txtAd);
        JLabel l3 = new JLabel("Seri Numarası:"); l3.setForeground(HEADER_TEXT_COLOR); l3.setFont(lblFont); formPanel.add(l3); formPanel.add(txtSeriNo);
        JLabel l4 = new JLabel("Birim Fiyatı (₺):"); l4.setForeground(HEADER_TEXT_COLOR); l4.setFont(lblFont); formPanel.add(l4); formPanel.add(txtFiyat);
        JLabel l5 = new JLabel("Miktar:"); l5.setForeground(HEADER_TEXT_COLOR); l5.setFont(lblFont); formPanel.add(l5); formPanel.add(txtMiktar);
        JLabel l6 = new JLabel("Kritik Stok Sınırı:"); l6.setForeground(HEADER_TEXT_COLOR); l6.setFont(lblFont); formPanel.add(l6); formPanel.add(txtKritik);
        JLabel l7 = new JLabel("Hedef Raf:"); l7.setForeground(HEADER_TEXT_COLOR); l7.setFont(lblFont); formPanel.add(l7); formPanel.add(cmbRaflar);

        JButton btnKaydet = new JButton("Devam Et (Not Ekle)");
        btnKaydet.setBackground(new Color(46, 204, 113)); btnKaydet.setForeground(Color.WHITE); btnKaydet.setFont(new Font("Segoe UI", Font.BOLD, 18));

        final int[] hataSayaci = {0};

        btnKaydet.addActionListener(e -> {
            try {
                String kategori = cmbKategori.getSelectedItem() != null ? cmbKategori.getSelectedItem().toString().trim() : "Genel";
                String ad = txtAd.getText(), seriNo = txtSeriNo.getText();
                if (ad.isEmpty() || seriNo.isEmpty()) throw new Exception("Alanları doldurunuz!");
                int miktar = Integer.parseInt(txtMiktar.getText());
                int kritik = Integer.parseInt(txtKritik.getText());
                double birimFiyat = Double.parseDouble(txtFiyat.getText().replace(",", "."));

                int rafIndex = cmbRaflar.getSelectedIndex();
                if (urunManager.urunBul(seriNo) != null) throw new Exception("Bu seri numarası zaten var!");

                if (miktar > rafManager.getRafKapasitesi(rafIndex)) {
                    int cevap = JOptionPane.showConfirmDialog(dialog, "Seçilen rafta yeterli yer yok!\nÜrünü parçalı dağıtmak ister misiniz?", "Yetersiz", JOptionPane.YES_NO_OPTION);
                    if (cevap == JOptionPane.YES_OPTION) {
                        dialog.dispose();
                        parcaliEklemeBaslat("Yeni Kayıt", ad, seriNo, miktar, null, kategori, kritik, birimFiyat);
                    }
                    return;
                }

                String kullaniciNotu = kullaniciNotuAl("Yeni Kayıt");

                Urun yeni = new Urun(ad, seriNo, miktar, rafIndex, kategori, kritik, birimFiyat);
                urunManager.getUrunler().add(yeni);
                rafManager.kapasiteGuncelle(rafIndex, -miktar);
                urunDao.veritabaniniGuncelle(urunManager.getUrunler());

                String logAciklama = "Sisteme ilk kez eklendi. Fiyat: " + birimFiyat + " ₺. Hedef: " + (rafIndex+1) + ". Raf. " + (kullaniciNotu.isEmpty() ? "" : "[Not: " + kullaniciNotu + "]");
                islemDao.logEkle(new IslemLog("YENİ KAYIT", ad, seriNo, "+" + miktar, logAciklama));

                sistemiYenile();
                JOptionPane.showMessageDialog(this, "Ürün Başarıyla Eklendi!");
                dialog.dispose();
            } catch (Exception ex) {
                hataSayaci[0]++;
                if(hataSayaci[0] >= 8) {
                    JOptionPane.showMessageDialog(dialog, "Çok fazla hatalı deneme yaptınız, ana menüye yönlendiriliyorsunuz.", "Güvenlik İptali", JOptionPane.ERROR_MESSAGE);
                    dialog.dispose(); // Formu tamamen kapatır
                } else {
                    JOptionPane.showMessageDialog(dialog, "Hata: Eksik veya geçersiz bilgi girdiniz! (Sayısal alanlara harf girmeyiniz)", "Hatalı Giriş", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel altPanel = new JPanel(); altPanel.setBackground(SIDEBAR_BG); altPanel.add(btnKaydet);
        dialog.add(formPanel, BorderLayout.CENTER); dialog.add(altPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // UPDATE İşlemi
    public void urunDuzenleFormuAc(Urun mevcutUrun) {
        JDialog dialog = new JDialog(this, true); dialog.setUndecorated(true); dialog.setSize(550, 750); dialog.setLocationRelativeTo(this); dialog.setLayout(new BorderLayout());
        JPanel titleBar = new JPanel(new BorderLayout()); titleBar.setBackground(SIDEBAR_BG); titleBar.setBorder(new EmptyBorder(10, 20, 10, 20));
        JLabel titleLabel = new JLabel("✏️ Ürün Bilgilerini Düzenle"); titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20)); titleLabel.setForeground(HEADER_TEXT_COLOR);
        JButton closeButton = new JButton("X"); closeButton.setBorderPainted(false); closeButton.setContentAreaFilled(false); closeButton.setForeground(HEADER_TEXT_COLOR); closeButton.addActionListener(e -> dialog.dispose());
        titleBar.add(titleLabel, BorderLayout.WEST); titleBar.add(closeButton, BorderLayout.EAST);

        // Sürükle-bırak için
        ComponentDragger dragger = new ComponentDragger(dialog);
        titleBar.addMouseListener(dragger); titleBar.addMouseMotionListener(dragger);
        dialog.add(titleBar, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(12, 1, 5, 5)); formPanel.setBorder(new EmptyBorder(20, 30, 20, 30)); formPanel.setBackground(SIDEBAR_BG);

        JComboBox<String> cmbKat = new JComboBox<>(); cmbKat.setEditable(true); cmbKat.putClientProperty(FlatClientProperties.STYLE, COMBO_STYLE);
        cmbKat.setFont(new Font("Segoe UI", Font.BOLD, 18));
        for (String k : urunDao.tumKategorileriGetir()) cmbKat.addItem(k); cmbKat.setSelectedItem(mevcutUrun.getKategori());

        JTextField txtAd = new JTextField(mevcutUrun.getAd()); txtAd.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtAd.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextField txtSeriNo = new JTextField(mevcutUrun.getSeriNo()); txtSeriNo.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtSeriNo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextField txtFiyat = new JTextField(String.valueOf(mevcutUrun.getBirimFiyat())); txtFiyat.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtFiyat.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextField txtKritik = new JTextField(String.valueOf(mevcutUrun.getKritikEsik())); txtKritik.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtKritik.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextField txtMiktar = new JTextField(mevcutUrun.getMiktar() + " Adet (Salt Okunur)"); txtMiktar.setEditable(false);
        txtMiktar.setFont(new Font("Segoe UI", Font.BOLD, 18));

        Font lblFont = new Font("Segoe UI", Font.BOLD, 16);
        JLabel l1 = new JLabel("Kategori:"); l1.setForeground(HEADER_TEXT_COLOR); l1.setFont(lblFont); formPanel.add(l1); formPanel.add(cmbKat);
        JLabel l2 = new JLabel("Ürün Adı:"); l2.setForeground(HEADER_TEXT_COLOR); l2.setFont(lblFont); formPanel.add(l2); formPanel.add(txtAd);
        JLabel l3 = new JLabel("Seri Numarası:"); l3.setForeground(HEADER_TEXT_COLOR); l3.setFont(lblFont); formPanel.add(l3); formPanel.add(txtSeriNo);
        JLabel l4 = new JLabel("Birim Fiyatı (₺):"); l4.setForeground(HEADER_TEXT_COLOR); l4.setFont(lblFont); formPanel.add(l4); formPanel.add(txtFiyat);
        JLabel l5 = new JLabel("Kritik Sınır:"); l5.setForeground(HEADER_TEXT_COLOR); l5.setFont(lblFont); formPanel.add(l5); formPanel.add(txtKritik);
        JLabel l6 = new JLabel("Miktar:"); l6.setForeground(HEADER_TEXT_COLOR); l6.setFont(lblFont); formPanel.add(l6); formPanel.add(txtMiktar);

        JButton btnKaydet = new JButton("Devam Et (Not Ekle)");
        btnKaydet.setBackground(new Color(52, 152, 219)); btnKaydet.setForeground(Color.WHITE); btnKaydet.setFont(new Font("Segoe UI", Font.BOLD, 18));

        // Gizli Sayacımız burada tanımlanıyor
        final int[] hataSayaci = {0};

        btnKaydet.addActionListener(e -> {
            try {
                String yeniKat = cmbKat.getSelectedItem().toString().trim();
                String yeniAd = txtAd.getText().trim();
                String yeniSeri = txtSeriNo.getText().trim();

                if (yeniAd.isEmpty() || yeniSeri.isEmpty()) throw new Exception("Alanları doldurunuz!");

                int yeniKritik = Integer.parseInt(txtKritik.getText().trim());
                double yeniFiyat = Double.parseDouble(txtFiyat.getText().replace(",", "."));

                String kullaniciNotu = kullaniciNotuAl("Ürün Düzenle");

                mevcutUrun.setKategori(yeniKat);
                mevcutUrun.setAd(yeniAd);
                mevcutUrun.setSeriNo(yeniSeri);
                mevcutUrun.setKritikEsik(yeniKritik);
                mevcutUrun.setBirimFiyat(yeniFiyat);
                urunDao.veritabaniniGuncelle(urunManager.getUrunler());

                String logAciklama = "Ürün bilgileri/fiyatı güncellendi. " + (kullaniciNotu.isEmpty() ? "" : "[Not: " + kullaniciNotu + "]");
                islemDao.logEkle(new IslemLog("DÜZENLEME", yeniAd, yeniSeri, "0", logAciklama));

                sistemiYenile();
                JOptionPane.showMessageDialog(this, "Güncellendi!");
                dialog.dispose();

            } catch (Exception ex) {
                // HATA YAKALAMA VE 8 HAK MANTIĞI
                hataSayaci[0]++;
                if(hataSayaci[0] >= 8) {
                    JOptionPane.showMessageDialog(dialog, "Çok fazla hatalı deneme yaptınız, ana menüye yönlendiriliyorsunuz.", "Güvenlik İptali", JOptionPane.ERROR_MESSAGE);
                    dialog.dispose(); // Formu tamamen kapatır
                } else {
                    JOptionPane.showMessageDialog(dialog, "Hata: Eksik veya geçersiz bilgi girdiniz! (Sayısal alanlara harf girmeyiniz)", "Hatalı Giriş", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel alt = new JPanel(); alt.setBackground(SIDEBAR_BG); alt.add(btnKaydet);
        dialog.add(formPanel, BorderLayout.CENTER); dialog.add(alt, BorderLayout.SOUTH); dialog.setVisible(true);
    }

    // Parçalı dağıtım algoritması: Rekürsif mantığa benzeyen, while döngüsü ile kalan ürün bitene kadar çalışan bir sistem.
    private void parcaliEklemeBaslat(String baslik, String ad, String seriNo, int miktar, Urun mevcut, String kat, int kritik, double fiyat) {
        int kalan = miktar;
        Urun islemUrunu = mevcut;

        if (islemUrunu == null) {
            islemUrunu = new Urun(ad, seriNo, miktar, 0, kat, kritik, fiyat);
            islemUrunu.getRafDagilimi().clear();
        } else {
            islemUrunu.setMiktar(islemUrunu.getMiktar() + miktar);
        }

        while (kalan > 0) {
            Integer secilenRaf = hedefRafSecimiAl(baslik, "Kalan: " + kalan + " adet.\nHangi rafa ekleyeceksiniz?");
            if (secilenRaf == null) break;

            int bosYer = rafManager.getRafKapasitesi(secilenRaf);
            if (bosYer <= 0) {
                JOptionPane.showMessageDialog(this, "Seçilen rafta boş yer yok!", "Dolu", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            Integer eklenecek = getGecerliPozitifSayi(baslik, "Seçilen rafta " + bosYer + " boş yer var.\nKaç adet koyacaksınız?", Math.min(bosYer, kalan));
            if (eklenecek == null) break;

            rafManager.kapasiteGuncelle(secilenRaf, -eklenecek);
            islemUrunu.rafVeMiktarEkle(secilenRaf, eklenecek);
            kalan -= eklenecek;
        }

        if (mevcut == null) {
            if (!islemUrunu.getRafDagilimi().isEmpty()) {
                islemUrunu.setMiktar(miktar - kalan);
                islemUrunu.setRafIndex(islemUrunu.getRafDagilimi().keySet().iterator().next());
                urunManager.getUrunler().add(islemUrunu);

                String kullaniciNotu = kullaniciNotuAl("Parçalı Yeni Kayıt");
                String logAciklama = "Sisteme ilk kez, " + islemUrunu.getRafDagilimi().size() + " farklı rafa parçalı olarak eklendi. " + (kullaniciNotu.isEmpty() ? "" : "[Not: " + kullaniciNotu + "]");
                islemDao.logEkle(new IslemLog("YENİ KAYIT", ad, seriNo, "+" + islemUrunu.getMiktar(), logAciklama));
            }
        } else {
            islemUrunu.setMiktar(islemUrunu.getMiktar() - kalan);
            if (miktar > kalan) {
                String kullaniciNotu = kullaniciNotuAl("Parçalı Stok Girişi");
                String logAciklama = "Parçalı olarak mevcut stoğa eklendi. " + (kullaniciNotu.isEmpty() ? "" : "[Not: " + kullaniciNotu + "]");
                islemDao.logEkle(new IslemLog("STOK GİRİŞİ", ad, seriNo, "+" + (miktar - kalan), logAciklama));
            }
        }
        urunDao.veritabaniniGuncelle(urunManager.getUrunler());
        sistemiYenile();
        JOptionPane.showMessageDialog(this, "Parçalı işlem tamamlandı!");
    }

    // Method Overloading
    private void parcaliEklemeBaslat(String baslik, String ad, String seriNo, int miktar, Urun mevcut) {
        parcaliEklemeBaslat(baslik, ad, seriNo, miktar, mevcut, mevcut.getKategori(), mevcut.getKritikEsik(), mevcut.getBirimFiyat());
    }

    public void mevcutStogaEkleFormuAc() {
        Urun u = getGecerliUrun("Stoğa Ekle", "Seri Numarası:"); if (u == null) return;
        Integer m = getGecerliPozitifSayi("Stoğa Ekle", "Eklenecek Miktar:", null); if (m == null) return;

        Integer raf = hedefRafSecimiAl("Stoğa Ekle", "Hangi rafa eklenecek?");
        if (raf == null) return;

        if (m > rafManager.getRafKapasitesi(raf)) {
            int cevap = JOptionPane.showConfirmDialog(this, "Seçilen rafta yer yok! Parçalı eklemek ister misiniz?", "Yetersiz", JOptionPane.YES_NO_OPTION);
            if(cevap == JOptionPane.YES_OPTION) parcaliEklemeBaslat("Mevcut Stoğa Ekle", u.getAd(), u.getSeriNo(), m, u);
            return;
        }

        String kullaniciNotu = kullaniciNotuAl("Stok Girişi");

        rafManager.kapasiteGuncelle(raf, -m); u.setMiktar(u.getMiktar() + m); u.rafVeMiktarEkle(raf, m);
        urunDao.veritabaniniGuncelle(urunManager.getUrunler());

        String logAciklama = (raf+1) + ". Rafa eklendi. " + (kullaniciNotu.isEmpty() ? "" : "[Not: " + kullaniciNotu + "]");
        islemDao.logEkle(new IslemLog("STOK GİRİŞİ", u.getAd(), u.getSeriNo(), "+" + m, logAciklama));

        sistemiYenile(); JOptionPane.showMessageDialog(this, "Başarılı!");
    }

    // DELETE Operasyonu
    public void urunCikarFormuAc() {
        Urun u = getGecerliUrun("Ürün Çıkar", "Seri Numarası:"); if(u == null || u.getRafDagilimi().isEmpty()) return;
        Object[] rArr = u.getRafDagilimi().keySet().stream().map(r -> (r + 1) + ". Raf").toArray(); // Modern Java (Stream API) Kullanımı
        String secRaf = modernSecimAl("Ürün Çıkar", "Hangi Raf?", rArr); if (secRaf == null) return;
        int raf = Integer.parseInt(secRaf.split("\\.")[0]) - 1;
        Integer m = getGecerliPozitifSayi("Ürün Çıkar", "Miktar:", u.getRafDagilimi().get(raf)); if(m == null) return;

        String kullaniciNotu = kullaniciNotuAl("Stok Çıkışı");

        rafManager.kapasiteGuncelle(raf, +m); u.raftanUrunEksilt(raf, m); u.setMiktar(u.getMiktar() - m);
        if (u.getMiktar() == 0) urunManager.getUrunler().remove(u); // Ürün tamamen bittiyse koleksiyondan temizle
        urunDao.veritabaniniGuncelle(urunManager.getUrunler());

        String logAciklama = (raf+1) + ". Raftan çıkarıldı. " + (kullaniciNotu.isEmpty() ? "" : "[Not: " + kullaniciNotu + "]");
        islemDao.logEkle(new IslemLog("STOK ÇIKIŞI", u.getAd(), u.getSeriNo(), "-" + m, logAciklama));

        sistemiYenile(); JOptionPane.showMessageDialog(this, "Başarılı!");
    }

    public void urunTasiFormuAc() {
        Urun u = getGecerliUrun("Ürün Taşı", "Seri Numarası:"); if(u == null || u.getRafDagilimi().isEmpty()) return;
        Object[] rArr = u.getRafDagilimi().keySet().stream().map(r -> (r + 1) + ". Raf").toArray();
        String eski = modernSecimAl("Ürün Taşı", "Kaynak Raf?", rArr); if (eski == null) return;
        int eRaf = Integer.parseInt(eski.split("\\.")[0]) - 1;
        Integer m = getGecerliPozitifSayi("Ürün Taşı", "Taşınacak Miktar:", u.getRafDagilimi().get(eRaf)); if (m == null) return;

        Integer yRaf = hedefRafSecimiAl("Ürün Taşı", "Hedef Raf:");
        if (yRaf == null) return;

        if (rafManager.getRafKapasitesi(yRaf) < m) { JOptionPane.showMessageDialog(this, "Yer yok!", "Hata", JOptionPane.ERROR_MESSAGE); return; }

        String kullaniciNotu = kullaniciNotuAl("Ürün Taşıma");

        rafManager.kapasiteGuncelle(eRaf, +m); rafManager.kapasiteGuncelle(yRaf, -m); u.rafTasimaGuncellemesi(eRaf, yRaf, m);
        urunDao.veritabaniniGuncelle(urunManager.getUrunler());

        String logAciklama = (eRaf+1) + ". Raftan " + (yRaf+1) + ". Rafa taşındı. " + (kullaniciNotu.isEmpty() ? "" : "[Not: " + kullaniciNotu + "]");
        islemDao.logEkle(new IslemLog("TAŞIMA", u.getAd(), u.getSeriNo(), String.valueOf(m), logAciklama));

        sistemiYenile(); JOptionPane.showMessageDialog(this, "Başarılı!");
    }

    public void rafSilFormuAc() {
        if(rafManager.getRafSayisi() == 0) return;
        Integer adet = getGecerliPozitifSayi("Raf Sil", "Kaç adet raf silmek istiyorsunuz?", rafManager.getRafSayisi());
        if (adet == null) return;

        List<Integer> silinecekler = new ArrayList<>();
        List<Object[]> detaylar = rafDao.getRafDetaylari();

        // Hata sayacımızı döngünün dışında tanımlıyoruz
        int hataSayaci = 0;

        for (int i = 1; i <= adet; i++) {
            List<String> secenekler = new ArrayList<>();
            for (int j = 0; j < detaylar.size(); j++) {
                if (!silinecekler.contains(j)) {
                    secenekler.add((j + 1) + ". Raf (Doluluk oranı: " + detaylar.get(j)[3] + ")");
                }
            }
            if (secenekler.isEmpty()) break;

            String secim = modernSecimAl("Raf Sil", "Silmek istediğiniz " + i + ". rafı seçiniz:", secenekler.toArray());

            // Kullanıcı kendi isteğiyle iptale basarsa
            if (secim == null) {
                if (!silinecekler.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "İşlem yarıda kesildi! Seçtiğiniz raflar için onay ekranına geçiliyor...", "Kısmi İşlem", JOptionPane.INFORMATION_MESSAGE);
                }
                break;
            }

            int secilenIndex = Integer.parseInt(secim.split("\\.")[0]) - 1;

            // Eğer seçilen raf doluysa
            if (urunManager.rafDoluMu(secilenIndex)) {
                hataSayaci++; // Sayacı artırıyoruz

                if (hataSayaci >= 8) { // 8 kere inat ederse
                    JOptionPane.showMessageDialog(this, "Çok fazla hatalı deneme yaptınız! İşlem yarıda kesiliyor.", "Güvenlik İptali", JOptionPane.ERROR_MESSAGE);
                    if (!silinecekler.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Şu ana kadar seçtiğiniz geçerli raflar için onay ekranına geçiliyor...", "Kısmi İşlem", JOptionPane.INFORMATION_MESSAGE);
                    }
                    break; // Döngüyü kırıp direkt onay aşamasına geçer
                } else {
                    JOptionPane.showMessageDialog(this, (secilenIndex + 1) + ". Raf DOLU! Sadece boş rafları silebilirsiniz.", "Uyarı", JOptionPane.WARNING_MESSAGE);
                    i--; // Hakkı yanmasın diye aynı i değerinden tekrar sorar
                    continue;
                }
            }

            // Eğer boş bir rafı başarıyla seçerse sayacı sıfırlıyoruz ki başka raflarda hakkı taze olsun
            hataSayaci = 0;
            silinecekler.add(secilenIndex);
        }

        // Eğer en baştan itibaren hiçbir raf seçemediyse veya işlemi komple iptal ettiyse
        if (silinecekler.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silinecek hiçbir geçerli raf seçilmedi. İşlem iptal edildi.", "İptal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Collections.sort(silinecekler, Collections.reverseOrder());

        StringBuilder sb = new StringBuilder();
        List<Integer> gosterimIcin = new ArrayList<>(silinecekler);
        Collections.sort(gosterimIcin);
        for (int r : gosterimIcin) sb.append(r + 1).append("., ");
        String rafListesiStr = sb.substring(0, sb.length() - 2);

        int onay = JOptionPane.showConfirmDialog(this, "Seçtiğiniz " + rafListesiStr + " raflarını silmekten emin misiniz?", "Toplu Silme Onayı", JOptionPane.YES_NO_OPTION);
        if (onay != JOptionPane.YES_OPTION) return;

        String kullaniciNotu = kullaniciNotuAl("Raf Silme");

        for (int idx : silinecekler) {
            if (rafManager.rafSil(idx)) {
                urunManager.rafSilindiktenSonraGuncelle(idx);
            }
        }

        String logAciklama = "Sistemden " + rafListesiStr + " numaralı raflar tamamen silindi. " + (kullaniciNotu.isEmpty() ? "" : "[Not: " + kullaniciNotu + "]");
        islemDao.logEkle(new IslemLog("RAF SİLME", "-", "-", "-" + silinecekler.size() + " Raf", logAciklama));

        sistemiYenile();
        JOptionPane.showMessageDialog(this, silinecekler.size() + " adet raf başarıyla silindi!");
    }

    public void cokluRafEkleFormuAc() {
        Integer adet = getGecerliPozitifSayi("Raf Ekle", "Kaç adet raf eklemek istiyorsunuz?", null);
        if(adet == null) return;

        RafDao rDao = new RafDao();
        List<Integer> eklenenKapasiteler = new ArrayList<>();

        for (int i = 1; i <= adet; i++) {
            Integer kap = getGecerliPozitifSayi("Raf Ekle", "Eklenecek " + i + ". rafın kapasitesi ne olsun?", null);
            if(kap == null) break;
            rDao.rafEkle(new Raf(kap));
            eklenenKapasiteler.add(kap);
        }

        if (eklenenKapasiteler.isEmpty()) return;

        String kullaniciNotu = kullaniciNotuAl("Raf Ekleme");
        String logAciklama = eklenenKapasiteler.size() + " adet yeni raf sisteme eklendi. (Kapasiteler: " + eklenenKapasiteler.toString() + ") " + (kullaniciNotu.isEmpty() ? "" : "[Not: " + kullaniciNotu + "]");
        islemDao.logEkle(new IslemLog("RAF EKLENDİ", "-", "-", "+" + eklenenKapasiteler.size() + " Raf", logAciklama));

        sistemiYenile(); JOptionPane.showMessageDialog(this, eklenenKapasiteler.size() + " adet raf başarıyla eklendi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
    }

    // Kurumsal projelerin vazgeçilmezi olan Raporlama modülleri.
    public void islemExcelRaporAl() {
        if (islemTabloModeli.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Dışa aktarılacak işlem kaydı bulunamadı!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        FileDialog fileDialog = new FileDialog(this, "İşlem Geçmişi Excel Kaydet", FileDialog.SAVE);
        fileDialog.setFile("WMS_Islem_Gecmisi.csv");
        fileDialog.setVisible(true);

        String dir = fileDialog.getDirectory(), file = fileDialog.getFile();
        if (dir != null && file != null) {
            try {
                FileOutputStream fos = new FileOutputStream(new File(dir, file));
                // Excel'de Türkçe karakterlerin (ş, ğ, İ) bozuk çıkmaması için UTF-8 BOM byte marker ekliyorum.
                fos.write(0xef); fos.write(0xbb); fos.write(0xbf);
                java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(fos, "UTF-8"));

                writer.println("Tarih / Saat;İşlem Tipi;Ürün Adı;Seri Numarası;Miktar;Açıklama");
                for (int i = 0; i < islemTabloModeli.getRowCount(); i++) {
                    writer.printf("\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";\"%s\"\n",
                            islemTabloModeli.getValueAt(i, 0), islemTabloModeli.getValueAt(i, 1),
                            islemTabloModeli.getValueAt(i, 2), islemTabloModeli.getValueAt(i, 3),
                            islemTabloModeli.getValueAt(i, 4), islemTabloModeli.getValueAt(i, 5));
                }
                writer.close();
                JOptionPane.showMessageDialog(this, "Excel Raporu Başarıyla Oluşturuldu!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void islemPdfRaporAl() {
        if (islemTabloModeli.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Dışa aktarılacak işlem kaydı bulunamadı!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        FileDialog fileDialog = new FileDialog(this, "İşlem Geçmişi PDF Kaydet", FileDialog.SAVE);
        fileDialog.setFile("WMS_Islem_Gecmisi.pdf");
        fileDialog.setVisible(true);

        String dir = fileDialog.getDirectory(), file = fileDialog.getFile();
        if (dir != null && file != null) {
            try {
                Document document = new Document();
                document.setPageSize(com.itextpdf.text.PageSize.A4.rotate());
                PdfWriter.getInstance(document, new FileOutputStream(new File(dir, file)));
                document.open();

                Paragraph baslik = new Paragraph("WMS Pro - Islem Gecmisi (Log)\n\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20));
                baslik.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(baslik);

                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{3f, 2.5f, 4f, 3f, 2f, 6f});

                table.addCell("Tarih / Saat"); table.addCell("Islem Tipi"); table.addCell("Urun Adi");
                table.addCell("Seri Numarasi"); table.addCell("Miktar"); table.addCell("Aciklama");

                for (int i = 0; i < islemTabloModeli.getRowCount(); i++) {
                    table.addCell(islemTabloModeli.getValueAt(i, 0).toString());
                    table.addCell(islemTabloModeli.getValueAt(i, 1).toString());
                    table.addCell(islemTabloModeli.getValueAt(i, 2).toString());
                    table.addCell(islemTabloModeli.getValueAt(i, 3).toString());
                    table.addCell(islemTabloModeli.getValueAt(i, 4).toString());
                    table.addCell(islemTabloModeli.getValueAt(i, 5).toString());
                }
                document.add(table); document.close();
                JOptionPane.showMessageDialog(this, "PDF Raporu Başarıyla Oluşturuldu!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void excelRaporAl() {
        if (urunManager.getUrunler().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Depoda ürün yok!", "Uyarı", JOptionPane.WARNING_MESSAGE); return;
        }

        FileDialog fileDialog = new FileDialog(this, "Excel Raporunu Kaydet", FileDialog.SAVE);
        fileDialog.setFile("WMS_Pro_Stok_Raporu.csv");
        fileDialog.setVisible(true);

        String dir = fileDialog.getDirectory(), file = fileDialog.getFile();
        if (dir != null && file != null) {
            try {
                FileOutputStream fos = new FileOutputStream(new File(dir, file));
                fos.write(0xef); fos.write(0xbb); fos.write(0xbf);
                java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(fos, "UTF-8"));

                writer.println("Kategori;Ürün Adı;Seri Numarası;Stok;Birim Fiyat;Toplam Değer;Raf Dağılımı;Kayıt Tarihi");
                for (Urun u : urunManager.getUrunler()) {
                    double toplamUrunDegeri = u.getMiktar() * u.getBirimFiyat();
                    writer.printf("\"%s\";\"%s\";\"%s\";%d;\"%,.2f\";\"%,.2f\";\"%s\";\"%s\"\n",
                            u.getKategori(), u.getAd(), u.getSeriNo(), u.getMiktar(), u.getBirimFiyat(), toplamUrunDegeri, u.getRafKodlariString(), u.getEklenmeTarihi());
                }
                writer.close();
                JOptionPane.showMessageDialog(this, "Excel Raporu Başarıyla Oluşturuldu!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void pdfRaporAl() {
        if (urunManager.getUrunler().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Depoda ürün yok!", "Uyarı", JOptionPane.WARNING_MESSAGE); return;
        }

        FileDialog fileDialog = new FileDialog(this, "PDF Raporunu Kaydet", FileDialog.SAVE);
        fileDialog.setFile("WMS_Pro_Stok_Raporu.pdf");
        fileDialog.setVisible(true);

        String dir = fileDialog.getDirectory(), file = fileDialog.getFile();
        if (dir != null && file != null) {
            try {
                Document document = new Document();
                document.setPageSize(com.itextpdf.text.PageSize.A4.rotate());
                PdfWriter.getInstance(document, new FileOutputStream(new File(dir, file)));
                document.open();

                Paragraph baslik = new Paragraph("WMS Pro - Guncel Stok ve Finans Raporu\n\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20));
                baslik.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(baslik);

                PdfPTable table = new PdfPTable(8);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{2f, 3f, 2f, 1f, 1.5f, 2f, 3f, 2f});

                table.addCell("Kategori"); table.addCell("Urun Adi"); table.addCell("Seri Numarasi");
                table.addCell("Stok"); table.addCell("B. Fiyat"); table.addCell("Top. Deger"); table.addCell("Raf Dagilimi"); table.addCell("Kayit Tarihi");

                for (Urun u : urunManager.getUrunler()) {
                    double toplamUrunDegeri = u.getMiktar() * u.getBirimFiyat();
                    table.addCell(u.getKategori()); table.addCell(u.getAd()); table.addCell(u.getSeriNo());
                    table.addCell(String.valueOf(u.getMiktar()));
                    table.addCell(String.format("%,.2f TL", u.getBirimFiyat()));
                    table.addCell(String.format("%,.2f TL", toplamUrunDegeri));
                    table.addCell(u.getRafKodlariString()); table.addCell(u.getEklenmeTarihi());
                }
                document.add(table); document.close();
                JOptionPane.showMessageDialog(this, "Rapor Alındı!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // STATE MANAGEMENT: Her veri değişiminde panellerin güncellenmesini (Reactivity) sağlayan merkezi metodum.
    private void sistemiYenile() {
        String secimKat = cmbKategoriFiltre != null && cmbKategoriFiltre.getSelectedItem() != null ? cmbKategoriFiltre.getSelectedItem().toString() : "Tüm Kategoriler";

        this.rafManager = new RafManager();
        this.urunManager = new UrunManager(this.rafManager);

        if (cmbKategoriFiltre != null) {
            cmbKategoriFiltre.removeAllItems(); cmbKategoriFiltre.addItem("Tüm Kategoriler");
            for (String kat : urunDao.tumKategorileriGetir()) cmbKategoriFiltre.addItem(kat);
            cmbKategoriFiltre.setSelectedItem(secimKat);
        }

        if (dashboardPanel != null) {
            dashboardPanel.dashboardGuncelle();
        }

        tabloyuGuncelle();
        islemLoglariniFiltrele("");

        if (rafTabloModeli != null) {
            rafTabloModeli.setRowCount(0);
            for (Object[] r : rafDao.getRafDetaylari()) rafTabloModeli.addRow(r);
        }

        kritikStokKontroluYap();
    }

    private void kritikStokKontroluYap() {
        int guncelKritikSayisi = 0;
        for (Urun u : urunManager.getUrunler()) {
            if (u.getMiktar() <= u.getKritikEsik()) {
                guncelKritikSayisi++;
            }
        }

        if (guncelKritikSayisi > 0 && guncelKritikSayisi > oncekiKritikSayisi) {
            String mesaj = "🚨 Dikkat: " + guncelKritikSayisi + " adet ürün kritik stok seviyesinde!";

            // Ana işlem akışını (Main Thread) dondurmamak için Timer ve invokeLater ile bildirimleri asenkron yolluyorum.
            Timer gecikme = new Timer(500, evt -> {
                showToastNotification(mesaj, new Color(231, 76, 60));
            });
            gecikme.setRepeats(false);
            gecikme.start();
        }
        oncekiKritikSayisi = guncelKritikSayisi;
    }

    // Modern "Toast" tarzı, ekranda belirip kaybolan, kullanıcıyı rahatsız etmeyen bildirim mekanizması.
    private void showToastNotification(String mesaj, Color arkaPlanRengi) {
        SwingUtilities.invokeLater(() -> {
            try {
                JDialog toast = new JDialog(this);
                toast.setUndecorated(true);
                toast.setAlwaysOnTop(true);
                toast.setFocusableWindowState(false); // Kullanıcının o an yazdığı metni bölmemek için odaklanmayı engelliyoruz.

                JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground(arkaPlanRengi);
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 2),
                        new EmptyBorder(15, 20, 15, 20)
                ));

                JLabel lblMesaj = new JLabel("<html><center><font size='5' color='#FFFFFF'><b>" + mesaj + "</b></font></center></html>");
                panel.add(lblMesaj, BorderLayout.CENTER);

                toast.add(panel);
                toast.pack();

                int x = AnaEkran.this.getLocationOnScreen().x + AnaEkran.this.getWidth() - toast.getWidth() - 30;
                int y = AnaEkran.this.getLocationOnScreen().y + AnaEkran.this.getHeight() - toast.getHeight() - 40;
                toast.setLocation(x, y);

                toast.setVisible(true);

                Timer timer = new Timer(4000, e -> {
                    toast.setVisible(false);
                    toast.dispose();
                });
                timer.setRepeats(false);
                timer.start();

            } catch (Exception ex) {
                System.out.println("Bildirim gösterilirken hata oluştu: " + ex.getMessage());
            }
        });
    }

    // Modal ekranları titlebar olmadan tutup sürükleyebilmek için MouseAdapter ile oluşturduğum Listener
    private static class ComponentDragger extends MouseAdapter {
        private Window w; private Point p;
        public ComponentDragger(Window w) { this.w = w; }
        public void mousePressed(MouseEvent e) { p = e.getPoint(); }
        public void mouseDragged(MouseEvent e) { Point c = e.getLocationOnScreen(); w.setLocation(c.x-p.x, c.y-p.y); }
    }

    // Diğer panellerin sadece okuma yapabilmesi için değişkenleri private tutup, Getter metotlarıyla kontrollü dışarı açıyorum.
    public Color getMainBg() { return MAIN_BG; }
    public Color getSidebarBg() { return SIDEBAR_BG; }
    public Color getTableBg() { return TABLE_BG; }
    public Color getTableGridColor() { return TABLE_GRID_COLOR; }
    public Color getHeaderTextColor() { return HEADER_TEXT_COLOR; }
    public Color getTextColor() { return TEXT_COLOR; }
    public boolean isDarkMode() { return isDarkMode; }
    public String getComboStyle() { return COMBO_STYLE; }
    public String getInputStyle() { return INPUT_STYLE; }
    public DefaultTableModel getTabloModeli() { return tabloModeli; }
    public DefaultTableModel getIslemTabloModeli() { return islemTabloModeli; }
    public JComboBox<String> getCmbKategoriFiltre() { return cmbKategoriFiltre; }
    public JComboBox<String> getCmbIslemTipiFiltre() { return cmbIslemTipiFiltre; }
    public UrunManager getUrunManager() { return urunManager; }
    public RafManager getRafManager() { return rafManager; }
    public RafDao getRafDao() { return rafDao; }
}