package server.forex.util.datetime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateUtil {
  public static final String yyyy_MM_dd = "yyyy-MM-dd";
  public static final String yyyyMMdd = "yyyyMMdd";
  public static final String yyyy_MM = "yyyy-MM";
  public static final String yyyyMM = "yyyyMM";
  public static final String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
  public static final String yyyy_MM_dd_HH_mm_ss_SSS = "yyyy-MM-dd HH:mm:ss:SSS";
  public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
  public static final String yyyy_MM_dd_zh = "yyyy年MM月dd日";
  public static final String yyyy_MM_dd_HH_mm_ss_zh = "yyyy年MM月dd日HH时mm分ss秒";
  public static final String yyyy_MM_dd_HH_mm_zh = "yyyy年MM月dd日HH时mm分";
  private static ThreadLocal dateFormat = new ThreadLocal() {
    protected Object initialValue() {
      return new SimpleDateFormat("yyyy-MM-dd");
    }
  };
  private static ThreadLocal yyyyMMdd_dateFormat = new ThreadLocal() {
    protected Object initialValue() {
      return new SimpleDateFormat("yyyyMMdd");
    }
  };
  private static ThreadLocal yyyy_MM_dd_HH_mm_ss_DateTimeFormat = new ThreadLocal() {
    protected Object initialValue() {
      return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
  };
  private static ThreadLocal yyyy_MM_dd_zh_DateTimeFormat = new ThreadLocal() {
    protected Object initialValue() {
      return new SimpleDateFormat("yyyy年MM月dd日");
    }
  };
  private static ThreadLocal yyyy_MM_dd_HH_mm_ss_zh_DateTimeFormat = new ThreadLocal() {
    protected Object initialValue() {
      return new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
    }
  };
  private static ThreadLocal yyyyMMddHHmmss_DateTimeFormat = new ThreadLocal() {
    protected Object initialValue() {
      return new SimpleDateFormat("yyyyMMddHHmmss");
    }
  };

  public DateUtil() {
  }

  public static DateFormat getDateFormat(String formatStr) {
    if (formatStr.equalsIgnoreCase("yyyy-MM-dd")) {
      return (DateFormat)dateFormat.get();
    } else if (formatStr.equalsIgnoreCase("yyyy-MM-dd HH:mm:ss")) {
      return (DateFormat)yyyy_MM_dd_HH_mm_ss_DateTimeFormat.get();
    } else if (formatStr.equalsIgnoreCase("yyyy年MM月dd日")) {
      return (DateFormat)yyyy_MM_dd_zh_DateTimeFormat.get();
    } else if (formatStr.equalsIgnoreCase("yyyy年MM月dd日HH时mm分ss秒")) {
      return (DateFormat)yyyy_MM_dd_HH_mm_ss_zh_DateTimeFormat.get();
    } else if (formatStr.equalsIgnoreCase("yyyyMMddHHmmss")) {
      return (DateFormat)yyyyMMddHHmmss_DateTimeFormat.get();
    } else {
      return (DateFormat)(formatStr.equalsIgnoreCase("yyyyMMdd") ? (DateFormat)yyyyMMdd_dateFormat.get() : new SimpleDateFormat(formatStr));
    }
  }

  public static Date strToDate(String str) {
    return strToDate(str, "yyyy-MM-dd HH:mm:ss");
  }

  public static Date strToDate(String str, String style) {
    try {
      if (str != null && !str.equals("")) {
        DateFormat sdf = getDateFormat(style);
        Date d = sdf.parse(str);
        return d;
      } else {
        return null;
      }
    } catch (ParseException var4) {
      throw new RuntimeException(var4);
    }
  }

  public static String dateToStr(long date, String style) {
    return dateToStr(new Date(date), style);
  }

  public static String dateToStr(long date) {
    return dateToStr(new Date(date), "yyyy-MM-dd HH:mm:ss");
  }

  public static String dateToStr(Date date, String style) {
    DateFormat df = getDateFormat(style);
    return df.format(date);
  }

  public static String getCuryyyy_MM_dd() {
    return dateToStr(Calendar.getInstance().getTime(), "yyyy-MM-dd");
  }

  public static String getCuryyyyMMdd() {
    return dateToStr(Calendar.getInstance().getTime(), "yyyyMMdd");
  }

  public static int getCuryyyyMMddInteger() {
    return Integer.parseInt(dateToStr(Calendar.getInstance().getTime(), "yyyyMMdd"));
  }

  public static long getCuryyyyyyyyMMddHHmmssL() {
    return Long.parseLong(dateToStr(Calendar.getInstance().getTime(), "yyyyMMddHHmmss"));
  }

  public static String getCuryyyyMMddzh() {
    return dateToStr(new Date(), "yyyy年MM月dd日");
  }

  public static String getCurDateTime() {
    return dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss");
  }

  public static String getCurDateTimezh() {
    return dateToStr(new Date(), "yyyy年MM月dd日HH时mm分ss秒");
  }

  public static Date getInternalDateByYear(Date d, int years) {
    Calendar now = Calendar.getInstance(TimeZone.getDefault());
    now.setTime(d);
    now.add(1, years);
    return now.getTime();
  }

  public static Date getInternalDateBySec(Date d, int sec) {
    Calendar now = Calendar.getInstance(TimeZone.getDefault());
    now.setTime(d);
    now.add(13, sec);
    return now.getTime();
  }

  public static Date getInternalDateByMin(Date d, int min) {
    Calendar now = Calendar.getInstance(TimeZone.getDefault());
    now.setTime(d);
    now.add(12, min);
    return now.getTime();
  }

  public static Date getInternalDateByHour(Date d, int hours) {
    Calendar now = Calendar.getInstance(TimeZone.getDefault());
    now.setTime(d);
    now.add(11, hours);
    return now.getTime();
  }

  public static String strToStr(String fromStr, String fromStyle, String toStyle) {
    Date d = strToDate(fromStr, fromStyle);
    return dateToStr(d, toStyle);
  }

  public static long compareDateStr(String time1, String time2) {
    Date d1 = strToDate(time1);
    Date d2 = strToDate(time2);
    return d2.getTime() - d1.getTime();
  }

  public static long compareDate(Date time1, Date time2) {
    return time2.getTime() - time1.getTime();
  }

  public static int getMin(Date d) {
    Calendar now = Calendar.getInstance(TimeZone.getDefault());
    now.setTime(d);
    return now.get(12);
  }

  public static int getHour(Date d) {
    Calendar now = Calendar.getInstance(TimeZone.getDefault());
    now.setTime(d);
    return now.get(11);
  }

  public static int getSecond(Date d) {
    Calendar now = Calendar.getInstance(TimeZone.getDefault());
    now.setTime(d);
    return now.get(13);
  }

  public static int getMilliSecond(Date d) {
    Calendar now = Calendar.getInstance(TimeZone.getDefault());
    now.setTime(d);
    return now.get(14);
  }

  public static int getDay(Date d) {
    Calendar now = Calendar.getInstance(TimeZone.getDefault());
    now.setTime(d);
    return now.get(5);
  }

  public static int getMonth(Date d) {
    Calendar now = Calendar.getInstance(TimeZone.getDefault());
    now.setTime(d);
    return now.get(2) + 1;
  }

  public static int getYear(Date d) {
    Calendar now = Calendar.getInstance(TimeZone.getDefault());
    now.setTime(d);
    return now.get(1);
  }

  public static String getYearMonthOfDate(Date d) {
    return dateToStr(d, "yyyyMM");
  }

  public static String getYearMonthOfLastMonth() {
    return dateToStr(new Date(addMonth((new Date()).getTime(), -1)), "yyyyMM");
  }

  public static String getCurYearMonth() {
    Calendar now = Calendar.getInstance(TimeZone.getDefault());
    String DATE_FORMAT = "yyyyMM";
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    sdf.setTimeZone(TimeZone.getDefault());
    return sdf.format(now.getTime());
  }

  public static int getCurentMonthDays() {
    Date date = Calendar.getInstance().getTime();
    return getMonthDay(date);
  }

  public static int getMonthDay(Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    return c.getActualMaximum(5);
  }

  public static long addMonth(Calendar oldTime, int months) {
    int year = oldTime.get(1);
    int month = oldTime.get(2);
    int date = oldTime.get(5);
    int hour = oldTime.get(11);
    int minute = oldTime.get(12);
    int second = oldTime.get(13);
    Calendar cal = new GregorianCalendar(year, month + months, date, hour, minute, second);
    return cal.getTime().getTime();
  }

  public static long addDay(long oldTime, int day) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(oldTime);
    return addDay(c, day);
  }

  public static long addDay(Calendar oldTime, int day) {
    int year = oldTime.get(1);
    int month = oldTime.get(2);
    int date = oldTime.get(5);
    int hour = oldTime.get(11);
    int minute = oldTime.get(12);
    int second = oldTime.get(13);
    Calendar cal = new GregorianCalendar(year, month, date + day, hour, minute, second);
    return cal.getTime().getTime();
  }

  public static long addMonth(long oldTime, int months) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(oldTime);
    return addMonth(c, months);
  }

  public static long getCurDayStarttime() {
    return strToDate(getCuryyyy_MM_dd() + " 00:00:00").getTime();
  }

  public static Date getMonday(Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.set(7, 2);
    return c.getTime();
  }

  public static Date getFriday(Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.set(7, 6);
    return c.getTime();
  }

  public static Date getMonthFirstDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(5, calendar.getActualMinimum(5));
    return calendar.getTime();
  }

  public static Date getMonthLastDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(5, calendar.getActualMaximum(5));
    return calendar.getTime();
  }

  public static Date getSeasonFirstDay(Date date) {
    Calendar cDay = Calendar.getInstance();
    cDay.setTime(date);
    int curMonth = cDay.get(2);
    if (curMonth >= 0 && curMonth <= 2) {
      cDay.set(2, 0);
    }

    if (curMonth >= 3 && curMonth <= 5) {
      cDay.set(2, 3);
    }

    if (curMonth >= 6 && curMonth <= 8) {
      cDay.set(2, 6);
    }

    if (curMonth >= 9 && curMonth <= 11) {
      cDay.set(2, 9);
    }

    cDay.set(5, cDay.getActualMinimum(5));
    return cDay.getTime();
  }

  public static Date getSeasonLastDay(Date date) {
    Calendar cDay = Calendar.getInstance();
    cDay.setTime(date);
    int curMonth = cDay.get(2);
    if (curMonth >= 0 && curMonth <= 2) {
      cDay.set(2, 2);
    }

    if (curMonth >= 3 && curMonth <= 5) {
      cDay.set(2, 5);
    }

    if (curMonth >= 6 && curMonth <= 8) {
      cDay.set(2, 8);
    }

    if (curMonth >= 9 && curMonth <= 11) {
      cDay.set(2, 11);
    }

    cDay.set(5, cDay.getActualMaximum(5));
    return cDay.getTime();
  }

  public static Date getYearFirstDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(6, 1);
    return calendar.getTime();
  }

  public static int getBeforeOrAfterDate(int day) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(5, day);
    return Integer.parseInt(dateToStr(calendar.getTime(), "yyyyMMdd"));
  }

  public static Date getYearLastDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(6, 1);
    calendar.roll(6, -1);
    return calendar.getTime();
  }

  public static void everyDayInYear(int year) {
    for(int m = 1; m < 13; ++m) {
      int month = m;
      Calendar cal = Calendar.getInstance();
      cal.set(1, year);
      cal.set(2, m - 1);
      int count = cal.getActualMaximum(5);

      for(int j = 1; j <= count; ++j) {
        StringBuilder sb = new StringBuilder();
        sb.append(year);
        if (month < 10) {
          sb.append("0");
        }

        sb.append(month);
        if (j < 10) {
          sb.append("0");
        }

        sb.append(j);
        System.out.println(sb);
      }
    }

  }

  public static void main(String[] args) {
    new DateUtil();
    everyDayInYear(2015);
    new DateUtil();
    everyDayInYear(2016);
    new DateUtil();
    everyDayInYear(2017);
  }
}

