package by.madcat.development.f1newsreader.classesUI;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.ExceptionReporter;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.util.ArrayList;

import by.madcat.development.f1newsreader.AnalyticsTrackers.AnalyticsTrackers;
import by.madcat.development.f1newsreader.Interfaces.NewsOpenListener;
import by.madcat.development.f1newsreader.R;
import by.madcat.development.f1newsreader.Services.BackgroundLoadNewsService;
import by.madcat.development.f1newsreader.Services.ReminderService;
import by.madcat.development.f1newsreader.Utils.PreferencesUtils;
import by.madcat.development.f1newsreader.data.DatabaseDescription.NewsTypes;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;

public class NewsListActivity extends AppCompatActivity
        implements NewsOpenListener{
    public static final String LIST_FRAGMENT_NAME = "list_fragment";
    public static final String SETTINGS_FRAGMENT_NAME = "settings_fragment";

    private CoordinatorLayout coordinatorLayout;

    private int sectionItemsCount;
    private ArrayList<String> newsIDs;
    private ArrayList<String> newsLinks;
    private NewsTypes nowType;

    private NewsListFragment fragment;
    private NavigationView navigationView;

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private ImageView imageView;

    private MaterialSearchView searchView;
    private Menu searchMenu;

    private TextView timerText;
    private TextView timer;

    public static Intent newIntent(Context context){
        return new Intent(context, NewsListActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUncaughtExceptionHandler();
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_news_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar_list);
        setSupportActionBar(toolbar);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout_list);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedTextAppearance);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedTextAppearance);

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout_list);

        imageView = (ImageView) findViewById(R.id.toolbar_image_list);

        loadTimerViews();

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // on full text search
                openSectionNews(nowType, query);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // on search text changed
                openSectionNews(nowType, newText);
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener(){
            @Override
            public void onSearchViewShown() {
                openSectionNews(nowType, "");
            }

            @Override
            public void onSearchViewClosed() {
                openSectionNews(nowType, null);
            }
        });

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();

//        navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);

        if(savedInstanceState == null) {
            nowType = NewsTypes.NEWS;
            openSectionNews(nowType, null);
        }else{
            Fragment reopenFragment = getSupportFragmentManager().findFragmentById(R.id.content_news_list);
            if(reopenFragment.getClass().equals(NewsListFragment.class)){
                fragment = (NewsListFragment) getSupportFragmentManager().findFragmentById(R.id.content_news_list);
                NewsTypes type = NewsTypes.valueOf(fragment.getArguments().getString(fragment.NEWS_TYPE));

                if (type == null || type.toString().equals("")) {
                    openSectionNews(NewsTypes.NEWS, null);
                } else {
                    setActivityTitle(type);
                }

                nowType = type;
            }else{
                nowType = NewsTypes.SETTINGS;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        clearNotifications(BackgroundLoadNewsService.NOTIFICATION_ID);
        clearNotifications(ReminderService.NOTIFICATION_ID);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Fragment reopenFragment = getSupportFragmentManager().findFragmentById(R.id.content_news_list);
        if(reopenFragment.getClass().equals(NewsListFragment.class)){
            nowType = NewsTypes.valueOf(fragment.getArguments().getString(fragment.NEWS_TYPE));
            if (nowType == null || nowType.toString().equals("")) {
                openSectionNews(NewsTypes.NEWS, null);
            } else {
                openSectionNews(nowType, null);
            }
        }else{
            nowType = NewsTypes.SETTINGS;
            openSettings(false);
        }
    }

//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (searchView.isSearchOpen()) {
//            searchView.closeSearch();
//        } else if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//
//            if(searchMenu != null)
//                searchMenu.findItem(R.id.action_search).setVisible(true);
//        }
//
//        NewsTypes type;
//
//        Fragment reopenFragment = getSupportFragmentManager().findFragmentById(R.id.content_news_list);
//        if(reopenFragment.getClass().equals(NewsListFragment.class)){
//            type = ((NewsListFragment)getSupportFragmentManager().findFragmentByTag(LIST_FRAGMENT_NAME)).getNewsType();
//            if(type == null || type.toString().equals("")) {
//                type = NewsTypes.NEWS;
//            }
//        }else{
//            type = NewsTypes.SETTINGS;
//        }
//
//        if(getSupportFragmentManager().findFragmentByTag(SETTINGS_FRAGMENT_NAME) != null){
//            getSupportFragmentManager().beginTransaction().remove(
//                    getSupportFragmentManager().findFragmentByTag(SETTINGS_FRAGMENT_NAME)).commit();
//        }
//
//        updateOnBackPressed(type);
//    }

//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//
//        switch (item.getItemId()){
//            case R.id.nav_news:
//                openSectionNews(NewsTypes.NEWS, null);
//                break;
//            case R.id.nav_memuar:
//                openSectionNews(NewsTypes.MEMUAR, null);
//                break;
//            case R.id.nav_interview:
//                openSectionNews(NewsTypes.INTERVIEW, null);
//                break;
//            case R.id.nav_tech:
//                openSectionNews(NewsTypes.TECH, null);
//                break;
//            case R.id.nav_history:
//                openSectionNews(NewsTypes.HISTORY, null);
//                break;
//            case R.id.nav_columns:
//                openSectionNews(NewsTypes.COLUMNS, null);
//                break;
//            case R.id.nav_autosport:
//                openSectionNews(NewsTypes.AUTOSPORT, null);
//                break;
//            case R.id.nav_settings:
//                openSettings(true);
//                break;
//        }
//
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main_menu, menu);
//
//        MenuItem item = menu.findItem(R.id.action_search);
//        searchView.setMenuItem(item);
//
//        searchMenu = menu;
//
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        switch (id){
//            case R.id.weekend_menu:
//                String timerGpTitle = PreferencesUtils.getNextGpCountry(this);
//                String weekendTitle = PreferencesUtils.getWeekendTitle(this);
//
////                if(timerGpTitle.toUpperCase().equals(weekendTitle.toUpperCase())){
//                if(searchMenu != null)
//                    searchMenu.findItem(R.id.action_search).setVisible(false);
//
//                appBarLayout.setExpanded(false);
//
//                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                transaction.replace(R.id.content_news_list, new WeekendInfoFragment());
//                transaction.addToBackStack(null);
//                transaction.commit();
//
//                collapsingToolbarLayout.setTitle(weekendTitle);
////                }
//                return true;
//            case R.id.online_menu:
//                Intent textIntent = new Intent(this, OnlineActivity.class);
//                startActivity(textIntent);
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void openSectionNews(NewsTypes type, String searchQuery){
        if(searchMenu != null)
            searchMenu.findItem(R.id.action_search).setVisible(true);

        appBarLayout.setExpanded(true);
        nowType = type;

        fragment = NewsListFragment.newInstance(type.toString(), searchQuery);
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(TRANSIT_FRAGMENT_FADE)
                .replace(R.id.content_news_list, fragment, LIST_FRAGMENT_NAME)
                .commit();

        setActivityTitle(type);
        searchQuery = null;
    }

    private void openSettings(boolean isNeedToBackStack){
        nowType = NewsTypes.SETTINGS;
        if(searchMenu != null)
            searchMenu.findItem(R.id.action_search).setVisible(false);

        appBarLayout.setExpanded(false);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setTransition(TRANSIT_FRAGMENT_FADE)
                .replace(R.id.content_news_list, new PreferencesFragment(), SETTINGS_FRAGMENT_NAME);
        if(isNeedToBackStack)
            transaction.addToBackStack(null);
        transaction.commit();

        collapsingToolbarLayout.setTitle(getString(R.string.settings_title));
    }

    private void setActivityTitle(NewsTypes type){
        String title = null;
        Drawable image = null;
        switch (type){
            case NEWS:
                title = getString(R.string.nav_news_title);
                image = getResources().getDrawable(R.drawable.drawerimage_news);
                break;
            case MEMUAR:
                title = getString(R.string.nav_memuar_title);
                image = getResources().getDrawable(R.drawable.drawerimage_memuar);
                break;
            case TECH:
                title = getString(R.string.nav_tech_title);
                image = getResources().getDrawable(R.drawable.drawerimage_tech);
                break;
            case HISTORY:
                title = getString(R.string.nav_history_title);
                image = getResources().getDrawable(R.drawable.drawerimage_history);
                break;
            case COLUMNS:
                title = getString(R.string.nav_columns_title);
                image = getResources().getDrawable(R.drawable.drawerimage_columns);
                break;
            case AUTOSPORT:
                title = getString(R.string.nav_autosport_title);
                image = getResources().getDrawable(R.drawable.drawerimage_autosport);
                break;
            case INTERVIEW:
                title = getString(R.string.nav_interview_title);
                image = getResources().getDrawable(R.drawable.drawerimage_interview);
                break;
        }

        if(!type.equals(NewsTypes.SETTINGS)) {
            collapsingToolbarLayout.setTitle(title);
            imageView.setImageBitmap(((BitmapDrawable) image).getBitmap());
        }
    }

    @Override
    public void sectionItemOpen(NewsTypes type, int positionID) {
        Intent intent = NewsPageActivity.getIntent(NewsListActivity.this, type, positionID, sectionItemsCount, newsIDs, newsLinks);
        startActivity(intent);
    }

//    private void updateOnBackPressed(NewsTypes type){
//        if(!type.equals(NewsTypes.SETTINGS))
//            setActivityTitle(type);
//
//        switch (type){
//            case NEWS:
//                navigationView.setCheckedItem(R.id.nav_news);
//                break;
//            case MEMUAR:
//                navigationView.setCheckedItem(R.id.nav_memuar);
//                break;
//            case INTERVIEW:
//                navigationView.setCheckedItem(R.id.nav_interview);
//                break;
//            case TECH:
//                navigationView.setCheckedItem(R.id.nav_tech);
//                break;
//            case HISTORY:
//                navigationView.setCheckedItem(R.id.nav_history);
//                break;
//            case COLUMNS:
//                navigationView.setCheckedItem(R.id.nav_columns);
//                break;
//            case AUTOSPORT:
//                navigationView.setCheckedItem(R.id.nav_autosport);
//                break;
//        }
//    }

    private void setUncaughtExceptionHandler(){
        Thread.UncaughtExceptionHandler myHandler = new ExceptionReporter(
                ((AnalyticsTrackers)getApplication()).getDefaultTracker(),
                Thread.getDefaultUncaughtExceptionHandler(),
                getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(myHandler);
    }

    public View getTimerTextLink(){
        return timerText;
    }

    public View getTimerLink(){
        return timer;
    }

    private void loadTimerViews(){
        timerText = (TextView) findViewById(R.id.timerText);
        timer = (TextView) findViewById(R.id.timer);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            PreferencesFragment pref_fragment = (PreferencesFragment)getSupportFragmentManager().findFragmentById(R.id.content_news_list);
            pref_fragment.updateReminderRingtone(data);
        }
    }

    public CoordinatorLayout getCoordinatorLayout(){
        return this.coordinatorLayout;
    }

    private void clearNotifications(int notificationsId){
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationsId);
    }
}
