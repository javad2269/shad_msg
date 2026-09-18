package ir.bikalam.shad;

import android.content.Context;
import android.os.Bundle;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;

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
