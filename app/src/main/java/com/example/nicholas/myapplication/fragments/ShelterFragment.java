package com.example.nicholas.myapplication.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nicholas.myapplication.R;

/**
 * Created by Nicholas on 01/11/2015.
 */
public class ShelterFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_shelter, container, false);
    }
}
