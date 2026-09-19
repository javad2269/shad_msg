package ir.bikalam.shad;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;
import java.net.InetAddress;

public class MainActivity extends BridgeActivity {

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
        
        if (!isNetworkAvailable()) {
            try {
                getBridge().getWebView().loadUrl("https://shad.bi-kalam.ir/offline.html");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            android.net.Network network = cm.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null && 
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
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
