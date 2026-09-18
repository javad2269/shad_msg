/* ============================================================
   Native Android Bridge
   - مدیریت دکمه‌ی back سخت‌افزاری
   - تابع خروج از اپ
   ============================================================ */
(function () {
  function init() {
    const Cap = window.Capacitor;
    if (!Cap || !Cap.isNativePlatform || !Cap.isNativePlatform()) {
      window.isNativeApp  = () => false;
      window.exitNativeApp = () => {};
      return;
    }

    window.isNativeApp = () => true;

    const App = Cap.Plugins && Cap.Plugins.App;

    window.exitNativeApp = function () {
      try { if (App && App.exitApp) App.exitApp(); } catch (e) {}
    };

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
        // ۱) sheet یا confirm باز است → ببند
        if (closeTopSheet())   return;
        if (closeTopConfirm()) return;

        // ۲) اگر در صفحه‌ی print هستیم (نه panel) → برگرد
        if (!document.getElementById('view')) {
          history.back();
          return;
        }

        // ۳) اگر در پنل و route != home → برو home
        const route = (location.hash || '#home').slice(1);
        if (route && route !== 'home') {
          location.hash = '#home';
          return;
        }

        // ۴) در خانه: دو بار بزن → خروج
        const now = Date.now();
        if (now - lastBackPress < 2000) {
          window.exitNativeApp();
        } else {
          lastBackPress = now;
          showToast('برای خروج دوباره بزنید');
        }
      });
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
