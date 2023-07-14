package fr.hedwin.sql.tables;

import fr.hedwin.objects.*;
import fr.hedwin.sql.ScriptRunner;
import fr.hedwin.sql.dao.DaoAdapter;
import fr.hedwin.sql.dao.DaoFactory;
import fr.hedwin.sql.exceptions.DaoException;
import fr.hedwin.sql.utils.Selectable;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TransactionDaoImpl extends DaoAdapter<Integer, Transaction> {

    private DaoFactory daoFactory;

    public TransactionDaoImpl(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    @Override
    public void add(Transaction transaction) throws DaoException {
        int id = daoFactory.request("INSERT INTO transactions (tiers_id, information, remboursement, categorie_id, comptes_id, mdp_id, montant, `change`, date_op) VALUES (?,?,?,?,?,?,?,?,?);", "SELECT last_insert_id() as id;", preparedStatement -> {
            preparedStatement.setInt(1, transaction.getTiers_id());
            preparedStatement.setString(2, transaction.getInformation());

            if(transaction.getRemboursement() != null) preparedStatement.setInt(3, transaction.getRemboursement());
            else preparedStatement.setNull(3, java.sql.Types.INTEGER);

            preparedStatement.setInt(4, transaction.getCategories_id());
            preparedStatement.setInt(5, transaction.getCompte_id());
            preparedStatement.setInt(6, transaction.getMdp_id());
            preparedStatement.setDouble(7, transaction.getMontant());
            preparedStatement.setDouble(8, transaction.getToEUR());
            preparedStatement.setDate(9, new Date(transaction.getDate().getTime()));
        }, resultat -> {
            if(resultat.next()){
                return resultat.getInt("id");
            }
            return null;
        });
        transaction.setId(id);
    }

    @Override
    public void add(List<Transaction> map) throws DaoException {
        daoFactory.request("INSERT INTO transactions (tiers_id, information, remboursement, categorie_id, comptes_id, mdp_id, montant, `change`, date_op) VALUES (?,?,?,?,?,?,?,?,?);", preparedStatement -> {
            for (Transaction transaction : map) {
                preparedStatement.setInt(1, transaction.getTiers_id());
                preparedStatement.setString(2, transaction.getInformation());
                if(transaction.getRemboursement() != null) preparedStatement.setInt(3, transaction.getRemboursement());
                else preparedStatement.setNull(3, java.sql.Types.INTEGER);
                preparedStatement.setInt(4, transaction.getCategories_id());
                preparedStatement.setInt(5, transaction.getCompte_id());
                preparedStatement.setInt(6, transaction.getMdp_id());
                preparedStatement.setDouble(7, transaction.getMontant());
                preparedStatement.setDouble(8, transaction.getToEUR());
                preparedStatement.setDate(9, new Date(transaction.getDate().getTime()));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        });
    }

    public void update(int id, String key, Consumer<PreparedStatement> consumer) throws DaoException {
        daoFactory.request("update transactions set "+key+" = ? where id = '"+id+"'", preparedStatement -> {
            consumer.accept(preparedStatement);
            preparedStatement.executeUpdate();
        });
    }

    @Override
    public void delete(Integer id) throws DaoException {
        daoFactory.request("delete from transactions where id = ?;", preparedStatement -> {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        });
    }

    @Override
    public Map<Integer, Transaction> select(Selectable selectable) throws DaoException {
        return daoFactory.request("select * from transactions "+selectable.toString(), resultat -> {
            while(resultat.next()){
                System.out.println(resultat.getInt("id"));
            }
            return null;
        });
    }

    public Map<String, Map<Integer, Object[]>> readSQLFile(String fileName) throws DaoException {
        try {
            Reader reader = new BufferedReader(new FileReader(fileName));
            ScriptRunner sr = new ScriptRunner(daoFactory.connection(null));
            sr.setAutoCommit(true);
            sr.setStopOnError(true);
            sr.runScript(reader);

            Map<String, Map<Integer, Object[]>> objects = sr.getObjects();
            return objects;
        } catch (SQLException | IOException sqlException) {
            sqlException.printStackTrace();
            throw DaoException.BDDErrorAcess(sqlException);
        }
    }

    public Map<Integer, Compte> getCompte() throws DaoException {
        return daoFactory.request("select * from comptes", resultat -> {
            Map<Integer, Compte> compteMap = new HashMap<>();
            while(resultat.next()){
                int id = resultat.getInt("id");
                String name = resultat.getString("name");
                compteMap.put(id, new Compte(id, name));
            }
            return compteMap;
        });
    }

    public Map<Integer, Tiers> getTiers() throws DaoException {
        return daoFactory.request("select * from tiers", resultat -> {
            Map<Integer, Tiers> compteMap = new HashMap<>();
            while(resultat.next()){
                int id = resultat.getInt("id");
                String name = resultat.getString("name");
                String cm_name = resultat.getString("cm_name");
                compteMap.put(id, new Tiers(id, name, cm_name));
            }
            return compteMap;
        });
    }

    public Map<Integer, Categorie> getCategories() throws DaoException {
        return daoFactory.request("select * from categorie", resultat -> {
            Map<Integer, Categorie> compteMap = new HashMap<>();
            while(resultat.next()){
                int id = resultat.getInt("id");
                String name = resultat.getString("name");
                compteMap.put(id, new Categorie(id, name));
            }
            return compteMap;
        });
    }

    public Map<Integer, PaymentType> getMDPs() throws DaoException {
        return daoFactory.request("select * from mdp", resultat -> {
            Map<Integer, PaymentType> compteMap = new HashMap<>();
            while(resultat.next()){
                int id = resultat.getInt("id");
                String name = resultat.getString("name");
                String cm_name = resultat.getString("cm_name");
                compteMap.put(id, new PaymentType(id, name, cm_name));
            }
            return compteMap;
        });
    }

    public LinkedHashMap<Integer, TransactionTable> getTableElement(Compte compte, Map<Integer, Tiers> tiersMap, Map<Integer, Categorie> categorieMap, Map<Integer, PaymentType> mdps, String dateFrom, String dateTo) throws DaoException{
        return daoFactory.request("""
            select t.id, t.tiers_id, t.information, t.remboursement, t.categorie_id, t.mdp_id, t.montant, t.change,
            (select  cast((select SUM(montant) from transactions where t.comptes_id = comptes_id and t.date_op >= date_op) as decimal(10,2))) as solde, t.date_op from transactions as t
            LEFT JOIN tiers as tid ON tid.id = t.tiers_id
            LEFT JOIN tiers as rid ON rid.id = t.remboursement
            LEFT JOIN comptes ON comptes.id = t.comptes_id
            LEFT JOIN categorie ON categorie.id = t.categorie_id
            LEFT JOIN mdp ON mdp.id = t.mdp_id
            where t.comptes_id =""" +compte.getId()+" and t.date_op >= '"+dateFrom+"' and t.date_op <= '"+dateTo+"' order by t.date_op desc;", resultat -> {
            LinkedHashMap<Integer, TransactionTable> elements = new LinkedHashMap<>();

            while(resultat.next()){
                int id = resultat.getInt("id");
                int tiers_id = resultat.getInt("tiers_id");
                String infos = resultat.getString("information");
                int remboursement = resultat.getInt("remboursement");
                int categorie_id = resultat.getInt("categorie_id");
                int mdp_id = resultat.getInt("mdp_id");
                double montant = resultat.getDouble("montant");
                double solde = resultat.getDouble("solde");
                double change = resultat.getDouble("change");
                java.sql.Date date = resultat.getDate("date_op");

                elements.put(id, new TransactionTable(id, tiersMap.get(tiers_id), infos, tiersMap.get(remboursement), compte, categorieMap.get(categorie_id), mdps.get(mdp_id), montant, change, solde, date.toLocalDate()));
            }

            return elements;
        });

    }

}
