package com.project.android.bicing;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Martin on 10/06/2015.
 */
public class Utils {
    public static String latin2utf(String latin) {
        String str = latin;
        str = str.replaceAll("&aacute;","á");
        str = str.replaceAll("&agrave;","à");
        str = str.replaceAll("&eacute;","é");
        str = str.replaceAll("&egrave;","è");
        str = str.replaceAll("&iacute;","í");
        str = str.replaceAll("&igrave;","ì");
        str = str.replaceAll("&oacute;","ó");
        str = str.replaceAll("&ograve;","ò");
        str = str.replaceAll("&uacute;","ú");
        str = str.replaceAll("&ugrave;","ù");
        str = str.replaceAll("&Aacute;","Á");
        str = str.replaceAll("&Agrave;","À");
        str = str.replaceAll("&Eacute;","É");
        str = str.replaceAll("&Egrave;","È");
        str = str.replaceAll("&Iacute;","Í");
        str = str.replaceAll("&Igrave;","Ì");
        str = str.replaceAll("&Oacute;","Ó");
        str = str.replaceAll("&Ograve;","Ò");
        str = str.replaceAll("&Uacute;","Ú");
        str = str.replaceAll("&Ugrave;","Ù");
        str = str.replaceAll("&ntilde;","ñ");
        str = str.replaceAll("&Ntilde;","Ñ");
        str = str.replaceAll("&ccedil;","ç");
        str = str.replaceAll("&Ccedil;","Ç");
        str = str.replaceAll("&Ntilde;","Ñ");
        str = str.replaceAll("&#039;","'");
        str = str.replaceAll("&uuml;","ü");
        str = str.replaceAll("&middot;","·");

        return str;
    }

    public static boolean getViewAllPref(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_key_check_all),
                true);
    }
}


