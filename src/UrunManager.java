import java.util.ArrayList;
import java.util.List;

// Ürün iş mantıklarını yürüten servis katmanımız.
public class UrunManager {
    private List<Urun> urunler;
    private RafManager rafManager;
    private UrunDao urunDao;

    // Dependency Injection (Bağımlılık Enjeksiyonu) prensibine uyarak, RafManager nesnesini dışarıdan alıyoruz.
    public UrunManager(RafManager rafManager) {
        this.rafManager = rafManager;
        this.urunDao = new UrunDao();
        this.urunler = urunDao.tumUrunleriGetir();
        if (this.urunler == null) this.urunler = new ArrayList<>();
    }

    public List<Urun> getUrunler() { return urunler; }

    // Listelerde lineer arama (Linear Search) algoritması kullanarak O(n) karmaşıklığında ürün bulma işlemi
    public Urun urunBul(String seriNo) {
        for (Urun u : urunler) {
            if (u.getSeriNo().equals(seriNo)) return u;
        }
        return null;
    }

    public boolean rafDoluMu(int rafIndex) {
        for (Urun u : urunler) {
            if (u.getRafDagilimi().containsKey(rafIndex)) return true;
        }
        return false;
    }

    // Raf silindiğinde kalan ürünlerin indekslerini kaydırıp veritabanını senkronize eder.
    public void rafSilindiktenSonraGuncelle(int silinenRafIndex) {
        for (Urun u : urunler) {
            u.rafIndexleriniKaydir(silinenRafIndex);
        }
        urunDao.veritabaniniGuncelle(urunler);
    }
}