
-- ------------------------------- --
-- VERGE_TYPE                      --
-- ------------------------------- --
INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier ) VALUES ('VERGE_TYPE', 'Kodeverk over gyldige typer av verge', '', 'VL');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'BARN', 'Verge for barn under 18 år', 'Verge som forelder til egne barn under 18 år, eventuelt foreldreansvar for ett barn som egentlig ikke er ditt eget.', to_date('2000-01-01', 'YYYY-MM-DD'), 'VERGE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FBARN', 'Verge for foreldreløst barn under 18 år', 'Oppnevnt som verge for et barn under 18 år som er uten foreldre (eks: mindreårige asylsøkere)', to_date('2000-01-01', 'YYYY-MM-DD'), 'VERGE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'VOKSEN', 'Verge for voksen', 'Oppnevnt som verge for en voksen', to_date('2000-01-01', 'YYYY-MM-DD'), 'VERGE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, '-', 'Ikke satt eller valgt kode', 'Ikke satt eller valgt kode', to_date('2000-01-01', 'YYYY-MM-DD'), 'VERGE_TYPE');

-- ------------------------------- --
-- MANDAT_TYPE                     --
-- ------------------------------- --
INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier ) VALUES ('MANDAT_TYPE', 'Kodeverk over gyldiga typer av mandat en verge kan ha', '', 'VL');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, '-', 'Ikke satt eller valgt kode', 'Ikke satt eller valgt kode', to_date('2000-01-01', 'YYYY-MM-DD'), 'MANDAT_TYPE');


-- ------------------------------- --
-- DOKUMENT_MOTTAKER               --
-- ------------------------------- --
CREATE TABLE DOKUMENT_MOTTAKER (
  id                                NUMBER(19) NOT NULL,
  behandling_grunnlag_id            NUMBER(19) NOT NULL,
  bruker_id                         NUMBER(19) NOT NULL,
  opprettet_av                      VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                     TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                         VARCHAR2(20 CHAR),
  endret_tid                        TIMESTAMP(3),
  CONSTRAINT PK_DOKUMENT_MOTTAKER   PRIMARY KEY (id),
  CONSTRAINT FK_DOKUMENT_MOTTAKER_1 FOREIGN KEY (bruker_id) REFERENCES BRUKER,
  CONSTRAINT FK_DOKUMENT_MOTTAKER_2 FOREIGN KEY (behandling_grunnlag_id) REFERENCES BEHANDLING_GRUNNLAG
);

CREATE UNIQUE INDEX UIDX_DOKUMENT_MOTTAKERE_1 ON DOKUMENT_MOTTAKER(behandling_grunnlag_id, bruker_id);
CREATE INDEX IDX_DOKUMENT_MOTTAKER_1 ON DOKUMENT_MOTTAKER (bruker_id);
CREATE INDEX IDX_DOKUMENT_MOTTAKER_2 ON DOKUMENT_MOTTAKER (behandling_grunnlag_id);

CREATE SEQUENCE SEQ_DOKUMENT_MOTTAKER MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE DOKUMENT_MOTTAKER                         IS 'Lister aktuelle mottakere av dokumenter, git en behandling';
COMMENT ON COLUMN DOKUMENT_MOTTAKER.behandling_grunnlag_id IS 'FK til BEHANDLING_GRUNNLAG';
COMMENT ON COLUMN DOKUMENT_MOTTAKER.bruker_id              IS 'FK til BRUKER';

-- ------------------------------- --
-- VERGE                           --
-- ------------------------------- --
CREATE TABLE VERGE (
  id                             NUMBER(19) NOT NULL,
  behandlinggrunnlag_id          NUMBER(19) NOT NULL,
  navn                           VARCHAR(256),
  bruker_id                      NUMBER(19) NOT NULL,
  vedtaksdato                    DATE,
  tvungen_forvaltning            VARCHAR2(1 CHAR),
  mandat_tekst                   VARCHAR2(2000) NOT NULL,
  gyldig_fom                     DATE,
  gyldig_tom                     DATE,
  verge_type                     VARCHAR2(100) NOT NULL,
  kl_verge_type                  VARCHAR2(100) AS ('VERGE_TYPE'),
  --mandat_type                    VARCHAR2(100) NOT NULL,
  --kl_mandat_type                 VARCHAR2(100) AS ('MANDAT_TYPE'),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_VERGE            PRIMARY KEY (id),
  CONSTRAINT FK_VERGE_1 FOREIGN KEY (bruker_id) REFERENCES BRUKER,
  CONSTRAINT FK_VERGE_2 FOREIGN KEY (behandlinggrunnlag_id) REFERENCES BEHANDLING_GRUNNLAG,
  CONSTRAINT FK_VERGE_3 FOREIGN KEY (kl_verge_type, verge_type)   REFERENCES KODELISTE (kodeverk, kode)
  --CONSTRAINT FK_VERGE_4 FOREIGN KEY (kl_mandat_type, mandat_type) REFERENCES KODELISTE (kodeverk, kode)
);

CREATE UNIQUE INDEX UIDX_VERGE_1 ON VERGE(behandlinggrunnlag_id);

CREATE INDEX IDX_VERGE_1 ON VERGE(bruker_id);
CREATE INDEX IDX_VERGE_2 ON VERGE(verge_type);
--CREATE INDEX IDX_VERGE_3 ON VERGE(mandat_type);

CREATE SEQUENCE SEQ_VERGE MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE VERGE                        IS 'Informasjon om verge';
COMMENT ON COLUMN VERGE.behandlinggrunnlag_id IS 'FK til BEHANDLING_GRUNNLAG';
COMMENT ON COLUMN VERGE.tvungen_forvaltning   IS 'Hvis soker er under tvungen forvaltning';
COMMENT ON COLUMN VERGE.bruker_id             IS 'FK til BRUKER';
COMMENT ON COLUMN VERGE.navn                  IS 'Navnet til vergen';
COMMENT ON COLUMN VERGE.vedtaksdato           IS 'Dato for vedtak ';
COMMENT ON COLUMN VERGE.mandat_tekst          IS 'Beskrivelse for tilpasset mandat';
COMMENT ON COLUMN VERGE.gyldig_fom            IS 'Hvis fullmakt er begrenset i periode, dato for når fullmakten er gyldig fra';
COMMENT ON COLUMN VERGE.gyldig_tom            IS 'Hvis fullmakt er begrenset i periode, dato for når fullmakten er gyldig til';
COMMENT ON COLUMN VERGE.verge_type            IS 'Type verge';
--COMMENT ON COLUMN VERGE.mandat_type           IS 'Type mandat ';
