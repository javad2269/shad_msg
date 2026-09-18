/* ============================================================
   Native Android Bridge
   - مدیریت دکمه‌ی back سخت‌افزاری
   - تابع خروج از اپ
   - باز کردن لینک در مرورگر سیستم
   ============================================================ */
(function () {
  function init() {
    const Cap = window.Capacitor;
    if (!Cap || !Cap.isNativePlatform || !Cap.isNativePlatform()) {
      window.isNativeApp  = () => false;
      window.exitNativeApp = () => {};
      window.openExternal = (url) => window.open(url, '_blank');
      return;
    }

    window.isNativeApp = () => true;

    const App     = Cap.Plugins && Cap.Plugins.App;
    const Browser = Cap.Plugins && Cap.Plugins.Browser;

    window.exitNativeApp = function () {
      try { if (App && App.exitApp) App.exitApp(); } catch (e) {}
    };

    // باز کردن لینک در مرورگر سیستم (Chrome)
    window.openExternal = function (url) {
      if (Browser && Browser.open) {
        Browser.open({ url, presentationStyle: 'fullscreen' });
      } else {
        // fallback
        window.open(url, '_system');
      }
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
        if (window.__isPrintPage) {
          location.href = 'panel.php#students';
          return;
        }

        if (closeTopSheet())   return;
        if (closeTopConfirm()) return;

        if (!document.getElementById('view')) {
          const now = Date.now();
          if (now - lastBackPress < 2000) {
            window.exitNativeApp();
          } else {
            lastBackPress = now;
            showToast('برای خروج دوباره بزنید');
          }
          return;
        }

        const route = (location.hash || '#home').slice(1);
        if (route && route !== 'home') {
          location.hash = '#home';
          return;
        }

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
