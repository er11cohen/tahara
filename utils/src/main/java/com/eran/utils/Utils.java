package com.eran.utils;

import android.R.drawable;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;


public class Utils extends Activity {
    // FEFF because this is the Unicode char represented by the UTF-8 byte order mark (EF BB BF).
    private static final String UTF8_BOM = "\uFEFF";

    public static final String androidOS = Build.VERSION.RELEASE;
    public static final String phonemModel = Build.MODEL;
    public static Boolean utilFullScreen;
    static final String Location_Permission = "android.permission.ACCESS_COARSE_LOCATION";

    public static String getVersionName(Context myContext) {
        String versionName = "-1";
        try {
            versionName = myContext.getPackageManager().getPackageInfo(myContext.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return versionName;
    }

    public static String ReadTxtFile(String filePath, Context myContext) {
        String text = null;
        byte abyte0[];
        try {
            //InputStream inputstream = myContext.getAssets().open("files/"+fileName);
            InputStream inputstream = myContext.getAssets().open(filePath);
            abyte0 = new byte[inputstream.available()];
            inputstream.read(abyte0);
            inputstream.close();
            text = new String(abyte0, "utf-8");
            text = removeSpecialCharacters(text);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return text;
    }

    private static String removeSpecialCharacters(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }

    public static void alertDialogShow(final WeakReference<Activity> aReference, Context context, String title, int iconNumber, String textDialogUri,
                                       String btnPositibeText, String btnNegativeText, final String btnNegativeTextIntent) {
        final Activity activity = aReference.get();
        if (activity == null) {
            return;
        }

        String message = ReadTxtFile(textDialogUri, context);
        message = message.replace("version", androidOS).replace("model", phonemModel)
                .replace("myApp", getVersionName(context));

        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setIcon(iconNumber);
        alertDialog.setMessage(fromHtml(message));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, btnPositibeText, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, btnNegativeText, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                shareApp(aReference, btnNegativeTextIntent);
            }
        });


        alertDialog.show();

        // Make the textview clickable. Must be called after show()
        ((TextView) alertDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }


    public static void toggleFullScreen(final WeakReference<Activity> aReference, final Context context, int webView,
                                        final ActionBar actionBar, final boolean fullScreen) {
        final Activity activity = aReference.get();
        if (activity == null) {
            return;
        }

        utilFullScreen = fullScreen;

        final GestureDetector gs = new GestureDetector
                (context,//getBaseContext(),
                        new GestureDetector.SimpleOnGestureListener() {
                            @SuppressLint("NewApi")
                            @Override
                            public boolean onDoubleTap(MotionEvent e) {
                                //float x = e.getX(); float y = e.getY(); Toast.makeText( getBaseContext(),"Tapped at: (" + x + "," + y + ")",Toast.LENGTH_LONG).show();

                                if (!utilFullScreen) {
                                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                                    actionBar.hide();
                                } else {
                                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                                    actionBar.show();
                                }

                                //	fullScreen = !fullScreen;
                                utilFullScreen = !utilFullScreen;
                                return true;
                            }
                        }
                );

        WebView wv = (WebView) activity.findViewById(webView);
        wv.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gs.onTouchEvent(event);
                return false;
            }
        });
    }


    public static int readSize(SharedPreferences references) {
        int size = references.getInt("size", 20);
        return size;
    }

    private static void writeSize(int size, SharedPreferences references) {
        SharedPreferences.Editor editor = references.edit();
        editor.putInt("size", size);
        editor.apply();
    }

    public static void changeSize(boolean increase, SharedPreferences references, WebSettings wvSetting) {

        int size = readSize(references);
        if (increase) {
            size = size + 2;

        } else {
            size = size - 2;
        }

        wvSetting.setDefaultFontSize(size);
        writeSize(size, references);

    }

    public static void NightMode(Boolean change, SharedPreferences references, WebView wv, MenuItem nightModeItem) {
        boolean nightMode = references.getBoolean("nightMode", false);

        if (change) {
            nightMode = !nightMode;
            SharedPreferences.Editor editor = references.edit();
            editor.putBoolean("nightMode", nightMode);
            editor.commit();
        }

        if (nightMode) {
            loadJS(wv, "document.body.style.color='white';document.body.style.background = 'black';");
            if (nightModeItem != null) {
                nightModeItem.setTitle("ביטול מצב לילה");
            }

        } else if (change) {
            loadJS(wv, "document.body.style.color='black';document.body.style.background = 'white';"); //for kitkat and above
            nightModeItem.setTitle("מצב לילה");
        }

    }

    public static void loadJS(WebView wv, String jsStr) {
        wv.evaluateJavascript(jsStr, null);
    }

    public static void firstDoubleClickInfo(SharedPreferences references, final WeakReference<Activity> aReference) {

        final Activity activity = aReference.get();
        if (activity == null) {
            return;
        }

        boolean firstDoubleClickInfo = references.getBoolean("firstDoubleClickInfo", true);
        if (firstDoubleClickInfo) {
            SuperActivityToast superActivityToast = new SuperActivityToast(activity);
            superActivityToast.setText("כדי להיכנס ולצאת ממסך מלא ניתן להקליק הקלקה כפולה");
            superActivityToast.setDuration(10000);
            superActivityToast.setBackground(SuperToast.Background.RED);
            superActivityToast.setTextColor(Color.BLACK);
            superActivityToast.setTouchToDismiss(true);
            superActivityToast.setAnimations(SuperToast.Animations.SCALE);
            superActivityToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.RIGHT);
            superActivityToast.show();

            //Toast toast = Toast.makeText(WebActivity.this,"לחץ הקלקה כפולה כדי להיכנס למצב מלא", Toast.LENGTH_LONG);
            //toast.setGravity(Gravity.TOP, 0, 200);
            //toast.show();

            SharedPreferences.Editor editor = references.edit();
            editor.putBoolean("firstDoubleClickInfo", false);
            editor.apply();
        }
    }

    public static void showWebView(WebView wv, ProgressBar progressBar, Boolean show) {
        if (show) {
            //hide loading image
            progressBar.setVisibility(View.GONE);
            //show webview
            wv.setVisibility(View.VISIBLE);
        } else {
            //show loading image
            progressBar.setVisibility(View.VISIBLE);
            //hide webview
            wv.setVisibility(View.GONE);
        }
    }

    public static void setOpacity(WebView wv, double opacity) {
        String opacityStyle = "document.body.style.opacity='" + opacity + "'";
        loadJS(wv, opacityStyle);
    }

    public static boolean isFirstTimeAskingPermission(Activity activity, String permission) {
        SharedPreferences defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(activity.getBaseContext());
        return defaultSharedPreferences.getBoolean(permission, true);
    }

    public static void setRingerMode(Activity activity, int phoneStatus, int startRingerMode) {
        if (phoneStatus != -1 && startRingerMode != 0/*silent*/) {
            if (phoneStatus == 0) {
                requestForDoNotDisturbPermissionOrSetDoNotDisturb(activity);
            } else {
                AudioManager am = (AudioManager) activity.getBaseContext().getSystemService(Context.AUDIO_SERVICE);
                am.setRingerMode(phoneStatus);
            }
        }
    }

    private static void requestForDoNotDisturbPermissionOrSetDoNotDisturb(final Activity activity) {
        NotificationManager notificationManager = (NotificationManager) activity.getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager.isNotificationPolicyAccessGranted()) {
            moveToSilentMode(activity);
        } else {
            ((TextView) new AlertDialog.Builder(activity)
                    .setTitle("צדיק תן לנו הרשאה")
                    .setIcon(drawable.ic_menu_info_details)
                    .setIcon(drawable.ic_input_add)
                    .setMessage("צדיק ביקשת לעבור למצב שקט בעת הלימוד, אנא תן לנו הרשאה על מנת שנוכל לשנות זאת")
                    .setPositiveButton("מסכים ברור", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            // Open Setting screen to ask for permission
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            activity.startActivity(intent);
                        }
                    })
                    .setNegativeButton("לא כעת", null)
                    .setCancelable(false)
                    .show()
                    .findViewById(android.R.id.message))
                    .setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private static void moveToSilentMode(Activity activity) {
        AudioManager audioManager = (AudioManager) activity.getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    @SuppressLint("NewApi")
    public static Spanned fromHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        }

        return Html.fromHtml(html);
    }

    @SuppressLint("NewApi")
    public static File getFilePath(Context context) {
        return context.getExternalFilesDir(null);
    }

    public static void shareApp(final WeakReference<Activity> aReference, String shareTextIntent) {
        final Activity activity = aReference.get();
        if (activity == null) {
            return;
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareTextIntent);
        sendIntent.setType("text/plain");
        activity.startActivity(sendIntent);
    }

}
