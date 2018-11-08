-- Tabell: BEHANDLING_STEG_STATUS_TYPE
CREATE TABLE BEHANDLING_STEG_STATUS_TYPE (
    kode            VARCHAR2(20 CHAR) NOT NULL,
    navn            VARCHAR2(50 CHAR) NOT NULL,
    beskrivelse     VARCHAR2(2000 CHAR),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_BEHANDLING_STEG_STATUS_TYPE PRIMARY KEY (kode)
);

COMMENT ON TABLE BEHANDLING_STEG_STATUS_TYPE IS 'Angir hvilke status et BehanlingSteg kan ha når det kjøres';
COMMENT ON COLUMN BEHANDLING_STEG_STATUS_TYPE.KODE IS 'PK - angir unik kode for statusen';
COMMENT ON COLUMN BEHANDLING_STEG_STATUS_TYPE.NAVN IS 'Et lesbart navn for statusen, ment for visning el.';
COMMENT ON COLUMN BEHANDLING_STEG_STATUS_TYPE.BESKRIVELSE IS 'Beskrivelse/forklaring av når statusen brukes'; 

INSERT INTO BEHANDLING_STEG_STATUS_TYPE(kode, navn, beskrivelse) VALUES ('INNGANG', 'Inngangkriterier er ikke oppfylt', 'Ved inngang til steget. Steget settes til STARTET når inngangskriterier er oppfylt');
INSERT INTO BEHANDLING_STEG_STATUS_TYPE(kode, navn, beskrivelse) VALUES ('STARTET', 'Steget er startet', 'Steget er startet etter at alle inngangskriterier er oppfylt.  Skjer etter INNGANG');
INSERT INTO BEHANDLING_STEG_STATUS_TYPE(kode, navn, beskrivelse) VALUES ('VENTER', 'På vent', 'Steget er satt på vent i påvente at at en ekstern hendelse eller tidspunkt skal passere.');
INSERT INTO BEHANDLING_STEG_STATUS_TYPE(kode, navn, beskrivelse) VALUES ('UTGANG', 'Utgangskriterier er ikke oppfylt', 'Ved utgang av steget. Steget settes til UTFØRT når alle utgangskriterier er oppfylt');
INSERT INTO BEHANDLING_STEG_STATUS_TYPE(kode, navn, beskrivelse) VALUES ('UTFØRT', 'Utført', 'Steget er ferdig utført');

ALTER TABLE BEHANDLING add behandling_steg_status varchar2(20 char);
create index IDX_BEHANDLING_6 ON BEHANDLING (behandling_steg_status);

