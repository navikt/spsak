-- legger til ny kode: VENTELØNN_VARTPENGER
INSERT INTO KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'VENTELØNN_VARTPENGER', 'Ventelønn eller vartpenger', 'OPPTJENING_AKTIVITET_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, 'VENTELØNN_VARTPENGER', 'Ventelønn eller vartpenger', 'ARBEID_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'), '{ "gui": "false" }');

INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES  (seq_kodeliste.nextval, 'OPPTJENING_AKTIVITET_TYPE', 'VENTELØNN_VARTPENGER','NB', 'Ventelønn eller vartpenger');

INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES  (seq_kodeliste.nextval, 'ARBEID_TYPE', 'VENTELØNN_VARTPENGER','NB', 'Ventelønn eller vartpenger');

INSERT INTO KODELISTE_RELASJON(ID, KODEVERK1, KODE1, KODEVERK2, KODE2, GYLDIG_FOM, GYLDIG_TOM)
values(seq_kodeliste_relasjon.nextval, 'OPPTJENING_AKTIVITET_TYPE', 'VENTELØNN_VARTPENGER', 'ARBEID_TYPE', 'VENTELØNN_VARTPENGER', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));

INSERT INTO KODELISTE_RELASJON(ID, KODEVERK1, KODE1, KODEVERK2, KODE2, GYLDIG_FOM, GYLDIG_TOM)
values(seq_kodeliste_relasjon.nextval, 'ARBEID_TYPE', 'VENTELØNN_VARTPENGER', 'OPPTJENING_AKTIVITET_TYPE', 'VENTELØNN_VARTPENGER', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));

-- legger til ny kode: ETTERLØNN_SLUTTPAKKE
INSERT INTO KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ETTERLØNN_SLUTTPAKKE', 'Etterlønn eller sluttpakke', 'OPPTJENING_AKTIVITET_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, 'ETTERLØNN_SLUTTPAKKE', 'Etterlønn eller sluttpakke', 'ARBEID_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'), '{ "gui": "false" }');

INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES  (seq_kodeliste.nextval, 'OPPTJENING_AKTIVITET_TYPE', 'ETTERLØNN_SLUTTPAKKE','NB', 'Etterlønn eller sluttpakke');

INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES  (seq_kodeliste.nextval, 'ARBEID_TYPE', 'ETTERLØNN_SLUTTPAKKE','NB', 'Etterlønn eller sluttpakke');

INSERT INTO KODELISTE_RELASJON(ID, KODEVERK1, KODE1, KODEVERK2, KODE2, GYLDIG_FOM, GYLDIG_TOM)
values(seq_kodeliste_relasjon.nextval, 'OPPTJENING_AKTIVITET_TYPE', 'ETTERLØNN_SLUTTPAKKE', 'ARBEID_TYPE', 'ETTERLØNN_SLUTTPAKKE', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));

INSERT INTO KODELISTE_RELASJON(ID, KODEVERK1, KODE1, KODEVERK2, KODE2, GYLDIG_FOM, GYLDIG_TOM)
values(seq_kodeliste_relasjon.nextval, 'ARBEID_TYPE', 'ETTERLØNN_SLUTTPAKKE', 'OPPTJENING_AKTIVITET_TYPE', 'ETTERLØNN_SLUTTPAKKE', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));
