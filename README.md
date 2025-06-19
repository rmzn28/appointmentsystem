
![Ekran görüntüsü 2025-05-23 141637](https://github.com/user-attachments/assets/e08522c3-5e1d-4183-a455-d60fede82b5f)





![Ekran görüntüsü 2025-05-23 135526](https://github.com/user-attachments/assets/b59efc64-fde9-4f11-93f7-76a0a336913b)






![Ekran görüntüsü 2025-05-23 150208](https://github.com/user-attachments/assets/5a7363db-6202-40de-a7d2-1f09235f2d99)





![Ekran görüntüsü 2025-05-25 182449](https://github.com/user-attachments/assets/7c054128-436d-4e45-bee9-919ca7bd1431)






AKADEMİK RANDEVU VE TAKİP SİSTEMİ – PROJE RAPORU
1. Projenin Amacı
Bu projenin temel amacı, üniversite öğrencilerinin öğretim üyeleriyle etkili ve düzenli şekilde iletişim kurabilmesini sağlayan bir masaüstü uygulaması geliştirmektir. Öğrenciler, öğretim üyelerinin belirlemiş olduğu uygun saat aralıklarına göre sistem üzerinden randevu talebinde bulunabilecek; öğretim üyeleri ise bu talepleri görüntüleyip uygun şekilde yönetebileceklerdir. Bu yapı sayesinde hem öğretim üyelerinin hem de öğrencilerin zamandan tasarruf etmeleri, aynı zamanda iletişim süreçlerinin dijital ortama taşınarak sistematik hale getirilmesi hedeflenmiştir.
2. Kullanılan Teknolojiler
Proje Java programlama dili kullanılarak masaüstü uygulaması biçiminde geliştirilecektir. Arayüz, Java Swing bileşenleriyle tasarlanmıştır. Veritabanı bağlantısı için JDBC (Java Database Connectivity) kütüphanesi kullanılmakta olup, veri saklama işlemleri için SQLite tercih edilmiştir.
Kullanılan teknolojiler şunlardır:
● Java Swing (Kullanıcı Arayüzü)
● JDBC (Veritabanı Bağlantısı)
● SQLite (Yerel Veritabanı Yönetimi)
● (İLGİLİ TABLOLAR OLUŞTURULDU:Appointments,Availabilty,Users…)
3. Kullanıcı Rolleri ve Yetkileri
Sistem iki ana kullanıcı rolüne sahiptir:
A. Öğrenci:
● Giriş yaptıktan sonra randevu almak istediği öğretim üyesini seçer.(JComboBox ile) (loadInstructors metodu ile bilgi çekilir)
● Takvim arayüzünden tarih ve saat aralığı belirler.
● Uygun saat dilimleri sistem tarafından sunulur.(loadAvailableTimes)
● Randevu talebini oluşturur(requestAppointment ile veritabanına kayıt işlemi), mevcut taleplerini görüntüleyebilir veya iptal edebilir.
B. Öğretim Üyesi:
● Giriş yaptıktan sonra kendi müsaitlik saatlerini sistem üzerinde tanımlar.(addAvailability)
● Öğrencilerden gelen randevu taleplerini görür.(loadAppointmentRequests)
● Randevuyu onaylar, reddeder veya başka bir saat önerir.(database güncellenir.)
● Onaylanan randevular takvim görünümünde listelenir.
4. İş Mantığı ve Sistem Akışı
1. Kullanıcılar sisteme kullanıcı adı ve şifre ile giriş yapar.(SQLite ile bilgiler veritabanında tutulur.)
2. Öğrenciler randevu talep etmek istedikleri öğretim üyesini ve tarihi seçer.(JDateChooser ile takvim görüntüsü sağlanmıştır.)
3. Sistem, seçilen öğretim üyesinin önceden tanımlamış olduğu uygunluk bilgilerini kontrol eder.
4. Daha önce alınmış randevular hariç tutularak boş zaman dilimleri belirlenir.(Availability tablosunda is_booked sütunu ile kontrol)
5. Bu boşluklar öğrenciye 20 dakikalık zaman dilimleri halinde sunulur.
6. Öğrenci talebi oluşturur; talep öğretim üyesine iletilir.(status= beklemede)
7. Öğretim üyesi talebi değerlendirir ve uygun şekilde yanıtlar.(sistem ; onay , red veya yeni saat önerme seçenekleriyle tasarlandı.)(Uygulanan işlemden sonra panel güncellenir)
