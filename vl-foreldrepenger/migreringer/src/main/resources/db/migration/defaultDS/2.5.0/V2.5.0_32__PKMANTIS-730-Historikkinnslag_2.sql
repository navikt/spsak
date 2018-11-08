-- Rydd opp i eksisterende kodeverk
UPDATE KODELISTE
SET EKSTRA_DATA = '{"mal": "TYPE1"}'
WHERE KODEVERK = 'HISTORIKKINNSLAG_TYPE'
  AND EKSTRA_DATA = 'TYPE1';

UPDATE KODELISTE
SET EKSTRA_DATA = '{"mal": "TYPE4"}'
WHERE KODEVERK = 'HISTORIKKINNSLAG_TYPE'
      AND KODE in ('BEH_VENT')
      AND EKSTRA_DATA = '{"mal": "TYPE1"}';

UPDATE KODELISTE
SET EKSTRA_DATA = '{"mal": "TYPE5"}'
WHERE KODEVERK = 'HISTORIKKINNSLAG_TYPE'
      AND KODE in ('KLAGE_BEH_NFP', 'KLAGE_BEH_NK')
      AND EKSTRA_DATA = '{"mal": "TYPE1"}';

UPDATE KODELISTE
SET EKSTRA_DATA = '{"mal": "TYPE7"}'
WHERE KODEVERK = 'HISTORIKKINNSLAG_TYPE'
      AND KODE in ('OVERSTYRT')
      AND EKSTRA_DATA = '{"mal": "TYPE2"}';

-- Drop
DELETE FROM KODELISTE WHERE kodeverk in ('HISTORIKK_SKJERMLENKE_TYPE','HISTORIKKINNSLAG_FELT_TYPE');
DELETE FROM KODEVERK WHERE kode in ('HISTORIKK_SKJERMLENKE_TYPE','HISTORIKKINNSLAG_FELT_TYPE');

ALTER TABLE HISTORIKKINNSLAG_FELT DROP CONSTRAINT FK_HISTORIKKINNSLAG_FELT_1;
DROP SEQUENCE SEQ_HISTORIKKINNSLAG_FELT;
DROP INDEX IDX_HISTORIKKINNSLAG_FELT_1;
DROP TABLE HISTORIKKINNSLAG_FELT;

DROP SEQUENCE SEQ_HISTORIKKINNSLAG_DEL;
DROP INDEX IDX_HISTORIKKINNSLAG_DEL_1;
ALTER TABLE HISTORIKKINNSLAG_DEL DROP CONSTRAINT FK_HISTORIKKINNSLAG_DEL_1;
DROP TABLE HISTORIKKINNSLAG_DEL;

-- Recreate
create table HISTORIKKINNSLAG_DEL (
  id              NUMBER(19) NOT NULL,
  HISTORIKKINNSLAG_ID       NUMBER(19) NOT NULL,
  -- aarsak
  -- begrunnelse
  -- skjermlenke
  -- resultat
  versjon         NUMBER(19) DEFAULT 0 NOT NULL,
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_HISTORIKKINNSLAG_DEL PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_HISTORIKKINNSLAG_DEL MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE INDEX IDX_HISTORIKKINNSLAG_DEL_1 ON HISTORIKKINNSLAG_DEL (HISTORIKKINNSLAG_ID);

ALTER TABLE HISTORIKKINNSLAG_DEL ADD CONSTRAINT FK_HISTORIKKINNSLAG_DEL_1 FOREIGN KEY ( HISTORIKKINNSLAG_ID ) REFERENCES HISTORIKKINNSLAG ( id ) ;

create table HISTORIKKINNSLAG_FELT (
  id                      NUMBER(19) NOT NULL,
  HISTORIKKINNSLAG_DEL_ID NUMBER(19) NOT NULL,
  historikkinnslag_felt_type VARCHAR(100) NOT NULL,
  NAVN                    VARCHAR(100) NULL,
  KL_NAVN                 VARCHAR(100) NULL,
  NAVN_VERDI              VARCHAR(4000) NULL,
  FRA_VERDI               VARCHAR(4000) NULL,
  TIL_VERDI               VARCHAR(4000) NULL,
  KL_VERDI                VARCHAR(100) NULL,
  SEKVENS_NR              NUMBER(5,0) NULL,
  versjon         NUMBER(19) DEFAULT 0 NOT NULL,
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_HISTORIKKINNSLAG_FELT PRIMARY KEY (id)
);

CREATE INDEX IDX_HISTORIKKINNSLAG_FELT_1 ON HISTORIKKINNSLAG_FELT (HISTORIKKINNSLAG_DEL_ID);

ALTER TABLE HISTORIKKINNSLAG_FELT ADD CONSTRAINT FK_HISTORIKKINNSLAG_FELT_1 FOREIGN KEY ( HISTORIKKINNSLAG_DEL_ID ) REFERENCES HISTORIKKINNSLAG_DEL ( id ) ;

CREATE SEQUENCE SEQ_HISTORIKKINNSLAG_FELT MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE HISTORIKKINNSLAG_DEL   IS 'Et historikkinnslag kan ha en eller flere deler';
COMMENT ON TABLE HISTORIKKINNSLAG_FELT  IS 'En historikkinnslagdel har typisk mange felt';

COMMENT ON COLUMN HISTORIKKINNSLAG_DEL.HISTORIKKINNSLAG_ID IS 'FK: HISTORIKKINNSLAG';
COMMENT ON COLUMN HISTORIKKINNSLAG_FELT.historikkinnslag_felt_type IS 'Hva slags type informasjon som er representert';
COMMENT ON COLUMN HISTORIKKINNSLAG_FELT.NAVN IS 'Navn på felt. Gjelder for endrede verdier og opplysninger';
COMMENT ON COLUMN HISTORIKKINNSLAG_FELT.FRA_VERDI IS 'Feltets gamle verdi. Kan være kodeverk eller en string';
COMMENT ON COLUMN HISTORIKKINNSLAG_FELT.TIL_VERDI IS 'Feltets nye verdi. Kan være kodeverk eller en string';
COMMENT ON COLUMN HISTORIKKINNSLAG_FELT.KL_VERDI IS 'FK: KODELISTE';
COMMENT ON COLUMN HISTORIKKINNSLAG_FELT.SEKVENS_NR IS 'Settes dersom historikkinnslagdelen har flere innslag med samme navn';

INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES ('HISTORIKKINNSLAG_FELT_TYPE', 'Kodeverk for endrede felt i historikkinnslag', 'Kodeverk for endrede felt i historikkinnslag','VL');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'AARSAK', 'aarsak', 'Årsak', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'BEGRUNNELSE', 'begrunnelse', 'Begrunnelse', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'HENDELSE', 'hendelse', 'Hendelse', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'RESULTAT', 'resultat', 'Resultat', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'SKJERMLENKE', 'skjermlenke', 'Skjermlenke', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'OPPLYSNINGER', 'opplysninger', 'Opplysninger', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'ENDRET_FELT', 'endredeFelter', 'Endret felt', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'AKSJONSPUNKT_BEGRUNNELSE', 'aksjonspunktBegrunnelse', 'Begrunnelse for beslutters vurdering', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'AKSJONSPUNKT_GODKJENT', 'aksjonspunktGodkjent', 'Har beslutter godkjent saksbehandlers vurdering?', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'AKSJONSPUNKT_KODE', 'aksjonspunktKode', 'Aksjonspunktskode', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

-- Nytt kodeverk HISTORIKK_ENDRET_FELT_TYPE
INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier) VALUES ('HISTORIKK_ENDRET_FELT_TYPE', 'Kodeverk for faktaendringtyper', 'Kodeverk for faktaendringtyper','VL');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'ADOPSJON', 'Adopsjon', 'Adopsjon', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'ADOPTERER_ALENE', 'Adopterer alene', 'Adopterer alene', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'ANTALL_BARN', 'Antall barn', 'Antall barn', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'AVKLARSAKSOPPLYSNINGER', 'Avklar saksopplysninger', 'Avklar saksopplysninger', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'BEHANDLENDE_ENHET', 'Behandlende enhet', 'Behandlende enhet', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'BEHANDLING', 'Behandling', 'Behandling', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'BRUK_ANTALL_I_TPS', 'Bruk antall fra folkeregisteret', 'Bruk antall fra folkeregisteret', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'BRUK_ANTALL_I_SOKNAD', 'Bruk antall fra søknad', 'Bruk antall fra søknad', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'BRUK_ANTALL_I_VEDTAKET', 'Bruk antall fra vedtaket', 'Bruk antall fra vedtaket', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'BRUTTO_NAERINGSINNTEKT', 'Brutto næringsinntekt', 'Avklar saksopplysninger', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'DOKUMENTASJON_FORELIGGER', 'Dokumentasjon foreligger', 'Dokumentasjon foreligger', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'EKTEFELLES_BARN', 'Ektefelles barn', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'ENDRING_NAERING', 'Endring i næring', 'Endring i næring', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'ER_SOKER_BOSATT_I_NORGE', 'Er søker bosatt i Norge?', 'Er søker bosatt i Norge?', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FRILANS_INNTEKT', 'Frilans inntekt', 'Frilans inntekt', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FORELDREANSVAR', 'Foreldreansvar', 'Foreldreansvar', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FODSEL', 'Fødsel', 'Fødsel', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FODSELSDATO', 'Fødselsdato', 'Fødselsdato', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'GYLDIG_MEDLEM_FOLKETRYGDEN', 'Gyldig medlem i folketrygden', 'Vurder om søker har gyldig medlemskap i perioden', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'INNTEKT_FRA_ARBEIDSFORHOLD', 'Inntekt fra arbeidsforhold', 'Inntekt fra arbeidsforhold', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'MANN_ADOPTERER', 'Mann adopterer', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OMSORGSOVERTAKELSESDATO', 'Omsorgsovertakelsesdato', 'Omsorgsovertakelsesdato', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OMSORGSVILKAR', 'Foreldreansvar', 'Foreldreansvar', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OPPHOLDSRETT_EOS', 'Bruker har oppholdsrett i EØS', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OPPHOLDSRETT_IKKE_EOS', 'Bruker har ikke oppholdsrett i EØS', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OVERSTYRT_BEREGNING', 'Overstyrt beregning', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OVERSTYRT_VURDERING', 'Overstyrt vurdering', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'SOKERSOPPLYSNINGSPLIKT', 'Søkers opplysningsplikt', 'Søkers opplysningsplikt', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'SOKNADSFRIST', 'Søknadsfrist', 'Søknadsfrist', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'TERMINDATO', 'Termindato', 'Termindato', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'UTSTEDTDATO', 'Utstedtdato', 'Utstedtdato', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'VILKAR_SOM_ANVENDES', 'Vilkår som anvendes', 'Vilkår som anvendes', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');

-- Nytt kodeverk HISTORIKK_ENDRET_FELT_VERDI_TYPE
INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier) VALUES ('HISTORIKK_ENDRET_FELT_VERDI_TYPE', 'Kodeverk for endret felt verdier', 'Kodeverk for endret felt verdier i historikkinnslag','VL');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'ADOPTERER_ALENE', 'adopterer alene', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'ADOPTERER_IKKE_ALENE', 'adopterer ikke alene', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'EKTEFELLES_BARN', 'ektefelles barn', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FORELDREANSVAR_2_TITTEL', 'Foreldreansvarsvilkåret §14-17 andre ledd', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FORELDREANSVAR_4_TITTEL', 'Foreldreansvarsvilkåret §14-17 fjerde ledd', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FORTSETT_BEHANDLING', 'Fortsett behandling', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'HENLEGG_BEHANDLING', 'Henlegg behandling', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'IKKE_EKTEFELLES_BARN', 'ikke ektefelles barn', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'IKKE_OPPFYLT', 'ikke oppfylt', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'IKKE_RELEVANT_PERIODE', 'Ikke relevant periode', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'INGEN_VARIG_ENDRING_NAERING', 'Ingen varig endring i næring', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OMSORGSVILKARET_TITTEL', 'Omsorgsvilkår §14-17 tredje ledd', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OPPFYLT', 'oppfylt', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'PERIODE_MEDLEM', 'Periode med medlemskap', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'PERIODE_UNNTAK', 'Periode med unntak fra medlemskap', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'VARIG_ENDRET_NAERING', 'Varig endret næring', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'VILKAR_IKKE_OPPFYLT', 'Vilkåret er ikke oppfylt', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'VILKAR_OPPFYLT', 'Vilkåret er oppfylt', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');

-- Nytt kodeverk HISTORIKK_OPPLYSNING_TYPE
INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier) VALUES ('HISTORIKK_OPPLYSNING_TYPE', 'Opplysningtype', 'Opplysningtype','VL');

--INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'ANTALL_BARN', 'Historikk.Template.5.AntallBarn', 'Antall barn', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_OPPLYSNING_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'ANTALL_BARN', 'Antall barn', 'Antall barn', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_OPPLYSNING_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FODSELSDATO', 'Fødselsdato', 'Fødselsdato', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_OPPLYSNING_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'TPS_ANTALL_BARN', 'Antall barn', 'Antall barn', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_OPPLYSNING_TYPE');

-- Nytt kodeverk HISTORIKK_RESULTAT_TYPE
INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier) VALUES ('HISTORIKK_RESULTAT_TYPE', 'Historikkinnslag resultat type', 'Historikkinnslag resultat type', 'VL');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'AVVIS_KLAGE', 'Klagen er avvist', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_RESULTAT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'MEDHOLD_I_KLAGE', 'Vedtaket er omgjort', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_RESULTAT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OPPHEVE_VEDTAK', 'Vedtaket er opphevet', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_RESULTAT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OPPRETTHOLDT_VEDTAK', 'Vedtaket er opprettholdt', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_RESULTAT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'STADFESTET_VEDTAK', 'Vedtaket er stadfestet', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_RESULTAT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'BEREGNET_AARSINNTEKT', 'Grunnlag for beregnet årsinntekt', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_RESULTAT_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'UTFALL_UENDRET', 'Overstyrt vurdering: Utfall er uendret', 'Overstyrt vurdering: Utfall er uendret', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_RESULTAT_TYPE');


-- Nytt kodeverk HISTORIKK_BEGRUNNELSE_TYPE
INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier) VALUES ('HISTORIKK_BEGRUNNELSE_TYPE', 'Historikkinnslag begrunnelse type', 'Historikkinnslag begrunnelse type', 'VL');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'SAKSBEH_START_PA_NYTT', 'Saksbehandling starter på nytt', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_BEGRUNNELSE_TYPE');
