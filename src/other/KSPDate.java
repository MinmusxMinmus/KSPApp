package other;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

public class KSPDate extends KSPObject {

    public static final String DELIMITER = ":KD:";
    public static final int ENCODE_FIELD_AMOUNT = 6;

    private final int year;
    private final int day;
    private final int hour;
    private final int minute;
    private final int second;
    private final OffsetDateTime realDate;

    public KSPDate(int year, int day, int hour, int minute, int second, OffsetDateTime date) {
        this.year = year;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.realDate = date;
    }

    public KSPDate(int year, int day, int hour, int minute, int second) {
        this(year, day, hour, minute, second, OffsetDateTime.now());
    }

    public KSPDate(int year, int day, OffsetDateTime date) {
        this(year, day, 0, 0, 0, date);
    }

    public KSPDate(int year, int day) {
        this(year, day, OffsetDateTime.now());
    }

    public KSPDate(String stored) {
        String[] parts = stored.split(DELIMITER);
        this.year = Integer.parseInt(parts[0]);
        this.day = Integer.parseInt(parts[1]);
        this.hour = Integer.parseInt(parts[2]);
        this.minute = Integer.parseInt(parts[3]);
        this.second = Integer.parseInt(parts[4]);
        this.realDate = OffsetDateTime.parse(parts[5]);
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

    public OffsetDateTime getRealDate() {
        return realDate;
    }

    public String toStorableString() {
        return new StringJoiner(DELIMITER)
                .add(Integer.toString(year))
                .add(Integer.toString(day))
                .add(Integer.toString(hour))
                .add(Integer.toString(minute))
                .add(Integer.toString(second))
                .add(realDate.toString())
                .toString();
    }

    public static KSPDate fromString(String s) {
        if (s.split(DELIMITER).length != ENCODE_FIELD_AMOUNT) return null;
        return new KSPDate(s);
    }

    @Override
    public int getFieldCount() {
        return 6;
    }

    @Override
    public String getFieldName(int index) {
        return switch (index) {
            case 0 -> "Year";
            case 1 -> "Day";
            case 2 -> "Hour";
            case 3 -> "Minute";
            case 4 -> "Second";
            case 5 -> "IRL date";
            default -> "???";
        };
    }

    @Override
    public String getFieldValue(int index) {
        return switch (index) {
            case 0 -> Integer.toString(year);
            case 1 -> Integer.toString(day);
            case 2 -> Integer.toString(hour);
            case 3 -> Integer.toString(minute);
            case 4 -> Integer.toString(second);
            case 5 -> realDate.toString();
            default -> "???";
        };
    }

    @Override
    public String getTextRepresentation() {
        return  "Y" + year +
                "D" + day +
                "H" + hour +
                "M" + minute +
                "S" + second +
                " (" + realDate.format(DateTimeFormatter.BASIC_ISO_DATE) + ")";
    }
}
