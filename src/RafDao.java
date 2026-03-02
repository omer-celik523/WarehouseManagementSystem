import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RafDao {
    private DBHelper dbHelper = new DBHelper();

    public int getToplamKapasite() {
        int toplam = 0;
        // Tüm rafların kapasitelerini SQL tarafında toplayarak Java tarafında döngüyle uğraşmaktan ve RAM harcamaktan kurtuldum. (Optimizasyon)
        String sql = "SELECT SUM(kapasite) FROM raflar";
        try (Connection conn = dbHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                toplam = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Raf Kapasite Hesaplama Hatası: " + e.getMessage());
        }
        return toplam;
    }

    // Arayüzdeki grafikler için raf detaylarını çeken karmaşık bir SQL yazdım.
    // LEFT JOIN kullanarak içi boş olan rafların da kaybolmadan listelenmesini sağladım.
    public List<Object[]> getRafDetaylari() {
        List<Object[]> rafListesi = new ArrayList<>();

        String sql = "SELECT r.id, r.kapasite AS bos_yer, COALESCE(SUM(uk.miktar), 0) AS dolu_miktar " +
                "FROM raflar r " +
                "LEFT JOIN urun_konumlari uk ON r.id = uk.raf_id " +
                "GROUP BY r.id, r.kapasite";

        try (Connection conn = dbHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int siraNo = 1;
            while (rs.next()) {
                int bosKapasite = rs.getInt("bos_yer");
                int doluMiktar = rs.getInt("dolu_miktar");

                // Veritabanında "kapasite" sütununda sadece boş yer tutulduğu için, toplam kapasiteyi anlık hesaplıyorum.
                int toplamKapasite = bosKapasite + doluMiktar;
                int dolulukOrani = (toplamKapasite > 0) ? (doluMiktar * 100 / toplamKapasite) : 0;

                // --- ÖNEMLİ UX DÜZELTMESİ ---
                // Eğer rafta en az 1 ürün varsa ama kapasite çok büyük olduğu için
                // oran %0.5 falan çıkıp Java bunu %0'a yuvarlıyorsa, zorla %1 yapıyoruz!
                if (doluMiktar > 0 && dolulukOrani == 0) {
                    dolulukOrani = 1;
                }

                rafListesi.add(new Object[]{
                        siraNo++ + ". Raf",
                        toplamKapasite + " Birim",
                        bosKapasite + " Birim",
                        "%" + dolulukOrani
                });
            }
        } catch (SQLException e) {
            System.out.println("Raf Detay Listeleme Hatası: " + e.getMessage());
        }
        return rafListesi;
    }

    public List<Raf> tumRaflariGetir() {
        List<Raf> raflar = new ArrayList<>();
        String sql = "SELECT * FROM raflar";

        try (Connection conn = dbHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                raflar.add(new Raf(rs.getInt("id"), rs.getInt("kapasite")));
            }
        } catch (SQLException e) {
            System.out.println("Raf Listeleme Hatası: " + e.getMessage());
        }
        return raflar;
    }

    public void rafEkle(Raf raf) {
        String sql = "INSERT INTO raflar (kapasite) VALUES (?)";
        // RETURN_GENERATED_KEYS kullanarak DB'nin otomatik verdiği ID'yi geri çekip nesneme aktarıyorum.
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, raf.getKapasite());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    raf.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Raf Ekleme Hatası: " + e.getMessage());
        }
    }

    public void rafSil(int id) {
        String sql = "DELETE FROM raflar WHERE id = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Raf Silme Hatası: " + e.getMessage());
        }
    }

    // Ram'deki raf kapasitesi değiştiğinde veritabanını (Disk) eşzamanlı güncelleyen metot.
    public void rafGuncelle(Raf raf) {
        String sql = "UPDATE raflar SET kapasite = ? WHERE id = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, raf.getKapasite());
            pstmt.setInt(2, raf.getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Raf Güncelleme Hatası: " + e.getMessage());
        }
    }
}