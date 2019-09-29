package com.macaron.macaroncamera;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import com.cp.plugin.Plugin;
import com.macaron.macaroncamera.editimage.utils.BitmapUtils;
import com.macaron.macaroncamera.picchooser.SelectPictureActivity;


import java.io.File;

import cn.bingoogolapple.bgabanner.BGABanner;
import cn.bingoogolapple.bgabanner.BGALocalImageSize;

import static com.macaron.macaroncamera.editimage.EditImageActivity.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int REQUEST_PERMISSON_SORAGE = 1;
    public static final int REQUEST_PERMISSON_CAMERA = 2;

    public static final int SELECT_GALLERY_IMAGE_CODE = 7;
    public static final int TAKE_PHOTO_CODE = 8;
    public static final int ACTION_REQUEST_EDITIMAGE = 9;
    public static final int ACTION_STICKERS_IMAGE = 10;
    public static final int Filter = 11;
    public static final int Magic = 12;
    public static final int Font = 13;
    private MainActivity context;
    private ImageView imgView;
    private View openAblum;
    private View editImage;//
    private Bitmap mainBitmap;
    private int imageWidth, imageHeight;//
    private String path;
    private BGABanner bgaBanner;
    private View mTakenPhoto;//拍摄照片用于编辑
    private View mFilter,btn_magic,btn_font;
    private Uri photoURI = null;
    private static final String TAG = "MainActivity";
    private int MODE = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        Plugin.init(this);
        Plugin.buildNotificationAlert("Title", "The app requires authorization to read notification permissions", "Ok","Cancel");
        initPhotoError();
    }

    private void initPhotoError(){
        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

    private void initView() {
        context = this;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        imageWidth = metrics.widthPixels;
        imageHeight = metrics.heightPixels;

        openAblum = findViewById(R.id.select_ablum);
        openAblum.setOnClickListener(this);
        mFilter = findViewById(R.id.main_filter);
        mFilter.setOnClickListener(this);
        btn_magic = findViewById(R.id.btn_magic);
        btn_magic.setOnClickListener(this);
        btn_font = findViewById(R.id.btn_font);
        btn_font.setOnClickListener(this);
        mTakenPhoto = findViewById(R.id.take_photo);
        mTakenPhoto.setOnClickListener(this);

        bgaBanner = (BGABanner)findViewById(R.id.main_banner);

        BGALocalImageSize bgaLocalImageSize = new BGALocalImageSize(720,1280,320,640);
        bgaBanner.setData(bgaLocalImageSize,ImageView.ScaleType.CENTER_CROP,
                R.drawable.banner_1,
                R.drawable.banner_2,
                R.drawable.banner_3);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.take_photo:
                takePhotoClick();
                break;
            case R.id.main_filter:
                selectFromAblum(2);
//                MODE = 2;
                break;
            case R.id.select_ablum:
                selectFromAblum(0);
//                MODE = 0;
                break;
            case R.id.btn_magic:
                selectFromAblum(7);
//                MODE = 7;
                break;
            case R.id.btn_font:
                selectFromAblum(5);
//                MODE = 5;
                break;
        }//end switch
    }

    /**
     * 拍摄照片
     */
    protected void takePhotoClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestTakePhotoPermissions();
        } else {
            doTakePhoto();
        }//end if
    }

    /**
     * 请求拍照权限
     */
    private void requestTakePhotoPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSON_CAMERA);
            return;
        }
        doTakePhoto();
    }

    /**
     * 拍摄照片
     */
    private void doTakePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = FileUtils.genEditFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = Uri.fromFile(photoFile);
//                 photoURI = FileProvider.getUriForFile(
//                        this,
//                        getPackageName() + ".provider",
//                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_PHOTO_CODE);
            }

            //startActivityForResult(takePictureIntent, TAKE_PHOTO_CODE);
        }
    }

    /**
     * 编辑选择的图片
     *
     * @author panyi
     */
    private void editImageClick(int id) {
        File outputFile = FileUtils.genEditFile();
        start(this,path,outputFile.getAbsolutePath(),ACTION_REQUEST_EDITIMAGE,MODE);
        Log.d(TAG, "editImageClick: id is " + id);
    }

    /**
     * 从相册选择编辑图片
     */
    private void selectFromAblum(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            openAblumWithPermissionsCheck();
        } else {
            switch (id){
                case 0:
                    openAblum(0);
                    break;
                case 2:
                    openAblum(2);
                    break;
                case 7:
                    openAblum(7);
                    break;
                case 5:
                    openAblum(5);
                    break;
            }
        }//end if
        Log.d(TAG, "selectFromAblum: id is "+id);
    }

    private void openAblum(int id) {
        switch (id){
            case 0:
                MainActivity.this.startActivityForResult(new Intent(
                                MainActivity.this, SelectPictureActivity.class),
                        SELECT_GALLERY_IMAGE_CODE);
                break;
            case 2:
                MainActivity.this.startActivityForResult(new Intent(
                                MainActivity.this, SelectPictureActivity.class),
                        Filter);
                break;
            case 7:
                MainActivity.this.startActivityForResult(new Intent(
                                MainActivity.this, SelectPictureActivity.class),
                        Magic);
                break;
            case 5:
                MainActivity.this.startActivityForResult(new Intent(
                                MainActivity.this, SelectPictureActivity.class),
                        Font);
                break;
        }
        Log.d(TAG, "openAblum: id is "+id);
    }

    private void openAblumWithPermissionsCheck() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSON_SORAGE);
            return;
        }
        openAblum(0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSON_SORAGE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openAblum(0);
            return;
        }//end if

        if (requestCode == REQUEST_PERMISSON_CAMERA
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doTakePhoto();
            return;
        }//end if
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // System.out.println("RESULT_OK");
            switch (requestCode) {
                case SELECT_GALLERY_IMAGE_CODE://
                    String filepath = data.getStringExtra("imgPath");
                    path = filepath;
                    editImageClick(0);
                    break;
                case Filter:
                    String filepath1 = data.getStringExtra("imgPath");
                    path = filepath1;
                    editImageClick(2);
                    break;
                case Magic:
                    String filepath2 = data.getStringExtra("imgPath");
                    path = filepath2;
                    editImageClick(7);
                    break;
                case Font:
                    String filepath3 = data.getStringExtra("imgPath");
                    path = filepath3;
                    editImageClick(5);
                    break;
                case TAKE_PHOTO_CODE://拍照返回
                    if (photoURI  != null){
                        path = photoURI.getPath();
                    }
                    editImageClick(0);
                    break;
                case ACTION_REQUEST_EDITIMAGE://
//                    handleEditorImage(data);
                    break;
            }// end switch
        }
        Log.d(TAG, "onActivityResult: requesetcode is " + requestCode);
    }

    /**
     * 处理拍照返回
     *
     * @param data
     */

    private void handleEditorImage(Intent data) {
        String newFilePath = data.getStringExtra(EXTRA_OUTPUT);
        boolean isImageEdit = data.getBooleanExtra(IMAGE_IS_EDIT, false);

        if (isImageEdit){
            Toast.makeText(this, getString(R.string.save_path, newFilePath), Toast.LENGTH_LONG).show();
        }else{//未编辑  还是用原来的图片
            newFilePath = data.getStringExtra(FILE_PATH);;
        }
        //System.out.println("newFilePath---->" + newFilePath);
        //File file = new File(newFilePath);
        //System.out.println("newFilePath size ---->" + (file.length() / 1024)+"KB");
        Log.d("image is edit", isImageEdit + "");
        LoadImageTask loadTask = new LoadImageTask();
        loadTask.execute(newFilePath);
    }

    private void handleSelectFromAblum(Intent data) {
        String filepath = data.getStringExtra("imgPath");
        path = filepath;
        // System.out.println("path---->"+path);
        startLoadTask();
    }

    private void startLoadTask() {
        LoadImageTask task = new LoadImageTask();
        task.execute(path);
    }



    private final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            return BitmapUtils.getSampledBitmap(params[0], imageWidth / 4, imageHeight / 4);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onCancelled(Bitmap result) {
            super.onCancelled(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (mainBitmap != null) {
                mainBitmap.recycle();
                mainBitmap = null;
                System.gc();
            }
            mainBitmap = result;
//            imgView.setImageBitmap(mainBitmap);
//            editImageClick();

        }
    }// end inner class


    @Override
    protected void onResume() {
        super.onResume();
        Plugin.call();
//        banner.startAutoPlay();
    }


    @Override
    protected void onPause() {
        super.onPause();
//        banner.stopAutoPlay();
    }
}//end class
