alter table BEHANDLING add sist_oppdatert_tidspunkt timestamp(3);
ALTER TABLE BEHANDLING ADD RELATERTE_YTELSER_STATUS VARCHAR2(30 char) DEFAULT 'IKKE_INNHENTET' NOT NULL;
ALTER TABLE BEHANDLING ADD KL_RELATERTE_YTELSER_STATUS VARCHAR2(30 char) DEFAULT 'RELATERTE_YTELSER_STATUS' NOT NULL;

alter table BEHANDLING add constraint FK_BEHANDLING_8 foreign key (RELATERTE_YTELSER_STATUS, KL_RELATERTE_YTELSER_STATUS) references
  KODELISTE (KODE, KODEVERK);

COMMENT ON COLUMN BEHANDLING.KL_RELATERTE_YTELSER_STATUS IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column BEHANDLING.RELATERTE_YTELSER_STATUS is 'Beskriver status på infotrygd innhenting';
comment on column BEHANDLING.sist_oppdatert_tidspunkt is 'Beskriver når grunnlagene til behandling ble sist innhentet';

MERGE INTO BEHANDLING behandling_update
  USING(SELECT * FROM BEHANDLING_GRUNNLAG be_gr) resultat
ON (behandling_update.BEHANDLING_GRUNNLAG_ID = resultat.id)
WHEN MATCHED THEN UPDATE SET behandling_update.RELATERTE_YTELSER_STATUS = resultat.YTELSER_INFOTRYGD_STATUS, behandling_update
.sist_oppdatert_tidspunkt = resultat.OPPLYSNINGER_OPPDATERT_TID;
