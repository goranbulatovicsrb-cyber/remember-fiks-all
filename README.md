# ⏰ Podsetnik App - Android aplikacija za podsetnike

![Android](https://img.shields.io/badge/Android-26%2B-green)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-blue)

Moderna Android aplikacija za podsetnik sa bogatim opcijama i ultra-modernim dizajnom.

## ✨ Funkcionalnosti

- 📅 **Postavljanje datuma i vremena** - Precizno biraj kada da se oglasi podsetnik
- 🔁 **Ponavljanje** - Jednom, svaki dan, nedeljno, mesečno, godišnje
- 🔔 **Opcije zvuka** - Tiho, vibracija, zvuk, zvuk + vibracija
- 🎵 **Izbor melodije** - Biraj melodiju alarma sa telefona
- ⚡ **Prioriteti** - Niska, srednja, visoka, hitno
- 🎨 **Boja podsetnika** - 8 boja za organizaciju
- 🔒 **Zaključani ekran** - Prikaži alarm i na zaključanom ekranu
- 📌 **Prikvačivanje** - Prikvači podsetnik na zaključani ekran
- 😴 **Snooze** - Odloži alarm (5, 10, 15, 20 ili 30 minuta)
- 🔍 **Pretraga** - Pretraži sve podsetnike
- 📊 **Statistike** - Vidi koliko imaš nadolazećih i isteklih

## 🚀 Kako dobiti APK

### Metod 1: GitHub Actions (preporučeno)

1. **Fork** ovaj repozitorijum na GitHub
2. Idi na **Actions** tab u tvom repozitorijumu  
3. Klikni na **"Build APK"** workflow
4. Klikni **"Run workflow"** → **"Run workflow"**
5. Sačekaj ~5-10 minuta da se završi build
6. Preuzmi APK iz **Artifacts** sekcije na dnu stranice

### Metod 2: Lokalni build

```bash
# Kloniraj repozitorijum
git clone https://github.com/TVOJE-IME/PodsetnikApp.git
cd PodsetnikApp

# Build debug APK (Windows)
gradlew.bat assembleDebug

# Build debug APK (Linux/Mac)  
./gradlew assembleDebug

# APK se nalazi u:
# app/build/outputs/apk/debug/app-debug.apk
```

## 📱 Instalacija na telefon

1. Preuzmi APK fajl
2. Na telefonu idi u **Podešavanja → Bezbenost → Nepoznati izvori** i uključi
3. Otvori APK fajl i instaliraj
4. Otvori aplikaciju i daj tražene dozvole

## 📋 Zahtevi sistema

- Android 8.0 (API 26) ili noviji
- Optimizovano za Android 14 i 15

## 🛠 Tehnologije

- **Kotlin** - Programski jezik
- **Jetpack Compose + Material 3** - Moderni UI
- **Room Database** - Lokalna baza podataka
- **AlarmManager** - Precizno zakazivanje alarma
- **Navigation Compose** - Navigacija između ekrana
- **DataStore** - Čuvanje podešavanja

## 📄 Dozvole

- `POST_NOTIFICATIONS` - Za prikaz obaveštenja (Android 13+)
- `SCHEDULE_EXACT_ALARM` - Za precizne alarme
- `USE_FULL_SCREEN_INTENT` - Za prikaz na zaključanom ekranu  
- `RECEIVE_BOOT_COMPLETED` - Repostavljanje alarma posle restarta
- `VIBRATE` - Vibracija

---
Made with ❤️ in Kotlin + Jetpack Compose
