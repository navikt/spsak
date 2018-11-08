merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'RELATERT_YTELSE_BEH_TEMA' and k.kode = 'ARAN')
when not matched then
  INSERT  (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
  VALUES (SEQ_KODELISTE.nextval, 'RELATERT_YTELSE_BEH_TEMA', 'ARAN', '', 'Andre behandlingstema Arena', 'Andre behandlingstema Arena', to_date('2000-01-01', 'YYYY-MM-DD'));

UPDATE KONFIG_VERDI
SET KONFIG_VERDI = 'https://navet.adeo.no/ansatt/Fag/Familie/Svangerskap%2C+fodsel%2C+adopsjon/Saksbehandlingsl%C3%B8sning+for+engangsst%C3%B8nad/rutiner-saksbehandlingsl%C3%B8sningen-for-foreldrepenger',
  GYLDIG_FOM = to_date('01.01.2018', 'dd.mm.yyyy')
where KONFIG_KODE = 'systemrutine.url';

ALTER TABLE INNTEKT MODIFY (UTBETALER VARCHAR2(100 CHAR));
