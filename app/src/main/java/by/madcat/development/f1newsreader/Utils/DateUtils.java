package by.madcat.development.f1newsreader.Utils;

public final class DateUtils {
    public static String transformDateTime(String dateTime){
        StringBuilder builder = new StringBuilder();

        String[] date = dateTime.split(",", 0);

        String dateNumber = date[0].split(" ")[0];
        String monthNumber = date[0].split(" ")[1];
        String yearNumber = date[0].split(" ")[2];

        builder.append(yearNumber).append("-");

        if(getMonthNumber(monthNumber) + 1 < 10)
            builder.append("0");
        builder.append(getMonthNumber(monthNumber) + 1).append("-");

        if(dateNumber.length() < 2)
            builder.append("0");
        builder.append(dateNumber);

        builder.append("_").append(date[1]);

        return builder.toString();
    }

    public static String untransformDateTime(String dateTime){
        StringBuilder builder = new StringBuilder();

        String[] date = dateTime.split("_", 0);

        String yearNumber = date[0].split("-")[0];
        String monthNumber = date[0].split("-")[1];
        String dateNumber = date[0].split("-")[2];

        builder.append(dateNumber).append(".").append(monthNumber).append(".").append(yearNumber);

        return builder.toString();
    }

    private static int getMonthNumber(String monthName){
        String[] months = new String[]{"января","февраля","марта",
                "апреля","мая","июня","июля","августа","сентября",
                "октября","ноября","декабря"};

        for(int i =0; i < months.length; i++)
            if(months[i].equals(monthName))
                return i;

        return -1;
    }
}
