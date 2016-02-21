package com.luismedinaweb.whatsthat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lmedina on 2/16/2016.
 */
public class Utility {

    public static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    private static final int REQUEST_TAKE_PHOTO = 1;
    public static boolean TWO_PANE;


    public static String createFileName() {
        String timeStamp = new SimpleDateFormat(Utility.DATE_FORMAT).format(new Date());
        return timeStamp + "_wt.jpg";
    }

    public static Date getDateFromFileName(String fileName) {
        String datePart = fileName.substring(0, Utility.DATE_FORMAT.length());
        try {
            return new SimpleDateFormat(Utility.DATE_FORMAT).parse(datePart);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }
//    public static int rotateAngle(String photoPath){
//        ExifInterface ei = null;
//        int rotateAngle = 0;
//        try {
//            ei = new ExifInterface(photoPath);
//
//            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
//
//            switch(orientation) {
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    rotateAngle = 173;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    rotateAngle = 180;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    rotateAngle = 270;
//                    break;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return 0;
//    }


}
