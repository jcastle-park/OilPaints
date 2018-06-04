package com.amarantech.js.oilpaints;

import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ImageFragment extends Fragment implements GLSurfaceView.Renderer {

    TextView mTextView;

    private Bitmap mBitmap;
    private Uri bitmapUri;

    ////////////////////////

    private GLSurfaceView mEffectView;
    private int[] mTextures = new int[2];
    private TextureRenderer mTexRenderer = new TextureRenderer();
    private int mImageWidth;
    private int mImageHeight;
    private boolean mInitialized = false;
    private int mEffectType = 0;

    ///////////////////////////

    public ImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            bitmapUri = bundle.getParcelable("uri");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, null);

        try {
            mBitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), bitmapUri);
        } catch (Exception e) {
            //Log.e("test", e.getMessage());
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mTextView = (TextView) view.findViewById(R.id.textView);
        mEffectView = (GLSurfaceView) view.findViewById(R.id.imageView);
        mEffectView.setEGLContextClientVersion(2);
        mEffectView.setRenderer(this);
        mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        // Nothing to do here
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mTexRenderer != null) {
            mTexRenderer.updateViewSize(width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //if (!mInitialized) {
            //Only need to do this once
            mTexRenderer.init(mEffectType);
            loadTextures();
            mInitialized = true;
        //}

        renderResult();
    }

    private void loadTextures() {
        // Generate textures
        GLES20.glGenTextures(2, mTextures, 0);

        // Load input bitmap
        mImageWidth = mBitmap.getWidth();
        mImageHeight = mBitmap.getHeight();
        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);

        // Upload to texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);

        // Set texture parameters
        GLToolbox.initTexParams();
    }

    private void renderResult() {
        /*if (mCurrentEffect != R.id.none) {
            // if no effect is chosen, just render the original bitmap
            mTexRenderer.renderTexture(mTextures[1]);
        } else*/ {
            // render the result of applyEffect()
            mTexRenderer.renderTexture(mTextures[0]);
        }
    }

    public void setText (String text) {
        mTextView.setText(text);
    }
    public void setEffect (int nType) { mEffectType = nType; mEffectView.requestRender(); }
}
