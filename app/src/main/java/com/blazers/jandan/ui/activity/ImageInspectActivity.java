package com.blazers.jandan.ui.activity;

import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blazers.jandan.R;
import com.blazers.jandan.model.event.ViewImageEvent;
import com.blazers.jandan.presenter.ImageInspectPresenter;
import com.blazers.jandan.ui.activity.base.BaseActivity;
import com.blazers.jandan.util.ShareHelper;
import com.blazers.jandan.util.log.Log;
import com.blazers.jandan.widgets.fresco.ZoomableDraweeView;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.image.ImageInfo;

import butterknife.BindView;

public class ImageInspectActivity extends BaseActivity<ImageInspectPresenter> implements ImageInspectView {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.viewer)
    ZoomableDraweeView view;
    //    @Bind(R.id.photo_drawee_view) PhotoDraweeView photoDraweeView;
    @BindView(R.id.btn_save)
    ImageButton btnSave;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.image_view_extra)
    RelativeLayout extras;

    /* Vars */
    private boolean uiIsShowing = true;
    private ViewImageEvent mViewImageEvent;


    private long touchStartTime;
    private float touchStartX, touchStartY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Object event =  getIntent().getSerializableExtra(ViewImageEvent.KEY);
        if (event == null) {
            // 提示
            finish();
        }
        setContentView(R.layout.activity_image_viewer);
        initToolbarByTypeWithShadow(null, toolbar, ToolbarType.FINISH);
        setReadyForImmersiveMode();
        setToolbarTitle("");
        // 设置Extra
        extras.setPadding(0, 0, 0, getNavigationBarHeight());
        initScalableImage();
        mPresenter = new ImageInspectPresenter(this);
        // SaveButton
        btnSave.setImageResource(R.mipmap.ic_publish_16dp);
        btnSave.setOnClickListener(v -> mPresenter.downloadImage(mViewImageEvent.mOriginUrl));
        // ShowImage
        DraweeController ctrl = Fresco.newDraweeControllerBuilder()
                .setUri(Uri.parse(mViewImageEvent.mOriginUrl))
                .setTapToRetryEnabled(true)
                .setAutoPlayAnimations(true)
                .setControllerListener(new FrescoControlListener())
                .build();
        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources())
                .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .setProgressBarImage(new ProgressBarDrawable())
                .build();
        view.setController(ctrl);
        view.setHierarchy(hierarchy);
        view.setController(ctrl);
        view.setHierarchy(hierarchy);
    }

    void initScalableImage() {
        // 触摸
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (event.getPointerCount() == 1) {
                        touchStartTime = System.currentTimeMillis();
                        touchStartX = event.getX();
                        touchStartY = event.getY();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (System.currentTimeMillis() - touchStartTime < 400
                            && event.getPointerCount() == 1
                            && Math.abs(Math.pow(touchStartX - event.getX(), 2) + Math.pow(touchStartY - event.getY(), 2)) < 200) {
                        toggleUI();
                    }
                    break;
            }
            return false;
        });
    }


    /**
     * 隐藏 / 全屏
     */
    public void toggleUI() {
        Log.e("C", "C");
        if (uiIsShowing) {
            // hide
            hideSystemUI(toolbar);
            extras.animate().translationY(extras.getMeasuredHeight()).setDuration(400).setStartDelay(200).start();
            uiIsShowing = false;
        } else {
            // show
            showSystemUI(toolbar);
            extras.animate().translationY(0).setDuration(400).setStartDelay(200).start();
            uiIsShowing = true;
        }
    }

    /**
     * Toolbar Menu TODO: Implement
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void aa() {
//        content.setText(mPresenter.getImageContent());
//        /* 判断是否已经下载 */
//        if (mPresenter.isDownloaded()) {
//            setDownloadButtonDone();
//        } else {
//            btnSave.setOnClickListener(v -> mPresenter.downloadImage());
//        }
    }

    @Override
    public void onShareImage() {
        ShareHelper.shareImage(this, mViewImageEvent.mContentStr, mViewImageEvent.mOriginUrl);
    }

    /**
     * 控制硬件加速开启与否
     */
    class FrescoControlListener extends BaseControllerListener<ImageInfo> {

        @Override
        public void onFinalImageSet(String s, ImageInfo imageInfo, Animatable animatable) {
            if (imageInfo == null) {
                return;
            }
            if (imageInfo.getWidth() > 2048 || imageInfo.getHeight() > 2048) {
                view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            } else
                view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            if (null != animatable)
                animatable.start();
        }
    }
}
