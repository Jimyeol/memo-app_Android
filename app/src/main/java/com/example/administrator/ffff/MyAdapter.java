package com.example.administrator.ffff;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-08-23.
 */
public class MyAdapter extends BaseAdapter {
    private ArrayList<NoteListItem> listViewItemList = new ArrayList<NoteListItem>() ;

    public MyAdapter() {
    }

    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public Object getItem(int i) {
        return listViewItemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.note_row, parent, false);
        }

        TextView titleTextView = (TextView) convertView.findViewById(R.id.textView1) ;
        TextView titleDateView = (TextView) convertView.findViewById(R.id.date_row) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        NoteListItem listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        titleTextView.setText(listViewItem.getTitle());
        titleDateView.setText(listViewItem.getDate());

        return convertView;

    }

    public void addItem(String title, String date) {
        NoteListItem item = new NoteListItem();

        item.setTitle(title);
        item.setDate(date);

        listViewItemList.add(item);
    }
}
