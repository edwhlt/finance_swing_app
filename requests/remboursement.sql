-- nombre transactions remboursées
select count(montant) from transactions where remboursement = 6 and montant < 0;

-- 100% rembousé non rendu
select cast(sum(montant) as DECIMAL(10,2)) as montant from transactions where categorie_id = 26 and remboursement is null;

-- solde remboursement évolution
select t.id, tid.name as tiers, t.information, t.montant, (select  cast((select SUM(montant) from transactions where t.remboursement = remboursement and t.date_op >= date_op) as decimal(10,2))) as solde, t.date_op from transactions as t
LEFT JOIN tiers as tid ON tid.id = t.tiers_id
where t.remboursement = 6 and t.date_op >= '2021-09-10' order by t.date_op desc;