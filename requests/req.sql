SET @COMPTE_IDX = 2;
SET @DATE_FROM = '2021-08-31';
SET @DATE_TO = '2022-09-01';

-- transactions avec nom
/*select t.id, tid.name as tiers, t.information, rid.name as remboursement, categorie.name as categorie, mdp.name as mdp, t.montant,
(select  cast((select SUM(montant) from transactions where t.comptes_id = comptes_id and t.date_op >= date_op) as decimal(10,2))) as solde, t.date_op from transactions as t
LEFT JOIN tiers as tid ON tid.id = t.tiers_id
LEFT JOIN tiers as rid ON rid.id = t.remboursement
LEFT JOIN comptes ON comptes.id = t.comptes_id
LEFT JOIN categorie ON categorie.id = t.categorie_id
LEFT JOIN mdp ON mdp.id = t.mdp_id
where t.comptes_id = @COMPTE_IDX and t.date_op > @DATE_FROM and t.date_op < @DATE_TO order by t.date_op desc;*/


-- balance categorie par mois
select DATE_FORMAT(date_op, '%m-%y') as mois, categorie_id, sum(montant) as valeur from transactions
where comptes_id = 2 and date_op > @DATE_FROM and date_op < @DATE_TO group by YEAR(date_op), MONTH(date_op), categorie_id;

-- balance categorie
select categorie.name as name, sum(montant) as valeur from transactions
LEFT JOIN categorie ON categorie.id = categorie_id
where comptes_id = 2 and date_op >= @DATE_FROM and date_op <= @DATE_TO group by categorie_id;

-- balance categorie non remboursé
select categorie.name as name, sum(montant) as valeur from transactions
LEFT JOIN categorie ON categorie.id = categorie_id
where comptes_id = 2 and remboursement is null and date_op >= @DATE_FROM and date_op <= @DATE_TO group by categorie_id;

-- balance categorie
select categorie.name as name, sum(montant) as valeur from transactions
LEFT JOIN categorie ON categorie.id = categorie_id
where comptes_id = 2 and date_op >= @DATE_FROM and date_op <= @DATE_TO group by categorie_id;

-- moyenne dépense par catégorie
select categorie.name as name, sum(montant)/count(categorie_id) as valeur from transactions
LEFT JOIN categorie ON categorie.id = categorie_id
where comptes_id = 2 and date_op >= @DATE_FROM and date_op <= @DATE_TO group by categorie_id;

-- dépense en plus sur l'épargne
select cast((select sum(montant) from transactions where tiers_id = 5 and date_op > @DATE_FROM and date_op < @DATE_TO) +
            (select sum(montant) from transactions where tiers_id = 4 and date_op > @DATE_FROM and date_op < @DATE_TO) +
            (select sum(montant) from transactions where tiers_id = 7 and date_op > @DATE_FROM and date_op < @DATE_TO) as DECIMAL(10,2)) as montant;