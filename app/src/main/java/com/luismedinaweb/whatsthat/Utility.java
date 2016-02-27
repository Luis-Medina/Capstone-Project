package com.luismedinaweb.whatsthat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lmedina on 2/16/2016.
 */
public class Utility {

    public static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
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


}
