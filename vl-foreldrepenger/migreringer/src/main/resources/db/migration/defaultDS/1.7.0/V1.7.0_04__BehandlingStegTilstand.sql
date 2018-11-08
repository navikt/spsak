-- Tabell: BEHANDLING_STEG_TILSTAND
CREATE TABLE BEHANDLING_STEG_TILSTAND (
    id             NUMBER(19) NOT NULL,
    behandling_id NUMBER(19) NOT NULL,
    behandling_steg    VARCHAR2(20 CHAR) NOT NULL,
    behandling_steg_status VARCHAR2(20 CHAR),
    behandling_steg_tilstand VARCHAR2(50 char),
    versjon           NUMBER(19) DEFAULT 0 NOT NULL,
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_BEHANDLING_STEG_TILSTAND PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_BEHANDLING_STEG_TILSTAND MINVALUE 1 START WITH 100000 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE BEHANDLING_STEG_TILSTAND IS 'Angir tilstand for behandlingsteg som kjøres';
COMMENT ON COLUMN BEHANDLING_STEG_TILSTAND.behandling_id IS 'Behandling steget er tilknyttet';
COMMENT ON COLUMN BEHANDLING_STEG_TILSTAND.BEHANDLING_STEG IS 'Hvilket BehandlingSteg som kjøres';
COMMENT ON COLUMN BEHANDLING_STEG_TILSTAND.BEHANDLING_STEG_STATUS IS 'Status på steg: (ved) INNGANG, STARTET, VENTER, (ved) UTGANG, UTFØRT';
COMMENT ON COLUMN BEHANDLING_STEG_TILSTAND.BEHANDLING_STEG_TILSTAND IS 'Intern tilstand for et steg som er på vent';

ALTER TABLE BEHANDLING_STEG_TILSTAND ADD CONSTRAINT FK_BEHANDLING_STEG_TILSTAND_1 FOREIGN KEY (behandling_id) REFERENCES BEHANDLING;
ALTER TABLE BEHANDLING_STEG_TILSTAND ADD CONSTRAINT FK_BEHANDLING_STEG_TILSTAND_2 FOREIGN KEY (behandling_steg) REFERENCES BEHANDLING_STEG_TYPE;
ALTER TABLE BEHANDLING_STEG_TILSTAND ADD CONSTRAINT FK_BEHANDLING_STEG_TILSTAND_3 FOREIGN KEY (behandling_steg_status) REFERENCES BEHANDLING_STEG_STATUS_TYPE;

CREATE UNIQUE INDEX UIDX_BEHANDLING_STEG_TILSTA_1 ON BEHANDLING_STEG_TILSTAND(behandling_id, behandling_steg);
CREATE INDEX IDX_BEHANDLING_STEG_TILSTAND_1 ON BEHANDLING_STEG_TILSTAND(behandling_steg);

-- rydd BEHANDLING
ALTER TABLE BEHANDLING DROP (BEHANDLING_STEG, BEHANDLING_STEG_STATUS);

