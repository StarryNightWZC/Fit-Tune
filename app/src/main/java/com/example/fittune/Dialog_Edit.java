package com.example.fittune;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class Dialog_Edit extends AppCompatDialogFragment {
    private CheckBox distance,fatburning,pace,duration;
    private Dialog dialog;
    private TextView done;
    private Boolean isdistance=false,isfatburning=false,ispace=false,isduration=false;
    private onDoneOnclickListener doneOnclickListener;
    StringBuilder sb=new StringBuilder();
    static Integer choicenumber=0;
    ArrayList<String> choice=new ArrayList<String>(2);

    public Dialog_Edit(){

    }
// Done button
    public void setdoneOnclickListener(onDoneOnclickListener doneOnclickListener) {
        this.doneOnclickListener = doneOnclickListener;
    }

    public interface onDoneOnclickListener {
        public void onDoneClick(StringBuilder sb);
    }
//

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = new Dialog(getActivity(), R.style.Theme_AppCompat_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View contentview = View.inflate(getActivity(), R.layout.editfunction, null);
        dialog.setContentView(contentview);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setDimAmount(0.6f);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.alpha = 1;
        lp.dimAmount = 0.8f;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        isdistance=false;
        ispace=false;
        isduration=false;
        isfatburning=false;
        distance=(CheckBox)contentview.findViewById(R.id.Distance);
        fatburning=(CheckBox)contentview.findViewById(R.id.FatBurning);
        pace=(CheckBox)contentview.findViewById(R.id.Pace);
        duration=(CheckBox)contentview.findViewById(R.id.Duration);
        done=(TextView)contentview.findViewById(R.id.done);

        initEvent(contentview);
        return dialog;
    }

    private void initEvent(final View contentview) {
// Fat Burning Choice
            Log.d("isfat",Boolean.toString(isfatburning));
            fatburning.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        isfatburning=true;
                        choicenumber+=1;
                        Log.d("count",Integer.toString(choicenumber));
                        fatburning.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                        choice.add(buttonView.getText().toString().trim());
                    }else {
                        isfatburning=false;
                        if(choicenumber==0){
                            choicenumber=0;
                        }else {
                            choicenumber-=1;
                            Log.d("count",Integer.toString(choicenumber));
                            fatburning.setTextColor(ContextCompat.getColor(getContext(),R.color.colordarkwhite));
                            choice.remove(buttonView.getText().toString().trim());
                        }
                    }

                }
            });


//Duration Choice
            Log.d("isduration",Boolean.toString(isduration));
            duration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        isduration=true;
                        choicenumber+=1;
                        Log.d("count",Integer.toString(choicenumber));
                        duration.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                        choice.add(buttonView.getText().toString().trim());
                    }else {
                        isduration=false;
                        if(choicenumber==0){
                            choicenumber=0;
                        }else{
                            choicenumber-=1;
                            Log.d("count",Integer.toString(choicenumber));
                            duration.setTextColor(ContextCompat.getColor(getContext(),R.color.colordarkwhite));
                            choice.remove(buttonView.getText().toString().trim());
                        }

                    }
                }
            });

//Pace Choice

            Log.d("ispace",Boolean.toString(ispace));
            pace.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        ispace=true;
                        choicenumber+=1;
                        Log.d("count",Integer.toString(choicenumber));
                        pace.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                        choice.add(buttonView.getText().toString().trim());
                    }else {
                        if(choicenumber==0){
                            choicenumber=0;
                        }else{
                            choicenumber-=1;
                            Log.d("count",Integer.toString(choicenumber));
                            pace.setTextColor(ContextCompat.getColor(getContext(),R.color.colordarkwhite));
                            choice.remove(buttonView.getText().toString().trim());
                        }
                        ispace=false;

                    }
                }
            });



//Distance Choice

            Log.d("isdistance",Boolean.toString(isdistance));
            distance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        isdistance=true;
                        choicenumber+=1;
                        Log.d("count",Integer.toString(choicenumber));
                        distance.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                        choice.add(buttonView.getText().toString().trim());
                    }else {

                        isdistance=false;
                        if(choicenumber==0){
                            choicenumber=0;
                        }else{
                            choicenumber-=1;
                            Log.d("count",Integer.toString(choicenumber));
                            distance.setTextColor(ContextCompat.getColor(getContext(),R.color.colordarkwhite));
                            choice.remove(buttonView.getText().toString().trim());
                        }

                    }
                }
            });




//Done Button
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(doneOnclickListener!=null){
                    for (int i =0;i<choice.size();i++) {

                        if(i==(choice.size()-1))
                        {
                            sb.append(choice.get(i));
                        }else {
                            sb.append(choice.get(i)+",");
                        }
                    }
                    doneOnclickListener.onDoneClick(sb);
                }
            }
        });


    }


}
