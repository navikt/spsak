-- Tabell
CREATE TABLE DOKUMENT_MAL_TYPE (
    kode            VARCHAR2(7 CHAR) NOT NULL,
    navn            VARCHAR2(50 CHAR) NOT NULL,
    doksys_id       VARCHAR2(50 CHAR) NOT NULL,
    beskrivelse     VARCHAR2(4000 CHAR),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
	CONSTRAINT PK_DOKUMENT_MAL_TYPE PRIMARY KEY (KODE)
);

-- Comments
COMMENT ON TABLE DOKUMENT_MAL_TYPE IS 'Angir type av dokument mal';

-- Inserts
INSERT INTO DOKUMENT_MAL_TYPE (kode, navn, doksys_id) VALUES ('DMT-001', 'Innhent dokumentasjon', '000049');

