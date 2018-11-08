--Sletter periodeårsaken fra_skjæringstidspunkt, alle som hadde den før får nå kode = -
update BEREGNINGSGRUNNLAG_PERIODE
set
periode_aarsak = '-'
where
periode_aarsak = 'FRA_SKJÆRINGSTIDSPUNKTET';

DELETE FROM KODELISTE WHERE KODEVERK = 'PERIODE_AARSAK' AND KODE = 'FRA_SKJÆRINGSTIDSPUNKTET';
