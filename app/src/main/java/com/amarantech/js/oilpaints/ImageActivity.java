package com.amarantech.js.oilpaints;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class ImageActivity extends AppCompatActivity implements EffectFragment.CustomOnClickListener {

    //private ImageView imageView;

    //private Bitmap mBitmap;
    private Uri bitmapUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        bitmapUri = (Uri) intent.getParcelableExtra("bmUri");

        Fragment fr = new ImageFragment(); // replace your custom fragment class
        //Fragment fr = getSupportFragmentManager().findFragmentById(R.id.imageFragment);
        Bundle bundle = new Bundle();
        FragmentTransaction frTransaction = getSupportFragmentManager().beginTransaction();
        bundle.putParcelable("uri", bitmapUri);
        fr.setArguments(bundle);
        frTransaction.add(R.id.imageFragment, fr);
        frTransaction.commit();
    }

    @Override
    public void onClicked( View v )  {
        ImageFragment imageFragment = (ImageFragment) getSupportFragmentManager().findFragmentById(R.id.imageFragment);
        switch( v.getId() ) {
            case R.id.reopenButton: {
                imageFragment.setText( "Reopen Button was clicked." );
                imageFragment.setEffect(0);
                break;
            }
            case R.id.oilButton: {
                imageFragment.setText( "Oil Button was clicked." );
                imageFragment.setEffect(1);
                break;
            }
            case R.id.filterButton: {
                imageFragment.setText( "Filter Button was clicked." );
                imageFragment.setEffect(2);
                break;
            }
            case R.id.saveButton: {
                imageFragment.setText( "Save Button was clicked." );
                break;
            }
        }
    }
}
