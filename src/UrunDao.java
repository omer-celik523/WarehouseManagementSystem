import javax.swing.JOptionPane;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UrunDao {
    private DBHelper dbHelper = new DBHelper();
    private RafDao rafDao = new RafDao(); // İlişkili verileri çekmek için RafDao'yu kompoze ettim.

    public List<String> tumKategorileriGetir() {
        List<String> kategoriler = new ArrayList<>();
        // Kategori isimlerinin combobox'a tekrarsız gelmesi için DISTINCT kullandım.
        String sql = "SELECT DISTINCT kategori FROM urunler WHERE kategori IS NOT NULL ORDER BY kategori ASC";
        try (Connection conn = dbHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                kategoriler.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kategoriler;
    }

    public List<Urun> tumUrunleriGetir() {
        List<Urun> urunler = new ArrayList<>();
        List<Raf> sistemdekiRaflar = rafDao.tumRaflariGetir();

        String sqlUrun = "SELECT * FROM urunler";
        String sqlKonum = "SELECT * FROM urun_konumlari WHERE urun_id = ?";

        try (Connection conn = dbHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rsUrun = stmt.executeQuery(sqlUrun)) {

            while (rsUrun.next()) {
                int urunId = rsUrun.getInt("id");

                Urun u = new Urun(
                        urunId,
                        rsUrun.getString("urun_adi"),
                        rsUrun.getString("seri_no"),
                        rsUrun.getInt("toplam_miktar"),
                        0,
                        rsUrun.getString("kategori"),
                        rsUrun.getInt("kritik_esik"),
                        rsUrun.getDouble("birim_fiyat")
                );

                String dbTarih = rsUrun.getString("eklenme_tarihi");
                u.setEklenmeTarihi(dbTarih != null ? dbTarih : "Bilinmiyor");
                u.getRafDagilimi().clear();

                // Ürünün raftaki dağılımlarını çekiyorum (N'e N ilişki çözümü)
                try (PreparedStatement pstmtKonum = conn.prepareStatement(sqlKonum)) {
                    pstmtKonum.setInt(1, urunId);
                    ResultSet rsKonum = pstmtKonum.executeQuery();
                    while (rsKonum.next()) {
                        int dbRafId = rsKonum.getInt("raf_id");
                        int miktar = rsKonum.getInt("miktar");

                        int gercekIndex = -1;
                        for (int i = 0; i < sistemdekiRaflar.size(); i++) {
                            if (sistemdekiRaflar.get(i).getId() == dbRafId) {
                                gercekIndex = i; break;
                            }
                        }
                        if (gercekIndex != -1) {
                            u.rafVeMiktarEkle(gercekIndex, miktar);
                        }
                    }
                }
                if(!u.getRafDagilimi().isEmpty()) {
                    u.setRafIndex(u.getRafDagilimi().keySet().iterator().next());
                }
                urunler.add(u);
            }
        } catch (SQLException e) {
            System.out.println("SQL Hatası (tumUrunleriGetir): " + e.getMessage());
        }
        return urunler;
    }

    // ACID (Atomicity, Consistency, Isolation, Durability) prensiplerini korumak için TRANSACTION yazdım!
    public void veritabaniniGuncelle(List<Urun> urunler) {
        List<Raf> sistemdekiRaflar = rafDao.tumRaflariGetir();

        String sqlDeleteKonum = "DELETE FROM urun_konumlari WHERE urun_id = ?";
        String sqlInsertUrun = "INSERT INTO urunler (urun_adi, seri_no, toplam_miktar, eklenme_tarihi, kategori, kritik_esik, birim_fiyat) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlUpdateUrun = "UPDATE urunler SET urun_adi = ?, seri_no = ?, toplam_miktar = ?, eklenme_tarihi = ?, kategori = ?, kritik_esik = ?, birim_fiyat = ? WHERE id = ?";
        String sqlInsertKonum = "INSERT INTO urun_konumlari (urun_id, raf_id, miktar) VALUES (?, ?, ?)";

        try (Connection conn = dbHelper.getConnection()) {

            // Eğer urun tablosu güncellenir ama konum tablosu güncellenirken hata çıkarsa veri tutarsız olur.
            // Bu yüzden otomatik kaydetmeyi kapatıyorum (Transaction Başlıyor)
            conn.setAutoCommit(false);

            try (PreparedStatement psInsertUrun = conn.prepareStatement(sqlInsertUrun, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psUpdateUrun = conn.prepareStatement(sqlUpdateUrun);
                 PreparedStatement psDeleteKonum = conn.prepareStatement(sqlDeleteKonum);
                 PreparedStatement psInsertKonum = conn.prepareStatement(sqlInsertKonum)) {

                List<Integer> islemGorenIdler = new ArrayList<>();

                for (Urun u : urunler) {
                    if (u.getId() > 0) { // Mevcut ürün güncellemesi
                        psUpdateUrun.setString(1, u.getAd());
                        psUpdateUrun.setString(2, u.getSeriNo());
                        psUpdateUrun.setInt(3, u.getMiktar());
                        psUpdateUrun.setString(4, u.getEklenmeTarihi());
                        psUpdateUrun.setString(5, u.getKategori());
                        psUpdateUrun.setInt(6, u.getKritikEsik());
                        psUpdateUrun.setDouble(7, u.getBirimFiyat());
                        psUpdateUrun.setInt(8, u.getId());
                        psUpdateUrun.executeUpdate();
                        islemGorenIdler.add(u.getId());

                        psDeleteKonum.setInt(1, u.getId());
                        psDeleteKonum.executeUpdate(); // Eski konumları sil
                    } else { // Yeni ürün eklemesi
                        psInsertUrun.setString(1, u.getAd());
                        psInsertUrun.setString(2, u.getSeriNo());
                        psInsertUrun.setInt(3, u.getMiktar());
                        psInsertUrun.setString(4, u.getEklenmeTarihi());
                        psInsertUrun.setString(5, u.getKategori());
                        psInsertUrun.setInt(6, u.getKritikEsik());
                        psInsertUrun.setDouble(7, u.getBirimFiyat());
                        psInsertUrun.executeUpdate();

                        try (ResultSet rs = psInsertUrun.getGeneratedKeys()) {
                            if (rs.next()) {
                                u.setId(rs.getInt(1));
                                islemGorenIdler.add(u.getId());
                            }
                        }
                    }

                    // Yeni konumları ekle
                    for (Map.Entry<Integer, Integer> entry : u.getRafDagilimi().entrySet()) {
                        int uiIndex = entry.getKey();
                        if (uiIndex >= 0 && uiIndex < sistemdekiRaflar.size()) {
                            int gercekDbRafId = sistemdekiRaflar.get(uiIndex).getId();
                            psInsertKonum.setInt(1, u.getId());
                            psInsertKonum.setInt(2, gercekDbRafId);
                            psInsertKonum.setInt(3, entry.getValue());
                            psInsertKonum.executeUpdate();
                        }
                    }
                }
                kalintilariTemizle(conn, islemGorenIdler);

                // Her şey başarılı olduysa veritabanına kalıcı olarak yaz. (Transaction Bitiyor)
                conn.commit();

            } catch (Exception ex) {
                // Herhangi bir hata durumunda tüm işlemleri geri al! (Rollback)
                conn.rollback();
                JOptionPane.showMessageDialog(null, "Veritabanı Güncelleme Hatası:\n" + ex.getMessage(), "Kritik SQL Hatası", JOptionPane.ERROR_MESSAGE);
            } finally {
                // İşimiz bitince DB ayarını normale çeviriyoruz
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Veritabanına bağlanılamadı:\n" + e.getMessage(), "Bağlantı Hatası", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kalintilariTemizle(Connection conn, List<Integer> islemGorenIdler) throws SQLException {
        String sqlGetDbIds = "SELECT id FROM urunler";
        String sqlDeleteUrunKonum = "DELETE FROM urun_konumlari WHERE urun_id = ?";
        String sqlDeleteUrun = "DELETE FROM urunler WHERE id = ?";

        List<Integer> dbUrunIds = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlGetDbIds)) {
            while (rs.next()) dbUrunIds.add(rs.getInt("id"));
        }

        try (PreparedStatement psDelKonum = conn.prepareStatement(sqlDeleteUrunKonum);
             PreparedStatement psDelUrun = conn.prepareStatement(sqlDeleteUrun)) {
            for (int dbId : dbUrunIds) {
                if (!islemGorenIdler.contains(dbId)) {
                    psDelKonum.setInt(1, dbId); psDelKonum.executeUpdate();
                    psDelUrun.setInt(1, dbId); psDelUrun.executeUpdate();
                }
            }
        }
    }
}