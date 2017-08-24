package ru.stepf.filemanager;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends ListActivity {

    private List<String> mPathList = null;
    private Comparator<? super File> mComparator;
    private String root = "/";
    private String mCurrentPath;
    private TextView mPathTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPathTextView = (TextView)findViewById(R.id.textViewPath);
        mComparator = fileComparator;
        root = Environment.getExternalStorageDirectory().getPath();
        getDir(root);
    }
    void getDir(String dirPath){
        mPathTextView.setText("Путь: " + dirPath);
        mCurrentPath = dirPath;

        ArrayList<String> itemList = new ArrayList<>();
        mPathList = new ArrayList<>();
        File dir = new File(dirPath);
        File[] fileList = dir.listFiles();

        if(!dirPath.equals(root)){
            itemList.add("root");
            mPathList.add(root);
            itemList.add("../");
            mPathList.add(dir.getParent());
        }

        Arrays.sort(fileList, mComparator);

        for (File file : fileList){
            if(!file.isHidden() && file.canRead()) {
                mPathList.add(file.getPath());
                if (file.isDirectory()) {
                    itemList.add(file.getName() + "/");
                } else {
                    itemList.add(file.getName());
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, itemList);
        setListAdapter(adapter);
    }

    Comparator<? super File>fileComparator = new Comparator<File>() {
        @Override
        public int compare(File file1, File file2) {
            if (file1.isDirectory()) {
                if (file2.isDirectory()) {
                    return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
                } else {
                    return -1;
                }
            } else {
                if (file2.isDirectory()) {
                    return 1;
                } else {
                    return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
                }
            }
        }
    };

    Comparator<? super File>fileComparatorByLastModified = new Comparator<File>() {
        @Override
        public int compare(File file1, File file2) {
            if (file1.isDirectory()) {
                if (file2.isDirectory()) {
                    return Long.valueOf(file1.lastModified()).compareTo(
                            file2.lastModified());
                } else {
                    return -1;
                }
            } else {
                if (file2.isDirectory()) {
                    return 1;
                } else {
                    return Long.valueOf(file1.lastModified()).compareTo(
                            file2.lastModified());
                }
            }
        }
    };

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        File file = new File(mPathList.get(position));
        if(file.isDirectory()){
            if(file.canRead()) {
                getDir(file.getPath());
            }else {
                new AlertDialog.Builder(this)
                        .setTitle("Папка " + mPathList.get(position) + " недоступна")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();

            }
        }else {

            String exifAttribute = null;
            String filename = file.getName();
            String ext = filename.substring(filename.lastIndexOf('.') + 1, filename.length());

            if(ext.equalsIgnoreCase("JPG")){
                try {
                    ExifInterface exif = new ExifInterface(file.toString());
                    exifAttribute = getExif(exif);
                }catch (IOException ex){

                }
            }

            String info = "Абсолютный путь: " + file.getAbsolutePath()
                    + "\n" + "Путь: " + file.getPath() + "\n"
                    +"Родитель: " + file.getParent() + "\n"
                    +"Последнее изменение: " + new Date(file.lastModified()) +"\n"
                    +"Имя: " + file.getName();


            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("Информация о файле")
                    .setMessage(info + " " +exifAttribute)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }
    }

    private String getExif(ExifInterface exif){
        String attribute = null;
        attribute += getTagString(ExifInterface.TAG_DATETIME, exif);
        attribute += getTagString(ExifInterface.TAG_FLASH,exif);
        attribute += getTagString(ExifInterface.TAG_GPS_LATITUDE,exif);
        attribute += getTagString(ExifInterface.TAG_GPS_LATITUDE_REF,exif);
        attribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE,exif);
        attribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE_REF,exif);
        attribute += getTagString(ExifInterface.TAG_IMAGE_LENGTH,exif);
        attribute += getTagString(ExifInterface.TAG_IMAGE_WIDTH,exif);
        attribute += getTagString(ExifInterface.TAG_MAKE,exif);
        attribute += getTagString(ExifInterface.TAG_MODEL,exif);
        attribute += getTagString(ExifInterface.TAG_ORIENTATION,exif);
        attribute += getTagString(ExifInterface.TAG_WHITE_BALANCE,exif);
        return attribute;
    }

    private String getTagString(String tag, ExifInterface exif){
        return (tag + " : " + exif.getAttribute(tag) + " ");
    }

    public void onDateModClick(View view) {
        mComparator = fileComparatorByLastModified;
        getDir(mCurrentPath);
    }

    public void onAlphabetClick(View view) {
        mComparator = fileComparator;
        getDir(mCurrentPath);
    }
}
