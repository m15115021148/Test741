package com.meigsmart.test741;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.meigsmart.test741.config.RequestCode;
import com.meigsmart.test741.db.TypeModel;
import com.meigsmart.test741.model.ResultModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenMeng on 2018/2/1.
 */

public class MainAdapter extends BaseAdapter {
    private Context mContext;
    private List<ResultModel> mList;
    private Holder holder;
    private Map<Integer,ResultModel> map = new HashMap<Integer,ResultModel>();
    private OnMainCallBack mCallBack;

    public MainAdapter(Context context,OnMainCallBack callBack){
        this.mContext = context;
        this.mCallBack = callBack;
        this.mList = getData();
    }

    public interface OnMainCallBack{
        void onOver();
    }

    private List<ResultModel> getData(){
        List<ResultModel> data = new ArrayList<ResultModel>();
        ResultModel m1 = new ResultModel();
        m1.setName("EMMC TEST");
        m1.setIsPass(0);

        ResultModel m2 = new ResultModel();
        m2.setName("MEMORY TEST");
        m2.setIsPass(0);

        ResultModel m3 = new ResultModel();
        m3.setName("AUDIO TEST");
        m3.setIsPass(0);

        ResultModel m4 = new ResultModel();
        m4.setName("VIDEO TEST");
        m4.setIsPass(0);

        ResultModel m5 = new ResultModel();
        m5.setName("LCD TEST");
        m5.setIsPass(0);

        ResultModel m6 = new ResultModel();
        m6.setName("CPU TEST");
        m6.setIsPass(0);

        ResultModel m7 = new ResultModel();
        m7.setName("REBOOT TEST");
        m7.setIsPass(0);

        map.put(0,m1);
        map.put(1,m2);map.put(2,m3);map.put(3,m4);
        map.put(4,m5);map.put(5,m6);map.put(6,m7);

        for (int i=0;i<map.size();i++){
            ResultModel m = new ResultModel();
            m.setName(map.get(i).getName());
            m.setIsPass(map.get(i).getIsPass());
            data.add(m);
        }

        return data;
    }

    public void setData(List<TypeModel> list){
        for (int i=0;i<list.size();i++){
            TypeModel model = list.get(i);
            if (RequestCode.ANDROID_EMMC.equals(model.getType())){
                this.mList.get(0).setIsPass(model.getIsPass());
            } else if (RequestCode.ANDROID_MEMORY.equals(model.getType())){
                this.mList.get(1).setIsPass(model.getIsPass());
            } else if (RequestCode.ANDROID_AUDIO.equals(model.getType())){
                this.mList.get(2).setIsPass(model.getIsPass());
            } else if (RequestCode.ANDROID_VIDEO.equals(model.getType())){
                this.mList.get(3).setIsPass(model.getIsPass());
            } else if (RequestCode.ANDROID_LCD.equals(model.getType())){
                this.mList.get(4).setIsPass(model.getIsPass());
            } else if (RequestCode.ANDROID_CPU.equals(model.getType())){
                this.mList.get(5).setIsPass(model.getIsPass());
            } else if (RequestCode.ANDROID_REBOOT.equals(model.getType())){
                this.mList.get(6).setIsPass(model.getIsPass());
                this.mList.get(6).setType(model.getIsRun());
            }
        }
        this.notifyDataSetChanged();
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.main_item,null);
            holder = new Holder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.result = (TextView) convertView.findViewById(R.id.result);
            holder.over = convertView.findViewById(R.id.over);
            convertView.setTag(holder);
        }else{
            holder = (Holder) convertView.getTag();
        }

        ResultModel model = mList.get(position);

        holder.name.setText(model.getName());

        if (model.getIsPass() == 0){
            holder.result.setText("待测试");
            holder.result.setTextColor(Color.parseColor("#888888"));
        }else if (model.getIsPass() == 1){
            holder.result.setText("PASS");
            holder.result.setTextColor(Color.parseColor("#499AFA"));
        }else {
            holder.result.setText("FAILURE");
            holder.result.setTextColor(Color.parseColor("#ff0000"));
        }

        if (model.getName().equals("REBOOT TEST")){
            if (model.getType() == -1){
                holder.over.setVisibility(View.VISIBLE);
            }else{
                holder.over.setVisibility(View.GONE);
            }
        }else{
            holder.over.setVisibility(View.GONE);
        }

        holder.over.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallBack!=null)mCallBack.onOver();
            }
        });

        return convertView;
    }

    private class Holder{
        TextView name,result,over;
    }

}
