package fr.hedwin.sql.utils;

import fr.hedwin.sql.exceptions.DaoException;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLFunction<T, R> {
    R apply(T t) throws SQLException, DaoException;

    @FunctionalInterface
    public interface Bi<T, U, R> {
        R apply(T t, U u) throws SQLException, DaoException;
    }

}
