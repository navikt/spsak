-- legger inn kodeverk
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'VENTELØNN', null, 'Ventelønn', NULL, to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'LØNN_UNDER_UTDANNING', null, 'Lønn under utdanning', NULL, to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'SLUTTPAKKE', null, 'Sluttpakke', NULL, to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'VARTPENGER', null, 'Vartpenger', NULL, to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'SYKEPENGER', null, 'Sykepenger', NULL, to_date('2000-01-01', 'YYYY-MM-DD'));

-- legger inn Kodeliste_relasjon

INSERT INTO KODELISTE_RELASJON(ID, KODEVERK1, KODE1, KODEVERK2, KODE2, GYLDIG_FOM, GYLDIG_TOM)
values(seq_kodeliste_relasjon.nextval, 'OPPTJENING_AKTIVITET_TYPE', 'MILITÆR_ELLER_SIVILTJENESTE', 'ARBEID_TYPE', 'MILITÆR_ELLER_SIVILTJENESTE', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));

INSERT INTO KODELISTE_RELASJON(ID, KODEVERK1, KODE1, KODEVERK2, KODE2, GYLDIG_FOM, GYLDIG_TOM)
values(seq_kodeliste_relasjon.nextval, 'OPPTJENING_AKTIVITET_TYPE', 'SYKEPENGER', 'ARBEID_TYPE', 'SYKEPENGER', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));

INSERT INTO KODELISTE_RELASJON(ID, KODEVERK1, KODE1, KODEVERK2, KODE2, GYLDIG_FOM, GYLDIG_TOM)
values(seq_kodeliste_relasjon.nextval, 'OPPTJENING_AKTIVITET_TYPE', 'VARTPENGER', 'ARBEID_TYPE', 'VARTPENGER', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));

INSERT INTO KODELISTE_RELASJON(ID, KODEVERK1, KODE1, KODEVERK2, KODE2, GYLDIG_FOM, GYLDIG_TOM)
values(seq_kodeliste_relasjon.nextval, 'OPPTJENING_AKTIVITET_TYPE', 'NÆRING', 'ARBEID_TYPE', 'SELVNAER', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));

INSERT INTO KODELISTE_RELASJON(ID, KODEVERK1, KODE1, KODEVERK2, KODE2, GYLDIG_FOM, GYLDIG_TOM)
values(seq_kodeliste_relasjon.nextval, 'OPPTJENING_AKTIVITET_TYPE', 'ARBEID', 'ARBEID_TYPE', 'ordinaertArbeidsforhold', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));