INSERT INTO KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'F_35', 'forvaltningsloven §§ 35', 'BG_HJEMMEL', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES  (seq_kodeliste.nextval, 'BG_HJEMMEL', 'F_35','NB', 'forvaltningsloven §§ 35');
