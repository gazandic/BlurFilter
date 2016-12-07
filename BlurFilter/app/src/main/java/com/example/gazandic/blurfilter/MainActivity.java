package com.example.gazandic.blurfilter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.gazandic.blurfilter.Adapter.ColorListAdapter;
import com.example.gazandic.blurfilter.ViewModel.ColorListViewModel;
import com.example.gazandic.blurfilter.ViewModel.MainActivityListener;
import com.example.gazandic.blurfilter.constant.RequestConstant;
import com.example.gazandic.blurfilter.databinding.ActivityMainBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MainActivityListener{
    private boolean onLoading = false;
    private static final int CAMERA_REQUEST = 1 ;
    private ActivityMainBinding binding;
    private ImageFiltering img;
    private ColorListAdapter adapter;
    private ColorListViewModel listViewModel;
    private String filePath;
    private Bitmap bitmap;
    private int mode = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content);
        binding = DataBindingUtil.inflate(this.getLayoutInflater(), R.layout.activity_main, viewGroup, false);
        View view = binding.getRoot();

        adapter = new ColorListAdapter(this, new ArrayList<NewColor>());
        adapter.setOnItemClickListener(new ColorListAdapter.ColorListClickListener() {
            @Override
            public void onItemClick(NewColor newColor) {

            }
        });
        listViewModel = new ColorListViewModel(this);
        binding.setItemList(listViewModel);
        binding.incNoItem.rlErrorNoItemFound.setVisibility(View.VISIBLE);
        setContentView(view);
        img = new ImageFiltering();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestConstant.REQUEST_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver()
                    .query(selectedImage, filePathColumn, null, null, null);

            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
            onLoading = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mode, menu);
        menu.getItem(0).getIcon().setColorFilter(getResources().getColor(R.color.map_orange), PorterDuff.Mode.MULTIPLY);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_load_more) {
            if (mode == 1) {
                mode = 0;
                item.getIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
            }
            else {
                mode = 1;
                item.getIcon().setColorFilter(getResources().getColor(R.color.map_orange), PorterDuff.Mode.MULTIPLY);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume(){
        super.onResume();
        if (onLoading) {
            showLoading();
            setData();
            onLoading = false;
        }

        binding.incNoItem.rlErrorNoItemFound.setVisibility(View.VISIBLE);

        binding.detail.setVisibility(View.GONE);
        if (bitmap != null)
        {
            binding.detail.setVisibility(View.VISIBLE);
            if (binding.imageafterfilter != null)
                binding.imageafterfilter.setImageBitmap(bitmap);

            binding.incNoItem.rlErrorNoItemFound.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();

    }

    //Use onSaveInstanceState(Bundle) and onRestoreInstanceState
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putParcelable("bitmap", bitmap);
        savedInstanceState.putParcelable("img", img);
        savedInstanceState.putInt("mode", mode);
        // etc.
        super.onSaveInstanceState(savedInstanceState);
    }

    //onRestoreInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        bitmap = savedInstanceState.getParcelable("bitmap");
        if (bitmap != null) {
            binding.incNoItem.rlErrorNoItemFound.setVisibility(View.GONE);
        }
        img = savedInstanceState.getParcelable("img");
        mode = savedInstanceState.getInt("mode");
    }

    private void showLoading() {
        adapter.showProgressBar(true);
        binding.incNoItem.rlErrorNoItemFound.setVisibility(View.GONE);
    }

    @Override
    public void onImageSaved() {
        if (bitmap != null) {
            saveImage(bitmap);
            Context context = getApplicationContext();
            CharSequence text = "Image saved at pengcit folder!";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    @Override
    public void onBrightnessChanged(int i) {
        if(img != null) {
            img.setBrightness(i);
            bitmap = img.retBitmap(mode);
            binding.imageafterfilter.setImageBitmap(bitmap);
        }
        binding.tvBrightness.setText("Brightness " + (i-50));
    }

    @Override
    public void onContrastChanged(int i) {
        if (img != null) {
            img.setContrast(i);
            bitmap = img.retBitmap(mode);
            binding.imageafterfilter.setImageBitmap(bitmap);
        }
        binding.tvContrast.setText("Contrast " + (i-50));

    }

    @Override
    public void onHistChanged(int i) {
        if (img != null) {
            img.setHistogram(i);
            bitmap = img.retBitmapAfterFilterGrayscale(mode);
            binding.imageafterfilter.setImageBitmap(bitmap);
        } else
        binding.tvHistogram.setText("Histogram " + (i-50));

    }

    @Override
    public void onPrewitt8() {
        if (img != null) {
//            bitmap = img.sobeloperator(mode);
            int[] sobel = getResources().getIntArray(R.array.prewitt8);
            List<Integer> list = new ArrayList<>();
            for(int each : sobel) {
                list.add(each);
            }
            Matrix matrix = new Matrix(list);
            bitmap = img.matrixLoader(mode, matrix, 2 , 8);

            binding.imageafterfilter.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onSmooth() {
        if (img != null) {
            bitmap = img.smoothing(mode);
            binding.imageafterfilter.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onKirsch() {
        if (img != null) {
//            bitmap = img.sobeloperator(mode);
            int[] sobel = getResources().getIntArray(R.array.kirschoperator);
            List<Integer> list = new ArrayList<>();
            for(int each : sobel) {
                list.add(each);
            }
            Matrix matrix = new Matrix(list);
            bitmap = img.matrixLoader(mode, matrix, 2 , 8);

            binding.imageafterfilter.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onRobinson3() {
        if (img != null) {
//            bitmap = img.sobeloperator(mode);
            int[] sobel = getResources().getIntArray(R.array.prewittoperator);
            List<Integer> list = new ArrayList<>();
            for(int each : sobel) {
                list.add(each);
            }
            Matrix matrix = new Matrix(list);
            bitmap = img.matrixLoader(mode, matrix, 2 , 8);

            binding.imageafterfilter.setImageBitmap(bitmap);
        }

    }

    @Override
    public void onRobinson5() {
        if (img != null) {
//            bitmap = img.sobeloperator(mode);
            int[] sobel = getResources().getIntArray(R.array.sobeloperator                                                                                                                      );
            List<Integer> list = new ArrayList<>();
            for(int each : sobel) {
                list.add(each);
            }
            Matrix matrix = new Matrix(list);
            bitmap = img.matrixLoader(mode, matrix, 2 , 8);

            binding.imageafterfilter.setImageBitmap(bitmap);
        }

    }

    @Override
    public void onFaceDetect() {
        if (img != null) {
//            bitmap = img.sobeloperator(mode);
            int[] sobel = getResources().getIntArray(R.array.sobeloperator );
            List<Integer> list = new ArrayList<>();
            for(int each : sobel) {
                list.add(each);
            }
            Matrix matrix = new Matrix(list);
            bitmap = img.faceDetect(mode, matrix, 2 , 8);
            binding.imageafterfilter.setImageBitmap(bitmap);
        }


    }

    @Override
    public void onPrewitt() {
        if (img != null) {
//            bitmap = img.sobeloperator(mode);
            int[] sobel = getResources().getIntArray(R.array.prewittoperator);
            List<Integer> list = new ArrayList<>();
            for(int each : sobel) {
                list.add(each);
            }
            Matrix matrix = new Matrix(list);
            bitmap = img.matrixLoader(mode, matrix, 1 , 1);

            binding.imageafterfilter.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onSharpen() {
        if (img != null) {
            bitmap = img.sharpen(mode);
            binding.imageafterfilter.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onRobert() {
        if (img != null) {
            bitmap = img.robertscross(mode);
            binding.imageafterfilter.setImageBitmap(bitmap);
        }
    }
    @Override
    public void onBlur() {
        if (img != null) {
            bitmap = img.blur(mode);
            binding.imageafterfilter.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onEqualization() {
        if (img != null) {
            bitmap = img.retBitmapAfterFilterGrayscale(mode);
            binding.imageafterfilter.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onFrei() {
        if (img != null) {
            bitmap = img.freioperator(mode);
            binding.imageafterfilter.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onSobel() {
        if (img != null) {
            int[] sobel = getResources().getIntArray(R.array.sobeloperator);
            List<Integer> list = new ArrayList<>();
            for(int each : sobel) {
                list.add(each);
            }
            Matrix matrix = new Matrix(list);
            bitmap = img.matrixLoader(mode, matrix, 1 , 1);

            binding.imageafterfilter.setImageBitmap(bitmap);
        }
    }

    public void saveImage(Bitmap bitmap){
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File( Environment.getExternalStorageDirectory(),File.separator+"Pengcit"); //my folder name where I want to save.
//        String receiverN = receiverName.getText().toString();

        myDir.mkdirs();
        Calendar c = Calendar.getInstance();
        String month, day, year, hour, minute, second;
        month = ""+ (c.get(Calendar.MONTH)+1);
        day = "" + c.get(Calendar.DAY_OF_MONTH);
        year = "" + c.get(Calendar.YEAR);
        hour = ""+c.get(Calendar.HOUR_OF_DAY);
        minute = "" + c.get(Calendar.MINUTE);
        int seconds = c.get(Calendar.SECOND);
        if (seconds<10) second = "0"+ seconds;
        else second = ""+seconds;

        String fname = "hasil eh" + "-" + hour + ":" + minute + ":" + second + "/"  + month + "-" + day + "-" + year +".jpg";
        fname = encodeString(fname);
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file); //from here it goes to catch block
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            String[] paths = {file.toString()};
            String[] mimeTypes = {"/image/jpeg"};
            MediaScannerConnection.scanFile(this, paths, mimeTypes, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PackageManager.PERMISSION_GRANTED);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant

                return;
            }
        }
    }

    public static String encodeString(String string) {
        if (string != null) {
            string = string.replace("/","-");
            string = string.replace(" ","_");
            string = string.replace(":", "-");
            string = string.replace("&", "-");
        }
        return string;
    }

    private void setData() {
        img.filter(ImageManipulation.compressImage(filePath, getApplicationContext()));
        bitmap = img.retFFTBitmap(mode);
        binding.imageafterfilter.setImageBitmap(bitmap);
        binding.sbBrightness.setProgress(50);
        binding.sbContrast.setProgress(50);
        binding.sbHistogram.setProgress(50);
        adapter.clearList();
        List<NewColor> nc = new ArrayList<NewColor>();
        for (int i=0;i<256;i++) {
            nc.add(new NewColor(i,i,i));
        }
        adapter.addList(nc);
        hideLoading();
    }

    private void hideLoading() {
        adapter.showProgressBar(false);
        if (adapter.getItemCount() == 0) {
            binding.incNoItem.rlErrorNoItemFound.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onAddFindMeButtonClicked() {
        Log.d("lol","lol");
        onPermission();
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, RequestConstant.REQUEST_GALLERY);
    }


}
