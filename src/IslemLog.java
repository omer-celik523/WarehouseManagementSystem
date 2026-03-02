import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Uygulamada yapılan her işlemi (Ekleme, Silme, Taşıma) kayıt altına almak için oluşturduğum DTO (Data Transfer Object) tarzı sınıfım.
public class IslemLog {
    private String tarih;
    private String islemTipi;
    private String urunAdi;
    private String seriNo;
    private String miktarDegisimi;
    private String aciklama;

    // Sistem yeni bir log kaydederken bunu çağırıyor. Tarihi dışarıdan almak yerine o anki sistem saatinden çekiyorum.
    public IslemLog(String islemTipi, String urunAdi, String seriNo, String miktarDegisimi, String aciklama) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
        this.tarih = dtf.format(LocalDateTime.now());

        this.islemTipi = islemTipi;
        this.urunAdi = urunAdi;
        this.seriNo = seriNo;
        this.miktarDegisimi = miktarDegisimi;
        this.aciklama = aciklama;
    }

    // Veritabanından geçmiş logları okurken zaten bir tarihi olduğu için bu kurucuyu kullanıyorum (Overloading).
    public IslemLog(String tarih, String islemTipi, String urunAdi, String seriNo, String miktarDegisimi, String aciklama) {
        this.tarih = tarih;
        this.islemTipi = islemTipi;
        this.urunAdi = urunAdi;
        this.seriNo = seriNo;
        this.miktarDegisimi = miktarDegisimi;
        this.aciklama = aciklama;
    }

    // Loglar sonradan değiştirilemez (Immutable olmalı) sadece okunabilir, o yüzden setter metodlarını sildim. Sadece getter var.
    public String getTarih() { return tarih; }
    public String getIslemTipi() { return islemTipi; }
    public String getUrunAdi() { return urunAdi; }
    public String getSeriNo() { return seriNo; }
    public String getMiktarDegisimi() { return miktarDegisimi; }
    public String getAciklama() { return aciklama; }
}