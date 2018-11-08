-- kodeliste  SKJERMLENKE_TYPE FAKTA_OM_VERGE
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FAKTA_OM_VERGE', 'Fakta om verge/fullmektig', 'Fakta om verge/fullmektig', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');

-- AKSJONSPUNKT_DEF
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'FAKTA_OM_VERGE' WHERE KODE = '5030';

-- HISTORIKKINNSLAG_TYPE
insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'REGISTRER_OM_VERGE', 'Registrering av opplysninger om verge/fullmektig', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE2');

-- HISTORIKK_ENDRET_FELT_TYPE
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'NAVN', 'Navn', 'Navn', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FNR', 'Fødselsnummer', 'Fødselsnummer', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'PERIODE_FOM', 'Periode f.o.m.', 'Periode f.o.m.', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'PERIODE_TOM', 'Periode t.o.m.', 'Periode t.o.m.', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'MANDAT', 'Mandat', 'Mandat', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'KONTAKTPERSON', 'Kontaktperson', 'Kontaktperson', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'BRUKER_TVUNGEN', 'Bruker er under tvungen forvaltning', 'Bruker er under tvungen forvaltning', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'TYPE_VERGE', 'Type verge', 'Type verge', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
