package fr.hedwin.sql.utils;

import fr.hedwin.sql.exceptions.DaoException;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLFunction<T, R> {
    R apply(T t) throws SQLException, DaoException;
}
