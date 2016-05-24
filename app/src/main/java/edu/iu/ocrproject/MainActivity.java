package edu.iu.ocrproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.opencv.android.BaseLoaderCallback;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.net.Uri;
import android.provider.MediaStore;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

import edu.iu.ocrproject.data.BaseManager;
import edu.iu.ocrproject.data.User;

public class MainActivity extends AppCompatActivity implements BaseManager.IQuery{

    public static final int RESULT_LOAD_IMAGE = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 2;

    public static MainActivity current;
    public BaseManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        current = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                initLibrary();
            }
        }).start();

    }

    public void openFragment(final Fragment fragment, final boolean add_to_backstack){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container_main,fragment);
                if(add_to_backstack){
                    transaction.addToBackStack(null);
                }
                transaction.commit();
            }
        });
    }

    private void initLibrary() {
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack)) {
            Log.e("OpenCV", "Cannot connect to OpenCV Manager");
            //todo hata göster
        }
    }

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Algorithms.initDir();
                            Algorithms.copyAssets(MainActivity.this);
                            dataManager = new BaseManager(MainActivity.this);
                            dataManager.getActive(MainActivity.this);
                        }
                    }).start();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            final String picturePath = cursor.getString(columnIndex);
            cursor.close();

            final User user = ActionFragment.current.getUser();
            final AnalyzeFragment fragment = AnalyzeFragment.newInstance(user);
            openFragment(fragment,true);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<Integer> result = Algorithms.fetch(picturePath);
                    String barcode = "";
                    for(Integer value:result){
                        barcode += String.valueOf(value);
                    }
                    fragment.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                    checkDataManager();
                    dataManager.controlProductByUser(fragment,barcode,user.username);
                }
            }).start();
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            final Bitmap imageBitmap = (Bitmap) extras.get("data");

            final User user = ActionFragment.current.getUser();
            final AnalyzeFragment fragment = AnalyzeFragment.newInstance(user);
            openFragment(fragment,true);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<Integer> result = Algorithms.fetch(imageBitmap);
                    String barcode = "";
                    for(Integer value:result){
                        barcode += String.valueOf(value);
                    }
                    fragment.setImageBitmap(imageBitmap);
                    checkDataManager();
                    dataManager.controlProductByUser(fragment,barcode,user.username);
                }
            }).start();
        }
    }

    @Override
    public void onResult(String func, Object... result) {
        if(result.length == 0){
            openFragment(FirstFragment.newInstance(),false);
        }else{
            openFragment(ActionFragment.newInstance((User)result[0]),false);
        }
    }

    public void checkDataManager(){
        if(dataManager == null){
            dataManager = new BaseManager(MainActivity.this);
        }
    }

    public void makeGalleryRequest(){
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    public void makeCameraRequest(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}
