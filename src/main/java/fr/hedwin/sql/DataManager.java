package fr.hedwin.sql;

import com.opencsv.CSVWriter;
import com.opencsv.ResultSetHelperService;
import fr.hedwin.ihm.IHMP;
import fr.hedwin.objects.Categorie;
import fr.hedwin.objects.Compte;
import fr.hedwin.objects.PaymentType;
import fr.hedwin.objects.Tiers;
import fr.hedwin.sql.dao.DaoFactory;
import fr.hedwin.sql.exceptions.DaoException;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {

    private final DaoFactory daoFactory;
    private IHMP ihmp;
    private Map<Integer, Compte> compteMap;
    private Map<Integer, Tiers> tiersMap;
    private Map<Integer, PaymentType> paymentTypeMap;
    private Map<Integer, Categorie> categorieMap;

    public DataManager(IHMP ihmp, DaoFactory daoFactory) throws DaoException {
        this.ihmp = ihmp;
        this.daoFactory = daoFactory;
        this.compteMap = daoFactory.getTransactionDao().getCompte();
        this.tiersMap = daoFactory.getTransactionDao().getTiers();
        this.paymentTypeMap = daoFactory.getTransactionDao().getMDPs();
        this.categorieMap = daoFactory.getTransactionDao().getCategories();
    }

    public DataManager(DaoFactory daoFactory) throws DaoException {
        this.daoFactory = daoFactory;
        this.compteMap = daoFactory.getTransactionDao().getCompte();
        this.tiersMap = daoFactory.getTransactionDao().getTiers();
        this.paymentTypeMap = daoFactory.getTransactionDao().getMDPs();
        this.categorieMap = daoFactory.getTransactionDao().getCategories();
    }

    public DaoFactory getDaoFactory() {
        return daoFactory;
    }

    public Map<Integer, Categorie> getCategorieMap() {
        return categorieMap;
    }

    public Categorie addCategorie(String name) throws DaoException {
        int id = daoFactory.request("insert into categorie(name) values (?)", "select last_insert_id() as id;", preparedStatement -> {
            preparedStatement.setString(1, name);
        }, resultat -> {
            if(resultat.next()){
                return resultat.getInt("id");
            }
            return null;
        });
        Categorie categorie = new Categorie(id, name);
        categorieMap.put(id, categorie);
        return categorie;
    }

    public void updateCategorie(int id, String name) throws DaoException {
        daoFactory.request("update categorie set name = ? where id = ?;", preparedStatement -> {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, id);
        });
        categorieMap.get(id).setName(name);
    }

    public void deleteCategorie(int id) throws DaoException {
        daoFactory.request("delete from categorie where id = ?;", preparedStatement -> {
            preparedStatement.setInt(1, id);
        });
        categorieMap.remove(id);
    }

    public Map<Integer, Compte> getCompteMap() {
        return compteMap;
    }

    public Compte addCompte(String name) throws DaoException {
        int id = daoFactory.request("insert into comptes(name) values (?)", "select last_insert_id() as id;", preparedStatement -> {
            preparedStatement.setString(1, name);
        }, resultat -> {
            if(resultat.next()){
                return resultat.getInt("id");
            }
            return null;
        });
        Compte compte = new Compte(id, name);
        compteMap.put(id, new Compte(id, name));
        return compte;
    }

    public void updateCompte(int id, String name) throws DaoException {
        daoFactory.request("update comptes set name = ? where id = ?;", preparedStatement -> {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, id);
        });
        compteMap.get(id).setName(name);
    }

    public void deleteCompte(int id) throws DaoException {
        daoFactory.request("delete from comptes where id = ?;", preparedStatement -> {
            preparedStatement.setInt(1, id);
        });
        compteMap.remove(id);
    }

    public Map<Integer, PaymentType> getPaymentTypeMap() {
        return paymentTypeMap;
    }

    public PaymentType addPaymentType(String name, String regex) throws DaoException {
        int id = daoFactory.request("insert into mdp(name,cm_name) values (?,?)", "select last_insert_id() as id;", preparedStatement -> {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, regex);
        }, resultat -> {
            if(resultat.next()){
                return resultat.getInt("id");
            }
            return null;
        });
        PaymentType paymentType = new PaymentType(id, name, regex);
        paymentTypeMap.put(id, paymentType);
        return paymentType;
    }

    public void updatePaymentType(int id, String name) throws DaoException {
        daoFactory.request("update mdp set name = ? where id = ?;", preparedStatement -> {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, id);
        });
        paymentTypeMap.get(id).setName(name);
    }

    public void updatePaymentTypeRegex(int id, String regex) throws DaoException {
        daoFactory.request("update mdp set cm_name = ? where id = ?;", preparedStatement -> {
            preparedStatement.setString(1, regex);
            preparedStatement.setInt(2, id);
        });
        paymentTypeMap.get(id).setRegex(regex);
    }

    public void deletePaymentType(int id) throws DaoException {
        daoFactory.request("delete from mdp where id = ?;", preparedStatement -> {
            preparedStatement.setInt(1, id);
        });
        paymentTypeMap.remove(id);
    }

    public Map<Integer, Tiers> getTiersMap() {
        return tiersMap;
    }

    public Tiers addTiers(String name, String cm_name) throws DaoException {
        int id = daoFactory.request("insert into tiers(name,cm_name) values (?,?)", "select last_insert_id() as id;", preparedStatement -> {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, cm_name);
        }, resultat -> {
            if(resultat.next()){
                return resultat.getInt("id");
            }
            return null;
        });
        Tiers tiers = new Tiers(id, name, cm_name);
        tiersMap.put(id, tiers);
        return tiers;
    }

    public void updateTiers(int id, String name) throws DaoException {
        daoFactory.request("update tiers set name = ? where id = ?;", preparedStatement -> {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, id);
        });
        tiersMap.get(id).setName(name);
    }

    public void updateTiersRegex(int id, String regex) throws DaoException {
        daoFactory.request("update tiers set cm_name = ? where id = ?;", preparedStatement -> {
            preparedStatement.setString(1, regex);
            preparedStatement.setInt(2, id);
        });
        tiersMap.get(id).setRegex(regex);
    }

    public void deleteTiers(int id) throws DaoException {
        daoFactory.request("delete from tiers where id = ?;", preparedStatement -> {
            preparedStatement.setInt(1, id);
        });
        tiersMap.remove(id);
    }

    public void exportToCSV(String fileName, String request){
        try(CSVWriter writer = new CSVWriter(new FileWriter(fileName), ',', (char)0, (char)0, "\n")){
            ResultSetHelperService resultSetHelperService= new ResultSetHelperService();
            resultSetHelperService.setDateFormat("yyyy-MM-dd");
            resultSetHelperService.setDateTimeFormat("yyyy-MM-dd HH:MI:SS");
            writer.setResultService(resultSetHelperService);
            daoFactory.request(request, resultSet -> {
                try {
                    writer.writeAll(resultSet, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            });
        } catch (IOException | DaoException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Map<String, String>> propertyColumnsTable(String table) throws DaoException {
        return daoFactory.request("""
        SELECT CONSTRAINT_NAME, TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME FROM information_schema.KEY_COLUMN_USAGE
         WHERE
           TABLE_SCHEMA = 'finance' AND
           TABLE_NAME = 'transactions' AND
           REFERENCED_TABLE_NAME is not null;
        """, resultSet -> {
            Map<String, Map<String, String>> t = new HashMap<>();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            while (resultSet.next()){
                String cn = resultSet.getString("COLUMN_NAME");
                for(int i = 1; i <= resultSetMetaData.getColumnCount(); i++){
                    String ob = resultSet.getString(i);
                    if(t.containsKey(cn)){
                        t.get(cn).put(resultSetMetaData.getColumnName(i), ob);
                    }else{
                        int finalI = i;
                        t.put(cn, new HashMap<>(){{put(resultSetMetaData.getColumnName(finalI), ob);}});
                    }
                }
            }
            return t;
        });
    }

}
