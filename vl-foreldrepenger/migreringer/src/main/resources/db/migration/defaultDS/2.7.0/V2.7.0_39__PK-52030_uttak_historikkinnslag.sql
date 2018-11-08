insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
values (seq_kodeliste.nextval, 'UTTAK_TREKKDAGER', 'Trekkdager', 'Trekkdager uttak', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
values (seq_kodeliste.nextval, 'UTTAK_STØNADSKONTOTYPE', 'Stønadskontotype', 'Stønadskontotype', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
values (seq_kodeliste.nextval, 'UTTAK_PERIODE_RESULTAT_TYPE', 'Resultattype for periode', 'Resultattype for periode', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
values (seq_kodeliste.nextval, 'UTTAK_PROSENT_UTBETALING', 'Utbetalingsgrad', 'Utbetalingsgrad', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
values (seq_kodeliste.nextval, 'UTTAK_TREKKDAGER_FLERBARN_KVOTE', 'Trekkdager flerbarn kvote', 'Trekkdager flerbarn kvote', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
values (seq_kodeliste.nextval, 'UTTAK_GRADERING', 'Gradering', 'Gradering', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
values (seq_kodeliste.nextval, 'UTTAK_PERIODE_RESULTAT_ÅRSAK', 'Resultat årsak', 'Resultat årsak', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
values (seq_kodeliste.nextval, 'UTTAK_SPLITT_TIDSPERIODE', 'Resulterende periode ved splitting', 'Resulterende periode ved splitting', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'OVST_UTTAK', 'Manuelt overstyring av uttak', to_date('2017-01-01', 'YYYY-MM-DD'), '{"mal": "TYPE5"}');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'OVST_UTTAK_SPLITT', 'Manuelt overstyring av uttak - splitting av periode', to_date('2017-01-01', 'YYYY-MM-DD'), '{"mal": "TYPE9"}');
