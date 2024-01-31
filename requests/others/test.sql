-- compte
select * from comptes;

SET @COMPTE_IDX = 2;
SET @DATE_FROM = '2019-08-31';
SET @DATE_TO = '2020-09-01';

-- transactions
select * from transactions as t where t.tiers_id = 95 and t.comptes_id = @COMPTE_IDX and t.date_op > @DATE_FROM and t.date_op < @DATE_TO order by t.date_op desc;

-- transactions 2
select *, (select  cast((select SUM(montant) from transactions where t.tiers_id = 4 and t.date_op >= date_op) as decimal(10,2))) as solde from transactions as t where t.tiers_id = 4 order by t.date_op desc;


-- drop kick
select sum(montant) from transactions where tiers_id = 72 and date_op >= "2021-07-01";

-- roadtrip italie
select t.id, tid.name as tiers, t.information, t.categorie_id, t.montant, (select  cast((select SUM(montant) from transactions where t.remboursement = remboursement and categorie_id = 49 and t.date_op >= date_op) as decimal(10,2))) as solde, t.date_op from transactions as t
LEFT JOIN tiers as tid ON tid.id = t.tiers_id
where t.remboursement = 189 and t.categorie_id = 49 order by t.date_op desc;