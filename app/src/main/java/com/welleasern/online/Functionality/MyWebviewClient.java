package com.welleasern.online.Functionality;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.browser.customtabs.CustomTabsIntent;

/**
 * Author: Junbong Jang
 * Date: 5/5/2019
 *
 * Prevents the webview from going to urls not owned by WellEastern Inc.
 */
public class MyWebviewClient extends WebViewClient{

    Context mContext;

    private final String[] VALID_HOST_LIST = {
            "www.welleastern.co.kr",
            "www.easternschool.co.kr",
            "www.welleastern.cn",
            "www.welleastern.com",
            "www.upenglish.co.kr",
            "stsp.welleastern.co.kr",
            "voca.welleastern.co.kr",
            "cvst.welleastern.co.kr"
    };

    public MyWebviewClient(Context c) {
        this.mContext = c;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.i("Override Url", url);
        Uri uri_obj = Uri.parse(url);
        if (this.checkValidHost(uri_obj.getHost())) {  // If Url is in my domain, do not override and let my WebView load the url

            // check if it is a speaking exercise
            if (url.indexOf("/IMENTOR/cn/speaking") > 0) {
                String new_url =  "https://www.welleastern.co.kr/IMENTOR/cn/speaking_android/index.php?" + uri_obj.getQuery();
                view.loadUrl(new_url);
            }
            else if (url.indexOf("/IMENTOR/speaking") > 0) {
                String new_url =  "https://www.welleastern.co.kr/IMENTOR/speaking_android/index.php?" + uri_obj.getQuery();
                view.loadUrl(new_url);
            }

//            else if (url.indexOf("/newSay/sub_study") > 0) {
//                int an_index = url.indexOf("/newSay/sub_study") + url.length();
//                String study_num = url.substring(an_index, an_index+1);
//                String new_url =  "https://cvst.welleastern.co.kr/newSay/sub_study" + study_num + ".html?" + uri_obj.getQuery();
//
//                view.loadUrl(new_url);
//            }
            // Note that moving the webview to custom tabs disable speech recognition features.
            else if (url.indexOf("www.welleastern.co.kr/IMENTOR/") == -1 &&
                    url.indexOf("www.welleastern.co.kr/user/") == -1 &&
                    url.indexOf("www.welleastern.co.kr/onacademy/") == -1 &&
                    url.indexOf("cvst.welleastern.co.kr/newHello/") == -1 &&
                    url.indexOf("cvst.welleastern.co.kr/newSay/") == -1 &&
                    url.indexOf("www.welleastern.co.kr/mypage/") == -1) {
                moveToCustomTabs(uri_obj);
                return true;
            }

            return false;
        } else { // don't allow moving outside the Welleastern website domain
            Toast.makeText(mContext, "Requested URL " + url + " is outside of our domain.", Toast.LENGTH_LONG).show();
            return true;
        }
    }

    public void onPageFinished(WebView view, String url){
//        view.loadUrl("javascript:recognizedTextResult('onPageFinished')");
    }

    private void moveToCustomTabs(Uri parsed_url) {
        Log.i("moveToCustomTabs", "");
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
//        customTabsIntent.intent.setPackage("com.android.chrome");
        customTabsIntent.launchUrl(this.mContext, parsed_url);
    }

    private boolean checkValidHost(String host_url) {
        boolean valid_host_bool = false;
        for (String valid_host_url : VALID_HOST_LIST) {
            if (host_url.equals(valid_host_url)) {
                valid_host_bool = true;
                break;
            }
        }
        return valid_host_bool;
    }
}
