package com.example.fittune;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class Dialog_chooseScenario extends AppCompatDialogFragment {

    private RadioGroup rgroup;
    private Dialog dialog;
    private onOutdoorOnclickListener outdoorOnclickListener;
    private onTreadmillOnclickListener treadmillOnclickListener;

    public Dialog_chooseScenario(){

    }


    public void setoutdoorOnclickListener(onOutdoorOnclickListener onOutdoorOnclickListener) {
        this.outdoorOnclickListener = onOutdoorOnclickListener;
    }

    public void setTreadmillOnclickListener(onTreadmillOnclickListener onTreadmillOnclickListener){
        this.treadmillOnclickListener=onTreadmillOnclickListener;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = new Dialog(getActivity(), R.style.Theme_AppCompat_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View contentview = View.inflate(getActivity(), R.layout.choosescenario_dialog, null);
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

        rgroup=(RadioGroup) contentview.findViewById(R.id.rgroup);
        initEvent();
        return dialog;
    }

    private void initEvent() {
        rgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.outdoor:
                        if( outdoorOnclickListener!=null){
                            outdoorOnclickListener.onOutdoorClick();
                        }
                        //Intent bindIntent = new Intent(getActivity(), StepDetectorService.class);
                        // bindService(bindIntent, connection, BIND_AUTO_CREATE);
                        ;break;
                    case R.id.treadmill:
                        if( treadmillOnclickListener!=null){
                            treadmillOnclickListener.ontreadmillClick();
                        }
                        ;break;
                }
            }
        });


    }

    public interface onOutdoorOnclickListener {
        public void onOutdoorClick();
    }
    public interface onTreadmillOnclickListener {
        public void ontreadmillClick();
    }


}
