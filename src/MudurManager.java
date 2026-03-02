import java.util.Scanner;

public class MudurManager {
    private MudurDao mudurDao;
    transient Scanner scanner = new Scanner(System.in);

    public MudurManager() {
        this.mudurDao = new MudurDao();
    }

    // YARDIMCI METOT
    private boolean hakTukendiMi(int hak) {
        if (hak <= 0) {
            System.out.println("\n❌ Çok fazla hatalı deneme yaptınız!");
            System.out.println("- Güvenlik sebebiyle işlem iptal edildi -");
            return true;
        }
        return false;
    }

    // 1. YENİ MÜDÜR KAYDI (Kayıt Ol)

    public void yeniMudurKaydi() {
        System.out.println("\n=== YENİ YÖNETİCİ KAYIT EKRANI ===");

        System.out.print("Belirlemek istediğiniz Kullanıcı Adı: ");
        String kAdi = scanner.nextLine();

        // Veritabanında bu isimde biri var mı kontrolü!
        if (mudurDao.kullaniciAdiDahaOnceAlinmisMi(kAdi)) {
            System.out.println("HATA! Bu kullanıcı adı zaten sistemde kayıtlı. Lütfen farklı bir isim seçin.");
            return;
        }

        System.out.print("Belirlemek istediğiniz Şifre: ");
        String sifre = scanner.nextLine(); // DÜZELTİLDİ: Artık nextInt() yok, String alıyoruz!

        Mudur yeniMudur = new Mudur(kAdi, sifre);

        if (mudurDao.mudurKaydet(yeniMudur)) {
            System.out.println("✅ Başarılı! Yeni yönetici sisteme kaydedildi.");
        } else {
            System.out.println("❌ Kayıt sırasında veritabanı hatası oluştu!");
        }
    }

    // 2. SİSTEME GİRİŞ YAP (Login)

    public boolean sistemeGirisYap() {
        int hak = 3; // 3 deneme hakkı

        System.out.println("\n=== WMS PRO - SİSTEM GİRİŞİ ===");

        // Eğer veritabanı bomboşsa, sistemi kitleme; direkt kayıt ekranına yönlendir
        if (!mudurDao.mudurVarMi()) {
            System.out.println("Sistemde kayıtlı hiçbir yönetici bulunamadı!");
            System.out.println("Lütfen önce bir yönetici hesabı oluşturun.");
            yeniMudurKaydi();
            return false;
        }

        while (hak > 0) {
            System.out.print("Kullanıcı Adı: ");
            String kAdi = scanner.nextLine();

            System.out.print("Şifre: ");
            String sifre = scanner.nextLine();

            // Dao üzerinden veritabanında sorgula
            if (mudurDao.girisYap(kAdi, sifre)) {
                System.out.println("\n✅ Giriş Başarılı! Sisteme hoş geldiniz, " + kAdi + ".");
                return true; // Giriş izni verildi
            } else {
                hak--;
                if (hakTukendiMi(hak)) return false;
                System.out.println("❌ Kullanıcı adı veya şifre hatalı! Kalan hakkınız: " + hak + "\n");
            }
        }
        return false; // Hak biterse false döner
    }
}