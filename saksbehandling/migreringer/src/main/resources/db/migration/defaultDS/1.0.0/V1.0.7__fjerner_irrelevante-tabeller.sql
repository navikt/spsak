drop table FH_ADOPSJON cascade;
drop table FH_UIDENTIFISERT_BARN cascade;
drop table FH_TERMINBEKREFTELSE cascade;
drop table GR_FAMILIE_HENDELSE cascade;

drop table SO_ANNEN_PART cascade;

ALTER TABLE SO_SOEKNAD DROP FOEDSELSDATO_FRA_SOEKNAD;
ALTER TABLE SO_SOEKNAD DROP UTSTEDT_DATO_TERMINBEKREFTELSE;
ALTER TABLE SO_SOEKNAD DROP TERMINDATO_FRA_SOEKNAD;
ALTER TABLE SO_SOEKNAD DROP ANTALL_BARN_FRA_SOEKNAD;
ALTER TABLE SO_SOEKNAD DROP ADOP_OMSORGOVER_DATO;
ALTER TABLE SO_SOEKNAD DROP ANNEN_PART_ID;
ALTER TABLE SO_SOEKNAD DROP NAVN_PAA_TERMINBEKREFTELSE;
ALTER TABLE SO_SOEKNAD DROP FAMILIE_HENDELSE_ID;
ALTER TABLE GR_PERSONOPPLYSNING DROP SO_ANNEN_PART_ID;

DROP SEQUENCE SEQ_ADOPSJON;
DROP SEQUENCE SEQ_ADOPSJON_BARN;
DROP SEQUENCE SEQ_SOEKNAD_ADOPSJON_BARN;
DROP SEQUENCE SEQ_FOEDSEL;
DROP SEQUENCE SEQ_TERMINBEKREFTELSE;
DROP SEQUENCE SEQ_OMSORGSOVERTAKELSE;
DROP SEQUENCE SEQ_UIDENTIFISERT_BARN;
DROP SEQUENCE SEQ_FAMILIE_HENDELSE;
DROP SEQUENCE SEQ_GR_FAMILIE_HENDELSE;
