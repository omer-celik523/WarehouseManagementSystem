import java.io.Serializable;

// İleride objeleri dosyalara kaydetme ihtiyacı olursa diye Serializable arayüzünü implement ettim.
public class Mudur implements Serializable {

    // Class sürümleri değiştiğinde uyumsuzluk çıkmasın diye best-practice olarak serialVersionUID tanımladım.
    private static final long serialVersionUID = 1L;

    private int id;
    private String kullaniciAdi;
    private String sifre;

    // Veritabanından veri çekerken kullandığım constructor
    public Mudur(int id, String kullaniciAdi, String sifre) {
        this.id = id;
        setKullaniciAdi(kullaniciAdi); // Doğrudan atama (this.kullaniciAdi = x) yerine setter kullandım ki filtrelerden geçsin.
        setSifre(sifre);
    }

    // Yeni kayıt yaparken kullandığım constructor (ID DB tarafından Auto-Increment verilecek)
    public Mudur(String kullaniciAdi, String sifre) {
        setKullaniciAdi(kullaniciAdi);
        setSifre(sifre);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getKullaniciAdi() { return kullaniciAdi; }

    // Boş kullanıcı adı girilmesini engellemek için setter içine ufak bir guard-clause (koruma bloğu) yazdım.
    public void setKullaniciAdi(String kullaniciAdi) {
        if (kullaniciAdi == null || kullaniciAdi.trim().isEmpty()) {
            this.kullaniciAdi = "GecersizKullanici";
        } else {
            this.kullaniciAdi = kullaniciAdi.trim();
        }
    }

    public String getSifre() { return sifre; }

    // Boş şifre girilirse default bir değer atayarak sistem hatasını engelliyorum.
    public void setSifre(String sifre) {
        if (sifre == null || sifre.trim().isEmpty()) {
            this.sifre = "123456";
        } else {
            this.sifre = sifre.trim(); // Sondaki ve baştaki boşlukları temizliyorum
        }
    }
}