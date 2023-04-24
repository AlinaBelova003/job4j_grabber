package grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class HabrCareerDateTimeParserTest {

    @Test
    public void whenDateRight() {
        HabrCareerDateTimeParser date = new HabrCareerDateTimeParser();
        LocalDateTime parsed = date.parse("2023-04-20T08:31:07+03:00");
        assertThat(parsed).isEqualTo("2023-04-20T08:31:07");
    }
}