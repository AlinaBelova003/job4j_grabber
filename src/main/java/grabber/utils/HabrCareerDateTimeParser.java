package grabber.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddТhh:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(parse, formatter);
        return localDateTime;
    }

}
