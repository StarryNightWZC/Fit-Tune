package com.example.fittune.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fittune.Model.ExerciseBlock;
import com.example.fittune.R;

import java.util.List;

public class ExerciseblockAdapter extends RecyclerView.Adapter<ExerciseblockAdapter.ExerciseblockHolder>{

    private Context mContext;
    private List<ExerciseBlock> mData;

    public ExerciseblockAdapter(Context context, List<ExerciseBlock>  mData) {
        this.mContext = context;
        this.mData = mData;
    }

    @NonNull
    @Override
    public ExerciseblockAdapter.ExerciseblockHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row= LayoutInflater.from(mContext).inflate(R.layout.exercise_block,parent,false);
        return new ExerciseblockHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseblockAdapter.ExerciseblockHolder holder, int position) {

        holder.tv_name.setText(mData.get(position).getname());
        holder.tv_value.setText(mData.get(position).getvalue());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ExerciseblockHolder extends RecyclerView.ViewHolder {

        TextView tv_name,tv_value;

        public ExerciseblockHolder(@NonNull View itemView) {
            super(itemView);
            tv_value=itemView.findViewById(R.id.Value);
            tv_name=itemView.findViewById(R.id.Name);
        }

    }

    public String getItemValue(int position){
        return mData.get(position).getvalue();
    }

    public String getItemName(int position){
        return mData.get(position).getname();
    }


    //public ExerciseBlock getItem(int position) {
    //    return Exerciseblock.get(position);
    //}

}
