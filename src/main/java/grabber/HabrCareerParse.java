package grabber;

import grabber.utils.DateTimeParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args)  {

    }

    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".style-ugc");
        return rows.text();
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> list = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            Connection connection = Jsoup.connect("%s?page=%s".formatted(list, i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Post post = new Post();
                Element titleElement = row.select(".vacancy-card__title").first();
                Elements dateElement = row.select(".vacancy-card__date");
                Element linkElement = titleElement.child(0);
                Element dateLinkElement = dateElement.first();
                String vacancyName = titleElement.text();
                String vacancyDate = dateLinkElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                try {
                    System.out.printf("%s %s%n %s%n %s", vacancyDate, vacancyName, link, retrieveDescription(link));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                list.add(post);
            });
        }
        return list;
    }
}
