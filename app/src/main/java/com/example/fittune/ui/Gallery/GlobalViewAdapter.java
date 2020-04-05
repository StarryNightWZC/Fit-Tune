package com.example.fittune.ui.Gallery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fittune.R;
import com.example.fittune.UploadFile;
import com.example.fittune.Userprofile;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GlobalViewAdapter extends RecyclerView.Adapter<GlobalViewAdapter.GlobalViewHolder> {

    //private Context mContext;
    private List<Userprofile> mUploads;
    private OnPicListener mOnPicListener;
    private String userId;

    //public GlobalViewAdapter(Context context, List<Userprofile> uploads, OnPicListener onPicListener) {
    public GlobalViewAdapter(List<Userprofile> uploads, OnPicListener onPicListener, String uid) {

        //mContext = context;
        mUploads = uploads;
        this.mOnPicListener = onPicListener;
        this.userId = uid;
    }

    @NonNull
    @Override
    public GlobalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_global, parent, false);
        return new GlobalViewHolder(view, mOnPicListener);
    }

    @Override
    public void onBindViewHolder(GlobalViewHolder holder, int position) {

        Userprofile uploadCurrent = mUploads.get(position);
        float distanceTotal = 0;
        distanceTotal += Float.valueOf(uploadCurrent.getMon());
        distanceTotal += Float.valueOf(uploadCurrent.getTue());
        distanceTotal += Float.valueOf(uploadCurrent.getWed());
        distanceTotal += Float.valueOf(uploadCurrent.getThu());
        distanceTotal += Float.valueOf(uploadCurrent.getFri());
        distanceTotal += Float.valueOf(uploadCurrent.getSat());
        distanceTotal += Float.valueOf(uploadCurrent.getSun());
        //holder.textViewName.setText(uploadCurrent.getName());
        Picasso.get()
                .load(uploadCurrent.getStorageRef())
                .fit()
                .centerCrop()
                .into(holder.imageView);
        //TODO update userprofile to take userID and storageref
        //holder.distance.setText(uploadCurrent.getDistance()+ " km");
        holder.distance.setText(Float.toString(distanceTotal).substring(0, 5)+ " km");
        if(uploadCurrent.getUserId().equals(userId)){
            holder.username.setText(uploadCurrent.getName() + " (you)");
        }else{
            holder.username.setText(uploadCurrent.getName());
        }

        //holder.imageView.setImageResource(mUploads[position]);
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class GlobalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView imageView;
        public TextView username;
        public TextView distance;
        public TextView detail;
        OnPicListener onPicListener;
        public GlobalViewHolder(View itemView, OnPicListener onPicListener) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.leaderboardProfileImage);
            this.username = itemView.findViewById(R.id.usernameTextView);
            this.distance = itemView.findViewById(R.id.distanceTextView);
            this.detail = itemView.findViewById(R.id.Detail_text);
            this.onPicListener = onPicListener;
            detail.setOnClickListener(this);
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