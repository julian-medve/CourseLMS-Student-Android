package com.nanasoft.olmaa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prevent screen capture
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        //Request Internet permission
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.INTERNET},
                1);

        // Initialize Android Networking
        AndroidNetworking.initialize(getApplicationContext());


        setContentView(R.layout.activity_main);
        WebView webview = (WebView) findViewById(R.id.webView);
        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl("https://google.com");

        // Get params from the URI
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();

        Log.d(TAG, action);
        if (data != null) {
            String user = data.getQueryParameter("user");
            String content = data.getQueryParameter("class");
            String uri = "http://lms.olmaa.net/api/v1/contentWithVideo";

            AndroidNetworking.get(uri)
//                    .addPathParameter("pageNumber", "0")
                    .addQueryParameter("userId", user)
                    .addQueryParameter("contentId", content)
//                    .addHeaders("token", "1234")
//                    .setTag("test")
                    .setPriority(Priority.LOW)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, response.toString());

                            try {
                                String provider = response.getString("provider");
                                String url = response.getString("url");

                                String htmlContent = "<body style=\"display: flex;  justify-content: center;  align-items: center;\" >";
                                if (provider.contains("Vimeo"))
                                    htmlContent += String.format("<iframe src=\"https://player.vimeo.com/video/%s\" frameborder=\"0\" allow=\"autoplay; fullscreen\" allowfullscreen></iframe>", url);

                                webview.setWebViewClient(new WebViewClient());
                                webview.loadData(htmlContent, "text/html", "UTF-8");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.d(TAG, anError.toString());
                        }
                    });
        }
    }
}