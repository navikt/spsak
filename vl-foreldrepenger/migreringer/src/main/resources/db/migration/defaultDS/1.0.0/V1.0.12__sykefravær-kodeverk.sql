INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES
  ('SYKEFRAVÆR_PERIODE_TYPE', 'Kodeverk over gyldige typer av sykefravær (egenmelding, sykemeldt osv)', '',
   'VL');
INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, '-', 'Udefinert', to_date('2000-01-01', 'YYYY-MM-DD'),
        'SYKEFRAVÆR_PERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'EGENMELDING', 'Egenmelding', to_date('2000-01-01', 'YYYY-MM-DD'),
        'SYKEFRAVÆR_PERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'SYKEMELDT', 'Sykemeldt', to_date('2000-01-01', 'YYYY-MM-DD'),
        'SYKEFRAVÆR_PERIODE_TYPE');
INSERT INTO KODELISTE_NAVN_I18N (id, KL_KODE, KL_KODEVERK, SPRAK, NAVN)
VALUES (SEQ_KODELISTE_NAVN_I18N.nextval, '-', 'SYKEFRAVÆR_PERIODE_TYPE', 'NB', 'Udefinert');
INSERT INTO KODELISTE_NAVN_I18N (id, KL_KODE, KL_KODEVERK, SPRAK, NAVN)
VALUES (SEQ_KODELISTE_NAVN_I18N.nextval, 'EGENMELDING', 'SYKEFRAVÆR_PERIODE_TYPE', 'NB', 'Egenmelding');
INSERT INTO KODELISTE_NAVN_I18N (id, KL_KODE, KL_KODEVERK, SPRAK, NAVN)
VALUES (SEQ_KODELISTE_NAVN_I18N.nextval, 'SYKEMELDT', 'SYKEFRAVÆR_PERIODE_TYPE', 'NB', 'Sykemeldt');
