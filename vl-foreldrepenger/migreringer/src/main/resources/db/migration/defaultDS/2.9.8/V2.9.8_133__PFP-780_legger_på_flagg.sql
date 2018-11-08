ALTER TABLE FAGSAK add til_infotrygd VARCHAR2(1 CHAR) DEFAULT 'N' NOT NULL;
COMMENT ON COLUMN FAGSAK.til_infotrygd IS 'J hvis saken må behandles av Infotrygd';

insert into kodeliste (id, kode, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'BEH_SAK_FOR', 'OPPGAVE_AARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));

insert into kodeliste (id, kode, KODEVERK, GYLDIG_FOM)
values (seq_kodeliste.nextval, 'MANGLER_BEREGNINGSREGLER', 'BEHANDLING_RESULTAT_TYPE', to_date('2000-01-01', 'yyyy-mm-dd'));

insert into KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
values (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'BEHANDLING_RESULTAT_TYPE',	'MANGLER_BEREGNINGSREGLER',	'NB',	'Mangler beregningsregler');
