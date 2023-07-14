package fr.hedwin.objects;

import java.util.Date;

public class Transaction {

    private int id;
    private int tiers_id;
    private String information;
    private Integer remboursement;
    private int compte_id;
    private int categories_id;
    private int mdp_id;
    private double montant;
    private double toEUR;
    private Date date;

    public Transaction(int id, int tiers_id, String information, Integer remboursement, int compte_id, int categories_id, int mdp_id, double montant, double toEUR, Date date) {
        this.id = id;
        this.tiers_id = tiers_id;
        this.information = information;
        this.remboursement = remboursement;
        this.compte_id = compte_id;
        this.categories_id = categories_id;
        this.mdp_id = mdp_id;
        this.montant = montant;
        this.toEUR = toEUR;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public int getTiers_id() {
        return tiers_id;
    }

    public String getInformation() {
        return information;
    }

    public Integer getRemboursement() {
        return remboursement;
    }

    public int getCategories_id() {
        return categories_id;
    }

    public int getCompte_id() {
        return compte_id;
    }

    public int getMdp_id() {
        return mdp_id;
    }

    public double getMontant() {
        return montant;
    }

    public double getToEUR() {
        return toEUR;
    }

    public Date getDate() {
        return date;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", tiers_id=" + tiers_id +
                ", information='" + information + '\'' +
                ", remboursement=" + remboursement +
                ", compte_id=" + compte_id +
                ", categories_id=" + categories_id +
                ", mdp_id=" + mdp_id +
                ", montant=" + montant +
                ", toEUR=" + toEUR +
                ", date=" + date +
                '}';
    }
}
