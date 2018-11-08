CREATE TABLE GR_PERSONOPPLYSNING (
    ID NUMBER(19, 0) NOT NULL,
    BEHANDLING_ID NUMBER(19, 0) NOT NULL,
    SOEKER_PERSONOPPL_ID NUMBER(19, 0),
    AKTIV CHAR(1) DEFAULT 'N' NOT NULL, -- Defaulter til N slik at man aktivt må skru på
    VERSJON NUMBER(19, 0) DEFAULT 0 NOT NULL,
    opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av                      VARCHAR2(20 CHAR),
    endret_tid                     TIMESTAMP(3),
    CONSTRAINT PK_GR_PERSONOPPLYSNING PRIMARY KEY (ID)
);

COMMENT ON TABLE GR_PERSONOPPLYSNING IS 'Behandlingsgrunnlag for Personopplysning (aggregat) for søker med familie';
COMMENT ON COLUMN GR_PERSONOPPLYSNING.AKTIV IS 'Angir aktivt grunnlag for Behandling.  Kun ett innslag tillates å være aktivt(J), men mange kan være inaktive(N) ';

alter table GR_PERSONOPPLYSNING add constraint CHK_GR_PERSONOPPLYSNING check (AKTIV in ('J', 'N'));
CREATE INDEX IDX_GR_PERSONOPPLYSNING_01 ON GR_PERSONOPPLYSNING (BEHANDLING_ID);
CREATE INDEX IDX_GR_PERSONOPPLYSNING_02 ON GR_PERSONOPPLYSNING (SOEKER_PERSONOPPL_ID);

ALTER TABLE GR_PERSONOPPLYSNING ADD CONSTRAINT FK_GR_PERSONOPPL_BEH FOREIGN KEY (BEHANDLING_ID) REFERENCES BEHANDLING;
ALTER TABLE GR_PERSONOPPLYSNING ADD CONSTRAINT FK_GR_PERSONOPPL_PERSOPPL FOREIGN KEY (SOEKER_PERSONOPPL_ID) REFERENCES PERSONOPPLYSNING;

-- tillatt kun en aktiv per behandling_id, men mange inaktive (dersom begge kolonner er NULL teller ikke Oracle dette mot uniqueness
-- paa sikt kan aktiv='N' ryddes eller flyttes til egen partisjon
CREATE UNIQUE INDEX UIDX_GR_PERSONOPPLYSNING_01 ON GR_PERSONOPPLYSNING (
  (CASE WHEN AKTIV = 'J' THEN BEHANDLING_ID ELSE NULL END),
  (CASE WHEN AKTIV = 'J' THEN AKTIV ELSE NULL END)
);

CREATE SEQUENCE SEQ_GR_PERSONOPPLYSNING MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

-- migrer data
insert into GR_PERSONOPPLYSNING (id, behandling_id, soeker_personoppl_id, aktiv)
SELECT SEQ_GR_PERSONOPPLYSNING.NEXTVAL as id, B.id as behandling_d, P.ID as soeker_personoppl_id, 'J' as aktiv
FROM PERSONOPPLYSNING P 
INNER JOIN BEHANDLING_GRUNNLAG BG ON P.BEHANDLING_GRUNNLAG_ID = BG.ID
inner join behandling b on B.BEHANDLING_GRUNNLAG_ID = bg.id;

-- tar aller sist
-- alter table personopplysning drop column behandling_grunnlag_id;

