package ir.bikalam.shad;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    private boolean offlineChecked = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            WebView webView = getBridge().getWebView();
            if (webView != null) {
                webView.addJavascriptInterface(new AndroidPrint(this, webView), "AndroidPrint");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // فقط یک بار بعد از ساخته شدن اپ چک می‌کنیم
        if (offlineChecked) return;
        offlineChecked = true;

        // کمی تاخیر تا WebView کاملاً آماده شود
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (!isNetworkAvailable()) {
                try {
                    WebView wv = getBridge().getWebView();
                    if (wv != null) {
                        // آدرس محلی خود اپ (نه سرور)
                        wv.loadUrl("https://localhost/offline.html");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 300);
    }

    /**
     * بررسی می‌کند که اینترنت فعال است یا نه.
     * فقط به NET_CAPABILITY_INTERNET اکتفا می‌کند (نه VALIDATED)
     * چون VALIDATED کمی طول می‌کشد تا ست شود.
     */
    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;

            Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            if (caps == null) return false;

            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } catch (Exception e) {
            return false;
        }
    }

    public class AndroidPrint {
        private final Context ctx;
        private final WebView wv;

        AndroidPrint(Context c, WebView w) {
            this.ctx = c;
            this.wv  = w;
        }

        @JavascriptInterface
        public void print() {
            runOnUiThread(() -> {
                try {
                    PrintManager pm = (PrintManager) ctx.getSystemService(Context.PRINT_SERVICE);
                    if (pm == null) return;
                    String jobName = "Print_Document";
                    PrintDocumentAdapter adapter = wv.createPrintDocumentAdapter(jobName);
                    pm.print(jobName, adapter, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
