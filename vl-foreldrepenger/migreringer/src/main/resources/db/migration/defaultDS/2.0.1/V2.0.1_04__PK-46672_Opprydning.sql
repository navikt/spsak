--rydder verge
alter table VERGE drop COLUMN BEHANDLINGGRUNNLAG_ID;
alter table VERGE drop COLUMN TVUNGEN_FORVALTNING;
alter table VERGE drop COLUMN NAVN;

alter table VERGE MODIFY (BREV_MOTTAKER not null);
alter table VERGE MODIFY (KL_BREV_MOTTAKER not null);
alter table VERGE MODIFY (STOENAD_MOTTAKER not null);
alter table VERGE MODIFY (KL_VERGE_TYPE not null);

--rydder dokument_mottaker
drop table DOKUMENT_MOTTAKER;