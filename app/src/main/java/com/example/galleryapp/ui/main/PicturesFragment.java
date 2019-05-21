package com.example.galleryapp.ui.main;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.galleryapp.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;


public class PicturesFragment extends Fragment {

    private ArrayList<String> mPicturesPathList;
    private final int EXTERNAL_STORAGE_PERMISSIONS = 12;

    public PicturesFragment() {
        // Required empty public constructor
    }

    public static PicturesFragment newInstance() {
        PicturesFragment fragment = new PicturesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getContext()));
        View view = inflater.inflate(R.layout.fragment_pictures, container, false);
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSIONS);
        mPicturesPathList = getFilePathsSplitUsingDate();
        GridView picturesGridView = (GridView)view.findViewById(R.id.pictures_grid_view);
        picturesGridView.setAdapter(new PicturesGridAdapter(getContext(), mPicturesPathList));
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ArrayList<String> getFilePathsSplitUsingDate()
    {
        Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA};
        Cursor c = null;
        SortedSet<String> dirList = new TreeSet<String>();
        ArrayList<File> resultIAV = new ArrayList<File>();

        String[] directories = null;
        if (u != null)
        {
            c = getContext().getContentResolver().query(u, projection, null, null, null);
        }

        if ((c != null) && (c.moveToFirst()))
        {
            do
            {
                String tempDir = c.getString(0);
                tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
                try{
                    dirList.add(tempDir);
                }
                catch(Exception e)
                {

                }
            }
            while (c.moveToNext());
            directories = new String[dirList.size()];
            dirList.toArray(directories);

        }

        for(int i=0;i<dirList.size();i++)
        {
            File imageDir = new File(directories[i]);
            File[] imageList = imageDir.listFiles();
            if(imageList == null)
                continue;
            for (File imagePath : imageList) {
                try {

                    if(imagePath.isDirectory())
                    {
                        imageList = imagePath.listFiles();

                    }
                    if ( imagePath.getName().contains(".jpg")|| imagePath.getName().contains(".JPG")
                            || imagePath.getName().contains(".jpeg")|| imagePath.getName().contains(".JPEG")
                            || imagePath.getName().contains(".png") || imagePath.getName().contains(".PNG")
                            || imagePath.getName().contains(".gif") || imagePath.getName().contains(".GIF")
                            || imagePath.getName().contains(".bmp") || imagePath.getName().contains(".BMP")
                    )
                    {
                        resultIAV.add(imagePath);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Collections.sort(resultIAV, LastModifiedFileComparator.LASTMODIFIED_REVERSE);

        ArrayList<String> fileList = new ArrayList<>();
        for(File f : resultIAV){
            fileList.add(f.toString());
        }

        return fileList;
    }

    class PicturesGridAdapter extends BaseAdapter{

        ArrayList<String> mPathList;
//        final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        public PicturesGridAdapter(Context context, ArrayList<String> pathList){
            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(context));
            mPathList = pathList;
        }

        @Override
        public int getCount() {
            return mPathList.size();
        }

        @Override
        public Object getItem(int position) {
            return mPathList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            BasicFileAttributes attributes = null;
//            try{
//                File f = new File(getItem(position).toString());
//                attributes = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
//            }catch (IOException e) {
//                System.out.println("Exception handled when trying to get file " +
//                        "attributes: " + e.getMessage());
//            }
//            String date = df.format(attributes.creationTime().toMillis()).toString();
            convertView = getLayoutInflater().inflate(R.layout.image_view_layout, parent, false);
            ImageView pictureView = (ImageView) convertView.findViewById(R.id.image_viewer);
            ImageLoader.getInstance().displayImage("file://" + getItem(position).toString(), pictureView);

            return convertView;
        }
    }
}
