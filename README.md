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
- Bir GitHub hesabı
- Android 8.0 (API 26) veya üzeri bir telefon

## Kurulum ve APK'yı derletme (Android Studio GEREKMEZ)

Bu projede `.github/workflows/build-apk.yml` adında bir GitHub Actions
tanımı var. Kodu GitHub'a push ettiğinde, GitHub'ın kendi sunucuları
otomatik olarak APK'yı senin için derler; sen sadece bitmiş dosyayı
indirirsin.

### 1. Adım — Projeyi kendi GitHub hesabına yükle
Klasörün içindeyken terminalde (git zaten bu proje içinde başlatılmış ve
commit edilmiş durumda):

```bash
git remote add origin https://github.com/KULLANICI_ADIN/REPO_ADIN.git
git push -u origin main
```

(Önce GitHub'da boş bir repo oluşturman gerekiyor: github.com/new — "Add a
README" seçeneğini işaretlemeden oluştur.)

### 2. Adım — Derlemenin bitmesini bekle
1. GitHub'da repo sayfana git → üstteki **Actions** sekmesine tıkla.
2. "APK Derle" adlı workflow'u göreceksin, push sonrası otomatik
   başlamış olmalı (birkaç dakika sürer). Sarı nokta = çalışıyor,
   yeşil tik = bitti.
3. Otomatik başlamadıysa: Actions sekmesinde soldan "APK Derle"yi seç →
   sağ üstten **"Run workflow"** butonuna bas.

### 3. Adım — APK'yı indir
1. Tamamlanan (yeşil tikli) workflow çalıştırmasına tıkla.
2. Sayfanın altında **Artifacts** bölümünde `auto-clicker-apk` adlı bir
   zip dosyası göreceksin, ona tıklayıp indir.
3. Zip'i aç, içinden `app-debug.apk` dosyasını telefonuna aktar (WhatsApp
   kendine mesaj, Google Drive, USB kablo — hangisi kolaysa).

### 4. Adım — Telefona kur
1. Telefonda APK dosyasına dokun, kurulum isteyecek.
2. "Bilinmeyen kaynaklardan yükleme" izni istenirse onayla (Play Store
   dışından kurduğun için normal bir uyarıdır).
3. Kurulum bitince uygulamayı aç.

> Kod üzerinde değişiklik yapıp tekrar `git push` ettiğinde, yeni bir
> derleme otomatik başlar; her seferinde Actions → Artifacts'tan güncel
> APK'yı indirebilirsin.

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
