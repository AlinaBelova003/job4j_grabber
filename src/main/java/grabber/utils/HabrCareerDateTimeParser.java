package grabber.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        LocalDateTime date = LocalDateTime.parse(parse, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return date;
    }

}
