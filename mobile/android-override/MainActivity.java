package ir.bikalam.shad;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    private boolean offlineRouted = false;

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
    public void onResume() {
        super.onResume();

        // بررسی سریع اینترنت بعد از رزومه شدن
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (offlineRouted) return;

            if (!isNetworkAvailable()) {
                offlineRouted = true;
                try {
                    WebView wv = getBridge().getWebView();
                    if (wv != null) {
                        String cur = wv.getUrl();
                        // فقط اگر روی دامنه هستیم، به offline.html برو
                        if (cur == null || !cur.contains("offline.html")) {
                            wv.loadUrl("https://localhost/offline.html");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 200);
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
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
                    PrintManager pm = (PrintManager)
                            ctx.getSystemService(Context.PRINT_SERVICE);
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
