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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dialog_Edit extends AppCompatDialogFragment {
    private CheckBox distance,fatburning,pace,duration,edm,rock,pop;
    private Dialog dialog;
    private TextView done;
    private Boolean isdistance,isfatburning,ispace,isduration;
    private Boolean isedm,isrock,ispop;
    private onDoneOnclickListener doneOnclickListener;
    StringBuilder sb=new StringBuilder();
    StringBuilder musicstyle=new StringBuilder();
    static Integer choicenumber=0;

    private HashMap<String, Boolean> exercisechoice=new HashMap<>();

    ArrayList<String> choice=new ArrayList<String>(2);

    static Integer musicchoicenumber=0;
    ArrayList<String> music=new ArrayList<String>(1);

    public Dialog_Edit(Boolean distance,Boolean fatburning,Boolean pace,Boolean duration,Boolean edm,Boolean rock, Boolean pop){

        isdistance=distance;isfatburning=fatburning;ispace=pace;isduration=duration;
        isedm=edm;isrock=rock;ispop=pop;

        exercisechoice.put("Distance",isdistance);
        exercisechoice.put("Fat Burning",isfatburning);
        exercisechoice.put("Pace",ispace);
        exercisechoice.put("Duration",isduration);
        exercisechoice.put("EDM",isedm);
        exercisechoice.put("Rock",isrock);
        exercisechoice.put("Pop",ispop);

    }
// Done button
    public void setdoneOnclickListener(onDoneOnclickListener doneOnclickListener) {
        this.doneOnclickListener = doneOnclickListener;
    }

    public interface onDoneOnclickListener {
        public void onDoneClick(StringBuilder sb,StringBuilder musicstyle);
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

        distance=(CheckBox)contentview.findViewById(R.id.Distance);
        fatburning=(CheckBox)contentview.findViewById(R.id.FatBurning);
        pace=(CheckBox)contentview.findViewById(R.id.Pace);
        duration=(CheckBox)contentview.findViewById(R.id.Duration);
        done=(TextView)contentview.findViewById(R.id.done);

        edm=(CheckBox)contentview.findViewById(R.id.EDM);
        rock=(CheckBox)contentview.findViewById(R.id.Rock);
        pop=(CheckBox)contentview.findViewById(R.id.Pop);

        InitChecked();

        initEvent(contentview);
        return dialog;
    }

    private void InitChecked(){
        choicenumber=0;
        musicchoicenumber=0;
        for (HashMap.Entry<String, Boolean> entry : exercisechoice.entrySet()) {
            SetinitChecked(entry.getKey(),entry.getValue());
        }
    }

    private void SetinitChecked(String key, Boolean value) {
        switch (key){
            case "Distance":
                if(value){
                    distance.setChecked(true);
                    distance.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                    choicenumber+=1;
                    choice.add(key);
                    ValidChoice(choicenumber,musicchoicenumber);
                }
                break;
            case "Fat Burning":
                if(value){
                    fatburning.setChecked(true);
                    fatburning.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                    choicenumber+=1;
                    choice.add(key);
                    ValidChoice(choicenumber,musicchoicenumber);
                }
                break;
            case "Pace":
                if(value){
                    pace.setChecked(true);
                    pace.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                    choicenumber+=1;
                    choice.add(key);
                    ValidChoice(choicenumber,musicchoicenumber);
                }
                break;
            case "Duration":
                if(value){
                    duration.setChecked(true);
                    duration.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                    choicenumber+=1;
                    choice.add(key);
                    ValidChoice(choicenumber,musicchoicenumber);
                }
                break;
            case "EDM":
                if(value){
                    edm.setChecked(true);
                    edm.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                    musicchoicenumber+=1;
                    music.add(key);
                    ValidChoice(choicenumber,musicchoicenumber);
                }
                break;
            case "Rock":
                if(value) {
                    rock.setChecked(true);
                    rock.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                    musicchoicenumber += 1;
                    music.add(key);
                    ValidChoice(choicenumber,musicchoicenumber);
                }
                break;
            case "Pop":
                if(value){
                    pop.setChecked(true);
                    pop.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                    musicchoicenumber+=1;
                    music.add(key);
                    ValidChoice(choicenumber,musicchoicenumber);
                }
                break;
        }

    }


    private void initEvent(final View contentview) {
// Fat Burning Choice

            fatburning.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        isfatburning=true;
                        choicenumber+=1;
                        fatburning.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                        choice.add(buttonView.getText().toString().trim());
                    }else {
                        isfatburning=false;
                        if(choicenumber==0){
                            choicenumber=0;
                        }else {
                            choicenumber-=1;
                            fatburning.setTextColor(ContextCompat.getColor(getContext(),R.color.colordarkwhite));
                            choice.remove(buttonView.getText().toString().trim());
                        }
                    }
                    ValidChoice(choicenumber,musicchoicenumber);

                }
            });


//Duration Choice

            duration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        isduration=true;
                        choicenumber+=1;
                        duration.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                        choice.add(buttonView.getText().toString().trim());
                    }else {
                        isduration=false;
                        if(choicenumber==0){
                            choicenumber=0;
                        }else{
                            choicenumber-=1;
                            duration.setTextColor(ContextCompat.getColor(getContext(),R.color.colordarkwhite));
                            choice.remove(buttonView.getText().toString().trim());
                        }

                    }

                    ValidChoice(choicenumber,musicchoicenumber);
                }
            });

//Pace Choice


            pace.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        ispace=true;
                        choicenumber+=1;
                        pace.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                        choice.add(buttonView.getText().toString().trim());
                    }else {
                        if(choicenumber==0){
                            choicenumber=0;
                        }else{
                            choicenumber-=1;
                            pace.setTextColor(ContextCompat.getColor(getContext(),R.color.colordarkwhite));
                            choice.remove(buttonView.getText().toString().trim());
                        }
                        ispace=false;

                    }
                    ValidChoice(choicenumber,musicchoicenumber);
                }
            });



//Distance Choice


            distance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        isdistance=true;
                        choicenumber+=1;
                        distance.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                        choice.add(buttonView.getText().toString().trim());
                    }else {

                        isdistance=false;
                        if(choicenumber==0){
                            choicenumber=0;
                        }else{
                            choicenumber-=1;
                            distance.setTextColor(ContextCompat.getColor(getContext(),R.color.colordarkwhite));
                            choice.remove(buttonView.getText().toString().trim());
                        }

                    }
                    ValidChoice(choicenumber,musicchoicenumber);
                }
            });





        rock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    isrock=true;
                    musicchoicenumber+=1;
                    rock.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                    music.add(buttonView.getText().toString().trim());
                }else {

                    isrock=false;
                    if(musicchoicenumber==0){
                        musicchoicenumber=0;
                    }else{
                        musicchoicenumber-=1;
                        rock.setTextColor(ContextCompat.getColor(getContext(),R.color.colordarkwhite));
                        music.remove(buttonView.getText().toString().trim());
                    }

                }

                ValidChoice(choicenumber,musicchoicenumber);
            }
        });

        edm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    isedm=true;
                    musicchoicenumber+=1;
                    edm.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                    music.add(buttonView.getText().toString().trim());
                }else {

                    isedm=false;
                    if(musicchoicenumber==0){
                        musicchoicenumber=0;
                    }else{
                        musicchoicenumber-=1;
                        edm.setTextColor(ContextCompat.getColor(getContext(),R.color.colordarkwhite));
                        music.remove(buttonView.getText().toString().trim());
                    }

                }
                ValidChoice(choicenumber,musicchoicenumber);
            }
        });


        pop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    ispop=true;
                    musicchoicenumber+=1;
                    pop.setTextColor(ContextCompat.getColor(getContext(),R.color.colorwhite));
                    music.add(buttonView.getText().toString().trim());
                }else {

                    ispop=false;
                    if(musicchoicenumber==0){
                        musicchoicenumber=0;
                    }else{
                        musicchoicenumber-=1;
                        pop.setTextColor(ContextCompat.getColor(getContext(),R.color.colordarkwhite));
                        music.remove(buttonView.getText().toString().trim());
                    }

                }

                ValidChoice(choicenumber,musicchoicenumber);
            }
        });





//Done Button
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(doneOnclickListener!=null){
                    for(int i=0;i<music.size();i++){
                        if(i==(music.size()-1)){
                            musicstyle.append(music.get(i));
                        }
                    }
                    for (int i =0;i<choice.size();i++) {

                        if(i==(choice.size()-1))
                        {
                            sb.append(choice.get(i));
                        }else {
                            sb.append(choice.get(i)+",");
                        }
                    }
                    doneOnclickListener.onDoneClick(sb,musicstyle);
                }
            }
        });


    }

    private void ValidChoice(Integer choicenumber, Integer musicchoicenumber) {

        if(choicenumber==2){
            if(!isdistance){
                distance.setEnabled(false);
            }
            if(!isfatburning){
                fatburning.setEnabled(false);
            }
            if(!ispace){
                pace.setEnabled(false);
            }
            if(!isduration){
                duration.setEnabled(false);
            }
        }else {
            distance.setEnabled(true);
            fatburning.setEnabled(true);
            pace.setEnabled(true);
            duration.setEnabled(true);
        }

        if(musicchoicenumber==1){
            if(!isedm){
                edm.setEnabled(false);
            }
            if(!ispop){
                pop.setEnabled(false);
            }
            if(!isrock){
                rock.setEnabled(false);
            }

        }else {
            edm.setEnabled(true);
            pop.setEnabled(true);
            rock.setEnabled(true);
        }

    }


}
