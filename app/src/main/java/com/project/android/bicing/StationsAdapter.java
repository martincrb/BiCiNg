package com.project.android.bicing;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.android.bicing.data.BicingContract;

/**
 * Created by Martin on 10/06/2015.
 */
public class StationsAdapter extends CursorAdapter {

    private Context mContext;

    public StationsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags); mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        Integer[] fav_stations;
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_station, viewGroup, false);
        StationsViewHolder viewHolder = new StationsViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        StationsViewHolder viewHolder = (StationsViewHolder) view.getTag();
        String street = cursor.getString(cursor.getColumnIndex(BicingContract.Station.COLUMN_STREET));
        String streetNumber = cursor.getString(cursor.getColumnIndex(BicingContract.Station.COLUMN_STREETNUMBER));
        String streetNumberContent = String.format(context.getString(R.string.format_streetnumber),
                streetNumber);
        viewHolder.streetNumberView.setText(streetNumberContent);
        viewHolder.streetView.setText(Utils.latin2utf(street));
        String slots = cursor.getString(cursor.getColumnIndex(BicingContract.Station.COLUMN_SLOTS));
        String slotsContent = String.format(context.getString(R.string.format_slots),
                slots);
        viewHolder.slotsView.setText(slotsContent);
        String bikes = cursor.getString(cursor.getColumnIndex(BicingContract.Station.COLUMN_BIKES));
        String textBikes = bikes;
        viewHolder.bikesView.setText(textBikes);
        int bikesint = Integer.parseInt(textBikes);
        if (bikesint > 10) {
            viewHolder.bikesView.setTextColor(Color.parseColor("#82cc00"));
            viewHolder.iconView.setImageResource(R.drawable.ic_bicing_ok);
        }
        else if (bikesint > 4) {
            viewHolder.bikesView.setTextColor(Color.parseColor("#dba000"));
            viewHolder.iconView.setImageResource(R.drawable.ic_bicing_mid);
        }
        else {
            viewHolder.bikesView.setTextColor(Color.parseColor("#ce0000"));
            viewHolder.iconView.setImageResource(R.drawable.ic_bicing_bad);
        }
    }

    public static class StationsViewHolder {
        //public final ImageView iconView;
        public final TextView streetView;
        public final TextView slotsView;
        public final TextView bikesView;
        public final TextView streetNumberView;
        public final ImageView iconView;

        public StationsViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.bike_icon);
            streetView = (TextView) view.findViewById(R.id.list_item_street_textview);
            slotsView = (TextView) view.findViewById(R.id.list_item_slots_textview);
            bikesView = (TextView) view.findViewById(R.id.list_item_bikes_textview);
            streetNumberView = (TextView) view.findViewById(R.id.list_item_streetnumber_textview);
        }

    }
}
