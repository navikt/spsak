DROP SEQUENCE SEQ_ADOPSJON;
DROP SEQUENCE SEQ_ADOPSJON_BARN;
DROP SEQUENCE SEQ_DOKUMENT_ADRESSE;
DROP SEQUENCE SEQ_AKSJONSPUNKT;
DROP SEQUENCE SEQ_AKTOER_NORSK_IDENT_MAP;
DROP SEQUENCE SEQ_BARN;
DROP SEQUENCE SEQ_BEHANDLING;
DROP SEQUENCE SEQ_BEHANDLING_GRUNNLAG;
DROP SEQUENCE SEQ_BEHANDLING_REL_YTELSER;
DROP SEQUENCE SEQ_BEHANDLING_RESULTAT;
DROP SEQUENCE SEQ_BEHANDLING_STEG_TILSTAND;
DROP SEQUENCE SEQ_BEHANDLING_TYPE_STEG_SEKV;
DROP SEQUENCE SEQ_BEHANDLING_VEDTAK;
DROP SEQUENCE SEQ_BEKREFTET_BARN;
DROP SEQUENCE SEQ_BEKREFTET_FORELDRE;
DROP SEQUENCE SEQ_BEREGNING;
DROP SEQUENCE SEQ_BEREGNING_RESULTAT;
DROP SEQUENCE SEQ_BRUKER;
DROP SEQUENCE SEQ_DOKUMENT_DATA;
DROP SEQUENCE SEQ_DOKUMENT_FELLES;
DROP SEQUENCE SEQ_DOKUMENT_TYPE_DATA;
DROP SEQUENCE SEQ_FAGSAK;
DROP SEQUENCE SEQ_FAGSAK_RELASJON;
DROP SEQUENCE SEQ_FOEDSEL;
DROP SEQUENCE SEQ_HISTORIKKINNSLAG;
DROP SEQUENCE SEQ_HISTORIKKINNSLAG_DOK_LINK;
DROP SEQUENCE SEQ_INNGANGSVILKAR_RESULTAT;
DROP SEQUENCE SEQ_INNTEKT;
DROP SEQUENCE SEQ_JOURNALDOKUMENT;
DROP SEQUENCE SEQ_KONFIG_VERDI;
DROP SEQUENCE SEQ_LAGRET_VEDTAK;
DROP SEQUENCE SEQ_MEDLEMSKAP;
DROP SEQUENCE SEQ_MEDLEMSKAP_PERIODER;
DROP SEQUENCE SEQ_MOTTATTE_DOKUMENT;
DROP SEQUENCE SEQ_OKO_ATTESTANT_180;
DROP SEQUENCE SEQ_OKO_AVSTEMMING_115;
DROP SEQUENCE SEQ_OKO_OPPDRAG_110;
DROP SEQUENCE SEQ_OKO_OPPDRAG_ENHET_120;
DROP SEQUENCE SEQ_OKO_OPPDRAG_LINJE_150;
DROP SEQUENCE SEQ_OMSORGSOVERTAKELSE;
DROP SEQUENCE SEQ_OPPDRAG_KONTROLL;
DROP SEQUENCE SEQ_OPPGAVE_BEHANDLING_KOBLING;
DROP SEQUENCE SEQ_PROSESS_TASK;
DROP SEQUENCE SEQ_PROSESS_TASK_GRUPPE;
DROP SEQUENCE SEQ_REGEL_MERKNAD;
DROP SEQUENCE SEQ_SAKSOPPLYSNING;
DROP SEQUENCE SEQ_SAKSOPPLYSNING_DOKUMENT;
DROP SEQUENCE SEQ_SAKSOPPLYSNING_METADATA;
DROP SEQUENCE SEQ_SATS;
DROP SEQUENCE SEQ_SOEKNAD;
DROP SEQUENCE SEQ_SOEKNAD_ADOPSJON_BARN;
DROP SEQUENCE SEQ_SOEKNAD_ANNEN_PART;
DROP SEQUENCE SEQ_SOEKNAD_BARN;
DROP SEQUENCE SEQ_STATSBORGERSKAP;
DROP SEQUENCE SEQ_TERMINBEKREFTELSE;
DROP SEQUENCE SEQ_TILKNYTNING_HJEMLAND;
DROP SEQUENCE SEQ_UTLANDSOPPHOLD;
DROP SEQUENCE SEQ_SOEKNAD_VEDLEGG;
DROP SEQUENCE SEQ_VILKAR;
DROP SEQUENCE SEQ_VURDER_PAA_NYTT_AARSAK;

CREATE SEQUENCE SEQ_ADOPSJON MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_ADOPSJON_BARN MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_DOKUMENT_ADRESSE MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_AKSJONSPUNKT MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_AKTOER_NORSK_IDENT_MAP MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_BARN MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_BEHANDLING MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_BEHANDLING_GRUNNLAG MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_BEHANDLING_REL_YTELSER MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_BEHANDLING_RESULTAT MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_BEHANDLING_STEG_TILSTAND MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_BEHANDLING_TYPE_STEG_SEKV MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_BEHANDLING_VEDTAK MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_BEKREFTET_BARN MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_BEKREFTET_FORELDRE MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_BEREGNING MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_BEREGNING_RESULTAT MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_BRUKER MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_DOKUMENT_DATA MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_DOKUMENT_FELLES MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_DOKUMENT_TYPE_DATA MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_FAGSAK MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_FAGSAK_RELASJON MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_FOEDSEL MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_HISTORIKKINNSLAG MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_HISTORIKKINNSLAG_DOK_LINK MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_INNGANGSVILKAR_RESULTAT MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_INNTEKT MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_JOURNALDOKUMENT MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_KONFIG_VERDI MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_LAGRET_VEDTAK MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_MEDLEMSKAP MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_MEDLEMSKAP_PERIODER MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_MOTTATTE_DOKUMENT MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_OKO_ATTESTANT_180 MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_OKO_AVSTEMMING_115 MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_OKO_OPPDRAG_110 MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_OKO_OPPDRAG_ENHET_120 MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_OKO_OPPDRAG_LINJE_150 MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_OMSORGSOVERTAKELSE MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_OPPDRAG_KONTROLL MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_OPPGAVE_BEHANDLING_KOBLING MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_PROSESS_TASK  MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_PROSESS_TASK_GRUPPE MINVALUE 10000000 START WITH 10000000 INCREMENT BY 1000000 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_REGEL_MERKNAD MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_SAKSOPPLYSNING MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_SAKSOPPLYSNING_DOKUMENT MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_SAKSOPPLYSNING_METADATA MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50;
CREATE SEQUENCE SEQ_SATS MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_SOEKNAD MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_SOEKNAD_ADOPSJON_BARN MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_SOEKNAD_ANNEN_PART MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_SOEKNAD_BARN MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_STATSBORGERSKAP MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_TERMINBEKREFTELSE MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_TILKNYTNING_HJEMLAND MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_UTLANDSOPPHOLD MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_SOEKNAD_VEDLEGG MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_VILKAR MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_VURDER_PAA_NYTT_AARSAK MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
