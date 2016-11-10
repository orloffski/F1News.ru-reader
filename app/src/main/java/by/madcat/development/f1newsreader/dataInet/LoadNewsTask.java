package by.madcat.development.f1newsreader.dataInet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import by.madcat.development.f1newsreader.data.DatabaseDescription.*;

public class LoadNewsTask extends AsyncTask<Void, Void, ArrayList<String>> {

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
            newsData = loadNewsData(dataLink.get(0), NewsTypes.valueOf(dataLink.get(1)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newsData;
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        super.onPostExecute(strings);

        String selection = News.COLUMN_LINK_NEWS + "=?";
        String[] selectionArgs = new String[]{strings.get(3)};

        ContentValues contentValues = new ContentValues();
        contentValues.put(News.COLUMN_TITLE, strings.get(0));
        contentValues.put(News.COLUMN_NEWS, strings.get(1));
        contentValues.put(News.COLUMN_NEWS_TYPE, strings.get(2));
        contentValues.put(News.COLUMN_LINK_NEWS, strings.get(3));
        contentValues.put(News.COLUMN_DATE, strings.get(4));
        contentValues.put(News.COLUMN_IMAGE, strings.get(5));

        Uri newNewsUri = context.getContentResolver().insert(News.CONTENT_URI, contentValues);
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
        Elements news_date = jsDoc.getElementsByClass(InternetDataRouting.NEWS_DATE_PARSE);
        newsData.add(news_date.text());

        // news image
        Elements news_image_div = jsDoc.getElementsByClass(InternetDataRouting.NEWS_IMAGE_DIV_PARSE);
        if(news_image_div.first() != null) {
            Elements linkDivs = news_image_div.first().getElementsByTag(InternetDataRouting.NEWS_IMAGE_TAG_PARSE);
            String image = linkDivs.first().attr(InternetDataRouting.NEWS_IMAGE_LINK_ATTR_PARSE);
            newsData.add(image);
        }else
            newsData.add("");

        return newsData;
    }
}
