package com.example.fittune.ui.Myaccount;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fittune.Model.ExerciseStats;
import com.example.fittune.R;

import java.util.List;

public class MyaccountViewAdapter extends RecyclerView.Adapter<MyaccountViewAdapter.MyaccountViewHolder> {

    //private Context mContext;
    private List<ExerciseStats> mUploads;
    private MyaccountViewAdapter.OnPicListener mOnPicListener;

    //public MyaccountViewAdapter(Context context, List<ExerciseStats> uploads, OnPicListener onPicListener) {
    public MyaccountViewAdapter(List<ExerciseStats> uploads, MyaccountViewAdapter.OnPicListener onPicListener) {

        //mContext = context;
        mUploads = uploads;
        this.mOnPicListener = onPicListener;
    }

    @NonNull
    @Override
    public MyaccountViewAdapter.MyaccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_exercise_myaccount, parent, false);
        return new MyaccountViewAdapter.MyaccountViewHolder(view, mOnPicListener);
    }

    @Override
    public void onBindViewHolder(MyaccountViewAdapter.MyaccountViewHolder holder, int position) {

        ExerciseStats uploadCurrent = mUploads.get(position);
        //holder.textViewName.setText(uploadCurrent.getName());
//        if(uploadCurrent.getExerciseType().equals("outdoor")){
//            Picasso.get()
//                    .load(R.drawable.outdoor)
//                    .fit()
//                    .centerCrop()
//                    .into(holder.imageView);
//        }else if(uploadCurrent.getExerciseType().equals("treadmill")){
//            Picasso.get()
//                    .load(R.drawable.treadmill)
//                    .fit()
//                    .centerCrop()
//                    .into(holder.imageView);
//        }

        //TODO update ExerciseStats to take userID and storageref
        String str = uploadCurrent.getExerciseType();
        holder.exerciseType.setText("Exercise Type: " + str.substring(0, 1).toUpperCase() + str.substring(1));
        holder.distance.setText("Distance: "+String.valueOf(uploadCurrent.getDistance()) + " km");
        //holder.duration.setText("Duration: "+uploadCurrent.getDuration());
        holder.pace.setText("Pace: "+uploadCurrent.getPace());
        String month;
        String day;
        month = uploadCurrent.getTimeStamp().substring(4,6);
        if(month.equals("01")){month = "JAN";}
        if(month.equals("02")){month = "FEB";}
        if(month.equals("03")){month = "MAR";}
        if(month.equals("04")){month = "APR";}
        if(month.equals("05")){month = "MAY";}
        if(month.equals("06")){month = "JUN";}
        if(month.equals("07")){month = "JUL";}
        if(month.equals("08")){month = "AUG";}
        if(month.equals("09")){month = "SEP";}
        if(month.equals("10")){month = "OCT";}
        if(month.equals("11")){month = "NOV";}
        if(month.equals("12")){month = "DEC";}
        day = uploadCurrent.getTimeStamp().substring(6,8);
        holder.month.setText(month);
        holder.day.setText(day);
        //holder.calories.setText("Calories: "+String.valueOf(uploadCurrent.getCalories())+" kcal");

        //holder.imageView.setImageResource(mUploads[position]);
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class MyaccountViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView imageView;
        public TextView exerciseType;
        public TextView distance;
        public TextView duration;
        public TextView pace;
        public TextView calories;
        public TextView detail;
        public TextView month;
        public TextView day;
        MyaccountViewAdapter.OnPicListener onPicListener;
        public MyaccountViewHolder(View itemView, MyaccountViewAdapter.OnPicListener onPicListener) {
            super(itemView);
            //this.imageView = itemView.findViewById(R.id.exerciseIcon);
            this.exerciseType = itemView.findViewById(R.id.exerciseType);
            this.distance = itemView.findViewById(R.id.exerciseDistance);
            //this.duration = itemView.findViewById(R.id.exerciseDuration);
            this.pace = itemView.findViewById(R.id.exercisePace);
            //this.calories = itemView.findViewById(R.id.exerciseCalories);
            this.detail = itemView.findViewById(R.id.Detail_text);
            this.month = itemView.findViewById(R.id.Month_text);
            this.day = itemView.findViewById(R.id.Day_text);
            this.onPicListener = onPicListener;
            detail.setOnClickListener(this);
            //itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onPicListener.onPicClick(getAdapterPosition());
        }
    }

    public interface OnPicListener{
        void onPicClick(int position);
    }
}