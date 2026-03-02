import java.util.ArrayList;
import java.util.List;

// İş mantığı (Business Logic) katmanımız (Service Layer).
// Arayüz doğrudan veritabanı ile konuşmaz, Manager sınıflarından hizmet alır. (Gevşek Bağımlılık - Loose Coupling)
public class RafManager {
    private List<Raf> raflar;
    private RafDao rafDao;

    public RafManager() {
        this.rafDao = new RafDao();
        // Uygulama RAM'den değil, kalıcılığı olan DB'den başlatılıyor
        this.raflar = rafDao.tumRaflariGetir();

        if (this.raflar == null) {
            this.raflar = new ArrayList<>();
        }
    }

    public int getRafSayisi() { return raflar.size(); }

    public int getRafKapasitesi(int index) {
        if (index >= 0 && index < raflar.size()) {
            return raflar.get(index).getKapasite();
        }
        return 0;
    }

    public void kapasiteGuncelle(int index, int degisim) {
        if (index >= 0 && index < raflar.size()) {
            Raf raf = raflar.get(index);
            raf.setKapasite(raf.getKapasite() + degisim);
            rafDao.rafGuncelle(raf); // Ram'deki veri değiştiğinde DB'yi de eşzamanlı güncelliyoruz.
        }
    }

    public int getToplamKapasite() {
        int toplam = 0;
        for (Raf r : raflar) { toplam += r.getKapasite(); }
        return toplam;
    }

    public boolean rafSil(int index) {
        if (index >= 0 && index < raflar.size()) {
            Raf silinecek = raflar.get(index);
            rafDao.rafSil(silinecek.getId());
            raflar.remove(index);
            return true;
        }
        return false;
    }

    public List<Raf> getRaflar() { return raflar; }
}