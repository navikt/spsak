insert into KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'OPPHØR', 'Opphør', 'VEDTAK_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN)
VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'VEDTAK_RESULTAT_TYPE', 'OPPHØR', 'NB', 'Opphør');
