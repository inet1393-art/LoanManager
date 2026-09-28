# قرض‌دار من (Loan Manager)

اپلیکیشن Native اندروید (Kotlin + Jetpack Compose + Material 3 + Room) برای مدیریت وام‌ها، اقساط و یادآورها با تاریخ شمسی.

Package: `com.reminder.loanmanager`

## گرفتن APK از GitHub
1. یک ریپازیتوری جدید در GitHub بسازید و محتوای این پوشه را در شاخه `main` پوش کنید.
2. به تب **Actions** بروید؛ workflow با نام **Build Android APK** خودکار اجرا می‌شود
   (یا با دکمه Run workflow دستی اجرا کنید).
3. بعد از پایان، از پایین صفحه‌ی همان اجرا، بخش **Artifacts** فایل `LoanManager-debug-apk` را دانلود کنید.
   داخل آن `app-debug.apk` است.

## ساخت روی کامپیوتر
Android Studio (Hedgehog یا جدیدتر) پروژه را باز کنید و Sync/Run بزنید. یا با Gradle 8.9 و JDK 17:
`gradle assembleDebug`
مسیر خروجی: `app/build/outputs/apk/debug/app-debug.apk`

## امکانات
- ثبت وام و ساخت خودکار اقساط ماهانه
- ثبت پرداخت قسط، تسویه خودکار وام
- یادآور خودکار قبل از سررسید + یادآور دستی (AlarmManager + Notification)
- نمایش یادآور روی سایر برنامه‌ها (Overlay، با اجازه‌ی کاربر)
- بازسازی یادآورها بعد از ریبوت
- تقویم شمسی، RTL، حالت تاریک
- داشبورد، گزارش‌ها، پشتیبان‌گیری/بازیابی JSON
