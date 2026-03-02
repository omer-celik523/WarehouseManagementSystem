import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        // Arayüzün eski Java gibi çirkin görünmemesi için FlatLaf temasını entegre ettim.
        // Hata verirse program çökmesin diye try-catch bloğuna aldım.
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Tema yüklenemedi: " + ex.getMessage());
        }

        // Java Swing arayüzleri "Thread-Safe" (iş parçacığı güvenli) değildir.
        // Bu yüzden UI bileşenlerini Event Dispatch Thread (EDT) üzerinde çalıştırıyorum.
        // Hocanın mülakatta sorabileceği kritik bir asenkron programlama detayı.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Program direkt ana ekrandan değil, güvenlik için Login ekranından başlıyor.
                LoginEkran login = new LoginEkran();
                login.setVisible(true);
            }
        });
    }
}