package by.madcat.development.f1newsreader;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import by.madcat.development.f1newsreader.data.DatabaseDescription.*;

public class NewsPageAdapter extends FragmentStatePagerAdapter {

    private int count;
    private NewsTypes type;
    private Uri newsUri;
    private ArrayList<String> links;

    public NewsPageAdapter(FragmentManager fm, int itemsCount, NewsTypes type, ArrayList<String> links) {
        super(fm);
        this.type = type;
        this.count = itemsCount;
        this.links = links;
    }

    @Override
    public Fragment getItem(int position) {
        newsUri = News.buildNewsUri(Long.parseLong(links.get(position)));

        return NewsPageFragment.newInstance(newsUri);
    }

    @Override
    public int getCount() {
        return count;
    }
}