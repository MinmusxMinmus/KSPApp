package other;

import java.time.LocalDate;

public class KSPDate {
    private final int year;
    private final int day;
    private final int hour;
    private final int minute;
    private final int second;
    private final LocalDate realDate;

    public KSPDate(int year, int day, int hour, int minute, int second, LocalDate date) {
        this.year = year;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.realDate = date;
    }

    public KSPDate(int year, int day, int hour, int minute, int second) {
        this(year, day, hour, minute, second, LocalDate.now());
    }

    public KSPDate(int year, int day, LocalDate date) {
        this(year, day, 0, 0, 0, date);
    }

    public KSPDate(int year, int day) {
        this(year, day, LocalDate.now());
    }

    public int getYear() {
        return year;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getSecond() {
        return second;
    }

    public LocalDate getRealDate() {
        return realDate;
    }
}
