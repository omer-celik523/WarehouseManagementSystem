import java.util.LinkedHashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Depodaki her bir ürünü temsil eden temel Model sınıfım.
// Object Oriented Programming'in (OOP) Kapsülleme (Encapsulation) prensibine uyarak değişkenleri private tuttum.
public class Urun {
    private int id;
    private String ad;
    private String seriNo;
    private int miktar;
    private int rafIndex;

    // Bir ürün birden fazla rafta parçalı bulunabileceği için bunu Map yapısında tutmayı tercih ettim.
    // LinkedHashMap kullandım çünkü rafların eklenme sırasının korunmasını istiyorum.
    private Map<Integer, Integer> rafDagilimi;

    private String eklenmeTarihi;
    private String kategori;
    private int kritikEsik;
    private double birimFiyat;

    // 1. Constructor: Yeni ürün kaydı yaparken UI'dan verileri almak için (ID yok çünkü DB kendi atayacak)
    public Urun(String ad, String seriNo, int miktar, int rafIndex, String kategori, int kritikEsik, double birimFiyat) {
        this.ad = ad;
        this.seriNo = seriNo;
        this.miktar = miktar;
        this.rafIndex = rafIndex;
        this.kategori = kategori;
        this.kritikEsik = kritikEsik;
        this.birimFiyat = birimFiyat;

        this.rafDagilimi = new LinkedHashMap<>();
        if (rafIndex >= 0) {
            this.rafDagilimi.put(rafIndex, miktar);
        }

        // Ürün sisteme girildiği anın tarihini otomatik olarak damgalıyorum.
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
        this.eklenmeTarihi = dtf.format(LocalDateTime.now());
    }

    // 2. Constructor: Method Overloading (Aşırı Yükleme) yaptım.
    // Bu yapıcı metodu sadece veritabanından veri çekerken (SELECT işlemi) kullanacağım.
    public Urun(int id, String ad, String seriNo, int miktar, int rafIndex, String kategori, int kritikEsik, double birimFiyat) {
        this.id = id;
        this.ad = ad;
        this.seriNo = seriNo;
        this.miktar = miktar;
        this.rafIndex = rafIndex;
        this.kategori = kategori;
        this.kritikEsik = kritikEsik;
        this.birimFiyat = birimFiyat;
        this.rafDagilimi = new LinkedHashMap<>();
    }

    // --- GETTER & SETTER METOTLARI ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getAd() { return ad; }
    public void setAd(String ad) {this.ad = ad;}
    public String getSeriNo() { return seriNo; }
    public void setSeriNo(String seriNo) {this.seriNo = seriNo;}
    public int getMiktar() { return miktar; }
    public void setMiktar(int miktar) { this.miktar = miktar; }
    public int getRafIndex() { return rafIndex; }
    public void setRafIndex(int rafIndex) { this.rafIndex = rafIndex; }
    public Map<Integer, Integer> getRafDagilimi() { return rafDagilimi; }
    public String getEklenmeTarihi() { return eklenmeTarihi; }
    public void setEklenmeTarihi(String eklenmeTarihi) { this.eklenmeTarihi = eklenmeTarihi; }

    // Eğer kategori null gelirse program patlamasın diye ternary if ile varsayılan değer atadım.
    public String getKategori() { return kategori != null ? kategori : "Genel"; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public int getKritikEsik() { return kritikEsik; }
    public void setKritikEsik(int kritikEsik) { this.kritikEsik = kritikEsik; }
    public double getBirimFiyat() { return birimFiyat; }
    public void setBirimFiyat(double birimFiyat) { this.birimFiyat = birimFiyat; }

    // --- ÖZEL İŞ MANTIKLARI (Business Logic) ---

    // Ürünü yeni bir rafa yerleştirme matematiği
    public void rafVeMiktarEkle(int rafId, int miktar) {
        this.rafDagilimi.put(rafId, this.rafDagilimi.getOrDefault(rafId, 0) + miktar);
    }

    // Ürün çıkışı yaparken raftaki miktarı kontrol ederek silme/azaltma işlemi
    public void raftanUrunEksilt(int rafId, int miktar) {
        if (this.rafDagilimi.containsKey(rafId)) {
            int mevcut = this.rafDagilimi.get(rafId);
            if (mevcut <= miktar) {
                this.rafDagilimi.remove(rafId);
            } else {
                this.rafDagilimi.put(rafId, mevcut - miktar);
            }
        }
    }

    // İki raf arası taşıma işlemini tek satırda çözmek için yardımcı metot yazdım
    public void rafTasimaGuncellemesi(int eskiRaf, int yeniRaf, int miktar) {
        raftanUrunEksilt(eskiRaf, miktar);
        rafVeMiktarEkle(yeniRaf, miktar);
    }

    // Eğer ortadan bir raf silinirse (Örn: 2. raf silindi), ondan sonraki rafların indekslerini (3'ü 2'ye) kaydırıyorum.
    // Map üzerinde ConcurrentModificationException almamak için yeni bir Map nesnesi oluşturarak referansı değiştirdim.
    public void rafIndexleriniKaydir(int silinenRafIndex) {
        Map<Integer, Integer> yeniDagilim = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : rafDagilimi.entrySet()) {
            int mevcutRaf = entry.getKey();
            int raftakiMiktar = entry.getValue();

            if (mevcutRaf > silinenRafIndex) {
                yeniDagilim.put(mevcutRaf - 1, raftakiMiktar);
            } else if (mevcutRaf < silinenRafIndex) {
                yeniDagilim.put(mevcutRaf, raftakiMiktar);
            }
        }
        this.rafDagilimi = yeniDagilim;

        if (this.rafIndex > silinenRafIndex) {
            this.rafIndex--;
        }
    }

    // Tabloda göstermek için map yapısını kullanıcı dostu bir string'e çeviriyorum.
    public String getRafKodlariString() {
        if (rafDagilimi.isEmpty()) return "Raf Tanımsız";
        StringBuilder sb = new StringBuilder(); // String birleştirme işlemleri için hafıza dostu StringBuilder kullandım.
        for (Map.Entry<Integer, Integer> entry : rafDagilimi.entrySet()) {
            sb.append((entry.getKey() + 1)).append(". Raf (").append(entry.getValue()).append(" ad.), ");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 2); // Sondaki virgülü kesiyorum
        return sb.toString();
    }
}