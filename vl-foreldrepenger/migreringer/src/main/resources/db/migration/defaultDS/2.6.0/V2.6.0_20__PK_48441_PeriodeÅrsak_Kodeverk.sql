-- ny kodeverk SPLITT_AARSAK for beregningsgrunnlagperioden, med kodeverdier
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('PERIODE_AARSAK', 'N', 'N', 'Periodeårsak', 'Årsaken til at beregningsgrunnlagperioden har blitt splittet');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'FRA_SKJÆRINGSTIDSPUNKTET', 'Fra skjæringstidspunktet', 'Benyttes alltid og bare for første periode', 'PERIODE_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'NATURALYTELSE_AVSLUTTET', 'Naturalytelse avsluttet', 'Ny periode etter at arbeidsgiver har sluttet å dekke naturalytelse', 'PERIODE_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'ARBEIDSFORHOLD_OG_NATURALYTELSE_AVSLUTTET', 'Arbeidsforhold og naturalytelse avsluttet', 'Ny periode etter at både tidsbegrenset arbeidsforhold og naturalytelse er avsluttet', 'PERIODE_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'ARBEIDSFORHOLD_AVSLUTTET', 'Arbeidsforhold avsluttet', 'Ny periode etter at tidsbegrenset arbeidsforhold er avsluttet', 'PERIODE_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'PERIODE_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));


alter table BEREGNINGSGRUNNLAG_PERIODE add periode_aarsak VARCHAR2(100 char);

update BEREGNINGSGRUNNLAG_PERIODE set periode_aarsak = '-';

alter table BEREGNINGSGRUNNLAG_PERIODE modify periode_aarsak not NULL ;
