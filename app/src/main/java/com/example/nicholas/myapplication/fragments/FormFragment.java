package com.example.nicholas.myapplication.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by h on 31/10/15.
 */
public class FormFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(com.example.nicholas.myapplication.R.layout.fragment_form, container, false);
    }
}
