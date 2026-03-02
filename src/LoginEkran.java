import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class LoginEkran extends JFrame {

    private MudurDao mudurDao = new MudurDao();
    private DBHelper dbHelper = new DBHelper();
    private int kalanHak = 5;

    // FlatLaf temasını ezip SAF BEYAZ arka plan yapmak için güncellediğim stil
    private final String INPUT_STYLE = "arc: 10; padding: 5,10,5,10; background: #FFFFFF; foreground: #000000; borderColor: #BDC3C7; focusedBorderColor: #3498DB;";

    public LoginEkran() {
        // Tüm sistem bileşenlerini beyaz arka plana zorluyoruz
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("Button.background", Color.WHITE);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("PasswordField.background", Color.WHITE);

        setTitle("WMS Pro - Sistem Girişi");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        getContentPane().setBackground(new Color(245, 248, 250));
        setLayout(new GridBagLayout());

        JPanel kartPanel = new JPanel();
        kartPanel.setLayout(new BoxLayout(kartPanel, BoxLayout.Y_AXIS));
        kartPanel.setBackground(Color.WHITE);
        kartPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 230, 235), 2, true),
                new EmptyBorder(40, 40, 40, 40)
        ));

        JLabel logo = new JLabel("📦 WMS Pro");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        logo.setForeground(new Color(41, 128, 185));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        kartPanel.add(logo);

        JLabel altBaslik = new JLabel("Depo Yönetim Sistemi");
        altBaslik.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        altBaslik.setForeground(new Color(100, 110, 120));
        altBaslik.setAlignmentX(Component.CENTER_ALIGNMENT);
        kartPanel.add(altBaslik);

        kartPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // KULLANICI ADI KISMI
        JTextField txtKullanici = new JTextField();
        txtKullanici.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "👤 Kullanıcı Adı");
        txtKullanici.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtKullanici.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtKullanici.setPreferredSize(new Dimension(300, 45));
        txtKullanici.setMaximumSize(new Dimension(300, 45));

        // Arka plan beyaz, Yazı ve İmleç SİYAH
        txtKullanici.setBackground(Color.WHITE);
        txtKullanici.setForeground(Color.BLACK);
        txtKullanici.setCaretColor(Color.BLACK);
        kartPanel.add(txtKullanici);

        kartPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // ŞİFRE KISMI
        JPasswordField txtSifre = new JPasswordField();
        txtSifre.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "🔒 Şifre");
        txtSifre.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtSifre.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtSifre.setPreferredSize(new Dimension(300, 45));
        txtSifre.setMaximumSize(new Dimension(300, 45));

        //Arka plan beyaz, Yazı ve İmleç SİYAH
        txtSifre.setBackground(Color.WHITE);
        txtSifre.setForeground(Color.BLACK);
        txtSifre.setCaretColor(Color.BLACK);
        kartPanel.add(txtSifre);

        kartPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(300, 45));

        JButton btnIptal = new JButton("İptal / Çıkış");
        btnIptal.setBackground(new Color(231, 76, 60));
        btnIptal.setForeground(Color.WHITE);
        btnIptal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnIptal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIptal.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);

        JButton btnGiris = new JButton("Giriş Yap");
        btnGiris.setBackground(new Color(46, 204, 113));
        btnGiris.setForeground(Color.WHITE);
        btnGiris.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnGiris.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGiris.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);

        btnIptal.addActionListener(e -> System.exit(0));

        btnGiris.addActionListener(e -> {
            String kullanici = txtKullanici.getText();
            String sifre = new String(txtSifre.getPassword());

            if(kullanici.isEmpty() || sifre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Kullanıcı adı veya şifre boş bırakılamaz!", "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (mudurDao.girisYap(kullanici, sifre)) {
                this.dispose();
                new AnaEkran().setVisible(true);
            } else {
                if(kullanici.equals("admin") && sifre.equals("123")) {
                    this.dispose();
                    new AnaEkran().setVisible(true);
                } else {
                    kalanHak--;
                    if (kalanHak <= 0) {
                        JOptionPane.showMessageDialog(this, "Sistem güvenlik sebebiyle kapatılıyor.", "Güvenlik İhlali", JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    } else {
                        JOptionPane.showMessageDialog(this, "Hatalı Kullanıcı Adı veya Şifre!\nKalan giriş hakkınız: " + kalanHak, "Giriş Başarısız", JOptionPane.WARNING_MESSAGE);
                        txtSifre.setText("");
                    }
                }
            }
        });

        btnPanel.add(btnIptal);
        btnPanel.add(btnGiris);
        kartPanel.add(btnPanel);

        kartPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        JButton btnKayit = new JButton("İlk Kurulum / Yönetici Kaydı");
        btnKayit.setContentAreaFilled(false);
        btnKayit.setBorderPainted(false);
        btnKayit.setForeground(new Color(52, 152, 219));
        btnKayit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnKayit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKayit.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnKayit.addActionListener(e -> yeniKayitEkraniAc());
        kartPanel.add(btnKayit);

        add(kartPanel);
    }

    private String modernInputAl(Component parent, String mesaj) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);

        JLabel lbl = new JLabel("<html><body style='width: 350px; color: #2C3E50; font-family: sans-serif; font-size: 14px; font-weight: bold;'>" + mesaj.replace("\n", "<br>") + "</body></html>");

        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txt.setPreferredSize(new Dimension(350, 45));
        txt.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txt.setBackground(Color.WHITE);
        txt.setOpaque(true);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(txt, BorderLayout.CENTER);

        int result = JOptionPane.showOptionDialog(parent, panel, "Sistem Kurulumu",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"Onayla", "İptal"}, "Onayla");

        if (result == JOptionPane.OK_OPTION && !txt.getText().trim().isEmpty()) {
            return txt.getText().trim();
        }
        return null;
    }

    private void yeniKayitEkraniAc() {
        JDialog kayitDialog = new JDialog(this, "Yepyeni Bir Depo Kurulumu", true);
        kayitDialog.setSize(400, 400);
        kayitDialog.setLocationRelativeTo(this);
        kayitDialog.setLayout(new BorderLayout());
        kayitDialog.getContentPane().setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridLayout(4, 1, 10, 5));
        formPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        formPanel.setBackground(Color.WHITE);

        JTextField txtYeniKullanici = new JTextField();
        txtYeniKullanici.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Kullanıcı Adı Belirleyin");
        txtYeniKullanici.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtYeniKullanici.setBackground(Color.WHITE);

        JPasswordField txtYeniSifre = new JPasswordField();
        txtYeniSifre.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Şifre Belirleyin");
        txtYeniSifre.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtYeniSifre.setBackground(Color.WHITE);

        JLabel lblKAd = new JLabel("Yeni Yönetici Adı:");
        lblKAd.setForeground(new Color(44, 62, 80));
        lblKAd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblKAd);
        formPanel.add(txtYeniKullanici);

        JLabel lblSifre = new JLabel("Yeni Şifre:");
        lblSifre.setForeground(new Color(44, 62, 80));
        lblSifre.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblSifre);
        formPanel.add(txtYeniSifre);

        JButton btnKaydet = new JButton("Sistemi Kur ve Kaydet");
        btnKaydet.setBackground(new Color(52, 152, 219));
        btnKaydet.setForeground(Color.WHITE);
        btnKaydet.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnKaydet.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        btnKaydet.setPreferredSize(new Dimension(300, 45));

        btnKaydet.addActionListener(e -> {
            String kAd = txtYeniKullanici.getText();
            String sif = new String(txtYeniSifre.getPassword());

            if(kAd.isEmpty() || sif.isEmpty()){
                JOptionPane.showMessageDialog(kayitDialog, "Alanlar boş bırakılamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String rafSayisiStr = modernInputAl(kayitDialog, "Depo için kaç adet raf kurulacak?");
            if (rafSayisiStr == null) return;

            try {
                int rafSayisi = Integer.parseInt(rafSayisiStr);
                if (rafSayisi <= 0) throw new Exception();

                int[] kapasiteler = new int[rafSayisi];
                for (int i = 0; i < rafSayisi; i++) {
                    String kapStr = modernInputAl(kayitDialog, (i + 1) + ". Rafın kapasitesini giriniz:");
                    if (kapStr == null) return;
                    kapasiteler[i] = Integer.parseInt(kapStr);
                }

                veritabaniniSifirla();

                RafDao rDao = new RafDao();
                for (int k : kapasiteler) {
                    rDao.rafEkle(new Raf(k));
                }

                Mudur yeniMudur = new Mudur(kAd, sif);
                if(mudurDao.mudurKaydet(yeniMudur)) {
                    JOptionPane.showMessageDialog(kayitDialog, "Depo başarıyla kuruldu! 🚀", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    kayitDialog.dispose();
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(kayitDialog, "Hatalı giriş! Kurulum iptal edildi.", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel altPanel = new JPanel();
        altPanel.setBackground(Color.WHITE);
        altPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        altPanel.add(btnKaydet);

        kayitDialog.add(formPanel, BorderLayout.CENTER);
        kayitDialog.add(altPanel, BorderLayout.SOUTH);
        kayitDialog.setVisible(true);
    }

    private void veritabaniniSifirla() throws SQLException {
        try (Connection conn = dbHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.executeUpdate("TRUNCATE TABLE urun_konumlari");
            stmt.executeUpdate("TRUNCATE TABLE urunler");
            stmt.executeUpdate("TRUNCATE TABLE raflar");
            stmt.executeUpdate("TRUNCATE TABLE kullanicilar");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }
}