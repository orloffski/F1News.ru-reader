package by.madcat.development.f1newsreader.dataInet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import by.madcat.development.f1newsreader.data.DatabaseDescription.*;
import by.madcat.development.f1newsreader.data.F1NewsReaderDatabaseHelper;

public class LoadNewsTask extends AsyncTask<Void, Void, ArrayList<String>> {

    private static final String NEWS_PREFIX = "/news/";
    private static final String MEMUAR_PREFIX = "/memuar/";

    public static final String IMAGE_PATH = "F1NewsImages";

    private ArrayList<String> dataLink;
    private Context context;

    public LoadNewsTask(ArrayList<String> dataLink, Context context){
        this.dataLink = dataLink;
        this.context = context;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... voids) {
        ArrayList<String> newsData = new ArrayList<>();
        try {
            if(!checkIssetNewsLinkInDB(dataLink.get(0), NewsTypes.valueOf(dataLink.get(1))))
                return null;

            newsData = loadNewsData(dataLink.get(0), NewsTypes.valueOf(dataLink.get(1)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newsData;
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        super.onPostExecute(strings);

        if(strings == null)
            return;

        if(!checkNewsLinkInSection(strings.get(3), NewsTypes.valueOf(strings.get(2))))
            return;

        ContentValues contentValues = new ContentValues();
        contentValues.put(News.COLUMN_TITLE, strings.get(0));
        contentValues.put(News.COLUMN_NEWS, strings.get(1));
        contentValues.put(News.COLUMN_NEWS_TYPE, strings.get(2));
        contentValues.put(News.COLUMN_LINK_NEWS, strings.get(3));
        contentValues.put(News.COLUMN_DATE, strings.get(4));
        contentValues.put(News.COLUMN_IMAGE, strings.get(5));

        context.getContentResolver().insert(News.CONTENT_URI, contentValues);
    }

    public ArrayList<String> loadNewsData(String urlString, NewsTypes type) throws IOException {
        String line;
        StringBuilder doc = new StringBuilder();
        ArrayList<String> newsData = new ArrayList<>();

        URL url = new URL(urlString);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));

        while((line = reader.readLine()) != null)
            doc.append(line);

        org.jsoup.nodes.Document jsDoc = Jsoup.parse(doc.toString(), "UTF-8");

        // news title
        Elements news_title = jsDoc.getElementsByClass(InternetDataRouting.NEWS_TITLE_PARSE);
        newsData.add(news_title.text());

        // news body
        Elements news_body = jsDoc.getElementsByClass(InternetDataRouting.NEWS_BODY_PARSE);
        newsData.add(news_body.text());

        // news type
        newsData.add(type.toString());

        // news link
        newsData.add(urlString);

        // news date
        String dateTime = "";
        Elements news_date = jsDoc.getElementsByClass(InternetDataRouting.NEWS_DATE_PARSE);
        if(news_date.isEmpty()){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
            dateTime = simpleDateFormat.format(new Date());
        }else{
            dateTime = news_date.text();
        }
        dateTime = transformDateTime(dateTime);
        newsData.add(dateTime);

        // news image
        Elements news_image_div = jsDoc.getElementsByClass(InternetDataRouting.NEWS_IMAGE_DIV_PARSE);
        if(news_image_div.first() != null) {
            Elements linkDivs = news_image_div.first().getElementsByTag(InternetDataRouting.NEWS_IMAGE_TAG_PARSE);
            String image = linkDivs.first().attr(InternetDataRouting.NEWS_IMAGE_LINK_ATTR_PARSE);
            loadNewsImage(image);
            newsData.add(image.split("/")[image.split("/").length - 1]);
        }else
            newsData.add("");

        return newsData;
    }

    private String transformDateTime(String dateTime){
        StringBuilder builder = new StringBuilder();

        String[] date = dateTime.split(",", 0);

        String dateNumber = date[0].split(" ")[0];
        String monthNumber = date[0].split(" ")[1];
        String yearNumber = date[0].split(" ")[2];

        builder.append(yearNumber).append(".");

        if(getMonthNumber(monthNumber) + 1 < 10)
            builder.append("0");
        builder.append(getMonthNumber(monthNumber) + 1).append(".");

        if(dateNumber.length() < 2)
            builder.append("0");
        builder.append(dateNumber);

        builder.append(" ").append(date[1]);

        return builder.toString();
    }

    private static int getMonthNumber(String monthName){
        String[] months = new String[]{"января","февраля","марта",
                "апреля","мая","июня","июля","августа","сентября",
                "октября","ноября","декабря"};

        for(int i =0; i < months.length; i++)
            if(months[i].equals(monthName))
                return i;

        return -1;
    }

    private boolean checkNewsLinkInSection(String link, NewsTypes type){

        // check link to correct news section (delete news from other sections)
        String prefix = "";

        switch (type){
            case NEWS:
                prefix = NEWS_PREFIX;
                break;
            case MEMUAR:
                prefix = MEMUAR_PREFIX;
                break;
        }

        if(!link.contains(prefix))
            return false;

        return true;
    }

    private boolean checkIssetNewsLinkInDB(String link, NewsTypes type){
        // check link to issue news in DB
        String selection = News.COLUMN_LINK_NEWS + "=?";
        String[] selectionArgs = new String[]{link};

        F1NewsReaderDatabaseHelper helper = new F1NewsReaderDatabaseHelper(context);
        Cursor cursor = helper.getWritableDatabase().query(News.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        if(cursor.getCount() != 0) {
            cursor.close();
            helper.close();
            return false;
        }

        cursor.close();
        helper.close();
        return true;
    }

    private void loadNewsImage(String imageUrl) throws IOException {
        File sdPath = context.getFilesDir();

        sdPath = new File(sdPath.getAbsolutePath() + "/" + IMAGE_PATH);

        if(!sdPath.exists())
            sdPath.mkdirs();

        String filename = imageUrl.split("/")[imageUrl.split("/").length - 1];
        File imageOnMemory = new File(sdPath, filename);

        Bitmap image = null;

        OutputStream fOut = new FileOutputStream(imageOnMemory);

        InputStream in = new URL(imageUrl).openStream();
        image = BitmapFactory.decodeStream(in);
        image.compress(Bitmap.CompressFormat.JPEG, 55, fOut);

        fOut.flush();
        fOut.close();
        in.close();
    }
}
