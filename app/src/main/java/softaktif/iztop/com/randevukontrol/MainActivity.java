package softaktif.iztop.com.randevukontrol;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.jsoup.Jsoup;


@EActivity
public class MainActivity extends AppCompatActivity {


    private static boolean flagEkle = false;
    private static String tempDomText = null;
    private static String controlDomText = null;
    //
    @ViewById(R.id.webView)
    WebView webView;
    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.edittext_Adres)
    EditText editTextAdres;
    @ViewById(R.id.edittext_DomId)
    EditText edittextDomId;
    //
    private ProgressDialog mProgressDialog;
    private CustomWebViewClient webViewClient;
    private String Url = "http://e-randevu.iem.gov.tr/randevu/Default.aspx";
    private int mInterval = 2000;
    private Handler mHandler;
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            webView.reload();
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    public static void showNotificationMessage(Context context, String title, String message, Intent intent) {


        if (intent == null) {
            intent = new Intent(context, MainActivity.class);
        }
        int icon = R.mipmap.ic_launcher;

        int mNotificationId = 100;


        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_ONE_SHOT
                );

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Notification notification = mBuilder
                .setSmallIcon(icon)
                .setTicker(title)
                .setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setStyle(inboxStyle)
                .setContentIntent(resultPendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), icon))
                .setSubText(message)
                .setContentText(message)

                .build();
        // notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_SHOW_LIGHTS;
        notification.flags = Notification.FLAG_SHOW_LIGHTS;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationId, notification);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar);

        mHandler = new Handler();

        editTextAdres.setText(Url);


        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "wk_tag");
        wakeLock.acquire();
        //showNotificationMessage(MainActivity.this, "Randevu Al", "Text Değişti", null);
    }

    void startRepeatingTask() {
        mHandler.post(mStatusChecker);
        //mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Click(R.id.btnBasla)
    public void btnBaslaClick() {
        if (edittextDomId.getText().toString() == null) {
            Snackbar.make(edittextDomId, "Kontrol edilecek alanın 'id' sini giriniz", Snackbar.LENGTH_INDEFINITE).show();
        } else {
            startRepeatingTask();
        }

    }

    @Click(R.id.btnDur)
    public void btnDurClick() {
        stopRepeatingTask();
    }

    @Click(R.id.btnDomEkle)
    public void btnEkleClick() {
        flagEkle = true;
        if (tempDomText != null) {
            controlDomText = tempDomText;
        } else {
            Snackbar.make(edittextDomId, "Belirttiğiniz kontrol id si sayfada bulunmuyor.Lütfen Kontrol ediniz.", Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    @Click(R.id.btnGit)
    public void buttonClick() {

        flagEkle = false;
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Yükleniyor...");

        webViewClient = new CustomWebViewClient();

        webView.getSettings().setBuiltInZoomControls(true); //zoom yapılmasına izin verir
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(webViewClient); //oluşturduğumuz webViewClient objesini webViewımıza set ediyoruz
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");


        Url = editTextAdres.getText().toString();
        webView.loadUrl(Url);
        // editTextAdres.setText(webView.getUrl());
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {//eğer varsa bir önceki sayfaya gidecek
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) { //Sayfa yüklenirken çalışır
            super.onPageStarted(view, url, favicon);

            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }

        }

        @Override
        public void onPageFinished(WebView view, String url) {//sayfamız yüklendiğinde çalışıyor.
            super.onPageFinished(view, url);

            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                webView.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Bu method açılan sayfa içinden başka linklere tıklandığında açılmasına yarıyor.
            //Bu methodu override etmez yada edip içini boş bırakırsanız ilk url den açılan sayfa dışında başka sayfaya geçiş yapamaz

            view.loadUrl(url);//yeni tıklanan url i açıyor
            editTextAdres.setText(url);
            return true;
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            //BU method webview yüklenirken herhangi bir hatayla karşilaşilırsa hata kodu dönüyor.
            //Dönen hata koduna göre kullanıcıyı bilgilendirebilir yada gerekli işlemleri yapabilirsiniz
            //errorCode ile hatayı alabilirsiniz
            //  if(errorCode==-8){
            //      Timeout
            //  } şeklinde kullanabilirsiniz

            //Hata Kodları aşağıdadır...

            /*
             *  /** Generic error
            public static final int ERROR_UNKNOWN = -1;

            /** Server or proxy hostname lookup failed
            public static final int ERROR_HOST_LOOKUP = -2;

            /** Unsupported authentication scheme (not basic or digest)
            public static final int ERROR_UNSUPPORTED_AUTH_SCHEME = -3;

            /** User authentication failed on server
            public static final int ERROR_AUTHENTICATION = -4;

            /** User authentication failed on proxy
            public static final int ERROR_PROXY_AUTHENTICATION = -5;

            /** Failed to connect to the server
            public static final int ERROR_CONNECT = -6;

            /** Failed to read or write to the server
            public static final int ERROR_IO = -7;

            /** Connection timed out
            public static final int ERROR_TIMEOUT = -8;

            /** Too many redirects
            public static final int ERROR_REDIRECT_LOOP = -9;

            /** Unsupported URI scheme
            public static final int ERROR_UNSUPPORTED_SCHEME = -10;

            /** Failed to perform SSL handshake
            public static final int ERROR_FAILED_SSL_HANDSHAKE = -11;

            /** Malformed URL
            public static final int ERROR_BAD_URL = -12;

            /** Generic file error
            public static final int ERROR_FILE = -13;

            /** File not found
            public static final int ERROR_FILE_NOT_FOUND = -14;

            /** Too many requests during this load
            public static final int ERROR_TOO_MANY_REQUESTS = -15;
            */

        }
    }

    class MyJavaScriptInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) {
            // Toast.makeText(MainActivity.this, html, Toast.LENGTH_LONG).show();

            tempDomText = null;
            String data = Jsoup.parse(html).select("#" + edittextDomId.getText().toString()).first().toString();
            tempDomText = data;
            // String data1 = Jsoup.parse(html).select("#LinkButton2").toString();
            if (!flagEkle) {
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
                Snackbar.make(edittextDomId, "abc", Snackbar.LENGTH_INDEFINITE).show();
                //showNotificationMessage(getApplicationContext(), "Randevu Al", "Text Değişti", null);

            } else {
                if (!data.equals(controlDomText)) {
                    showNotificationMessage(getApplicationContext(), "Randevu Al", "Text Değişti", null);
                }
            }
            // Jsoup.parse(html).body()
        }
    }

}
