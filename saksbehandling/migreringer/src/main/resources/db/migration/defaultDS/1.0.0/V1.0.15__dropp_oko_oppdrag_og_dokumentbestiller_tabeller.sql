drop table OPPDRAG_KONTROLL cascade constraints purge;
drop table oppdrag_KVITTERING cascade constraints purge;
drop table OKO_ATTESTANT_180 cascade constraints purge;
drop table OKO_AVSTEMMING_115 cascade constraints purge;
drop table OKO_GRAD_170 cascade constraints purge;
drop table OKO_OPPDRAG_110 cascade constraints purge;
drop table OKO_OPPDRAG_ENHET_120 cascade constraints purge;
drop table OKO_OPPDRAG_LINJE_150 cascade constraints purge;
drop table OKO_REFUSJONSINFO_156 cascade constraints purge;


drop sequence SEQ_OKO_ATTESTANT_180;
drop sequence SEQ_OKO_AVSTEMMING_115;
drop sequence SEQ_OKO_GRAD_170;
drop sequence SEQ_OKO_OPPDRAG_110;
drop sequence SEQ_OKO_OPPDRAG_ENHET_120;
drop sequence SEQ_OPPDRAG_KONTROLL;
drop sequence SEQ_OKO_OPPDRAG_LINJE_150;
drop sequence SEQ_OKO_REFUSJONSINFO_156;
drop sequence SEQ_OPPDRAG_KVITTERING;

drop table AKSJONSPUNKT_UTELUKKENDE cascade constraints purge;

-- GAMLE DOK TABELLER
drop sequence SEQ_DOKUMENT_DATA;
drop sequence SEQ_DOKUMENT_FELLES;
drop sequence SEQ_DOKUMENT_TYPE_DATA;
drop table dokument_adresse cascade constraints purge;
drop table dokument_data cascade constraints purge;
drop table dokument_felles cascade constraints purge;
drop table dokument_type_data cascade constraints purge;


