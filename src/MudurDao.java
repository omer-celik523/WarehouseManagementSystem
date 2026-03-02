import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MudurDao {
    private DBHelper dbHelper = new DBHelper();

    // Sistemde hiç yönetici var mı kontrolü. Sistemin ilk kez kurulup kurulmadığını anlamak için bu fonksiyonu yazdım.
    public boolean mudurVarMi() {
        String sql = "SELECT COUNT(*) FROM kullanicilar";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("SQL Hatası (mudurVarMi): " + e.getMessage());
        }
        return false;
    }

    // Aynı kullanıcı adıyla başka biri kayıtlı mı? Unique constraint kontrolünü Java tarafında yapıyorum.
    public boolean kullaniciAdiDahaOnceAlinmisMi(String kullaniciAdi) {
        String sql = "SELECT COUNT(*) FROM kullanicilar WHERE kullanici_adi = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, kullaniciAdi);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Sonuç 0'dan büyükse isim dolu demek.
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Hatası: " + e.getMessage());
        }
        return false;
    }

    public boolean mudurKaydet(Mudur mudur) {
        String sql = "INSERT INTO kullanicilar (kullanici_adi, sifre) VALUES (?, ?)";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, mudur.getKullaniciAdi());
            pstmt.setString(2, mudur.getSifre());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("SQL Hatası (mudurKaydet): " + e.getMessage());
            return false;
        }
    }

    // Kullanıcı girişini kontrol eden metot. PreparedStatement sayesinde giriş ekranından yapılacak SQL Injection saldırıları tamamen engellendi.
    public boolean girisYap(String kullaniciAdi, String sifre) {
        String sql = "SELECT * FROM kullanicilar WHERE kullanici_adi = ? AND sifre = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, kullaniciAdi);
            pstmt.setString(2, sifre);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Eşleşme varsa true, yoksa false döner.
            }

        } catch (SQLException e) {
            System.out.println("SQL Hatası (girisYap): " + e.getMessage());
            return false;
        }
    }
}