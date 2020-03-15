package com.example.fittune;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fittune.ExerciseStats;
import com.example.fittune.ui.Gallery.GlobalViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProfileViewAdapter extends RecyclerView.Adapter<ProfileViewAdapter.ProfileViewHolder> {

    //private Context mContext;
    private List<ExerciseStats> mUploads;
    private ProfileViewAdapter.OnPicListener mOnPicListener;

    //public ProfileViewAdapter(Context context, List<ExerciseStats> uploads, OnPicListener onPicListener) {
    public ProfileViewAdapter(List<ExerciseStats> uploads, ProfileViewAdapter.OnPicListener onPicListener) {

        //mContext = context;
        mUploads = uploads;
        this.mOnPicListener = onPicListener;
    }

    @NonNull
    @Override
    public ProfileViewAdapter.ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_exercise, parent, false);
        return new ProfileViewAdapter.ProfileViewHolder(view, mOnPicListener);
    }

    @Override
    public void onBindViewHolder(ProfileViewAdapter.ProfileViewHolder holder, int position) {

        ExerciseStats uploadCurrent = mUploads.get(position);
        //holder.textViewName.setText(uploadCurrent.getName());
        if(uploadCurrent.getExerciseType().equals("outdoor")){
            Picasso.get()
                    .load(R.drawable.outdoor)
                    .fit()
                    .centerCrop()
                    .into(holder.imageView);
        }else if(uploadCurrent.getExerciseType().equals("treadmill")){
            Picasso.get()
                    .load(R.drawable.treadmill)
                    .fit()
                    .centerCrop()
                    .into(holder.imageView);
        }

        //TODO update ExerciseStats to take userID and storageref
        holder.distance.setText("Distance: "+String.valueOf(uploadCurrent.getDistance()) + " km");
        holder.duration.setText("Duration: "+uploadCurrent.getDuration());
        holder.pace.setText("Pace: "+uploadCurrent.getPace());
        holder.calories.setText("Calories: "+String.valueOf(uploadCurrent.getCalories())+" kcal");

        //holder.imageView.setImageResource(mUploads[position]);
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class ProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView imageView;
        public TextView exerciseType;
        public TextView distance;
        public TextView duration;
        public TextView pace;
        public TextView calories;
        ProfileViewAdapter.OnPicListener onPicListener;
        public ProfileViewHolder(View itemView, ProfileViewAdapter.OnPicListener onPicListener) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.exerciseIcon);
            this.exerciseType = itemView.findViewById(R.id.exerciseType);
            this.distance = itemView.findViewById(R.id.exerciseDistance);
            this.duration = itemView.findViewById(R.id.exerciseDuration);
            this.pace = itemView.findViewById(R.id.exercisePace);
            this.calories = itemView.findViewById(R.id.exerciseCalories);
            this.onPicListener = onPicListener;
            itemView.setOnClickListener(this);
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
