# Auto Clicker (Android)

Belirlediğin bir ekran bölgesi içinde, ayarladığın aralıkla otomatik dokunma
(tap) yapan basit bir Android uygulaması. Android'in **Erişilebilirlik
(Accessibility) Servisi** API'sini kullanır — bu, üçüncü parti bir
uygulamanın ekrana programatik dokunabilmesinin tek resmi/izinli yoludur.

Uygulama tamamen cihazında çalışır, hiçbir veri internete gönderilmez.

## Özellikler
- Ayarlanabilir tıklama aralığı (ms)
- Ekranda sürükleyerek dikdörtgen bölge seçme
- **Anahtar kelime tabanlı tıklama**: seçilen bölgedeki butonların/yazıların
  gerçek etiketini okur; girdiğin anahtar kelimelerden birini içeren bir
  butona rastladığında otomatik olarak ona tıklar (örn. `topla, ödül al,
  devam` yazarsan sadece bu kelimelerden birini içeren butonlara basar,
  başka hiçbir yere dokunmaz).
  - Tıklama, koordinatı rastgele "dürtmek" yerine gerçek butonun kendisini
    tetikler (ACTION_CLICK) — bu yüzden normal Android arayüzlerinde
    (native View tabanlı uygulamalar) çok daha güvenilirdir. Not: Bu yöntem
    metni okunabilen (erişilebilir) arayüzlerde çalışır; Unity/Unreal gibi
    tamamen çizim tabanlı oyunlarda buton yazıları okunamayabilir.
- Ekranın üzerinde kalan, sürüklenebilir kontrol paneli (Başlat/Durdur)

## Gereksinimler
- Android Studio (Koala veya üzeri önerilir)
- Android 8.0 (API 26) veya üzeri bir telefon/emülatör

## Kurulum (Android Studio ile)
1. Android Studio'yu aç → **Open** → bu proje klasörünü seç.
2. Studio ilk açılışta Gradle'ı senkronize edecek (internet ister, birkaç
   dakika sürebilir). "Sync Now" çıkarsa tıkla.
3. Telefonunu USB ile bağla (Geliştirici Seçenekleri > USB hata ayıklama açık
   olmalı) veya bir emülatör başlat.
4. Üstteki yeşil ▶️ **Run** düğmesine bas.

## Kullanım
1. Uygulama açıldığında sırasıyla:
   - **"1. Erişilebilirlik iznini ver"** → açılan ayarlar sayfasında
     "Auto Clicker" servisini bul ve aç.
   - **"2. Ekranın üzerinde gösterme iznini ver"** → izni onayla.
2. **"Kontrol Panelini Başlat"** butonuna bas. Ekranın üzerinde küçük bir
   panel belirir.
3. Panelde **"Bölge Seç"**'e bas, ekranda taranmasını istediğin alanı
   parmağınla sürükleyerek işaretle (örneğin butonların olduğu bölge).
4. **"Anahtar kelimeler"** kutusuna, tıklanmasını istediğin butonların
   üzerinde geçen kelimeleri virgülle ayırarak yaz (ör. `topla, ödül al,
   devam`). Bu kelimelerden biri geçmeyen hiçbir butona dokunulmaz.
5. Aralığı (ms) gir, **"Başlat"**a bas. Durdurmak için tekrar aynı butona bas
   (artık "Durdur" yazacak).
6. Paneli tamamen kapatmak için **"Kapat"**a bas.

## Önemli notlar
- Bazı oyun/uygulamaların kullanım şartları otomasyon araçlarını
  yasaklayabilir; özellikle rekabetçi/çevrimiçi oyunlarda hesabının
  kısıtlanma riski olabilir. Tek oyunculu/idle görevler için genelde sorun
  olmaz, yine de sorumluluk sana aittir.
- `applicationId` olarak `com.example.autoclicker` kullanıldı; Play Store'a
  yüklemeyi düşünürsen bunu kendi paket adınla değiştirmen gerekir
  (`app/build.gradle` içinde).
- Bölge seçimi ve aralık şu an kalıcı olarak saklanmıyor (uygulama
  kapanınca sıfırlanır). İstersen `SharedPreferences` ekleyip kalıcı hale
  getirebiliriz.

## Bu projeyi kendi GitHub hesabına yükleme
Proje klasörünün içindeyken terminalde:

```bash
git init
git add .
git commit -m "İlk sürüm: Auto Clicker Android uygulaması"
git branch -M main
git remote add origin https://github.com/KULLANICI_ADIN/REPO_ADIN.git
git push -u origin main
```

(Önce GitHub'da boş bir repo oluşturman gerekiyor: github.com/new)
