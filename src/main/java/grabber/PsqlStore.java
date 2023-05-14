package grabber;

import java.sql.*;
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
    @Override
    public void save(Post post) {

    }

    @Override
    public List<Post> getAll() {
        List<Post> postList = new ArrayList<>();
        try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM post")) {
            try (ResultSet resultSet = statement.executeQuery()) {
               while (resultSet.next()) {
                   postList.add(new Post(
                           resultSet.getInt("id"),
                           resultSet.getString("title"),
                           resultSet.getString("link"),
                           resultSet.getString("description"),
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
        try (PreparedStatement statement = conn.prepareStatement("INSERT INTO post(title, link) VALUES (?, ?) RETURNING (id)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString("Что сюда вставить?")
            statement.setString();
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {

                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
