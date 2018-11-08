alter table VERGE add brev_mottaker VARCHAR2(100);
alter table VERGE add kl_brev_mottaker VARCHAR2(100) AS ('BREV_MOTTAKER');
alter table VERGE add stoenad_mottaker VARCHAR2(1 CHAR);
alter table VERGE MODIFY BEHANDLINGGRUNNLAG_ID NULL;

COMMENT ON COLUMN VERGE.brev_mottaker IS 'Brev mottaker type';
COMMENT ON COLUMN VERGE.stoenad_mottaker IS 'J hvis verge skal motta stønad';
COMMENT ON COLUMN VERGE.kl_brev_mottaker IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';

-- kodeverk og kodeliste
insert into kodeverk (kode, navn, beskrivelse, KODEVERK_SYNK_EKSISTERENDE, KODEVERK_SYNK_NYE)
values ('BREV_MOTTAKER', 'BrevMottaker', 'Internt kodeverk for brev mottaker i forbindelse med verge.', 'N', 'N');
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'BREV_MOTTAKER', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'VERGE', 'Verge', 'Verge skal motta brev', 'BREV_MOTTAKER', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'SOEKER', 'Søker', 'Søker skal motta brev', 'BREV_MOTTAKER', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'BEGGE', 'Begge', 'Både verge og søker skal motta brev', 'BREV_MOTTAKER', to_date('2000-01-01', 'YYYY-MM-DD'));

-- migrer data
UPDATE Verge v
SET brev_mottaker = 'BEGGE'
WHERE EXISTS
(SELECT d.BEHANDLING_GRUNNLAG_ID
 FROM DOKUMENT_MOTTAKER d
 WHERE d.BEHANDLING_GRUNNLAG_ID = v.BEHANDLINGGRUNNLAG_ID
 GROUP BY d.BEHANDLING_GRUNNLAG_ID
 HAVING COUNT(*) > 1
);

update Verge v
set brev_mottaker = 'SOEKER'
where v.BEHANDLINGGRUNNLAG_ID in (
  select BEHANDLINGGRUNNLAG_ID from VERGE
  minus
  select DOKUMENT_MOTTAKER.BEHANDLING_GRUNNLAG_ID from DOKUMENT_MOTTAKER);

UPDATE Verge v
SET brev_mottaker = 'VERGE'
WHERE EXISTS
(SELECT d.BEHANDLING_GRUNNLAG_ID
 FROM DOKUMENT_MOTTAKER d
 WHERE d.BEHANDLING_GRUNNLAG_ID = v.BEHANDLINGGRUNNLAG_ID
 GROUP BY d.BEHANDLING_GRUNNLAG_ID
 HAVING COUNT(*) = 1
);

UPDATE Verge v
SET v.stoenad_mottaker = v.TVUNGEN_FORVALTNING;
