CREATE TABLE GR_VERGE (
    ID NUMBER(19, 0) NOT NULL,
    BEHANDLING_ID NUMBER(19, 0) NOT NULL,
    VERGE_ID NUMBER(19, 0),
    AKTIV CHAR(1) DEFAULT 'N' NOT NULL, -- Defaulter til N slik at man aktivt må skru på
    VERSJON NUMBER(19, 0) DEFAULT 0 NOT NULL,
    opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av                      VARCHAR2(20 CHAR),
    endret_tid                     TIMESTAMP(3),
    CONSTRAINT PK_GR_VERGE PRIMARY KEY (ID)
);

COMMENT ON TABLE GR_VERGE IS 'Behandlingsgrunnlag for Verge (aggregat)';
COMMENT ON COLUMN GR_VERGE.ID IS 'Primary Key';
COMMENT ON COLUMN GR_VERGE.BEHANDLING_ID IS 'FK: BEHANDLING';
COMMENT ON COLUMN GR_VERGE.VERGE_ID IS 'FK: VERGE';
COMMENT ON COLUMN GR_VERGE.AKTIV IS 'Angir aktivt grunnlag for Behandling.  Kun ett innslag tillates å være aktivt(J), men mange kan være inaktive(N) ';

alter table GR_VERGE add constraint CHK_GR_VERGE check (AKTIV in ('J', 'N'));
CREATE INDEX IDX_GR_VERGE_01 ON GR_VERGE (BEHANDLING_ID);
CREATE INDEX IDX_GR_VERGE_02 ON GR_VERGE (VERGE_ID);

ALTER TABLE GR_VERGE ADD CONSTRAINT FK_GR_VERGE_BEH FOREIGN KEY (BEHANDLING_ID) REFERENCES BEHANDLING;
ALTER TABLE GR_VERGE ADD CONSTRAINT FK_GR_VERGE_VID FOREIGN KEY (VERGE_ID) REFERENCES VERGE;

-- tillatt kun en aktiv per behandling_id, men mange inaktive (dersom begge kolonner er NULL teller ikke Oracle dette mot uniqueness
-- paa sikt kan aktiv='N' ryddes eller flyttes til egen partisjon
CREATE UNIQUE INDEX UIDX_GR_VERGE_01 ON GR_VERGE (
  (CASE WHEN AKTIV = 'J' THEN BEHANDLING_ID ELSE NULL END),
  (CASE WHEN AKTIV = 'J' THEN AKTIV ELSE NULL END)
);

CREATE SEQUENCE SEQ_GR_VERGE MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

-- migrer data
insert into GR_VERGE (id, behandling_id, VERGE_ID, aktiv)
SELECT SEQ_GR_VERGE.NEXTVAL as id, B.id as behandling_d, V.ID as verge_id, 'J' as aktiv
FROM VERGE V
INNER JOIN BEHANDLING_GRUNNLAG BG ON V.BEHANDLINGGRUNNLAG_ID = BG.ID
inner join behandling b on B.BEHANDLING_GRUNNLAG_ID = bg.id;
