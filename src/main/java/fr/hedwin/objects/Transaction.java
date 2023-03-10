package fr.hedwin.objects;

import java.util.Date;

public class Transaction {

    private int id;
    private int tiers_id;
    private String information;
    private int remboursement;
    private int compte_id;
    private int categories_id;
    private int mdp_id;
    private double montant;
    private Date date;

    public Transaction(int id, int tiers_id, String information, Integer remboursement, int compte_id, int categories_id, int mdp_id, double montant, Date date) {
        this.id = id;
        this.tiers_id = tiers_id;
        this.information = information;
        this.remboursement = remboursement;
        this.compte_id = compte_id;
        this.categories_id = categories_id;
        this.mdp_id = mdp_id;
        this.montant = montant;
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

    public int getRemboursement() {
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

    public Date getDate() {
        return date;
    }

    public void setId(int id) {
        this.id = id;
    }
}
