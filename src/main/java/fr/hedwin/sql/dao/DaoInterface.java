package fr.hedwin.sql.dao;

import fr.hedwin.sql.exceptions.DaoException;
import fr.hedwin.sql.utils.Selectable;

import java.util.Map;

public interface DaoInterface<K, T> {
    void add(T t) throws DaoException;
    void add(Map<K, T> map) throws DaoException;
    void update(T t) throws DaoException;
    void delete(K id) throws DaoException;
    Map<K, T> select(Selectable selectable) throws DaoException;
    Map<K, T> lister() throws DaoException;
    Object other(Selectable selectable) throws DaoException;
    void truncate() throws DaoException;
}
