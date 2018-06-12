package cn.com.btc.utils;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class MyDateFormat {
    public static long getLongTime() {
        return System.currentTimeMillis();
    }

    public static Date parse(String pattern, String strDate)
            throws ParseException {
        SimpleDateFormat lsdf = getSdf();
        lsdf.setTimeZone(timezone);
        lsdf.applyPattern(pattern);
        return lsdf.parse(strDate);
    }

    public static Date parse(Locale locale, String pattern, String strDate)
            throws ParseException {
        SimpleDateFormat lsdf = getLocaleSdf();
        lsdf.setDateFormatSymbols(DateFormatSymbols.getInstance(locale));
        lsdf.setTimeZone(timezone);
        lsdf.applyPattern(pattern);
        return lsdf.parse(strDate);
    }

    public static String format(String pattern, Date date) {
        SimpleDateFormat lsdf = getSdf();
        lsdf.setTimeZone(timezone);
        lsdf.applyPattern(pattern);
        return lsdf.format(date);
    }

    public static String format(Locale locale, String pattern, Date date) {
        SimpleDateFormat lsdf = getLocaleSdf();
        lsdf.setDateFormatSymbols(DateFormatSymbols.getInstance(locale));
        lsdf.setTimeZone(timezone);
        lsdf.applyPattern(pattern);
        return lsdf.format(date);
    }

    public static String convertFromTo(String srcPattern, String dstPattern,
                                       String strDate) throws ParseException {
        SimpleDateFormat lsdf = getSdf();
        lsdf.setTimeZone(timezone);
        lsdf.applyPattern(srcPattern);
        Date date = lsdf.parse(strDate);
        lsdf.setTimeZone(timezone);
        lsdf.applyPattern(dstPattern);
        return lsdf.format(date);
    }

    private MyDateFormat() {
        throw new UnsupportedOperationException();
    }

    private static ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>();

    private static SimpleDateFormat getSdf() {
        SimpleDateFormat lsdf = sdf.get();
        if (lsdf != null) {
            return lsdf;
        }
        lsdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.set(lsdf);
        return lsdf;
    }

    private static ThreadLocal<SimpleDateFormat> localeSdf = new ThreadLocal<SimpleDateFormat>();

    private static SimpleDateFormat getLocaleSdf() {
        SimpleDateFormat lsdf = localeSdf.get();
        if (lsdf != null) {
            return lsdf;
        }
        lsdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        localeSdf.set(lsdf);
        return lsdf;
    }

    private static final TimeZone timezone = TimeZone.getDefault();
}
