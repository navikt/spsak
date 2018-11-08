-- oppdatering til kodeverk PERIODE_AARSAK for beregningsgrunnlaget
update KODELISTE
set
kode = 'NATURALYTELSE_BORTFALT',
navn='Naturalytelse bortfalt',
beskrivelse='Ny periode etter at naturalytelse har bortfalt, dvs arbeidsgiver har sluttet å dekke naturalytelse'
where
kode = 'NATURALYTELSE_AVSLUTTET' and kodeverk = 'PERIODE_AARSAK';

update BEREGNINGSGRUNNLAG_PERIODE
set
periode_aarsak = 'NATURALYTELSE_BORTFALT'
where
periode_aarsak = 'NATURALYTELSE_AVSLUTTET';

update KODELISTE
set
kode = 'ARBEIDSFORHOLD_AVSLUTTET_OG_NATURALYTELSE_BORTFALT',
navn='Arbeidsforhold avsluttet og naturalytelse bortfalt',
beskrivelse='Ny periode etter at både tidsbegrenset arbeidsforhold er avsluttet og naturalytelse bortfalt'
where
kode = 'ARBEIDSFORHOLD_OG_NATURALYTELSE_AVSLUTTET' and kodeverk = 'PERIODE_AARSAK';

update BEREGNINGSGRUNNLAG_PERIODE
set
periode_aarsak = 'ARBEIDSFORHOLD_AVSLUTTET_OG_NATURALYTELSE_BORTFALT'
where
periode_aarsak = 'ARBEIDSFORHOLD_OG_NATURALYTELSE_AVSLUTTET';
