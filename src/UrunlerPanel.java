import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Kullanıcının depo operasyonlarını yönettiği arayüz paneli.
// Bu sınıf sadece butonları ve UI tasarımını barındırır. İş mantığı (Business Logic) AnaEkran ve Manager sınıflarındadır.
// Bu sayede "Separation of Concerns" (Sorumlulukların Ayrılığı) prensibini korumuş oluyoruz.
public class UrunlerPanel extends JPanel {

    // Bağımlılık Enjeksiyonu (Dependency Injection): AnaEkran referansını dışarıdan alıyoruz.
    private AnaEkran anaEkran;

    public UrunlerPanel(AnaEkran anaEkran, Color mainBg, Color headerTextColor) {
        this.anaEkran = anaEkran;

        setBackground(mainBg);
        setBorder(new EmptyBorder(30, 40, 30, 40));
        setLayout(new BorderLayout());

        JLabel baslik = new JLabel("Ürün Operasyon Merkezi");
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 28));
        baslik.setForeground(headerTextColor);
        add(baslik, BorderLayout.NORTH);

        // Ekran boyutuna göre butonların otomatik dizilmesi için esnek GridLayout kullandım.
        JPanel islemGrid = new JPanel(new GridLayout(2, 2, 30, 30));
        islemGrid.setOpaque(false);
        islemGrid.setBorder(new EmptyBorder(50, 50, 50, 50));

        JButton btnYeniKayit = createBigActionButton("➕ Yeni Ürün Kaydı Ekle", "Depoya yepyeni bir ürün tanımlayın", new Color(46, 204, 113));
        JButton btnStokEkle = createBigActionButton("📦 Mevcut Stoğa Ekle", "Sistemde var olan ürünün adetini artırın", new Color(52, 152, 219));
        JButton btnUrunCikar = createBigActionButton("➖ Ürün Çıkar / Sil", "Depodan ürün çıkışı yapın", new Color(231, 76, 60));
        JButton btnUrunTasi = createBigActionButton("🔄 Ürün Taşı", "Ürünleri raflar arasında transfer edin", new Color(241, 196, 15));

        // Event-Driven Programming (Olay Yönelimli Programlama): Butonlara tıklandığında Lambda ifadeleri (e ->)
        // ile AnaEkran'daki ilgili iş mantığını tetikliyorum. Spagetti kod yazmaktan kaçındım.
        btnYeniKayit.addActionListener(e -> anaEkran.yeniKayitTetikle());
        btnStokEkle.addActionListener(e -> anaEkran.mevcutStogaEkleFormuAc());
        btnUrunCikar.addActionListener(e -> anaEkran.urunCikarFormuAc());
        btnUrunTasi.addActionListener(e -> anaEkran.urunTasiFormuAc());

        islemGrid.add(btnYeniKayit);
        islemGrid.add(btnStokEkle);
        islemGrid.add(btnUrunCikar);
        islemGrid.add(btnUrunTasi);

        add(islemGrid, BorderLayout.CENTER);
    }

    // DRY (Don't Repeat Yourself) Prensibi: Aynı buton tasarımını 4 kere yazmak yerine
    // bunu bir yardımcı (helper) metoda bağlayıp parametrik hale getirdim.
    private JButton createBigActionButton(String t, String s, Color c) {
        JButton b = new JButton("<html><center><font size='5' color='#FFFFFF'>" + t + "</font><br><font size='3' color='#F0F0F0'>" + s + "</font></center></html>");
        b.setBackground(c);
        b.setFocusPainted(false);
        b.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        return b;
    }
}