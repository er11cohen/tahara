package com.eran.tahara;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;

import com.eran.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WebActivity extends Activity {

    WebView wv;
    ProgressBar progressBar;
    WebSettings wvSetting;
    int scrollY = 0;
    MenuItem nightModeItem = null;
    MenuItem PreviousMI = null;
    MenuItem NextMI = null;
    MenuItem paragraphMI = null;
    SharedPreferences taharaPreferences;
    SharedPreferences defaultSharedPreferences;
    boolean fullScreen = false;
    AudioManager am;
    String phoneStatus;
    int startRingerMode = 2;//RINGER_MODE_NORMAL

    String halachHe, href;
    int htmlPageIndex;
    boolean pageReady = false;
    ArrayList<Halach> arrHalach;
    GestureDetector gs = null;
    ActionBar actionBar = null;
    int activeMatch = 0;
    int totalMatch = 0;
    String currentQuery = "";
    int noResultCount = 0;
    SearchView searchView;
    Boolean isFirstOnPageFinished = true;
    String appName = "/Tahara";
    int lastPageIndex = 5;

    public enum Search {
        WITHOUT_SEARCH, PREVIOUS_SEARCH, NEXT_SEARCH
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        fullScreen = defaultSharedPreferences.getBoolean("CBFullScreen", false);
        setContentView(R.layout.activity_web);
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (fullScreen) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            actionBar.hide();
        }

        boolean keepScreenOn = defaultSharedPreferences.getBoolean("CBKeepScreenOn", false);
        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        am = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        phoneStatus = defaultSharedPreferences.getString("phone_status", "-1");

        taharaPreferences = getSharedPreferences("taharaPreferences", MODE_PRIVATE);
        wv = (WebView) findViewById(R.id.webView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        wvSetting = wv.getSettings();

        Intent intent = getIntent();
        String requiredFileName = intent.getStringExtra("requiredFileName");
        if (requiredFileName != null) {
            ArrayList<Halach> locationList;
            Gson gson = new Gson();

            SharedPreferences preferences = getSharedPreferences("Locations", MODE_PRIVATE);
            String preferencesLocationsJson = preferences.getString("preferencesLocationsJson", null);
            if (preferencesLocationsJson != null) {
                locationList = gson.fromJson(preferencesLocationsJson, new TypeToken<ArrayList<Halach>>() {
                }.getType());
                Halach requiredLocation = null;
                if (requiredFileName.equals("-1")/*lastLocation*/) {
                    int lastLocation = locationList.size() - 1;
                    requiredLocation = locationList.get(lastLocation);
                } else//History
                {
                    for (int i = locationList.size() - 1; i >= 0; i--) {
                        requiredLocation = locationList.get(i);
                        if (requiredLocation.getTime().equals(requiredFileName)) {
                            break;
                        }
                    }
                }

                if (requiredLocation != null) {
                    halachHe = requiredLocation.getHalachHe();
                    htmlPageIndex = requiredLocation.getHtmlPageIndex();
                    scrollY = requiredLocation.getScrollY();
                } else {
                    finish();//not need to arrive to here
                }
            } else {
                finish();//not need to arrive to here
            }
        } else {
            halachHe = intent.getStringExtra("halachHe");
            htmlPageIndex = intent.getIntExtra("htmlPageIndex", 1);
            href = intent.getStringExtra("href");
        }

        wvSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        wvSetting.setJavaScriptEnabled(true);
        LoadWebView(Search.WITHOUT_SEARCH);
        WeakReference<Activity> weakReferenceActivity = new WeakReference<Activity>(this);
        Utils.toggleFullScreen(weakReferenceActivity, getApplicationContext(), R.id.webView, actionBar, fullScreen);
        Utils.firstDoubleClickInfo(defaultSharedPreferences, weakReferenceActivity);
    }

    protected void onResume() {
        super.onResume();//Always call the superclass method first

        startRingerMode = am.getRingerMode();

        Utils.setRingerMode(this, Integer.parseInt(phoneStatus), startRingerMode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web, menu);
        nightModeItem = menu.findItem(R.id.nightMode);
        PreviousMI = menu.findItem(R.id.previous);
        NextMI = menu.findItem(R.id.next);
        paragraphMI = menu.findItem(R.id.select_paragraph);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {

            //Toast.makeText(getApplicationContext(),"onQueryTextChange "  ,Toast.LENGTH_LONG).show();
            menu.findItem(R.id.select_paragraph).setVisible(true);

            MenuItem searchItem = menu.findItem(R.id.menu_item_search);
            searchItem.setVisible(true);///////////////////////////
            searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("חיפוש בפרק הנוכחי");

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(new OnQueryTextListener() {

                @Override
                public boolean onQueryTextChange(String query) {
                    //Toast.makeText(getApplicationContext(),"onQueryTextChange " +query ,Toast.LENGTH_LONG).show();

                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    currentQuery = query;
                    noResultCount = 0;
                    scrollY = wv.getScrollY();//save the location before the search
                    find(currentQuery, Search.NEXT_SEARCH);
                    // TODO Auto-generated method stub
                    return true;
                }


            });

            searchView.setOnCloseListener(new OnCloseListener() {
                @Override
                public boolean onClose() {
                    closeSearch(true);
                    return false;
                }

            });

            searchView.setOnSearchClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    paragraphMI.setVisible(false);
                }
            });
        }


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.nightMode:
                Utils.NightMode(true, taharaPreferences, wv, nightModeItem);
                break;
            case R.id.zoomUp:
                Utils.changeSize(true, taharaPreferences, wvSetting);
                break;
            case R.id.zoomDown:
                Utils.changeSize(false, taharaPreferences, wvSetting);
                break;
            case R.id.previous:
                previous();
                break;
            case R.id.next:
                next();
                break;
            case R.id.select_paragraph:
                OnMenuCreated();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    private void find(String query, Search searchKind) {
        boolean firstFindResultReceived = true;
        final Search finalSearch[] = new Search[1];
        finalSearch[0] = searchKind;


        wv.findAllAsync(query);

        PreviousMI.setVisible(true);
        NextMI.setVisible(true);

    }

    private void previous() {
        wv.findNext(false);
    }

    private void next() {
        wv.findNext(true);
    }

    private void LoadWebView(final Search searchKind) {
        Utils.setOpacity(wv, 0.1);//for where the wv already exist

        // Toast.makeText(getApplicationContext(),""+htmlPageIndex, Toast.LENGTH_SHORT).show();
        wv.loadUrl("file:///android_asset/html/tahara_" + htmlPageIndex + ".html");

        int size = Utils.readSize(taharaPreferences);
        wvSetting.setDefaultFontSize(size);

        wv.setWebViewClient(new WebViewClient() {


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (searchKind == Search.WITHOUT_SEARCH) {
                    if (isFirstOnPageFinished) {
                        Utils.setOpacity(wv, 0.1);
                        Utils.showWebView(wv, progressBar, true);
                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            ChangeWebViewBySettings();
                            if (scrollY != 0) {
                                isFirstOnPageFinished = false;
                                wv.scrollTo(0, scrollY);
                            } else if (isFirstOnPageFinished) {
                                isFirstOnPageFinished = false;

                                //wv.loadUrl("javascript:window.location.hash = '';window.location.hash = '#" + href + "';");
                                Utils.loadJS(wv, "window.location.hash = '';window.location.hash = '#" + href + "';");
                            }

                            pageReady = true;
                            Utils.setOpacity(wv, 1);
                        }
                    }, 1100);
                } else//fromSearch
                {
                    ChangeWebViewBySettings();
                    find(currentQuery, searchKind);
                    Utils.setOpacity(wv, 1);
                }
            }
        });


    }

    private void ChangeWebViewBySettings() {
        Utils.NightMode(false, taharaPreferences, wv, nightModeItem);
    }


    @Override
    protected void onPause() {
        super.onPause();  //Always call the superclass method first

        if (!phoneStatus.equals("-1") && startRingerMode != 0/*silent*/) {
            am.setRingerMode(startRingerMode);
        }

        File path = Utils.getFilePath(getApplicationContext());
        File folder = new File(path + appName);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }

        if (success && pageReady) {
            SharedPreferences preferences = getSharedPreferences("Locations", MODE_PRIVATE);
            String preferencesLocationsJson = preferences.getString("preferencesLocationsJson", null);

            if (preferencesLocationsJson == null)//for second install, remove the old files
            {
                if (folder.isDirectory()) {
                    String[] children = folder.list();
                    for (int i = 0; i < children.length; i++) {
                        new File(folder, children[i]).delete();
                    }
                }
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String currentTime = sdf.format(new Date());

            View content = findViewById(R.id.layout);
            content.setDrawingCacheEnabled(true);
            Bitmap bitmap = content.getDrawingCache();
            File file = new File(path + appName + "/" + currentTime + ".png");
            ArrayList<Halach> locationList = new ArrayList<Halach>();
            Gson gson = new Gson();
            try {
                file.createNewFile();
                FileOutputStream ostream = new FileOutputStream(file);
                bitmap.compress(CompressFormat.PNG, 100, ostream);
                ostream.close();

                scrollY = wv.getScrollY();
                Halach location = new Halach(currentTime, scrollY, halachHe, htmlPageIndex);


                if (preferencesLocationsJson != null) {
                    locationList = gson.fromJson(preferencesLocationsJson, new TypeToken<ArrayList<Halach>>() {
                    }.getType());
                    if (locationList.size() >= 10) {
                        String idFirstLocation = locationList.get(0).getTime();
                        File imageToDelete = new File(path + appName + "/" + idFirstLocation + ".png");
                        if (imageToDelete.exists()) {
                            boolean deleted = imageToDelete.delete();
                        }

                        locationList.remove(0);
                    }
                }


                locationList.add(location);

                String json = gson.toJson(locationList);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("preferencesLocationsJson", json);
                editor.commit();

            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

    }


    private void OnMenuCreated() {
        scrollY = 0;//in order to prevent jump to last location
        arrHalach = UtilTahara.getHalachArray(getApplicationContext());
        List<String> simanimList = new ArrayList<>();

        final ArrayList<Integer> simanimIndexArr = new ArrayList<Integer>();

        for (int i = 0; i < arrHalach.size(); i++) {
            if (arrHalach.get(i).getHtmlPageIndex() == htmlPageIndex) {
                simanimList.add(arrHalach.get(i).getHalachHe());
                simanimIndexArr.add(i);
            }
        }

        String[] simanim = simanimList.toArray(new String[simanimList.size()]);

        new AlertDialog.Builder(this)
                .setTitle("בחרי סימן")
                .setItems(simanim,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                                int halachIndex = simanimIndexArr.get(i);
                                Halach halach = arrHalach.get(halachIndex);
                                OnMenuSelected(halach);
                            }
                        })
                .show();
    }

    private void OnMenuSelected(Halach halach) {
        String hash = halach.getHref();
        Utils.loadJS(wv, "window.location.hash = '';window.location.hash = '#" + hash + "';");
    }

    @Override
    public void onBackPressed() {

        wv.clearFocus();//for close pop-up of copy, select etc.

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            if (!searchView.isIconified()) {
                closeSearch(false);
                return;
            }
        }

        super.onBackPressed();
    }

    //for voice search
    @SuppressLint("NewApi")
    @Override
    protected void onNewIntent(Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                String query = intent.getStringExtra(SearchManager.QUERY);
                searchView.setQuery(query, true);

                //close the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private void closeSearch(Boolean fromListener) {
        paragraphMI.setVisible(true);
        wv.clearMatches();//clear the finds
        PreviousMI.setVisible(false);
        NextMI.setVisible(false);

        if (!fromListener) {// the listener do this by himself
            searchView.setIconified(true);// clear the searchView
            searchView.onActionViewCollapsed();//close the searchView
        }
    }
}


