/* ============================================================
   Native Android Bridge
   - مدیریت دکمه‌ی back سخت‌افزاری
   - تابع خروج از اپ
   ============================================================ */
(function () {
  const isNative = !!(window.Capacitor
    && window.Capacitor.isNativePlatform
    && window.Capacitor.isNativePlatform());

  // در مرورگر معمولی: no-op
  if (!isNative) {
    window.isNativeApp  = () => false;
    window.exitNativeApp = () => {};
    return;
  }

  window.isNativeApp = () => true;

  const Plugins = window.Capacitor.Plugins || {};
  const App     = Plugins.App;

  // ---------- خروج از اپ ----------
  window.exitNativeApp = function () {
    try {
      if (App && App.exitApp) App.exitApp();
    } catch (e) {}
  };

  // ---------- دکمه‌ی back سخت‌افزاری ----------
  let lastBackPress = 0;

  function closeTopSheet() {
    const sheet = document.querySelector('.sheet.open');
    if (!sheet) return false;
    const backdrop = document.querySelector('.sheet-backdrop.open');
    backdrop?.classList.remove('open');
    sheet.classList.remove('open');
    setTimeout(() => { backdrop?.remove(); sheet.remove(); }, 320);
    return true;
  }

  function closeTopConfirm() {
    const c = document.querySelector('.confirm.open');
    if (!c) return false;
    c.querySelector('[data-act="cancel"]')?.click();
    return true;
  }

  function showToast(msg) {
    if (typeof window.toast === 'function') window.toast(msg);
  }

  if (App && App.addListener) {
    App.addListener('backButton', () => {
      // ۱) اگر sheet یا دیالوگ باز است → ببند
      if (closeTopSheet())   return;
      if (closeTopConfirm()) return;

      // ۲) اگر داخل پنل هستیم و route != home → برو خانه
      const route = (location.hash || '#home').slice(1);
      if (route && route !== 'home' && document.getElementById('view')) {
        location.hash = '#home';
        return;
      }

      // ۳) در صفحه‌ی خانه (یا لاگین): دو بار بزن تا خارج شود
      const now = Date.now();
      if (now - lastBackPress < 2000) {
        window.exitNativeApp();
      } else {
        lastBackPress = now;
        showToast('برای خروج دوباره بزنید');
      }
    });
  }
})();
