package fr.hedwin.sql.dao;

import fr.hedwin.sql.exceptions.DaoException;
import fr.hedwin.sql.utils.Selectable;

import java.util.List;
import java.util.Map;

public abstract class DaoAdapter<K, T> implements DaoInterface<K, T> {

    @Override
    public void add(T t) throws DaoException {

    }

    @Override
    public void add(List<T> map) throws DaoException {

    }

    @Override
    public void update(T t) throws DaoException {

    }

    @Override
    public void delete(K id) throws DaoException {

    }

    @Override
    public Map<K, T> select(Selectable selectable) throws DaoException {
        return null;
    }

    @Override
    public Map<K, T> lister() throws DaoException {
        return select(new Selectable());
    }

    @Override
    public Object other(Selectable selectable) throws DaoException {
        return null;
    }

    @Override
    public void truncate() throws DaoException {

    }
}
