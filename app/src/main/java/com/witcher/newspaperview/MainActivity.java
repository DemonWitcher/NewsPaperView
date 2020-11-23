package com.witcher.newspaperview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mFuncCrop, mFuncDecal, mFuncFilter;

    private CropView mCropView;
    private NewsPaperView mNewsPaperView;

    private Toolbar toolbar;

    private RecyclerView mRecyclerView;

    private String[] decalList;
    private String[] desList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    public void initView() {
        mFuncCrop = (TextView) findViewById(R.id.tv_main_crop);
        mFuncCrop.setOnClickListener(this);
        mFuncDecal = (TextView) findViewById(R.id.tv_main_decal);
        mFuncDecal.setOnClickListener(this);
        mFuncFilter = (TextView) findViewById(R.id.tv_main_filter);
        mFuncFilter.setOnClickListener(this);
        mFuncFilter.setVisibility(View.GONE);
        mFuncCrop.setSelected(true);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCropView = (CropView) findViewById(R.id.main_crop_view);
        mNewsPaperView = (NewsPaperView) findViewById(R.id.main_decal_view);

        initRecyclerView();
    }

    public void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.decal_list);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        decalList = getResources().getStringArray(R.array.decal_bitmap_items);
        desList = getResources().getStringArray(R.array.decal_des_items);
        mRecyclerView.setAdapter(new MyAdapter());
        mRecyclerView.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MediaPickerActivity.REQUEST_MEDIA_PICKER_ACTIVITY && resultCode == RESULT_OK) {
            ImageLoader.getInstance().loadImage(data.getStringExtra(MediaPickerActivity.MEDIA_URI), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    // Do whatever you want with Bitmap
                    mCropView.setBackgroundBitmap(loadedImage);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_settings:
                clickCameraButton();
                break;
        }
        return true;
    }

    public void clickCameraButton() {
        Intent intent = new Intent(this, MediaPickerActivity.class);
        intent.putExtra(MediaPickerActivity.MEDIA_TYPE, MediaPickerActivity.MEDIA_IMAGE);
        startActivityForResult(intent, MediaPickerActivity.REQUEST_MEDIA_PICKER_ACTIVITY);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_main_crop:
                mFuncCrop.setSelected(false);
                mFuncDecal.setSelected(false);
                mFuncFilter.setSelected(false);
                mFuncCrop.setSelected(true);
                mRecyclerView.setVisibility(View.GONE);
                mNewsPaperView.setVisibility(View.GONE);
                break;

            case R.id.tv_main_decal:
                mFuncCrop.setSelected(false);
                mFuncDecal.setSelected(false);
                mFuncFilter.setSelected(false);
                mFuncDecal.setSelected(true);
                mRecyclerView.setVisibility(View.VISIBLE);
                mNewsPaperView.setVisibility(View.VISIBLE);
                break;

            case R.id.tv_main_filter:
                mFuncCrop.setSelected(false);
                mFuncDecal.setSelected(false);
                mFuncFilter.setSelected(false);
                mFuncFilter.setSelected(true);
                mRecyclerView.setVisibility(View.GONE);
                break;

            default:
                mFuncCrop.setSelected(false);
                mFuncDecal.setSelected(false);
                mFuncFilter.setSelected(false);
                mFuncCrop.setSelected(true);
                mRecyclerView.setVisibility(View.GONE);
                break;
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dical, parent, false);
            MyViewHolder vh = new MyViewHolder(itemView);

            return vh;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.tv_des.setText(desList[position]);
            ImageLoader.getInstance().displayImage(decalList[position], holder.iv_decal);
        }

        @Override
        public int getItemCount() {
            return decalList.length;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView iv_decal;
        public TextView tv_des;

        public MyViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            iv_decal = (ImageView) view.findViewById(R.id.iv_decal);
            tv_des = (TextView) view.findViewById(R.id.tv_des);
        }

        @Override
        public void onClick(View v) {
            ImageLoader.getInstance().loadImage(decalList[getPosition()], new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    // Do whatever you want with Bitmap
                    if(count%2==0){
                        Bitmap b = Bitmap.createScaledBitmap(loadedImage,loadedImage.getWidth()/2,loadedImage.getHeight()/2,true);
                        mNewsPaperView.addPaper(b);
                    }else{
                        Bitmap b = Bitmap.createScaledBitmap(loadedImage,loadedImage.getWidth()/2,loadedImage.getHeight(),true);
                        mNewsPaperView.addPeople(b,true);
                    }
                    count++;
                }
            });
        }

    }
    public static int count = 0;

}