package by.madcat.development.f1newsreader.Services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import by.madcat.development.f1newsreader.Interfaces.NewsLoadSender;
import by.madcat.development.f1newsreader.R;
import by.madcat.development.f1newsreader.Utils.SystemUtils;
import by.madcat.development.f1newsreader.classesUI.NewsListActivity;
import by.madcat.development.f1newsreader.dataInet.InternetDataRouting;
import by.madcat.development.f1newsreader.dataInet.LoadLinkListTask;

public class BackgroundLoadNewsService extends IntentService implements NewsLoadSender{
    private static final String TAG = "BackgroundLoadNewsService";

    private int countNewsToLoad;
    private int countNewsLoaded;

    public static Intent newIntent(Context context){
        return new Intent(context, BackgroundLoadNewsService.class);
    }

    public BackgroundLoadNewsService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(!SystemUtils.getUiLoadFlag(getApplicationContext())) {
            SystemUtils.setBgLoadFlag(getApplicationContext(), true);

            runLoad();
        }
    }

    public static void setServiceAlarm(Context context, boolean isOn, int timePause){
        Intent i = BackgroundLoadNewsService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        if(isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), timePause, pi);
        }
        else{
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }

    private void sendNotification(int countNews){
        Resources resources = getResources();
        Intent i = NewsListActivity.newIntent(this);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        Resources res = getApplicationContext().getResources();

        Notification notification = new NotificationCompat.Builder(this)
                .setTicker(resources.getString(R.string.get_new_news))
                .setSmallIcon(R.drawable.ic_notif_logo)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setNumber(countNews)
                .setContentTitle(resources.getString(R.string.get_new_news))
                .setContentText(resources.getQuantityString(R.plurals.news_plurals, countNews, countNews))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, notification);
    }

    @Override
    public void checkNewsLoadCount(boolean loaded) {
        if(loaded)
            this.countNewsLoaded += 1;

        if(this.countNewsLoaded == this.countNewsToLoad)
            loadComplete();
    }

    @Override
    public void sendNewsLoadCount(int count) {
        if(count == 0){
            this.countNewsToLoad = 0;
            this.countNewsLoaded = 0;
        }else {
            this.countNewsToLoad = count;
        }
    }

    @Override
    public void cancelLinkLoad() {
        this.countNewsToLoad--;
        checkNewsLoadCount(false);
    }

    @Override
    public void loadStart() {
    }

    @Override
    public void loadComplete() {
        if(this.countNewsToLoad == 0)
            return;

        sendNotification(countNewsToLoad);

        SystemUtils.setBgLoadFlag(getApplicationContext(), false);
    }

    @Override
    public void loadCanceled() {

    }

    private void runLoad(){
        if(!isNetworkAvailableAndConnected())
            return;

        InternetDataRouting dataRouting = InternetDataRouting.getInstance();
        LoadLinkListTask loadLinksTask = new LoadLinkListTask(dataRouting.getRoutingMap(), dataRouting.getMainSiteAdress(), getApplicationContext(), this);
        loadLinksTask.execute();
    }
}
