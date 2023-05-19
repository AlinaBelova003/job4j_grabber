package grabber;

import grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    private Connection conn;

    /**
     * Конструктор класса PsqlStore используется для создания объекта,
     * который обеспечивает взаимодействие с базой данных PostgreSQL.
     * @param cfg Свойства для подключения
     */
    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
           conn = DriverManager.getConnection(
                   cfg.getProperty("url"),
                   cfg.getProperty("login"),
                   cfg.getProperty("password")
           );
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Сохраняет объект Post в базу данных с помощью PreparedStatement
     * Мы также используем Statement.RETURN_GENERATED_KEYS, чтобы получить сгенерированный идентификатор.
     * Мы вызываем метод getGeneratedKeys() объекта PreparedStatement, который возвращает объект ResultSet, содержащий только один столбец сгенерированными ключами.
     * Затем мы вызываем метод getInt(), чтобы получить сгенерированный идентификатор, и устанавливаем его в объект Post.
     * Используем дополнительный параметр ON CONFLICT (link) DO NOTHING, который указывает PostgreSQL игнорировать вставку,
     * если уже существует запись с тем же значением в столбце link. Это позволяет избежать дублирования записей в таблице.
     */
    @Override
    public Post save(Post post) {
        try (PreparedStatement statement = conn.prepareStatement("INSERT INTO post(name, text, link, created) VALUES( ?, ?, ?, ?) ON CONFLICT (link) DO NOTHING;",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setObject(4, post.getCreated());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return post;
    }

    @Override
    public List<Post> getAll() {
        List<Post> postList = new ArrayList<>();
        try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM post")) {
            try (ResultSet resultSet = statement.executeQuery()) {
               while (resultSet.next()) {
                   postList.add(new Post(
                           resultSet.getInt("id"),
                           resultSet.getString("name"),
                           resultSet.getString("text"),
                           resultSet.getString("link"),
                           resultSet.getTimestamp("created").toLocalDateTime()
                   ));
               }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return postList;
    }

    @Override
    public Post findById(int id) {
        Post post = new Post();
        try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM post where id = ?")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                post.setId(resultSet.getInt("id"));
                post.setTitle(resultSet.getString("name"));
                post.setDescription(resultSet.getString("text"));
                post.setLink(resultSet.getString("link"));
                post.setCreated(resultSet.getTimestamp("created").toLocalDateTime());
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (conn != null) {
            conn.close();
        }
    }

    /**
     * Метод создаёт и загружает свойства из файла "post.properties"
     * Чтобы мы могли на основе его сконструировать объект PsqlStore
     */
    public static Properties load() {
        Properties properties = new Properties();
        try (InputStream inputStream = PsqlStore.class.getClassLoader().getResourceAsStream("post.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    /**
     * 1. Создает объект Post, инициализируя его поля значениями.
     * 2. Создает объект PsqlStore, передавая в его конструктор результат вызова метода load(), который загружает свойства из файла "post.properties".
     * 3. Сохраняет созданный объект Post в базе данных, вызывая метод save у объекта PsqlStore.
     * 4. Получает все записи из базы данных, вызывая метод getAll у объекта PsqlStore.
     * 5. Ищет запись с идентификатором 2 в базе данных, вызывая метод findById у объекта PsqlStore.
     *
     * Код демонстрирует пример использования объекта PsqlStore для сохранения и получения записей из базы данных.
     */
    public static void main(String[] args) {
        Post post = new Post(1, "java developer", "java developer job", "www.example.com",
                LocalDateTime.of(2023, 5, 15, 12, 24, 0));
        Post post2 = new Post(2, "Разработчик базы данных для рекламной системы", "java developer Middle", "https://yandex.ru/jobs/vacancies/разработчик-базы",
                LocalDateTime.of(2023, 6, 18, 6, 24, 0));
        Post post3 = new Post(3, "Разработчик сервиса", "java developer ТГ", "sberbank.com",
                LocalDateTime.of(2023, 3, 25, 12, 43, 0));
        PsqlStore psqlStore = new PsqlStore(load());
        post = psqlStore.save(post);
        System.out.println(psqlStore.getAll());
        System.out.println(psqlStore.findById(post.getId()).toString());
    }
}
