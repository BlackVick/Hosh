package com.blackviking.hosh.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blackviking.hosh.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HookUp extends Fragment {


    public HookUp() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hook_up, container, false);
    }

}
