CREATE TABLE GR_MEDLEMSKAP (
    ID NUMBER(19, 0) NOT NULL,
    BEHANDLING_ID NUMBER(19, 0) NOT NULL,
    REGISTRERT_ID NUMBER(19, 0),
    OPPGITT_ID NUMBER(19, 0),
    VURDERING_ID NUMBER(19, 0),
    AKTIV CHAR(1) DEFAULT 'N' NOT NULL, -- Defaulter til N slik at man aktivt må skru på
    VERSJON NUMBER(19, 0) DEFAULT 0 NOT NULL,
    opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av                      VARCHAR2(20 CHAR),
    endret_tid                     TIMESTAMP(3),
    CONSTRAINT PK_GR_MEDLEMSKAP PRIMARY KEY (ID)
);

COMMENT ON TABLE GR_MEDLEMSKAP IS 'Behandlingsgrunnlag for MEDLEMSKAP (aggregat) for søker med familie';
COMMENT ON COLUMN GR_MEDLEMSKAP.AKTIV IS 'Angir aktivt grunnlag for Behandling.  Kun ett innslag tillates å være aktivt(J), men mange kan være inaktive(N) ';

alter table GR_MEDLEMSKAP add constraint CHK_GR_MEDLEMSKAP check (AKTIV in ('J', 'N'));
CREATE INDEX IDX_GR_MEDLEMSKAP_01 ON GR_MEDLEMSKAP (BEHANDLING_ID);
CREATE INDEX IDX_GR_MEDLEMSKAP_02 ON GR_MEDLEMSKAP (REGISTRERT_ID);
CREATE INDEX IDX_GR_MEDLEMSKAP_03 ON GR_MEDLEMSKAP (OPPGITT_ID);
CREATE INDEX IDX_GR_MEDLEMSKAP_04 ON GR_MEDLEMSKAP (VURDERING_ID);

alter table TILKNYTNING_HJEMLAND RENAME TO MEDLEMSKAP_OPPG_TILKNYT;
COMMENT ON TABLE MEDLEMSKAP_OPPG_TILKNYT IS 'Oppgitt tilknytning til hjemland fra Søker';
alter table MEDLEMSKAP RENAME TO MEDLEMSKAP_VURDERING;
COMMENT ON TABLE MEDLEMSKAP_VURDERING IS 'Medlemskap slik det er vurdert av systemet eller saksbehandler';

alter table UTLANDSOPPHOLD RENAME TO MEDLEMSKAP_OPPG_UTLAND;

ALTER TABLE GR_MEDLEMSKAP ADD CONSTRAINT FK_GR_MEDL_BEH FOREIGN KEY (BEHANDLING_ID) REFERENCES BEHANDLING;
ALTER TABLE GR_MEDLEMSKAP ADD CONSTRAINT FK_GR_MEDL_TILKN_HJEM FOREIGN KEY (OPPGITT_ID) REFERENCES MEDLEMSKAP_OPPG_TILKNYT;
ALTER TABLE GR_MEDLEMSKAP ADD CONSTRAINT FK_GR_MEDL_MEDL FOREIGN KEY (VURDERING_ID) REFERENCES MEDLEMSKAP_VURDERING;

-- tillatt kun en aktiv per behandling_id, men mange inaktive (dersom begge kolonner er NULL teller ikke Oracle dette mot uniqueness
-- paa sikt kan aktiv='N' ryddes eller flyttes til egen partisjon
CREATE UNIQUE INDEX UIDX_GR_MEDLEMSKAP_01 ON GR_MEDLEMSKAP (
  (CASE WHEN AKTIV = 'J' THEN BEHANDLING_ID ELSE NULL END),
  (CASE WHEN AKTIV = 'J' THEN AKTIV ELSE NULL END)
);

CREATE SEQUENCE SEQ_GR_MEDLEMSKAP MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

alter table MEDLEMSKAP_PERIODER add MEDLEMSKAP_REGISTRERT_ID NUMBER(19, 0);
alter table MEDLEMSKAP_PERIODER modify behandling_grunnlag_id NULL;
alter table MEDLEMSKAP_VURDERING modify behandling_grunnlag_id NULL;

create INDEX IDX_MEDLEMSKAP_PERIODER_03 ON MEDLEMSKAP_PERIODER(MEDLEMSKAP_REGISTRERT_ID);

--  MEDLEMSKAP_REGISTRERT
CREATE TABLE MEDLEMSKAP_REGISTRERT (
    ID NUMBER(19, 0) NOT NULL,
    VERSJON NUMBER(19, 0) DEFAULT 0 NOT NULL,
    KILDE varchar2(200 char) DEFAULT 'MEDL' NOT NULL,
    behandling_grunnlag_id NUMBER(19, 0),
    opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av                      VARCHAR2(20 CHAR),
    endret_tid                     TIMESTAMP(3),
    CONSTRAINT PK_MEDLEMSKAP_REGISTRERT PRIMARY KEY (ID)
);

COMMENT ON TABLE MEDLEMSKAP_REGISTRERT IS 'Aggregat for medlemskap opplysninger fra register (MEDL)';
ALTER TABLE GR_MEDLEMSKAP ADD CONSTRAINT FK_GR_MEDL_REG_MEDL FOREIGN KEY (REGISTRERT_ID) REFERENCES MEDLEMSKAP_REGISTRERT;

CREATE SEQUENCE SEQ_MEDLEMSKAP_REGISTRERT MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE UNIQUE INDEX IDX_MEDLEMSKAP_REGISTRERT_01 ON MEDLEMSKAP_REGISTRERT (behandling_grunnlag_id);

-- migrer data i MEDLEMSKAP_PERIODER

insert into MEDLEMSKAP_REGISTRERT (id, behandling_grunnlag_id) select SEQ_MEDLEMSKAP_REGISTRERT.nextval, bg.id from BEHANDLING_GRUNNLAG bg;

merge into MEDLEMSKAP_PERIODER mp
using (select id, behandling_grunnlag_id from MEDLEMSKAP_REGISTRERT) mr
on (mp.behandling_grunnlag_id = mr.behandling_grunnlag_id)
when matched then update set mp.MEDLEMSKAP_REGISTRERT_id = mr.id;


-- migrer data til GR_MEDLEMSKAP
insert into GR_MEDLEMSKAP (id, behandling_id, REGISTRERT_ID, OPPGITT_ID, VURDERING_ID, aktiv)
SELECT SEQ_GR_MEDLEMSKAP.NEXTVAL as id, behandling_id, REGISTRERT_ID, OPPGITT_ID, VURDERING_ID, 'J' as aktiv
FROM 
(SELECT distinct b.id as behandling_id, r.id as REGISTRERT_ID, s.tilknytning_hjemland_id as OPPGITT_ID, v.id as VURDERING_ID
FROM BEHANDLING b
INNER JOIN BEHANDLING_GRUNNLAG bg on b.behandling_grunnlag_id = bg.id
LEFT OUTER JOIN MEDLEMSKAP_REGISTRERT r ON r.behandling_grunnlag_id = bg.id
LEFT OUTER JOIN SOEKNAD s ON s.tilknytning_hjemland_id IS NOT NULL AND s.behandling_grunnlag_id = bg.id
LEFT OUTER JOIN MEDLEMSKAP_VURDERING V ON V.BEHANDLING_GRUNNLAG_ID = BG.ID
)
;

-- tar aller sist etter andre migreringer
-- drop unused columns (destruktivt), legg innn når vi får bort søknad
--alter table MEDLEMSKAP_REGISTRERT set unused (behandling_grunnlag_id);
--alter table MEDLEMSKAP_PERIODER set unused (behandling_grunnlag_id);
--alter table MEDLEMSKAP_VURDERING set unused (behandling_grunnlag_id);

