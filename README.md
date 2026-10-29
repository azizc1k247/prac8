# PermissionsLab - Android Ruxsatnomalari Bo'yicha Laboratoriya Ishi

Ushbu loyiha **uz.ttpu.permissionslab** paketi ostida Android-da ruxsatnomalarni (Permissions) boshqarishning barcha turlarini namoyish etadi.

---

## 📱 Sinalgan Android API Darajalari
- **API 34 (Android 14)** – Pixel Emulator (Tosh so'rovlar, POST_NOTIFICATIONS runtime ruxsati, One-time Location va Overlay tekshirildi)
- **API 30 (Android 11)** – Emulator (Camera 3-state, Denied twice, Settings intent yo'naltirishlari tekshirildi)
- **API 32 (Android 12L)** – POST_NOTIFICATIONS so'rovisiz avtomatik bildirishnoma yuborilishi tekshirildi.

---

## 🔍 5-topshiriq Kuzatuv Natijalari ("Only this time" Joylashuv Ruxsati)

**Kuzatuv:**
Joylashuv dialogida **"Faqat bu safar" (Only this time)** opsiyasi tanlanganda, ilova ekranida joylashuv holati darhol *"Aniq"* yoki *"Taxminiy"* ga o'tadi. Biroq, ilova fonga o'tkazilib (Home tugmasi bosilib) biroz vaqtdan keyin ilovaga qaytilganda yoki ilova jarayoni to'xtatilganda, Android operatsion tizimi ushbu vaqtinchalik ruxsatni avtomatik ravishda bekor qiladi.

**Xulosa:**
Shu sababli ruxsat holatini faqat `onCreate()` metodi ichida bir marta tekshirish yetarli emas. Ilova hayotiy siklining `ON_RESUME` (`LifecycleEventEffect(Lifecycle.Event.ON_RESUME)`) hodisasida joylashuv ruxsatlarini har safar qayta tekshirish zarur.

---

## 🔔 6-topshiriq Kuzatuv Natijalari (POST_NOTIFICATIONS: API 32 vs API 34)

- **API 32 (Android 12) va undan pastida:** `POST_NOTIFICATIONS` ruxsati runtime so'rov talab qilmaydi. `Build.VERSION.SDK_INT < 33` sharti sababli so me'yorda so'rov o'tkazib yuboriladi va test bildirishnomasi darhol bildirishnomalar paneliga tushadi.
- **API 34 (Android 14) va undan yuqorisida:** Tizim runtime dialog ko'rsatadi. Foydalanuvchi "Allow" tugmasini bosgandan so'nggina bildirishnoma yuboriladi. Rad etilganda bildirishnoma yuborilmaydi.

---

## 📚 A Qism: Nazariy Savollar va Javoblar (19 ta Savol)

### 1-savol
**Kontekst:** Talaba Rasmga olish funksiyasini qo'shishda darhol CAMERA ruxsati uchun dialog kodini yozmoqchi.  
**Vazifa:** Ruxsatnomalar bilan ishlash jarayonining (workflow) 4 ta qarori va ruxsatnomasiz bajariladigan holat misoli.  
**Javob:**
1. **Workflow'ning 4 ta qarori:**
   - *1-qaror:* Ruxsatnomasiz bajarsa bo'ladimi? (Masalan, Implicit Intent orqali tizim kamerasi ilovasiga topshirish).
   - *2-qaror:* Ruxsatnoma allaqachon berilganmi? (`ContextCompat.checkSelfPermission` bilan tekshirish).
   - *3-qaror:* Foydalanuvchiga tushuntirish (rationale) ko'rsatish kerakmi? (`shouldShowRequestPermissionRationale`).
   - *4-qaror:* Runtime dialogini so'rash (`rememberLauncherForActivityResult` yoki `requestPermission`).
2. **Ruxsatnomasiz bajariladigan holatga misol:** `ActivityResultContracts.TakePicture()` yoki `MediaStore.ACTION_IMAGE_CAPTURE` intent'i orqali tizimdagi standart Kamera ilovasini chaqirib rasm olish.
3. **Nega bu eng yaxshi variant?** Foydalanuvchidan ortiqcha ruxsat so'rab uni cho'chitmaydi, apparat xavfsizligi va saqlash logikasini tizimning tayyor Kamera ilovasiga topshiradi va dastur kodi hajmini qisqartiradi.

---

### 2-savol
**Kontekst:** Jamoadosh barcha ruxsatnomalarga bir xil kod yozadi.  
**Vazifa:** Ruxsatnomalarning 3 ta toifasi va ularning kod hajmiga ta'siri.  
**Javob:**
1. **Uchta toifa:**
   - **Install-time ruxsatnomalar (Normal va Signature):** Manifestda e'lon qilinadi, o'rnatish vaqtida tizim tomonidan avtomatik beriladi. Kod hajmi: juda kam (faqat Manifestda 1 qator).
   - **Runtime ruxsatnomalar (Dangerous):** Ilova ishlayotganda foydalanuvchiga muloqot oynasi (dialog) chiqariladi. Kod hajmi: o'rtacha (check, rationale, request launcher, result callback handler).
   - **Special ruxsatnomalar (Overlay, Write Settings va b.):** Tizim Sozlamalari (Settings) ekraniga Intent orqali o'tib beriladi. Kod hajmi: eng ko'p (check, Settings Intent, ON_RESUME lifecycle observer).

---

### 3-savol
**Kontekst:** Dasturchi INTERNET uchun `checkSelfPermission` tekshiruvi yozmoqda. Boshqa ilovada Manifestdagi qator unutilgan.  
**Vazifa:** Normal ruxsatnomalarga runtime tekshiruv nega kerak emas, unutilsa qanday exception beradi va 3 ta misol.  
**Javob:**
1. **Nega runtime tekshiruv kerak emas:** Normal ruxsatnomalar foydalanuvchi maxfiyligiga va qurilmaga minimal xavf soladi, shuning uchun Android ularni o'rnatish vaqtida avtomatik beradi.
2. **Manifestdagi qator unutilsa:** Ilova tarmoq so'rovi yuborayotganda `java.lang.SecurityException: Permission denied (missing INTERNET permission)` xatosini beradi.
3. **Uchta misol:** `INTERNET`, `ACCESS_NETWORK_STATE`, `CHANGE_NETWORK_STATE` (yoki `RECEIVE_BOOT_COMPLETED`, `VIBRATE`).

---

### 4-savol
**Kontekst:** "Bank" va "Hamyon" ilovalari. Hamyon Bankning `READ_ACCOUNTS` ruxsatnomasi bilan himoyalangan xizmatiga ulanmoqchi.  
**Vazifa:** Signature ruxsatnomasi kimga beriladi, protectionLevel, natijalar va dialog ko'rsatilishi.  
**Javob:**
1. **Kimga beriladi:** Faqat ruxsatnomani e'lon qilgan ilova (Bank) bilan **bir xil raqamli kalit (keystore/certificate)** orqali imzolangan ilovalarga.
2. **ProtectionLevel:** `android:protectionLevel="signature"`.
3. **Natija:**
   - **Hamyon (agar Bank kaliti bilan imzolangan bo'lsa):** Ruxsatnoma o'rnatish vaqtida avtomatik beriladi. Muloqot oynasi (dialog) ko'rsatilmaydi.
   - **Uchinchi tomon ilovasi (boshqa kalit bilan imzolangan):** Ruxsatnoma o'rnatish vaqtida avtomatik ravishda rad etiladi. Dialog ko'rsatilmaydi.

---

### 5-savol
**Kontekst:** Install-time ruxsatnomalarni dialog orqali so'rash haqida noto'g'ri tushuncha.  
**Vazifa:** Install-time ruxsatnomalar qachon va qayerda ko'rsatiladi, tizim ularni qachon beradi va qaysi 2 tur kiradi?  
**Javob:**
1. **Qachon va qayerda ko'rsatiladi:** Google Play Store do'konida ilovani yuklab olish sahifasida / ma'lumotlarida o'rnatishdan oldin ko'rsatiladi.
2. **Tizim qachon beradi:** Ilova qurilmaga o'rnatilayotgan (installation) vaqtda tizim ularni avtomatik beradi.
3. **Qaysi ikki tur kiradi:** Normal ruxsatnomalar (`INTERNET`) va Signature ruxsatnomalar (`protectionLevel="signature"`).

---

### 6-savol
**Kontekst:** minSdk 21. Android 5.1 (API 22) da CAMERA dialogi chiqmayapti, Android 14 da esa chiqadi.  
**Vazifa:** Versiyalar farqi, API 22 da runtime ruxsatnoma holati va `granted(kind, sdk)` modeli.  
**Javob:**
1. **Joriy qilingan versiya:** Runtime ruxsatnomalar **Android 6.0 (API 23, Marshmallow)** versiyasida joriy qilingan.
2. **API 22 da nega dialog chiqmaydi:** Android 5.1 (API 22) va undan past versiyalarda barcha ruxsatnomalar o'rnatilish vaqtida (install-time) to'liq va bir marta berilgan.
3. **`granted(kind, sdk)` modeli:**
   - Agar `sdk < 23` bo'lsa: Manifestdagi barcha ruxsatnomalar o'rnatish vaqtida beriladi (`granted = true`).
   - Agar `sdk >= 23` va `kind == RUNTIME` bo'lsa: Ruxsatnoma holati foydalanuvchining bajarilish vaqtida (runtime) bergan javobiga bog'liq bo'ladi.

---

### 7-savol
**Kontekst:** Kamera ruxsati ikki marta rad etildi. Tugma bosilganda `launch()` chaqirilsa hech narsa ko'rinmaydi.  
**Vazifa:** Runtime holatlar ketma-ketligi, `shouldShowRationale` qiymati, bajariladigan kod va yo'naltiriladigan joy.  
**Javob:**
1. **3 ta holat va `shouldShowRationale` qiymatlari:**
   - *1-holat: Hali so'ralmagan (Boshlang'ich):* `shouldShowRationale = false`, `checkSelfPermission != GRANTED`. Kod: `launch()` orqali tizim dialogini chiqarish.
   - *2-holat: Bir marta rad etilgan:* `shouldShowRationale = true`. Kod: Rationale (tushuntirish matni) va "Ruxsat so'rash" tugmasini ko'rsatish (`launch()`).
   - *3-holat: Ikki marta rad etilgan ("Don't ask again"):* `shouldShowRationale = false`, `checkSelfPermission != GRANTED` (so'rov urinishidan keyin). Kod: Tizim dialog chiqarmaydi, shuning uchun "Sozlamalarni ochish" tugmasini chiqarish kerak.
2. **Yo'naltiriladigan joy:** `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` intent'i orqali `Uri.fromParts("package", context.packageName, null)` parametri bilan Tizim Sozlamalari (Settings) sahifasi.

---

### 8-savol
**Kontekst:** Accompanist `rememberPermissionState` va `@OptIn(ExperimentalPermissionsApi::class)` ishlatilgan kod.  
**Vazifa:** 3 tarmoq ishlashi, Compose recomposition'dan omon qolish sababi va `ExperimentalPermissionsApi` ma'nosi.  
**Javob:**
1. **3 tarmoq ishlashi:**
   - `isGranted == true`: Ruxsat berilgan. Ekran asil funksiyani (masalan, Kamera korpusini) ko'rsatadi.
   - `shouldShowRationale == true`: Ruxsat bir marta rad etilgan. Ekranda tushuntirish xabari va "Ruxsat berish" tugmasi chiqadi.
   - `else`: Ruxsat hali so me'yorda so'ralmagan yoki ikki marta rad etilgan. Ekranda "Ruxsat so'rash" yoki "Sozlamalarga o'tish" tugmasi chiqadi.
2. **Nega recomposition'dan omon qoladi:** `rememberPermissionState` ichki Compose `remember` va `DisposableEffect` (LifecycleObserver) yordamida ruxsat holatini xotirada saqlaydi va UI qayta chizilganda (recomposition) holat yoqolmaydi.
3. **`ExperimentalPermissionsApi`:** Ushbu API Accompanist eksperimental kutubxonasiga tegishli bo'lib, kelajakda o'zgarishi mumkinligini ogohlantiruvchi anotatsiyadir.

---

### 9-savol
**Kontekst:** Loyihaga qo'shimcha kutubxona qo'shmasdan ruxsat so'rash.  
**Vazifa:** Activity Result API bilan yozish, holatni va rationale'ni aniqlash funksiyalari va Accompanist bilan solishtirish.  
**Javob:**
1. **Kutubxonasiz yozish:** `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> ... }` orqali muloqot oynasi ochiladi.
2. **Joriy holatni o'qish:** `ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)`.
3. **Rationale'ni aniqlash:** `ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)`.
4. **Accompanist bilan solishtirish:** Standart Activity Result API hech qanday tashqi kutubxonasiz ishlaydi, rasmiy AndroidX standarti hisoblanadi va kod ustidan to'liq nazorat beradi. Accompanist esa ushbu standart funksiyalarni Compose uchun soddalashtirilgan wrapper (o'rovchi) holatida taqdim etadi.

---

### 10-savol
**Kontekst:** Aniq (`ACCESS_FINE_LOCATION`) va taxminiy (`ACCESS_COARSE_LOCATION`) joylashuv birga so'ralmoqda.  
**Vazifa:** `RequestMultiplePermissions` natija shakli (`Map<String, Boolean>`), birgalikda ko'rinishi va ikkalasi rad etilgandagi harakat.  
**Javob:**
1. **Natija shakli:** `Map<String, Boolean>` ko'rinishida qaytadi. Masalan: `{"android.permission.ACCESS_FINE_LOCATION": true, "android.permission.ACCESS_COARSE_LOCATION": true}`.
2. **Nega bitta interfeys ko'radi:** Android operatsion tizimi foydalanuvchiga bitta dialog ichida "Precise" (Aniq) va "Approximate" (Taxminiy) tanlovini taklif etadi.
3. **Ikkala ruxsat ham rad etilsa:** Ilova joylashuvga bog'liq funksiyani o'chirishi, foydalanuvchiga tushuntirish xabarini ko'rsatishi yoki joylashuvni qo'lda shahar nomi orqali qidirish imkonini berishi kerak.

---

### 11-savol
**Kontekst:** Special ruxsatnomalar oddiy runtime dialog orqali chiqmaydi.  
**Vazifa:** Special ruxsatnomalar farqi va jadvalni to'ldirish.  
**Javob:**
1. **Farqi:** Special ruxsatnomalar odatiy muloqot dialogi orqali emas, balki foydalanuvchini Tizim Sozlamalari (Settings) sahifasiga Intent orqali o'tkazish yordamida beriladi.
2. **Jadval:**

| Ruxsatnoma | Nimaga ruxsat beradi | Tekshirish chaqiruvi |
| :--- | :--- | :--- |
| `SYSTEM_ALERT_WINDOW` | Boshqa ilovalar ustidan oyna chiqarish (Overlay) | `Settings.canDrawOverlays(context)` |
| `WRITE_SETTINGS` | Tizim sozlamalarini o'zgartirish | `Settings.System.canWrite(context)` |
| `MANAGE_EXTERNAL_STORAGE` | Barcha xotira fayllariga to'liq kirish | `Environment.isExternalStorageManager()` |
| `REQUEST_INSTALL_PACKAGES` | Noma'lum manbalardan ilovalar o'rnatish | `context.packageManager.canRequestPackageInstalls()` |
| `ACCESS_BACKGROUND_LOCATION` | Fonda joylashuvni kuzatish | `ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)` |

---

### 12-savol
**Kontekst:** Sozlamalardan qaytganda ruxsat holati yangilanmayapti.  
**Vazifa:** "Tekshir → Sozlamalarni och → qaytganda tekshir" andozasi va `LifecycleEventEffect` orqali tuzatish.  
**Javob:**
1. **Nega callback kelmaydi:** `startActivity` orqali Sozlamalar ilovasiga o'tiladi. Sozlamalar alohida tizim ilovasi bo'lgani uchun undan qaytganda standart ActivityResult callback yettib kelmaydi.
2. **Tuzatilgan kod:**
```kotlin
@Composable
fun OverlayScreen() {
    val context = LocalContext.current
    var canDraw by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        canDraw = Settings.canDrawOverlays(context)
    }

    Switch(
        checked = canDraw,
        onCheckedChange = { openOverlaySettings(context) }
    )
}
```

---

### 13-savol
**Kontekst:** `ACCESS_FINE_LOCATION` va `ACCESS_BACKGROUND_LOCATION` birga so'ralganda fon joylashuvi ishlamayapti.  
**Vazifa:** Ikki bosqichli tartib va `SDK_INT >= Build.VERSION_CODES.Q` sharti.  
**Javob:**
1. **1-bosqich:** Avval old fon joylashuv ruxsati (`ACCESS_FINE_LOCATION` yoki `COARSE`) so'raladi.
2. **2-bosqich:** Faqat 1-bosqichda ruxsat berilgach, alohida muloqot oynasi orqali fon joylashuvi (`ACCESS_BACKGROUND_LOCATION`) so'raladi.
3. **Nega birga so'ralmaydi:** Android tizimi xavfsizlik nuqtai nazaridan ikkala ruxsatni bitta so'rovda berishga ruxsat bermaydi; birga so'ralganda fon ruxsati avtomatik rad etiladi.
4. **Nega `SDK_INT >= Q` sharti zarur:** Background Location ruxsati Android 10 (API 29, `Q`) versiyasida kiritilgan. API 29 dan past versiyalarda old fon ruxsati avtomatik ravishda fon joylashuvini ham qamrab oladi.

---

### 14-savol
**Kontekst:** 5 ta ruxsatnoma so'ralganda 3 ta dialog chiqdi.  
**Vazifa:** Permission group tushunchasi, 3 ta guruh misoli va kodda guruh tarkibiga tayanmaslik sababi.  
**Javob:**
1. **Permission Group:** Android mantiqan bog'liq ruxsatnomalarni guruhlarga biriktiradi (masalan, Fine va Coarse Location bitta guruhda). Agar guruhdagi bitta ruxsatnomaga ruxsat berilsa, tizim shu guruhdagi boshqa ruxsatnomalarga yangi dialog ko me'yorda avtomatik ruxsat beradi.
2. **3 ta guruh misoli:**
   - `LOCATION` (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`)
   - `CONTACTS` (`READ_CONTACTS`, `WRITE_CONTACTS`, `GET_ACCOUNTS`)
   - `CAMERA` (`CAMERA`)
3. **Nega guruh tarkibiga tayanmaslik kerak:** Android operatsion tizimi versiyalari o'zgarganda guruhlar tarkibini o'zgartirishi yoki bekor qilishi mumkin. Shuning uchun kodda har bir ruxsatnoma an'anaviy ravishda aniq so'ralishi kerak.

---

### 15-savol
**Kontekst:** CAMERA uchun dialog yozildi, lekin Manifestga e'lon qilinmadi.  
**Vazifa:** Oqibati, e'lon qilish farqlari va `android:maxSdkVersion` atributi.  
**Javob:**
1. **Nima bo'ladi:** Ilova ruxsat so'rashga urinayotganda tizim dialog chiqarish o'rniga so'rovni ko'rmaydi yoki bajarilish vaqtida `java.lang.SecurityException` xatosini tashlab ilova crash bo'ladi.
2. **E'lon qilish jarayoni:** Barcha ruxsatnoma turlari (install-time, runtime, special) majburiy ravishda `AndroidManifest.xml` faylida `<uses-permission>` bilan e'lon qilinishi SHART.
3. **`android:maxSdkVersion` atributi:** Ruxsatnoma faqat ma'lum bir API versiyasigacha kerak bo'lganda ishlatiladi. Masalan: `<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />`. Android 10+ (API 29+) da Scoped Storage joriy qilingani uchun `WRITE_EXTERNAL_STORAGE` ruxsati talab qilinmaydi.

---

### 16-savol
**Kontekst:** Kameradan ixtiyoriy foydalanuvchi ilovani kamerasiz qurilmalarga o'rnatish.  
**Vazifa:** `required="true/false"`, `hasSystemFeature` va `visibleOn` modelining 4 natijasi.  
**Javob:**
1. **Play Store filtrlashi:**
   - `required="true"`: Play Store kamerasiz qurilmalarga (masalan, Android TV, ba'zi planshetlar) ilovani umuman ko'rsatmaydi va yuklab olishni taqiqlaydi.
   - `required="false"`: Ilova barcha qurilmalarga ko'rinadi va yuklab olishga ruxsat beriladi.
2. **Kodda tekshirish:** `context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)`.
3. **`visibleOn` modelining 4 ta natijasi:**
   - *Apparat bor + Ruxsat berilgan:* Funksiya to'liq va muvaffaqiyatli ishlaydi.
   - *Apparat bor + Ruxsat rad etilgan:* Funksiya foydalanuvchi rad etgani sababli ishlamaydi (dialog chiqarish mumkin).
   - *Apparat yo'q + Ruxsat berilgan/rad etilgan:* Apparat yo'qligi sababli funksiya o'chiriladi (UI tugmasi yashiriladi).
   - *Do'kon filtrlashi:* `required=true` do'kondan yashiradi, `required=false` o'rnatishga ruxsat beradi.

---

### 17-savol
**Kontekst:** "Faqat bu safar" (Only this time) tanlangach, fonga o'tib qaytganda kamera ishlamayapti.  
**Vazifa:** Bir martalik ruxsatnoma ishlashi, bekor qilinishi, keshlamaslik va `ON_RESUME` tuzatishi.  
**Javob:**
1. **Ishlashi va bekor qilinishi:** Bir martalik ruxsatnoma (Android 11, API 30) faqat ilova faol bo'lgan joriy seans uchun amal qiladi. Ilova fonga o'tkazilganda yoki ma'lum vaqt ishlatilmaganda tizim ruxsatni avtomatik bekor qiladi.
2. **Nega keshlamaslik kerak:** Ruxsat holati ilovadan tashqarida (tizim tomonidan) dinamik o'zgargani uchun "granted" qiymatini saqlab qo'yish xatolikka olib keladi.
3. **Tuzatilgan kod:**
```kotlin
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}
```

---

### 18-savol
**Kontekst:** Ilova bir necha oy ishlatilmagach ruxsat yo'qolgan. Yangi versiyada `READ_CONTACTS` kerak emas.  
**Vazifa:** (1) Auto-reset nima qiladi va `ACTION_AUTO_REVOKE_PERMISSIONS`; (2) `revokeSelfPermissionOnKill` (API 33) vazifasi.  
**Javob:**
1. **Auto-reset (Permissions Auto-revoke):** Agar foydalanuvchi ilovani bir necha oy davomida ochmasa, Android 11+ tizimi ilovaning barcha runtime ruxsatnomalarini avtomatik ravishda nolga tushiradi (bekor qiladi). Foydalanuvchi uni `Intent(Settings.ACTION_AUTO_REVOKE_PERMISSIONS, Uri.parse("package:${context.packageName}"))` orqali Sozlamalardan o'chirib qo'yishi mumkin.
2. **`revokeSelfPermissionOnKill`:** Android 13 (API 33) da kiritilgan bu funksiya orqali ilova o'ziga ilgari berilgan runtime ruxsatnomani (masalan, `READ_CONTACTS`) ixtiyoriy ravishda qaytarib topshirishi (bekor qilishi) mumkin. Bu foydalanuvchi shaxsiy ma'lumotlari xavfsizligini oshirish uchun juda foydali.

---

### 19-savol
**Kontekst:** Android versiyalari bo'yicha ruxsatnomalar o'zgarishi jadvali.  
**Vazifa:** API 23, API 30 va API 33 uchun asosiy o'zgarishlar va `POST_NOTIFICATIONS` tushuntirishi.  
**Javob:**
1. **Asosiy o'zgarishlar:**
   - **API 23 (Android 6.0):** Runtime ruxsatnomalar joriy qilindi (muloqot dialogi ilova ishlayotganda chiqariladigan bo'ldi).
   - **API 30 (Android 11):** Bir martalik ruxsatnomalar ("Only this time") va Auto-reset mexanizmi kiritildi.
   - **API 33 (Android 13):** `POST_NOTIFICATIONS` runtime ruxsatnomaga aylantirildi va `revokeSelfPermissionOnKill` qo'shildi.
2. **`POST_NOTIFICATIONS` evolyutsiyasi:** Android 13 (API 33) ga qadar bildirishnoma yuborish uchun runtime ruxsat talab qilinmagan (avtomatik ruxsat berilgan). API 33+ da esa u runtime ruxsatga aylandi. API 33 dan past versiyalarda so'ralganda tizim hech narsa ko'rsatmaydi, shuning uchun `Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU` sharti bilan o'rash majburiydir.
