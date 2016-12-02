package by.madcat.development.f1newsreader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import by.madcat.development.f1newsreader.R;

public class NewsCardsAdapter extends NewsListAbstractAdapter {

    private final NewsListAbstractAdapter.ClickListener clickListener;

    public NewsCardsAdapter(ClickListener clickListener, Context context) {
        this.context = context;
        this.clickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_cards_card, parent, false);

        return new ViewHolder(view, clickListener);
    }
}
