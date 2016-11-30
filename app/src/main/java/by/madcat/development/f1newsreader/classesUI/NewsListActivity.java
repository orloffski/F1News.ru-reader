package by.madcat.development.f1newsreader.classesUI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.analytics.ExceptionReporter;

import java.util.ArrayList;

import by.madcat.development.f1newsreader.AnalyticsTrackers.AnalyticsTrackers;
import by.madcat.development.f1newsreader.Interfaces.NewsOpenListener;
import by.madcat.development.f1newsreader.R;
import by.madcat.development.f1newsreader.data.DatabaseDescription.*;

public class NewsListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        NewsOpenListener {
    public static final String LIST_FRAGMENT_NAME = "list_fragment";

    private int sectionItemsCount;
    private ArrayList<String> links;

    private NewsListFragment fragment;
    private NavigationView navigationView;

    public static Intent newIntent(Context context){
        return new Intent(context, NewsListActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setUncaughtExceptionHandler();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(savedInstanceState == null)
            openSectionNews(NewsTypes.NEWS);
        else{
            fragment = (NewsListFragment) getSupportFragmentManager().findFragmentById(R.id.content_news_list);
            NewsTypes type = NewsTypes.valueOf(fragment.getArguments().getString(fragment.NEWS_TYPE));
            setActivityTitle(type);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

        NewsTypes type = ((NewsListFragment)getSupportFragmentManager().findFragmentByTag(LIST_FRAGMENT_NAME)).getNewsType();
        updateOnBackPressed(type);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_news:
                openSectionNews(NewsTypes.NEWS);
                break;
            case R.id.nav_memuar:
                openSectionNews(NewsTypes.MEMUAR);
                break;
            case R.id.nav_interview:
                openSectionNews(NewsTypes.INTERVIEW);
                break;
            case R.id.nav_tech:
                openSectionNews(NewsTypes.TECH);
                break;
            case R.id.nav_history:
                openSectionNews(NewsTypes.HISTORY);
                break;
            case R.id.nav_columns:
                openSectionNews(NewsTypes.COLUMNS);
                break;
            case R.id.nav_autosport:
                openSectionNews(NewsTypes.AUTOSPORT);
                break;
            case R.id.nav_settings:
                openSettings();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openSectionNews(NewsTypes type){
        fragment = NewsListFragment.newInstance(type);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_news_list, fragment, LIST_FRAGMENT_NAME).commit();

        setActivityTitle(type);
    }

    private void openSettings(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_news_list, new PreferencesFragment());
        transaction.addToBackStack(null);
        transaction.commit();

        setTitle(getString(R.string.settings_title));
    }

    private void setActivityTitle(NewsTypes type){
        String title = null;
        switch (type){
            case NEWS:
                title = getString(R.string.nav_news_title);
                break;
            case MEMUAR:
                title = getString(R.string.nav_memuar_title);
                break;
            case TECH:
                title = getString(R.string.nav_tech_title);
                break;
            case HISTORY:
                title = getString(R.string.nav_history_title);
                break;
            case COLUMNS:
                title = getString(R.string.nav_columns_title);
                break;
            case AUTOSPORT:
                title = getString(R.string.nav_autosport_title);
                break;
            case INTERVIEW:
                title = getString(R.string.nav_interview_title);
                break;
        }
        setTitle(title);
    }

    @Override
    public void sectionItemOpen(NewsTypes type, int positionID) {
        Intent intent = NewsPageActivity.getIntent(NewsListActivity.this, type, positionID, sectionItemsCount, links);
        startActivity(intent);
    }

    @Override
    public void setSectionItemsCount(int count) {
        this.sectionItemsCount = count;
    }

    @Override
    public void setSectionNewsLinks(ArrayList<String> links) {
        this.links = links;
    }

    private void updateOnBackPressed(NewsTypes type){
        setActivityTitle(type);

        switch (type){
            case NEWS:
                navigationView.setCheckedItem(R.id.nav_news);
                break;
            case MEMUAR:
                navigationView.setCheckedItem(R.id.nav_memuar);
                break;
            case INTERVIEW:
                navigationView.setCheckedItem(R.id.nav_interview);
                break;
            case TECH:
                navigationView.setCheckedItem(R.id.nav_tech);
                break;
            case HISTORY:
                navigationView.setCheckedItem(R.id.nav_history);
                break;
            case COLUMNS:
                navigationView.setCheckedItem(R.id.nav_columns);
                break;
            case AUTOSPORT:
                navigationView.setCheckedItem(R.id.nav_autosport);
                break;
        }
    }

    private void setUncaughtExceptionHandler(){
        Thread.UncaughtExceptionHandler myHandler = new ExceptionReporter(
                ((AnalyticsTrackers)getApplication()).getDefaultTracker(),
                Thread.getDefaultUncaughtExceptionHandler(),
                getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(myHandler);
    }
}
