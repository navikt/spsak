alter table VILKAR ADD VILKAR_UTFALL_MANUELL varchar2(100 char) default '-' not null;
alter table VILKAR ADD VILKAR_UTFALL_OVERSTYRT varchar2(100 char) default '-' not null;

UPDATE VILKAR set VILKAR_UTFALL_MANUELL = VILKAR_UTFALL WHERE MANUELT_VURDERT = 'J';
UPDATE VILKAR set VILKAR_UTFALL_OVERSTYRT = VILKAR_UTFALL WHERE OVERSTYRT = 'J';
-- Fjern boolske flagg (trenger dem ikke lenger nå som hele utfallet er lagret)
ALTER TABLE VILKAR DROP COLUMN MANUELT_VURDERT;
ALTER TABLE VILKAR DROP COLUMN OVERSTYRT;

ALTER TABLE VILKAR ADD constraint FK_VILKAR_7 foreign key (VILKAR_UTFALL_MANUELL, KL_VILKAR_UTFALL_TYPE) references KODELISTE (KODE, KODEVERK);
ALTER TABLE VILKAR ADD constraint FK_VILKAR_8 foreign key (VILKAR_UTFALL_OVERSTYRT, KL_VILKAR_UTFALL_TYPE) references KODELISTE (KODE, KODEVERK);

create index IDX_VILKAR_8 on VILKAR(VILKAR_UTFALL_MANUELL);
create index IDX_VILKAR_9 on VILKAR(VILKAR_UTFALL_OVERSTYRT);

-- Fjerner OPPRINNELIG_VILKAR_UTFALL, som ikke lenger er i bruk noe sted
alter table VILKAR DROP CONSTRAINT FK_VILKAR_6;
alter table VILKAR DROP COLUMN OPPRINNELIG_VILKAR_UTFALL;

-- Kun for test. Denne satsen skal aldri settes i prod (år = 2016). Settes til verdi 0; hvis den blir anvendt ved feil, vil økonomi fange opp dette
MERGE INTO sats s USING dual ON (
  dual.dummy IS NOT NULL
  AND s.fom = TO_DATE('01.01.2016','DD.MM.YYYY')
  AND s.tom = TO_DATE('31.12.2016','DD.MM.YYYY')
  AND sats_type = 'ENGANG'
)
when not matched then
  INSERT (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'ENGANG', to_date('01.01.2016','DD.MM.YYYY'), to_date('31.12.2016','DD.MM.YYYY'), 0);
