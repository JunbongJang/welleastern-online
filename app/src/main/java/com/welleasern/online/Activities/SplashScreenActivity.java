package com.welleasern.online.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.welleasern.online.R;

import java.util.Locale;

/**
 * Author: Junbong Jang
 * Date: 5/23/2019
 *
 * Activity that displays the splash screen briefly when app first loads
 * It decides which webview activity to load based on the user's locale.
 */
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gotoWebviewActivity();
    }

    public void gotoWebviewActivity() {
        Intent i = new Intent(this, ZhWebviewActivity.class);
        startActivity(i);

//        if (getCurrentLocale(this).getLanguage().toLowerCase().equals("zh")) {
//            i = new Intent(this, ZhWebviewActivity.class);
//        } else {
//            i = new Intent(this, KoWebviewActivity.class);
//        }
    }

    Locale getCurrentLocale(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }
}
