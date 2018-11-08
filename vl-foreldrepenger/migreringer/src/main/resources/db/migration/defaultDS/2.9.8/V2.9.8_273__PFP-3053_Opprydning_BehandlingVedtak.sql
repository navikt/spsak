update behandling_vedtak
set vedtak_resultat_type = 'AVSLAG'
where vedtak_resultat_type = 'OPPHØR';
