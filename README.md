<img width="1728" height="1117" alt="Ekran Resmi 2026-03-02 22 50 30" src="https://github.com/user-attachments/assets/f80c210b-98af-4a6f-9491-76d7fbfc8cea" />#📦 WMS Pro - Gelişmiş Depo ve Stok Yönetim Sistemi

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVC%20%7C%20DAO-success?style=for-the-badge)
![UI](https://img.shields.io/badge/UI-FlatLaf-blue?style=for-the-badge)

> Nesne Yönelimli Programlama (OOP) prensipleri ve katmanlı mimari (Layered Architecture) kullanılarak Java ile sıfırdan geliştirilmiş; yüksek performanslı, veri bütünlüğü garantili (ACID) ve modern arayüzlü Depo Yönetim Sistemi.

<br>

<div align="cen
    <img width="1728" height="1117" alt="Ekran Resmi 2026-03-02 22 37 43" src="https://github.com/user-attachments/assets/815f566f-dc43-476e-840c-22ad42bab9af" />

  
</div>

---

## 📑 İçindekiler
1. [Projenin Amacı ve Çözdüğü Sorunlar](#-projenin-amacı-ve-çözdüğü-sorunlar)
2. [Sistem Mimarisi ve OOP Prensipleri](#-sistem-mimarisi-ve-oop-prensipleri)
3. [Veritabanı Tasarımı (Database Schema)](#-veritabanı-tasarımı-database-schema)
4. [Gelişmiş Algoritmalar ve Güvenlik](#-gelişmiş-algoritmalar-ve-güvenlik)
5. [Kullanıcı Deneyimi (UI/UX)](#-kullanıcı-deneyimi-uiux)
6. [Kullanılan Teknolojiler](#-kullanılan-teknolojiler)
7. [Ekran Görüntüleri](#-ekran-görüntüleri)
8. [Kurulum ve Çalıştırma](#-kurulum-ve-çalıştırma)

---

## 🎯 Projenin Amacı ve Çözdüğü Sorunlar
Klasik depo yönetim süreçlerinde yaşanan manuel takip hataları, kapasite aşımları ve veri kayıplarını engellemek amacıyla tasarlanmıştır. Bu proje:
* N'e N (Many-to-Many) ilişkili ürün ve raf karmaşasını algoritmik olarak çözer.
* Parçalı ürün yerleşimine (Örn: 100 kapasiteli bir ürünün, boşluk durumuna göre 3 farklı rafa otomatik dağıtılması) olanak tanır.
* Yapılan her işlemi (Ekleme, Silme, Güncelleme) kimin, ne zaman yaptığını `islem_loglari` tablosuna kaydederek tam izlenebilirlik sağlar.

---

## 🏗️ Sistem Mimarisi ve OOP Prensipleri

Projede "Spaghetti Code" yazımından kaçınılmış, sürdürülebilirlik (Maintainability) ve genişletilebilirlik (Scalability) için katmanlı mimari tercih edilmiştir:

* **Model Katmanı (Encapsulation):** Veritabanındaki her tablo, Java'da bir nesneye (Entity) dönüştürülmüştür (`Urun.java`, `Raf.java`). Veri güvenliği için değişkenler `private` tutulmuş ve Getter/Setter metotları ile kapsüllenmiştir.
* **DAO (Data Access Object) Katmanı:** Arayüz sınıflarının doğrudan SQL sorgusu yazması engellenmiştir. Veritabanı ile iletişim tamamen `UrunDao`, `RafDao` gibi sınıflar üzerinden izole bir şekilde (Abstraction) sağlanmıştır.
* **Manager (İş Mantığı) Katmanı:** Veritabanına veri gitmeden önce kapasite kontrollerinin, matematiksel yuvarlamaların ve validasyonların (Doğrulama) yapıldığı, Controller görevi gören katmandır.

---

## 🗄️ Veritabanı Tasarımı (Database Schema)
Sistem **MySQL** üzerinde 3. Normal Form (3NF) kurallarına uygun olarak tasarlanmıştır:
1. `urunler`: Ürün barkodu, adı, kategorisi ve toplam miktarı.
2. `raflar`: Raf kodu, maksimum kapasitesi ve mevcut doluluk oranı.
3. `islem_loglari`: Sistemdeki hareketlerin anlık timestamp (zaman damgası) ile tutulduğu denetim (audit) tablosu.

*(Veri bütünlüğünü korumak için tablolar arası `FOREIGN KEY` ve `CASCADE` yapısı kurgulanmıştır.)*

---

## 🛡️ Gelişmiş Algoritmalar ve Güvenlik

### 1. Kısmi İşlem Onayı (Partial Execution)
Kullanıcı çoklu raf veya ürün silme işlemi başlattığında, sistem önce işlemi simüle eder. Eğer seçilen 5 raftan 1'i doluysa ve silinmemesi gerekiyorsa, sistem işlemi tamamen çökertmek yerine kullanıcıya *"Dolu raflar atlandı, sadece boş olan 4 rafı silmek ister misiniz?"* şeklinde esnek bir çözüm sunar.

### 2. Gizli Hata Sayacı (Shadow Error Counter)
Kullanıcıların formu kasten bozmaya çalışmasına (Brute-force, harf yerine sayı girme, null bırakma) karşı arka planda sessiz bir sayaç çalışır. 8 hatalı denemede form kendini güvenlik amacıyla kilitler ve işlemi iptal eder.

### 3. Asenkron İşlemler ve Transaction (ACID) Yönetimi
* **ACID Uyumluluğu:** Bir ürün birden fazla rafa dağıtılırken elektrik kesintisi veya yazılım hatası olursa, veritabanının bozulmaması için `Connection.setAutoCommit(false)` ve `Rollback` mekanizmaları kullanılmıştır.
* **Non-blocking UI:** Kritik stok uyarıları ve animasyonlar, ana ekranı kilitlememesi için Java'nın `SwingWorker` ve `Timer` sınıfları ile asenkron (iş parçacığı güvenli) olarak çalıştırılır.

---

## 🎨 Kullanıcı Deneyimi (UI/UX)
* **Single Page Application (SPA):** Yeni pencereler açarak sistem belleğini (RAM) yormak yerine, Java Swing `CardLayout` kullanılarak tek çerçeve içinde akıcı menü geçişleri kurgulanmıştır.
* **Akıllı UX Yuvarlama Algoritması:** Devasa bir rafta tek bir ürün olsa dahi, matematiksel %0 sonucunu zorla %1 olarak göstererek kullanıcının dolu bir rafı "boş" sanıp silmesini engelleyen akıllı bir düzeltme (Integer Division Fix) uygulanmıştır.

---

## 💻 Kullanılan Teknolojiler
* **Core:** Java (JDK 11+), Object-Oriented Programming (OOP)
* **Veritabanı:** MySQL, JDBC Driver
* **UI/UX:** Java Swing, FlatLaf (Dark/Light Modern Tema Motoru)
* **Veri Görselleştirme:** XChart (Pasta ve Çubuk Grafikleri)
* **Raporlama:** iTextPDF (Logların kurumsal PDF dökümüne çevrilmesi)

---

## 📸 Ekran Görüntüleri

<div align="center">
    <img width="1728" height="1117" alt="Ekran Resmi 2026-03-02 22 30 40" src="https://github.com/user-attachments/assets/cf396c32-7fac-4142-ba08-1e2aef82e422" />
    <img width="1728" height="1117" alt="Ekran Resmi 2026-03-02 22 31 18" src="https://github.com/user-attachments/assets/e9055281-7469-4cc2-ac74-efe9e241dfd3" />
</div>
<br>
<div align="center">
    <img width="1728" height="1117" alt="Ekran Resmi 2026-03-02 22 33 06" src="https://github.com/user-attachments/assets/c6ec32ab-3990-4286-ac23-4570a254ed92" />
    <img width="1728" height="1117" alt="Ekran Resmi 2026-03-02 22 41 12" src="https://github.com/user-attachments/assets/361ef067-2028-424a-9fe5-307b2065b9b6" />

</div>

---

## ⚙️ Kurulum ve Çalıştırma

1. **MySQL Veritabanını Hazırlayın:**
   Yerel sunucunuzda `depo_yonetim` adında bir veritabanı oluşturun.
2. **Tabloları İçe Aktarın:**
   Proje dizinindeki `SQL_Schema.sql` dosyasını oluşturduğunuz veritabanına *Import* edin.
3. **Bağlantı Konfigürasyonu:**
   IDE üzerinden projeyi açın ve `src/DBHelper.java` içerisindeki veritabanı şifrenizi yerel ayarlarınıza göre güncelleyin.
4. **Kütüphaneler:**
   Projenin çalışması için gerekli `.jar` dosyalarının (FlatLaf, XChart, iTextPDF, MySQL Connector) projeye dahil edildiğinden emin olun ve `Main.java` sınıfını çalıştırın.

---

## 👨‍💻 Geliştirici
**Ömer Çelik** * GitHub: [@omer-celik523](https://github.com/omer-celik523)
* *Yazılım Mühendisliği Öğrencisi & Geliştirici*
