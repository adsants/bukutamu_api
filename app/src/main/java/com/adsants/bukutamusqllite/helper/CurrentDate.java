package com.adsants.bukutamusqllite.helper;

import java.util.Calendar;

public class CurrentDate {

    public static Calendar calendar = Calendar.getInstance();
    public static int year      =   calendar.get(Calendar.YEAR);
    public static int month      =   calendar.get(Calendar.MONTH);
    public static int day      =   calendar.get(Calendar.DAY_OF_MONTH);

}
