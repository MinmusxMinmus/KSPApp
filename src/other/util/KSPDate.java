package other.util;

import other.KSPObject;
import controller.ControllerInterface;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
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

    public KSPDate(ControllerInterface controller, int year, int day, int hour, int minute, int second, OffsetDateTime date) {
        super(controller);
        this.year = year;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.realDate = date;
    }

    public KSPDate(ControllerInterface controller, int year, int day, int hour, int minute, int second) {
        this(controller, year, day, hour, minute, second, OffsetDateTime.now());
    }

    public KSPDate(ControllerInterface controller, int year, int day, OffsetDateTime date) {
        this(controller, year, day, 0, 0, 0, date);
    }

    public KSPDate(ControllerInterface controller, int year, int day) {
        this(controller, year, day, OffsetDateTime.now());
    }

    public KSPDate(ControllerInterface controller, String stored) {
        super(controller);
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

    public static KSPDate fromString(ControllerInterface controller, String s) {
        if (s.split(DELIMITER).length != ENCODE_FIELD_AMOUNT) return null;
        return new KSPDate(controller, s);
    }

    @Override
    public void ready() { }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Year", Integer.toString(year)));
        fields.add(new Field("Day", Integer.toString(day)));
        fields.add(new Field("Hour", Integer.toString(hour)));
        fields.add(new Field("Minute", Integer.toString(minute)));
        fields.add(new Field("Second", Integer.toString(second)));
        fields.add(new Field("IRL date", realDate.format(DateTimeFormatter.ofPattern("MM/dd/uuuu, hh:mm:ss"))));

        return fields;
    }

    @Override
    public String toString() {
        return   "Y" + year +
                " D" + day +
                " H" + hour +
                " M" + minute +
                " S" + second +
                " (" + realDate.format(DateTimeFormatter.ofPattern("MM/dd/uuuu, hh:mm:ss")) + ")";
    }

    public String toString(boolean realDate, boolean fullDate) {
        StringBuilder sb = new StringBuilder();
        sb.append("Y")
                .append(year)
                .append(" D")
                .append(day);
        if (fullDate)
            sb.append(" H")
                    .append(hour)
                    .append(" M")
                    .append(minute)
                    .append(" S")
                    .append(second);
        if (realDate)
            sb.append(" (")
                    .append(this.realDate.format(DateTimeFormatter.ofPattern("MM/dd/uuuu, hh:mm:ss")) )
                    .append(")");
        return sb.toString();
    }

}
