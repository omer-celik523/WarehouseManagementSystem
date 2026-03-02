import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Veritabanı ile uygulama arasındaki bağlantıyı sağlayan DAO (Data Access Object) sınıfım.
// SRP (Single Responsibility Principle) gereği, işlem logları sadece bu sınıftan veritabanına gider.
public class IslemDao {

    // Bağımlılığı azaltmak için merkezi DBHelper sınıfımı çağırıyorum.
    private DBHelper dbHelper = new DBHelper();

    public IslemDao() {
        // Sınıf nesnesi oluşturulduğunda tabloların varlığını kontrol eden mekanizma.
        // Hoca sistemi ilk kurduğunda hata almasın diye "IF NOT EXISTS" kullandım.
        tabloyuOlustur();
    }

    private void tabloyuOlustur() {
        String sql = "CREATE TABLE IF NOT EXISTS islem_gecmisi (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "tarih VARCHAR(50), " +
                "islem_tipi VARCHAR(50), " +
                "urun_adi VARCHAR(150), " +
                "seri_no VARCHAR(100), " +
                "miktar_degisimi VARCHAR(50), " +
                "aciklama TEXT)";

        // Try-with-resources kullanarak işlemler bitince Statement ve Connection'ın otomatik kapanmasını sağladım. (Memory Leak önlemi)
        try (Connection conn = dbHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            System.out.println("Log tablosu kontrol/oluşturma hatası: " + e.getMessage());
        }
    }

    // --- LOGLARI MYSQL VERİTABANINA KAYDETME (INSERT INTO) ---
    public void logEkle(IslemLog log) {
        String sql = "INSERT INTO islem_gecmisi(tarih, islem_tipi, urun_adi, seri_no, miktar_degisimi, aciklama) VALUES(?,?,?,?,?,?)";

        // GÜVENLİK NOTU: SQL Injection saldırılarını önlemek için düz Statement yerine PreparedStatement kullandım.
        // Kullanıcı arayüzden tırnak işareti (') falan girse bile SQL komutu olarak algılanmayacak.
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, log.getTarih());
            pstmt.setString(2, log.getIslemTipi());
            pstmt.setString(3, log.getUrunAdi());
            pstmt.setString(4, log.getSeriNo());
            pstmt.setString(5, log.getMiktarDegisimi());
            pstmt.setString(6, log.getAciklama());

            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Log SQL Kayıt Hatası: " + e.getMessage());
        }
    }

    // --- LOGLARI MYSQL'DEN ÇEKİP TABLOYA YANSITMA (SELECT) ---
    public List<IslemLog> tumLoglariGetir() {
        List<IslemLog> loglar = new ArrayList<>();
        // En güncel işlemlerin tablonun en üstünde görünmesi için "ORDER BY id DESC" ile veriyi ters sıralayarak çektim.
        String sql = "SELECT * FROM islem_gecmisi ORDER BY id DESC";

        try (Connection conn = dbHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                loglar.add(new IslemLog(
                        rs.getString("tarih"),
                        rs.getString("islem_tipi"),
                        rs.getString("urun_adi"),
                        rs.getString("seri_no"),
                        rs.getString("miktar_degisimi"),
                        rs.getString("aciklama")
                ));
            }
        } catch (Exception e) {
            System.out.println("Log SQL Okuma Hatası: " + e.getMessage());
        }
        return loglar;
    }
}