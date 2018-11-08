ALTER TABLE YF_FORDELING_PERIODE ADD SAMTIDIG_UTTAKSPROSENT DECIMAL(5, 2) DEFAULT NULL;
COMMENT ON COLUMN YF_FORDELING_PERIODE.SAMTIDIG_UTTAKSPROSENT IS 'Hvor stor søker har tenkt å ha for samtidig uttak';

INSERT INTO KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'UTTAK_MØDREKVOTE_ANNEN_FORELDER', 'Annen foreldre har uttak av Mødrekvote.', 'OPPHOLD_AARSAK_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'UTTAK_FEDREKVOTE_ANNEN_FORELDER', 'Annen foreldre har uttak av Fedrekvote.', 'OPPHOLD_AARSAK_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'UTTAK_FORELDREPENGER_ANNEN_FORELDER', 'Annen foreldre har uttak av Foreldrepenger.', 'OPPHOLD_AARSAK_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'OPPHOLD_AARSAK_TYPE',	'UTTAK_MØDREKVOTE_ANNEN_FORELDER',	'NB',	'Annen foreldre har uttak av Mødrekvote.');
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'OPPHOLD_AARSAK_TYPE',	'UTTAK_FEDREKVOTE_ANNEN_FORELDER',	'NB',	'Annen foreldre har uttak av Fedrekvote.');
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'OPPHOLD_AARSAK_TYPE',	'UTTAK_FORELDREPENGER_ANNEN_FORELDER',	'NB',	'Annen foreldre har uttak av Foreldrepenger.');

-- migrering data fra gammel kolonn til ny kolonn
update YF_FORDELING_PERIODE set AARSAK_TYPE = 'UTTAK_FEDREKVOTE_ANNEN_FORELDER' where id in (
  select distinct yfp.id from YF_FORDELING_PERIODE yfp
  join YF_FORDELING yf on yfp.fordeling_id = yf.id and yfp.KL_AARSAK_TYPE = 'OPPHOLD_AARSAK_TYPE' and yfp.AArsak_type= 'UTTAK_KVOTE_ANNEN_FORELDER'
  join GR_YTELSES_FORDELING gryf on gryf.SO_FORDELING_ID = yf.id
  join BEHANDLING b on b.id = gryf.BEHANDLING_ID
  join FAGSAK f on f.id = b.FAGSAK_ID and f.BRUKER_ROLLE = 'MORA'
);
update YF_FORDELING_PERIODE set AARSAK_TYPE = 'UTTAK_MØDREKVOTE_ANNEN_FORELDER' where id in (
  select distinct yfp.id from YF_FORDELING_PERIODE yfp
  join YF_FORDELING yf on yfp.fordeling_id = yf.id and yfp.KL_AARSAK_TYPE = 'OPPHOLD_AARSAK_TYPE' and yfp.AARSAK_TYPE = 'UTTAK_KVOTE_ANNEN_FORELDER'
  join GR_YTELSES_FORDELING gryf on gryf.SO_FORDELING_ID = yf.id
  join BEHANDLING b on b.id = gryf.BEHANDLING_ID
  join FAGSAK f on f.id = b.FAGSAK_ID and f.BRUKER_ROLLE != 'MORA'
);

DELETE from KODELISTE_NAVN_I18N WHERE KL_KODEVERK = 'OPPHOLD_AARSAK_TYPE' AND KL_KODE = 'UTTAK_KVOTE_ANNEN_FORELDER';
DELETE from KODELISTE WHERE KODEVERK = 'OPPHOLD_AARSAK_TYPE' AND KODE = 'UTTAK_KVOTE_ANNEN_FORELDER';
