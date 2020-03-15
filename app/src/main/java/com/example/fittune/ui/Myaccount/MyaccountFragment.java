package com.example.fittune.ui.Myaccount;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.fittune.R;
import com.example.fittune.SignedInActivity;

public class MyaccountFragment extends Fragment {

    private MyaccountViewModel notificationsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(MyaccountViewModel.class);
        View root = inflater.inflate(R.layout.fragment_myaccount, container, false);
        final TextView textView = root.findViewById(R.id.text_notifications);
        final Button button = root.findViewById(R.id.backtoSignIn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SignedInActivity.class);
                startActivity(intent);
            }
        });
        notificationsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}