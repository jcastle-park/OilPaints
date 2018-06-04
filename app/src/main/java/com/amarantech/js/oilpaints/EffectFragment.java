package com.amarantech.js.oilpaints;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class EffectFragment extends Fragment {

    Button mReopenButton;
    Button mOilButton;
    Button mFilterButton;
    Button mSaveButton;

    public interface CustomOnClickListener {
        public void onClicked( View v );
    }

    private CustomOnClickListener customOnClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_effect, null);
        mReopenButton = (Button) view.findViewById(R.id.reopenButton);
        mOilButton = (Button) view.findViewById(R.id.oilButton);
        mFilterButton = (Button) view.findViewById(R.id.filterButton);
        mSaveButton = (Button) view.findViewById(R.id.saveButton);

        mReopenButton.setOnClickListener( new View.OnClickListener() {
            public void onClick( View v ) {
                buttonClicked( v );
            }
        } );

        mOilButton.setOnClickListener( new View.OnClickListener() {
            public void onClick( View v ) {
                buttonClicked( v );
            }
        } );

        mFilterButton.setOnClickListener( new View.OnClickListener() {
            public void onClick( View v ) {
                buttonClicked( v );
            }
        } );

        mSaveButton.setOnClickListener( new View.OnClickListener() {
            public void onClick( View v ) {
                buttonClicked( v );
            }
        } );

        return view;
    }

    public void buttonClicked( View v ) {
        customOnClickListener.onClicked(v);
    }

    @Override
    @Deprecated
    public void onAttach( Context context ) {
        super.onAttach( context );
        customOnClickListener = (CustomOnClickListener)context;
    }
}
