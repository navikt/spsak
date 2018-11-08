INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'AKTIVITET_PERIODE', 'Perioden med aktivitet', 'Perioden med aktivitet er endret', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'AKTIVITET', 'Aktivitet', 'Aktivitet fra opptjening', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'FAKTA_OM_OPPTJENING', 'Fakta om opptjening', 'Fakta om opptjening', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'OPPTJENING', 'Behandlet opptjeningsperiode', to_date('2017-01-01', 'YYYY-MM-DD'), '{"mal": "TYPE8"}');
