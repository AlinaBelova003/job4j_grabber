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

}
