package fr.hedwin.objects;

import java.time.LocalDate;

public class TransactionTable {

    private int id;
    private Tiers tiers;
    private String information;
    private Tiers remboursement;
    private Compte compte;
    private Categorie categorie;
    private PaymentType paymentType;
    private double montant;
    private double solde;
    private LocalDate date;

    public TransactionTable(int id, Tiers tiers, String information, Tiers remboursement, Compte compte, Categorie categorie, PaymentType paymentType, double montant, double solde, LocalDate date) {
        this.id = id;
        this.tiers = tiers;
        this.information = information;
        this.remboursement = remboursement;
        this.compte = compte;
        this.categorie = categorie;
        this.paymentType = paymentType;
        this.montant = montant;
        this.solde = solde;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public Tiers getTiers() {
        return tiers;
    }

    public String getInformation() {
        return information;
    }

    public Tiers getRemboursement() {
        return remboursement;
    }

    public Compte getCompte() {
        return compte;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public double getMontant() {
        return montant;
    }

    public double getSolde() {
        return solde;
    }

    public LocalDate getDate() {
        return date;
    }

}
