package fr.hedwin;

import fr.hedwin.sql.DataManager;
import fr.hedwin.sql.dao.DaoFactory;
import fr.hedwin.sql.exceptions.DaoException;

import java.sql.ResultSetMetaData;
import java.util.Map;
import java.util.StringJoiner;

public class MainTesting {

    private static final DaoFactory daoFactory = DaoFactory.getInstance();
    public static void main(String[] args) throws DaoException {
        DataManager dataManager = new DataManager(daoFactory);
        Map<String, Map<String, String>> t = dataManager.propertyColumnsTable("transactions");

        StringJoiner sj = new StringJoiner(" ");
        StringJoiner column = new StringJoiner(", ");
        int i = 1;
        for(Map<String, String> v : t.values()) {
            String s = "left join "+v.get("REFERENCED_TABLE_NAME")+" as t"+i+" on t"+i+"."+v.get("REFERENCED_COLUMN_NAME")+" = t."+v.get("COLUMN_NAME");
            System.out.println(s);
            sj.add(s);
            column.add("t"+i+".name");
            i++;
        }

        daoFactory.request("select "+column+" from transactions as t "+ sj, resultSet -> {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            StringJoiner sc = new StringJoiner("\t\t");
            for(int j = 1; j <= resultSetMetaData.getColumnCount(); j++){
                sc.add(resultSetMetaData.getColumnName(j));
            }
            System.out.println(sc);

            while (resultSet.next()){
                StringJoiner stringJoiner = new StringJoiner("\t\t");
                for(int k = 1; k <= resultSetMetaData.getColumnCount(); k++){
                    stringJoiner.add(resultSet.getString(k));
                }
                System.out.println(stringJoiner);
            }
            return null;
        });
    }

}
