import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBHelper {
    // Projenin her yerinde url ve şifre yazıp kod tekrarı (Code Duplication) yapmamak için
    // veritabanı bağlantı ayarlarını bu sınıfta merkezi hale getirdim.

    private String userName = "root";
    private String password = "qazwsxedc"; // Kendi MySQL şifremi buraya tanımladım.
    private String dbUrl = "jdbc:mysql://localhost:3306/depo_yonetim";

    // DAO sınıflarımın veritabanına bağlanmak için çağıracağı metodum.
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, userName, password);
    }
}