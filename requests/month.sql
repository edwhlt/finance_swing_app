SET @COMPTE_IDX = 2;
SET @DATE_FROM = '2020-08-31';
SET @DATE_TO = '2021-09-01';

-- balance categorie par mois

select categorie_id, sum(valeur)/count(mois) from (select DATE_FORMAT(date_op, '%m-%y') as mois, categorie_id, sum(montant) as valeur from transactions
where comptes_id = 2 and date_op > @DATE_FROM and date_op < @DATE_TO group by YEAR(date_op), MONTH(date_op), categorie_id) group by categorie_id;

