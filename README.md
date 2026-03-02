# 🏭 Depo Yönetim Sistemi (Warehouse Management System)

> **Java ile geliştirilmiş, katmanlı mimari yapısına sahip, dosya tabanlı (File I/O) stok takip ve yönetim otomasyonu.**

Bu proje, nesne yönelimli programlama (OOP) prensipleri gözetilerek; stok takibi, raf yönetimi ve yetkilendirme süreçlerini dijitalleştirmek amacıyla geliştirilmiştir. Veri kalıcılığı için özel bir dosya yönetim modülü kullanılarak veritabanı bağımsızlığı sağlanmıştır.

---

## 🚀 Proje Mimarisi ve Teknik Detaylar

Proje, **"Separation of Concerns" (İlgi Alanlarının Ayrımı)** ilkesine uygun olarak tasarlanmıştır. İş mantığı (Business Logic), Veri Erişimi (Data Access) ve Varlıklar (Entities) birbirinden soyutlanmıştır.

### 🛠 Kullanılan Teknolojiler ve Yöntemler
* **Programlama Dili:** Java (JDK 17+)
* **Veri Kalıcılığı:** Java I/O (File Handling - `.txt` tabanlı veritabanı simülasyonu)
* **Mimari Desen:** Manager Design Pattern (Yönetici Tasarım Deseni)
* **Sürüm Kontrol:** Git & GitHub

---

## ⚙️ Temel Fonksiyonlar (Features)

### 1. Ürün ve Stok Yönetimi (Inventory Management)
* **CRUD İşlemleri:** Ürün ekleme, silme ve güncelleme işlemleri `UrunManager` sınıfı tarafından kontrol edilir.
* **Benzersiz ID Takibi:** Her ürün sisteme benzersiz bir kimlik ile kaydedilir.
* **Kategorizasyon:** Ürünler türlerine göre ayrıştırılarak yönetilebilir.

### 2. Raf ve Lokasyon Yönetimi (Shelf Optimization)
* **Akıllı Raf Atama:** Ürünlerin depodaki fiziksel konumları (`Raf` sınıfı) dijital ortamda eşleştirilir.
* **Kapasite Kontrolü:** Rafların doluluk oranları anlık olarak hesaplanır ve hatalı yerleştirmeler engellenir.

### 3. Yönetici ve Yetkilendirme (Admin & Auth)
* **Güvenli Giriş:** `Mudur` ve `MudurManager` sınıfları üzerinden yetkili girişi doğrulaması yapılır.
* **Operasyonel Yetki:** Kritik stok değişiklikleri sadece yetkili kullanıcılar tarafından yapılabilir.

### 4. Raporlama ve I/O İşlemleri
* **Persistance (Kalıcılık):** Program kapatılsa bile veriler `DosyaIslemleri` sınıfı sayesinde kaybolmaz.
* **Listeleme:** Mevcut envanter durumu, `Listeleme` modülü ile detaylı olarak raporlanır.

---

## 📂 Sınıf Yapısı (Class Breakdown)

Proje modüler bir yapıda geliştirilmiştir:

| Sınıf / Paket | Açıklama |
| :--- | :--- |
| **`Main.java`** | Uygulamanın giriş noktasıdır. Menü navigasyonunu yönetir. |
| **`Entity` (Varlıklar)** | `Urun`, `Raf`, `Mudur` sınıfları veri modellerini temsil eder (POJO). |
| **`Manager` (İş Mantığı)** | `UrunManager`, `RafManager` gibi sınıflar veriler üzerindeki kuralları işletir. |
| **`DosyaIslemleri`** | Verilerin `.txt` dosyalarına yazılmasını ve okunmasını sağlayan I/O katmanıdır. |
| **`Listeleme`** | Kullanıcıya sunulan çıktıların formatlandığı yardımcı sınıftır. |

---

## 💻 Kurulum ve Çalıştırma (Getting Started)

Projeyi yerel makinenizde çalıştırmak için aşağıdaki adımları izleyebilirsiniz:

1.  **Repoyu Klonlayın:**
    ```bash
    git clone [https://github.com/KULLANICI_ADINIZ/WarehouseManagementSystem.git](https://github.com/KULLANICI_ADINIZ/WarehouseManagementSystem.git)
    ```
2.  **Projeyi IDE'de Açın:**
    IntelliJ IDEA veya Eclipse kullanarak proje dizinini açın.
3.  **SDK Kontrolü:**
    Proje ayarlarından (Project Structure) Java SDK sürümünün seçili olduğundan emin olun.
4.  **Çalıştırın:**
    `src/Main.java` dosyasını çalıştırarak uygulamayı başlatın.

---

## 🔜 Gelecek Hedefleri (Roadmap)
* [ ] Veri tabanı entegrasyonu (MySQL veya PostgreSQL).
* [ ] Grafiksel Kullanıcı Arayüzü (JavaFX veya Swing).
* [ ] Detaylı Excel raporlama modülü.

---

**Geliştirici:** [Ömer ÇELİK]
*Yazılım Mühendisliği Öğrencisi*
