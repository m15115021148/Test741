package com.meigsmart.test741.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.meigsmart.test741.R;
import com.meigsmart.test741.model.SplashModel;

import java.util.List;

/**
 * Created by chenMeng on 2018/2/5.
 */

public class SplashAdapter extends BaseAdapter {
    private Context mContext;
    private List<SplashModel> mList ;
    private Holder holder;

    public SplashAdapter(Context context,List<SplashModel> list){
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.splash_item,null);
            holder = new Holder();
            holder.time = convertView.findViewById(R.id.time);
            holder.select = convertView.findViewById(R.id.select);
            convertView.setTag(holder);
        }else{
            holder = (Holder) convertView.getTag();
        }

        SplashModel model = mList.get(position);

        holder.time.setText("测试时间："+model.getTime()+"h");
        if (model.getSelect() == 0){
            holder.select.setSelected(false);
        }else{
            holder.select.setSelected(true);
        }

        return convertView;
    }

    private class Holder{
        TextView time;
        ImageView select;
    }

}
