# SMS Forwarder - Android Mobil Uygulaması

Bu Android uygulaması, cihaza gelen SMS mesajlarını otomatik olarak belirli bir sunucuya yönlendiren bir mobil uygulamadır.

## Özellikler

- 📱 **SMS Dinleme**: Cihaza gelen SMS'leri otomatik olarak dinler
- 🔄 **Token Yönetimi**: Sunucudan otomatik token alır ve yönetir
- 🔧 **Token Yenileme**: "Tokeni Yeniden Oluştur" butonu ile yeni token alabilir
- 🔄 **Arka Plan Çalışma**: Uygulama kapalı olsa bile SMS'leri dinlemeye devam eder
- 🚀 **Otomatik Başlatma**: Cihaz yeniden başlatıldığında otomatik olarak servis başlar
- 📊 **SIM Kart Desteği**: Çift SIM kartlı cihazlarda hangi SIM'den SMS geldiğini tespit eder
- 🌐 **HTTP API**: SMS'leri JSON formatında sunucuya gönderir

## SMS Veri Formatı

SMS'ler aşağıdaki JSON formatında sunucuya gönderilir:

```json
{
  "sender": "gönderen_numara",
  "receiver": "alıcı_numara", 
  "message": "SMS_mesajı",
  "date": "2024-01-01 12:00:00",
  "simSlot": 0,
  "deviceToken": "cihaz_tokeni"
}
```

## Gerekli İzinler

Uygulama aşağıdaki izinleri gerektirir:

- `RECEIVE_SMS`: SMS alımını dinlemek için
- `READ_SMS`: SMS'leri okumak için
- `READ_PHONE_STATE`: Telefon durumu ve SIM kart bilgileri için
- `READ_PHONE_NUMBERS`: Telefon numarasını okumak için
- `INTERNET`: Sunucuya bağlanmak için
- `FOREGROUND_SERVICE`: Arka planda çalışmak için
- `RECEIVE_BOOT_COMPLETED`: Cihaz açıldığında otomatik başlamak için

## Kurulum

1. Projeyi Android Studio'da açın
2. `app/src/main/java/com/example/smsforwarder/network/RetrofitClient.kt` dosyasında `BASE_URL` değişkenini gerçek sunucu URL'si ile değiştirin
3. Projeyi derleyin ve APK oluşturun
4. APK'yı Android cihaza yükleyin

## API Endpoints

### Token Alma
```
POST /api/token
```

**Yanıt:**
```json
{
  "token": "cihaz_tokeni",
  "success": true,
  "message": "Token başarıyla oluşturuldu"
}
```

### SMS Gönderme
```
POST /api/sms
```

**İstek Gövdesi:**
```json
{
  "sender": "gönderen_numara",
  "receiver": "alıcı_numara",
  "message": "SMS_mesajı", 
  "date": "2024-01-01 12:00:00",
  "simSlot": 0,
  "deviceToken": "cihaz_tokeni"
}
```

## Kullanım

1. Uygulamayı açın
2. Gerekli izinleri verin
3. "Servisi Başlat" butonuna basın
4. Uygulama artık arka planda SMS'leri dinlemeye başlar
5. Token yenilemek için "Tokeni Yeniden Oluştur" butonunu kullanın

## Teknik Detaylar

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Dil**: Kotlin
- **Mimari**: MVVM
- **Ağ Kütüphanesi**: Retrofit + OkHttp
- **JSON**: Gson
- **Arka Plan İşlemleri**: WorkManager

## Güvenlik

- SMS verileri yerel olarak saklanmaz
- Token güvenli bir şekilde SharedPreferences'da saklanır
- HTTPS bağlantısı kullanılması önerilir

## Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## Destek

Herhangi bir sorun veya öneri için lütfen issue açın. "# Project-X" 
