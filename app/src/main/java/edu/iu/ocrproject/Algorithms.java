package edu.iu.ocrproject;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class Algorithms {

    private static final String TAG = "OpenCV";
    private static final String FOLDER = "OCRProject";

    public static void initDir(){
        File cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), FOLDER);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public static ArrayList<Integer> fetch(String path){
        return fetch(Highgui.imread(path, Highgui.CV_LOAD_IMAGE_GRAYSCALE));
    }

    public static ArrayList<Integer> fetch(Bitmap bitmap){
        Mat img = new Mat();
        Utils.bitmapToMat(bitmap, img);
        return fetch(img);
    }

    private static ArrayList<Integer> fetch(Mat img) {

        Imgproc.threshold(img, img, 200, 255, Imgproc.THRESH_OTSU);

        writeFile("otsu_barkod.png", img);

        int imgColSize = img.cols();
        int imgRowSize = img.rows();
        int[][] imgMtr = new int[imgRowSize][imgColSize];
        double[] temp;
        for (int i = 0; i < imgRowSize; i++) {
            for (int j = 0; j < imgColSize; j++) {

                temp = img.get(i, j);
                imgMtr[i][j] = (int) temp[0];

            }
        }

        //find the starting row
        boolean whiteFlag = true;
        boolean blackFlag = false;
        boolean endoftheBarkod = false;
        int endRowofBarkod = -1;
        int pixelValueSum = 0;
        //delete barkod bars
        for (int i = 0; i < imgRowSize; i++) {
            for (int j = 0; j < imgColSize; j++) {
                pixelValueSum += imgMtr[i][j];
            }
            pixelValueSum /= imgColSize;
            if (pixelValueSum < 254 && blackFlag == false && whiteFlag == true) {

                blackFlag = true;
                whiteFlag = false;

            }
            if (pixelValueSum == 255 && blackFlag == true && whiteFlag == false) {

                endoftheBarkod = true;
                endRowofBarkod = i;
                break;
            }

        }

        Log.i(TAG, String.valueOf(endRowofBarkod));
        //After barkod bars we have numbers
        whiteFlag = true;
        blackFlag = false;
        int barkodNumberStartRow = -1;
        int barkodNumberEndRow = -1;
        pixelValueSum = 0;
        for (int i = endRowofBarkod; i < imgRowSize; i++) {
            for (int j = 0; j < imgColSize; j++) {
                pixelValueSum += imgMtr[i][j];
            }
            pixelValueSum /= imgColSize;
            if (pixelValueSum < 254 && blackFlag == false && whiteFlag == true) {

                blackFlag = true;
                whiteFlag = false;
                barkodNumberStartRow = i;

            }
            if (pixelValueSum >= 255 && blackFlag == true && whiteFlag == false) {

                endoftheBarkod = true;
                barkodNumberEndRow = i;
                break;
            }

        }

        Log.i(TAG, String.valueOf(barkodNumberStartRow));
        Log.i(TAG, String.valueOf(barkodNumberEndRow));


        //Column Control
        whiteFlag = true;
        blackFlag = false;
        endoftheBarkod = false;
        int barkodNumberStartCol = -1;
        int barkodNumberEndCol = -1;
        pixelValueSum = 0;
        for (int j = 0; j < imgColSize; j++) {
            for (int i = endRowofBarkod; i < imgRowSize; i++) {
                pixelValueSum += imgMtr[i][j];
            }
            pixelValueSum /= (imgRowSize - endRowofBarkod);
            if (pixelValueSum < 254 && blackFlag == false && whiteFlag == true && !endoftheBarkod) {

                blackFlag = true;
                whiteFlag = false;
                barkodNumberStartCol = j;

            }
            if (pixelValueSum >= 255 && blackFlag == true && whiteFlag == false) {

                endoftheBarkod = true;
                barkodNumberEndCol = j; //j +1;
                blackFlag = false;
                whiteFlag = true;
                //break;
            }
            if (pixelValueSum < 254 && blackFlag == false && whiteFlag == true && endoftheBarkod) {

                blackFlag = true;
                whiteFlag = false;

                //break;
            }

        }

        Log.i(TAG, String.valueOf(barkodNumberStartCol));
        Log.i(TAG, String.valueOf(barkodNumberEndCol));

        Rect rectCrop = new Rect(barkodNumberStartCol, barkodNumberStartRow, barkodNumberEndCol - barkodNumberStartCol, barkodNumberEndRow - barkodNumberStartRow);

        Mat image_roi = new Mat(img, rectCrop);
        writeFile("crop_img.png", image_roi);

        //create matrix of crop_roi pixel values
        int crop_imgColSize = image_roi.cols();
        int crop_imgRowSize = image_roi.rows();
        int[][] crop_imgMtr = new int[crop_imgRowSize][crop_imgColSize];
        double[] crop_temp;
        for (int i = 0; i < crop_imgRowSize; i++) {
            for (int j = 0; j < crop_imgColSize; j++) {

                crop_temp = image_roi.get(i, j);
                crop_imgMtr[i][j] = (int) crop_temp[0];

            }
        }

        //Find

        ArrayList<Character> characterList = new ArrayList<>();
        ArrayList<Integer> spaceX_Y = new ArrayList<>();

        whiteFlag = false;
        blackFlag = true;
        endoftheBarkod = false;
        int numberStartCol = -1;
        int numberEndCol = -1;
        pixelValueSum = 0;
        int spaceIndex = 0;
        for (int j = 0; j < crop_imgColSize; j++) {
            for (int i = 0; i < crop_imgRowSize; i++) {
                pixelValueSum += crop_imgMtr[i][j];
            }
            pixelValueSum /= (crop_imgRowSize);
            if (pixelValueSum < 254 && blackFlag == false && whiteFlag == true && !endoftheBarkod) {

                blackFlag = true;
                whiteFlag = false;
                barkodNumberStartCol = j;

            }
            if (pixelValueSum >= 255 && blackFlag == true && whiteFlag == false) {

                endoftheBarkod = true;
                numberEndCol = j;
                blackFlag = false;
                whiteFlag = true;
                spaceX_Y.add(numberEndCol);
                spaceIndex++;

            }
            if (pixelValueSum < 254 && blackFlag == false && whiteFlag == true && endoftheBarkod) {

                blackFlag = true;
                whiteFlag = false;
                spaceX_Y.add(j);
                spaceIndex++;
            }

        }

        //extract characters
        for (int i = 0; i < (spaceX_Y.size()); i++) {
            Character chr = new Character();
            if (i == 0) {
                chr.setLeft_top(0);
                chr.setLeft_bottom(crop_imgRowSize);
                chr.setRight_top(spaceX_Y.get(i));
            } else if (i == (spaceX_Y.size() - 1)) {
                chr.setLeft_top(spaceX_Y.get(i));
                chr.setLeft_bottom(crop_imgRowSize);
                chr.setRight_top(crop_imgColSize);
            } else {
                chr.setLeft_top(spaceX_Y.get(i));
                chr.setLeft_bottom(crop_imgRowSize);
                chr.setRight_top(spaceX_Y.get(++i));
            }

            characterList.add(chr);
        }

        //Crop chars
        ArrayList<Mat> imgMat = new ArrayList<Mat>();
        for (int i = 0; i < (spaceX_Y.size() / 2) + 1; i++) {
            Rect rectChar;
            if (i == 0) {
                rectChar = new Rect(0, 0, characterList.get(i).getRight_top() - characterList.get(i).getLeft_top(), characterList.get(i).getLeft_bottom());

            } else {
                rectChar = new Rect(characterList.get(i).getLeft_top(), 0, characterList.get(i).getRight_top() - characterList.get(i).getLeft_top(), characterList.get(i).getLeft_bottom());
            }

            Mat charImg = new Mat(image_roi, rectChar);
            imgMat.add(charImg);
            //Highgui.imwrite("charImg_" + i + ".png", charImg);

        }

        //resize crop char images to 10X20
        Size s = new Size(10, 20);
        for (int i = 0; i < imgMat.size(); i++) {
            Imgproc.resize(imgMat.get(i), imgMat.get(i), s);
            Imgproc.threshold(imgMat.get(i), imgMat.get(i), 200, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
            writeFile("charImg_" + i + ".png", imgMat.get(i));
        }

        // For database images
        ArrayList<Mat> databaseImgMat = new ArrayList<Mat>();
        File cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), FOLDER);
        for (int i = 0; i < 11; i++) {
            File file = new File(cacheDir,"" + i + ".png");
            Mat databaseImage = Highgui.imread(file.getAbsolutePath(), Highgui.CV_LOAD_IMAGE_GRAYSCALE);
            Imgproc.resize(databaseImage, databaseImage, s);

            Imgproc.threshold(databaseImage, databaseImage, 200, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
            writeFile(i + ".png", databaseImage);
            databaseImgMat.add(databaseImage);
        }

        //Matrix Matching
        ArrayList<Integer> ocrResultChars = new ArrayList<>();
        ArrayList<Integer> matchingSimilarity = new ArrayList<>();
        double[] tempBarkod;
        double[] tempDatabase;


        for (int i = 0; i < imgMat.size(); i++) {
            Mat barkodMat = imgMat.get(i);
            for (int k = 0; k < databaseImgMat.size(); k++) {
                matchingSimilarity.add(0);
            }
            for (int databaseIndex = 0; databaseIndex < 11; databaseIndex++) {
                Mat databaseMat = databaseImgMat.get(databaseIndex);
                for (int j = 0; j < barkodMat.rows(); j++) {
                    for (int j2 = 0; j2 < barkodMat.cols(); j2++) {
                        tempBarkod = barkodMat.get(j, j2);
                        tempDatabase = databaseMat.get(j, j2);
                        if (tempBarkod[0] == tempDatabase[0]) {
                            matchingSimilarity.set(databaseIndex, matchingSimilarity.get(databaseIndex) + 1);
                        }

                    }
                }

            }

            //find max similarity
            int max = matchingSimilarity.get(0);
            int rootIndex = 0;
            for (int i2 = 1; i2 < matchingSimilarity.size(); i2++) {
                if (max < matchingSimilarity.get(i2)) {
                    max = matchingSimilarity.get(i2);
                    rootIndex = i2;
                }
            }
            if (rootIndex == 10) //ikinci stil 1 iÁin
                rootIndex = 1;
            ocrResultChars.add(rootIndex);
            matchingSimilarity.clear();

        }

        return ocrResultChars;
    }

    private static void writeFile(String name, Mat values) {
        File file = new File(FOLDER, name);
        Highgui.imwrite(file.getAbsolutePath(), values);
    }

    public static void copyAssets(Context context) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for(int i=0; i < 11 ; i++) {
            String filename = String.valueOf(i) + ".png";
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);

                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), FOLDER) ;

                File outFile = new File(dir, filename);


                out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
        }
    }
    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
