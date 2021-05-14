package com.nanasoft.olmaa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    WebView webView;
    GifImageView loadingVimeo;

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

        // Full Screen
        hideSystemUI();

        // Initialize Android Networking
        AndroidNetworking.initialize(getApplicationContext());


        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webView);
        loadingVimeo = (GifImageView) findViewById(R.id.vimeoLoading);

        if(savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        }
        else {
            webView.getSettings().setJavaScriptEnabled(true);

            // Get params from the URI
            Intent intent = getIntent();
            String action = intent.getAction();
            Uri data = intent.getData();

            Log.d(TAG, action);
            if (data != null) {
                String user = data.getQueryParameter("user");
                String content = data.getQueryParameter("class");
                String uri = "http://api.lmsiq.com/api/v1/contentWithVideo";

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

                                    if (provider.contains("Vimeo")) {
                                        htmlContent +=
                                                "<iframe src=\"https://player.vimeo.com/video/" + url + "?badge=0&amp;autopause=0&amp;player_id=0&amp;app_id=213858\" frameborder=\"0\" allow=\"autoplay; fullscreen; picture-in-picture\" allowfullscreen style=\"position:absolute;top:0;left:0;width:100%;height:100%;\" title=\"1.mp4\">\n</iframe>\n <script src=\"https://player.vimeo.com/api/player.js\"></script>";
                                    } else if (provider.contains("HTML5") || provider.contains("file"))
                                        htmlContent += String.format("<video width=\"100%%\" controls playsinline id=\"player\" class=\"html-video-frame\" src=\"%s\" type=\"video/mp4\"></video>", url);

                                    htmlContent += String.format("<marquee width=\"100%%\" behavior=\"scroll\" direction=\"right\" scrollamount=\"5\" style=\"top: 20%%; position: absolute; color: gray; font-size: 40px; opacity:0.2;\">%s</marquee>", user);
                                    htmlContent += String.format("<marquee width=\"100%%\" behavior=\"scroll\" direction=\"left\" scrollamount=\"5\" style=\"top: 35%%; position: absolute; color: gray; font-size: 40px; opacity:0.2;\">%s</marquee>", user);
                                    htmlContent += String.format("<marquee width=\"100%%\" behavior=\"scroll\" direction=\"right\" scrollamount=\"5\" style=\"top: 50%%; position: absolute; color: gray; font-size: 40px; opacity:0.2;\">%s</marquee>", user);
                                    htmlContent += String.format("<marquee width=\"100%%\" behavior=\"scroll\" direction=\"left\" scrollamount=\"5\" style=\"top: 65%%; position: absolute; color: gray; font-size: 40px; opacity:0.2;\">%s</marquee>", user);
                                    htmlContent += "</body>";


                                    webView.getSettings().setJavaScriptEnabled(true);
                                    webView.setWebViewClient(new WebViewClient(){
                                        public void onPageFinished(WebView view, String url) {
                                            loadingVimeo.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                    webView.loadData(htmlContent, "text/html", "UTF-8");

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

    @Override
    protected void onSaveInstanceState(Bundle outState ){
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    public void hideSystemUI(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}