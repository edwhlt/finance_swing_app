package fr.hedwin.sql.dao;

import fr.hedwin.Main;
import fr.hedwin.ihm.IHMP;
import fr.hedwin.objects.Transaction;
import fr.hedwin.sql.ScriptRunner;
import fr.hedwin.sql.exceptions.DaoException;
import fr.hedwin.sql.tables.*;
import fr.hedwin.sql.utils.SQLConsumer;
import fr.hedwin.sql.utils.SQLFunction;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DaoFactory {

    private static String SUPP_URL = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

    private static DaoFactory daoFactory;

    private final String url;
    private final String username;
    private final String password;

    DaoFactory(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static DaoFactory getInstance() {
        Properties properties = new Properties();
        try {
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("db.properties");
            properties.load(inputStream);
        } catch (IOException ignored) {}

        String url = properties.getProperty("db.url", "localhost:3306");
        String username = properties.getProperty("db.username", "root");
        String password = properties.getProperty("db.password", "");
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("'com.mysql.cj.jdbc.Driver' introuvable !");
            e.printStackTrace();
        }
        if(daoFactory == null) daoFactory = new DaoFactory("jdbc:mariadb://"+url+"/finance?allowMultiQueries=true", username, password);
        return daoFactory;
    }

    public Connection connection(String information) throws SQLException {
        Connection connexion = DriverManager.getConnection(url, username, password);
        connexion.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        //connexion.setAutoCommit(false);
        if(information != null) System.out.println("SQL Info: "+information);
        return connexion;
    }

    public TransactionDaoImpl getTransactionDao(){
        return new TransactionDaoImpl(this);
    }

    public void request(String request, SQLConsumer<PreparedStatement> procedure) throws DaoException {
        Connection connexion = null;
        PreparedStatement statement = null;
        try {
            connexion = connection(null);
            statement = connexion.prepareStatement(request);
            procedure.accept(statement);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw DaoException.BDDErrorAcess(throwables);
        } finally {
            if(connexion != null) {
                try {
                    connexion.close();
                } catch (SQLException throwables) {
                    throw DaoException.BDDErrorAcess(throwables);
                }
            }
        }
    }

    public void request(String request) throws DaoException {
        request(request, preparedStatement -> {
            preparedStatement.executeUpdate();
        });
    }

    public <T> List<T> requestList(String request, SQLFunction<ResultSet, T> procedure) throws DaoException {
        return request(request, (SQLFunction<ResultSet, List<T>>) resultSet ->
            Stream.generate(() -> {
                try {
                    if(resultSet.next()) return procedure.apply(resultSet);
                } catch (SQLException | DaoException e) {
                    throw new RuntimeException(e);
                }
                return null;
            })
            .limit(Long.MAX_VALUE)
            .takeWhile(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedList::new))
        );
    }

    public <T> T request(String request, SQLFunction<ResultSet, T> procedure) throws DaoException {
        Connection connexion = null;
        Statement statement = null;
        ResultSet resultat = null;
        try {
            connexion = connection(null);
            statement = connexion.createStatement();
            resultat = statement.executeQuery(request);
            return procedure.apply(resultat);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw DaoException.BDDErrorAcess(throwables);
        } finally {
            if(connexion != null) {
                try {
                    connexion.close();
                } catch (SQLException throwables) {
                    throw DaoException.BDDErrorAcess(throwables);
                }
            }
        }
    }

    public <T> T request(String request, String getRequest, SQLConsumer<PreparedStatement> procedure, SQLFunction<ResultSet, T> result) throws DaoException {
        Connection connexion = null;
        PreparedStatement statement = null;
        ResultSet resultat = null;
        try {
            connexion = connection(null);
            statement = connexion.prepareStatement(request);
            procedure.accept(statement);
            statement.executeUpdate();

            ResultSet resultSet = connexion.createStatement().executeQuery(getRequest);

            return result.apply(resultSet);
        } catch (SQLException throwables) {
            throw DaoException.BDDErrorAcess(throwables);
        } finally {
            if(connexion != null) {
                try {
                    connexion.close();
                } catch (SQLException throwables) {
                    throw DaoException.BDDErrorAcess(throwables);
                }
            }
        }
    }

}
