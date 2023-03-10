package fr.hedwin.sql.exceptions;

public class DaoException extends Exception{
    public DaoException(String message) {
        super(message);
    }
    public static DaoException BDDErrorAcess(Exception sqlException){
        return new DaoException("Impossible de communiquer avec la base de données : "+sqlException.getMessage());
    }
}
