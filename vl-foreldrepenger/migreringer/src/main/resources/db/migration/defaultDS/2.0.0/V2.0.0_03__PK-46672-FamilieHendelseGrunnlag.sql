-- rename konflikterende tabeller
DROP INDEX UIDX_TERMINBEKREFTELSE_1;
DROP INDEX UIDX_ADOPSJON_1;
RENAME SEQ_SOEKNAD_BARN TO SEQ_UIDENTIFISERT_BARN;
ALTER TABLE SOEKNAD
  ADD familie_hendelse_id NUMBER(19);
ALTER TABLE SOEKNAD
  DROP CONSTRAINT FK_SOEKNAD_1;
ALTER TABLE SOEKNAD_BARN
  DROP CONSTRAINT FK_SOEKNAD_BARN;
ALTER TABLE TERMINBEKREFTELSE
  ADD familie_hendelse_id NUMBER(19);
ALTER TABLE TERMINBEKREFTELSE
  ADD navn VARCHAR(256);
ALTER TABLE ADOPSJON
  ADD familie_hendelse_id NUMBER(19);
ALTER TABLE ADOPSJON
  ADD omsorg_vilkaar_type VARCHAR2(100 CHAR) DEFAULT '-' NOT NULL;
ALTER TABLE ADOPSJON
  ADD kl_omsorg_vilkaar_type VARCHAR2(100) AS ('OMSORGSOVERTAKELSE_VILKAR');
ALTER TABLE ADOPSJON
  ADD CONSTRAINT FK_ADOPSJON_2 FOREIGN KEY (kl_omsorg_vilkaar_type, omsorg_vilkaar_type) REFERENCES KODELISTE (kodeverk, kode);
ALTER TABLE SOEKNAD_BARN
  ADD familie_hendelse_id NUMBER(19);
ALTER TABLE SOEKNAD_BARN
RENAME TO UIDENTIFISERT_BARN;
ALTER TABLE UIDENTIFISERT_BARN
  MODIFY BARN_NUMMER NUMBER(19) NULL;
-- KODEVERK

INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES
  ('FAMILIE_HENDELSE_TYPE', 'Kodeverk over gyldige typer av familie hendelser (fødsel, adopsjon, omsorgovertakelse', '',
   'VL');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'ADPSJN', 'Adopsjon', 'Adopsjon', to_date('2000-01-01', 'YYYY-MM-DD'),
        'FAMILIE_HENDELSE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'OMSRGO', 'Omsorgoverdragelse', 'Omsorgoverdragelse', to_date('2000-01-01', 'YYYY-MM-DD'),
   'FAMILIE_HENDELSE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'FODSL', 'Fødsel', 'Fødsel', to_date('2000-01-01', 'YYYY-MM-DD'),
   'FAMILIE_HENDELSE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'TERM', 'Termin', 'Termin', to_date('2000-01-01', 'YYYY-MM-DD'),
        'FAMILIE_HENDELSE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, '-', 'Ikke satt eller valgt kode', 'Ikke satt eller valgt kode',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'FAMILIE_HENDELSE_TYPE');

-- Opprett tabeller
CREATE TABLE FAMILIE_HENDELSE (
  id                         NUMBER(19)                        NOT NULL,
  TMP_behandling_grunnlag_id NUMBER(19),
  antall_barn                NUMBER(3),
  familie_hendelse_type      VARCHAR2(100 CHAR)                NOT NULL,
  kl_familie_hendelse_type   VARCHAR2(100) AS ('FAMILIE_HENDELSE_TYPE'),
  versjon                    NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av               VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid              TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                  VARCHAR2(20 CHAR),
  endret_tid                 TIMESTAMP(3),
  CONSTRAINT PK_FAMILIE_HENDELSE PRIMARY KEY (id),
  CONSTRAINT FK_FAMILIE_HENDELSE_1 FOREIGN KEY (kl_familie_hendelse_type, familie_hendelse_type) REFERENCES KODELISTE (kodeverk, kode)
);

-- CREATE TABLE ADOPSJON (
--   id                      NUMBER(19)                        NOT NULL,
--   familie_hendelse_id     NUMBER(19)                        NOT NULL,
--   omsorgsovertakelse_dato DATE,
--   ektefelles_barn         VARCHAR2(1 CHAR),
--   adopterer_alene         VARCHAR2(1 CHAR),
--   omsorg_vilkaar_type     VARCHAR2(100 CHAR)                NOT NULL,
--   kl_omsorg_vilkaar_type  VARCHAR2(100) AS ('OMSORGSOVERTAKELSE_VILKAR'),
--   versjon                 NUMBER(19) DEFAULT 0              NOT NULL,
--   opprettet_av            VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
--   opprettet_tid           TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
--   endret_av               VARCHAR2(20 CHAR),
--   endret_tid              TIMESTAMP(3),
--   CONSTRAINT PK_TMP_ADOPSJON PRIMARY KEY (id),
--   CONSTRAINT FK_TMP_ADOPSJON_1 FOREIGN KEY (familie_hendelse_id) REFERENCES FAMILIE_HENDELSE,
--   CONSTRAINT FK_TMP_ADOPSJON_2 FOREIGN KEY (kl_omsorg_vilkaar_type, omsorg_vilkaar_type) REFERENCES KODELISTE (kodeverk, kode)
-- );
--
-- -- ALter table
-- CREATE TABLE TERMINBEKREFTELSE (
--   id                  NUMBER(19)                        NOT NULL,
--   familie_hendelse_id NUMBER(19)                        NOT NULL,
--   utstedt_dato        DATE                              NOT NULL,
--   termin_dato         DATE                              NOT NULL,
--   navn                VARCHAR(256)                      NOT NULL,
--   versjon             NUMBER(19) DEFAULT 0              NOT NULL,
--   opprettet_av        VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
--   opprettet_tid       TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
--   endret_av           VARCHAR2(20 CHAR),
--   endret_tid          TIMESTAMP(3),
--   CONSTRAINT PK_TMP_TERMINBEKREFTELSE PRIMARY KEY (id),
--   CONSTRAINT FK_TMP_TERMINBEKREFTELSE_1 FOREIGN KEY (familie_hendelse_id) REFERENCES FAMILIE_HENDELSE
-- );

-- CREATE TABLE UIDENTIFISERT_BARN (
--   id                  NUMBER(19)                        NOT NULL,
--   familie_hendelse_id NUMBER(19)                        NOT NULL,
--   barn_nummer         NUMBER(3),
--   foedselsdato        DATE                              NOT NULL,
--   opprettet_av        VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
--   opprettet_tid       TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
--   endret_av           VARCHAR2(20 CHAR),
--   endret_tid          TIMESTAMP(3),
--   CONSTRAINT PK_UIDENTIFISERT_BARN PRIMARY KEY (id),
--   CONSTRAINT FK_UIDENTIFISERT_BARN_1 FOREIGN KEY (familie_hendelse_id) REFERENCES FAMILIE_HENDELSE
-- );

CREATE TABLE GR_FAMILIE_HENDELSE (
  id                            NUMBER(19)                        NOT NULL,
  behandling_id                 NUMBER(19)                        NOT NULL,
  soeknad_familie_hendelse_id   NUMBER(19)                        NOT NULL,
  bekreftet_familie_hendelse_id NUMBER(19),
  overstyrt_familie_hendelse_id NUMBER(19),
  aktiv                         VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL,
  versjon                       NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av                  VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid                 TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                     VARCHAR2(20 CHAR),
  endret_tid                    TIMESTAMP(3),
  CONSTRAINT PK_GR_FAMILIE_HENDELSE PRIMARY KEY (id),
  CONSTRAINT FK_GR_FAMILIE_HENDELSE_1 FOREIGN KEY (behandling_id) REFERENCES BEHANDLING,
  CONSTRAINT FK_GR_FAMILIE_HENDELSE_2 FOREIGN KEY (soeknad_familie_hendelse_id) REFERENCES FAMILIE_HENDELSE,
  CONSTRAINT FK_GR_FAMILIE_HENDELSE_3 FOREIGN KEY (bekreftet_familie_hendelse_id) REFERENCES FAMILIE_HENDELSE,
  CONSTRAINT FK_GR_FAMILIE_HENDELSE_4 FOREIGN KEY (overstyrt_familie_hendelse_id) REFERENCES FAMILIE_HENDELSE,
  CONSTRAINT CHK_GR_FAMILIE_HENDELSE CHECK (AKTIV IN ('J', 'N'))
);

CREATE UNIQUE INDEX UIDX_GR_FAMILIE_HENDELSE_01
  ON GR_FAMILIE_HENDELSE (
    (CASE WHEN AKTIV = 'J'
      THEN BEHANDLING_ID
     ELSE NULL END),
    (CASE WHEN AKTIV = 'J'
      THEN AKTIV
     ELSE NULL END)
  );

CREATE SEQUENCE SEQ_GR_FAMILIE_HENDELSE
MINVALUE 1
START WITH 1
INCREMENT BY 50
NOCACHE
NOCYCLE;
-- Rename søknad barn seq istedenfor
-- CREATE SEQUENCE SEQ_UIDENTIFISERT_BARN MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_FAMILIE_HENDELSE
MINVALUE 1
START WITH 1
INCREMENT BY 50
NOCACHE
NOCYCLE;

-- Legg til kommentarer

-- Migrer data til nye tabeller
-- #1 Lager hendelse av søknaden
INSERT INTO FAMILIE_HENDELSE (id, TMP_behandling_grunnlag_id, antall_barn, familie_hendelse_type)
  SELECT
    SEQ_FAMILIE_HENDELSE.NEXTVAL AS id,
    grunnlag_id,
    antall_barn,
    '-'                          AS type
  FROM
    (SELECT DISTINCT
       bg.id                     AS grunnlag_id,
       s.antall_barn_fra_soeknad AS antall_barn
     FROM BEHANDLING b
       INNER JOIN BEHANDLING_GRUNNLAG bg ON b.behandling_grunnlag_id = bg.id
       LEFT OUTER JOIN SOEKNAD s ON s.behandling_grunnlag_id = bg.id
    );
-- ## Oppretter grunnlag for alle behandlingsgrunnlag
INSERT INTO GR_FAMILIE_HENDELSE (id, behandling_id, soeknad_familie_hendelse_id, aktiv)
  SELECT
    SEQ_GR_FAMILIE_HENDELSE.nextval AS id,
    behandling_id,
    soknad_hendelse,
    'J'                             AS aktiv
  FROM (
    SELECT DISTINCT
      b.id                                       AS behandling_id,
      (SELECT f.id
       FROM FAMILIE_HENDELSE f
       WHERE TMP_behandling_grunnlag_id = bg.id) AS soknad_hendelse
    FROM BEHANDLING b
      INNER JOIN BEHANDLING_GRUNNLAG bg ON b.behandling_grunnlag_id = bg.id
  );

-- #3 Terminbekreftelse av søknad
INSERT INTO TERMINBEKREFTELSE (id, behandling_grunnlag_id, familie_hendelse_id, utstedt_dato, termindato, navn)
  SELECT
    SEQ_TERMINBEKREFTELSE.NEXTVAL AS id,
    behandling_id,
    familie_hendelse_id,
    utstedt_dato,
    termin_dato                   AS termindato,
    navn
  FROM
    (
      SELECT DISTINCT
        b.behandling_grunnlag_id         AS behandling_id,
        gr.soeknad_familie_hendelse_id   AS familie_hendelse_id,
        s.termindato_fra_soeknad         AS termin_dato,
        s.utstedt_dato_terminbekreftelse AS utstedt_dato,
        s.navn_paa_terminbekreftelse     AS navn
      FROM BEHANDLING b
        INNER JOIN BEHANDLING_GRUNNLAG bg ON b.behandling_grunnlag_id = bg.id
        LEFT OUTER JOIN SOEKNAD s ON s.behandling_grunnlag_id = bg.id
        INNER JOIN GR_FAMILIE_HENDELSE gr ON gr.behandling_id = b.id
      WHERE s.termindato_fra_soeknad IS NOT NULL
            AND s.utstedt_dato_terminbekreftelse IS NOT NULL
    );
-- # Adopsjon / omsorg av søknad
INSERT INTO ADOPSJON (id, behandling_grunnlag_id, familie_hendelse_id, OMSORGSOVERTAKELSE_DATO, adopterer_alene, ektefelles_barn, omsorg_vilkaar_type)
  SELECT
    SEQ_ADOPSJON.NEXTVAL   AS id,
    behandling_id,
    familie_hendelse_id    AS familie_hendelse_id,
    omsorgovertakelse_dato AS OMSORGSOVERTAKELSE_DATO,
    'N'                    AS ektefelles_barn,
    adopterer_alene        AS adopterer_alene,
    omsorgsvilkår          AS omsorg_vilkaar_type
  FROM
    (
      SELECT DISTINCT
        b.behandling_grunnlag_id       AS behandling_id,
        gr.soeknad_familie_hendelse_id AS familie_hendelse_id,
        s.adop_omsorgover_dato         AS omsorgovertakelse_dato,
        CASE s.far_SOEKer_TYPE
        WHEN 'ADOPTERER_ALENE'
          THEN 'J'
        ELSE 'N' END                   AS adopterer_alene,
        CASE s.far_SOEKer_TYPE
        WHEN 'ADOPTERER_ALENE'
          THEN '-'
        WHEN '-'
          THEN '-'
        ELSE 'FP_VK_5' END             AS omsorgsvilkår
      FROM BEHANDLING b
        INNER JOIN BEHANDLING_GRUNNLAG bg ON b.behandling_grunnlag_id = bg.id
        INNER JOIN GR_FAMILIE_HENDELSE gr ON gr.behandling_id = b.id
        LEFT OUTER JOIN SOEKNAD s ON s.behandling_grunnlag_id = bg.id
      WHERE s.ADOP_OMSORGOVER_DATO IS NOT NULL
    );
-- ## Fødsel av søkand

MERGE INTO SOEKNAD s
USING (SELECT
         s.id AS soeknad_id,
         gr.SOEKNAD_FAMILIE_HENDELSE_ID
       FROM SOEKNAD s
         INNER JOIN BEHANDLING_GRUNNLAG bg ON s.BEHANDLING_GRUNNLAG_ID = bg.id
         INNER JOIN BEHANDLING b ON bg.id = b.BEHANDLING_GRUNNLAG_ID
         INNER JOIN GR_FAMILIE_HENDELSE gr ON gr.behandling_id = b.id) si
ON (s.id = si.soeknad_id)
WHEN MATCHED THEN UPDATE SET s.familie_hendelse_id = si.soeknad_familie_hendelse_id;

MERGE INTO SOEKNAD s
USING (SELECT
         s.id AS soeknad_id,
         gr.SOEKNAD_FAMILIE_HENDELSE_ID
       FROM SOEKNAD s
         INNER JOIN BEHANDLING_GRUNNLAG bg ON s.BEHANDLING_GRUNNLAG_ID = bg.id
         INNER JOIN BEHANDLING b ON b.id = bg.original_behandling_id
         INNER JOIN GR_FAMILIE_HENDELSE gr ON gr.behandling_id = b.id
       WHERE s.familie_hendelse_id IS NULL) si
ON (s.id = si.soeknad_id)
WHEN MATCHED THEN UPDATE SET s.familie_hendelse_id = si.soeknad_familie_hendelse_id;

-- ## barn av søkand
UPDATE UIDENTIFISERT_BARN sb
SET familie_hendelse_id = (SELECT DISTINCT familie_hendelse_id
                           FROM SOEKNAD s
                           WHERE s.id = sb.soeknad_id);

CREATE UNIQUE INDEX UIDX_UIDENTIFISERT_BARN_1
  ON UIDENTIFISERT_BARN (FAMILIE_HENDELSE_ID, BARN_NUMMER);

-- ### Søknad Adopsjon barn
INSERT INTO UIDENTIFISERT_BARN (id, soeknad_id, barn_nummer, familie_hendelse_id, foedsel_dato)
  SELECT
    SEQ_UIDENTIFISERT_BARN.nextval AS id,
    soeknad_id,
    barn_nummer                    AS barn_nummer,
    familie_hendelse_id            AS familie_hendelse_id,
    foedsel_dato                   AS foedsel_dato
  FROM (SELECT DISTINCT
          b.id                           AS behandling_id,
          sb.soeknad_id,
          sb.barn_nummer                 AS barn_nummer,
          sb.foedsel_dato                AS foedsel_dato,
          gr.soeknad_familie_hendelse_id AS familie_hendelse_id
        FROM BEHANDLING b
          INNER JOIN BEHANDLING_GRUNNLAG bg ON b.behandling_grunnlag_id = bg.id
          INNER JOIN GR_FAMILIE_HENDELSE gr ON gr.behandling_id = b.id
          LEFT OUTER JOIN SOEKNAD s ON s.behandling_grunnlag_id = bg.id
          LEFT OUTER JOIN SOEKNAD_ADOPSJON_BARN sb ON sb.soeknad_id = s.id
        WHERE sb.foedsel_dato IS NOT NULL AND sb.soeknad_id IS NOT NULL
  );

-- Skal ikke kjøres
-- DECLARE
--   l_id                    NUMBER(19);
--   l_antall_barn           NUMBER(3);
--   l_foedsel_dato          VARCHAR2(15);
--   l_bekreftet_hendelse_id NUMBER(19);
--   l_soeknad_id            NUMBER(19);
--   sql_stmt                VARCHAR2(4000);
--   CURSOR c_foedsler IS
--     SELECT
--       f.antall_barn_fra_soeknad,
--       to_char(f.foedselsdato_fra_soeknad, 'dd.mm.yyyy') AS foedselsdato_fra_soeknad,
--       gr.soeknad_familie_hendelse_id,
--       f.id                                              AS soeknad_id
--     FROM BEHANDLING b
--       INNER JOIN BEHANDLING_GRUNNLAG bg ON b.behandling_grunnlag_id = bg.id
--       INNER JOIN GR_FAMILIE_HENDELSE gr ON gr.behandling_id = b.id
--       INNER JOIN SOEKNAD f ON f.behandling_grunnlag_id = bg.id
--     WHERE f.foedselsdato_fra_soeknad IS NOT NULL
--           AND f.ADOP_OMSORGOVER_DATO IS NULL;
-- BEGIN
--   OPEN c_foedsler;
--   LOOP
--     FETCH c_foedsler INTO l_antall_barn, l_foedsel_dato, l_bekreftet_hendelse_id, l_soeknad_id;
--     EXIT WHEN c_foedsler%NOTFOUND;
--     FOR loop_count IN 1..l_antall_barn
--     LOOP
--       sql_stmt :=
--       'INSERT INTO UIDENTIFISERT_BARN (id, foedsel_dato, barn_nummer, familie_hendelse_id, soeknad_id) VALUES (SEQ_UIDENTIFISERT_BARN.nextval '
--       ||
--       ', to_date(''' || l_foedsel_dato || ''',''dd.mm.yyyy''), ' || loop_count || ', ' || l_bekreftet_hendelse_id ||
--       ', ' || l_soeknad_id || ')';
--       DBMS_OUTPUT.put_line(sql_stmt);
--       EXECUTE IMMEDIATE sql_stmt;
--     END LOOP;
--   END LOOP;
--   CLOSE c_foedsler;
-- END;
-- /

-- ## Bekreftet "nivå"
UPDATE FAMILIE_HENDELSE
SET TMP_behandling_grunnlag_id = NULL;

INSERT INTO FAMILIE_HENDELSE (id, TMP_behandling_grunnlag_id, familie_hendelse_type)
  SELECT
    SEQ_FAMILIE_HENDELSE.NEXTVAL AS id,
    grunnlag_id,
    '-'                          AS type
  FROM
    (SELECT DISTINCT
       b.id  AS behandling_id,
       bg.id AS grunnlag_id
     FROM BEHANDLING b
       INNER JOIN BEHANDLING_GRUNNLAG bg ON b.behandling_grunnlag_id = bg.id
       INNER JOIN Foedsel f ON f.behandling_grunnlag_id = bg.id
    WHERE f.kilde_saksbehandler = 'N'
    );
UPDATE GR_FAMILIE_HENDELSE gr
SET bekreftet_familie_hendelse_id = (SELECT f.id
                                     FROM FAMILIE_HENDELSE f
                                     WHERE TMP_behandling_grunnlag_id IS NOT NULL AND
                                           TMP_behandling_grunnlag_id IN (SELECT bg.id
                                                                          FROM Behandling_grunnlag bg
                                                                            INNER JOIN BEHANDLING b
                                                                              ON b.behandling_grunnlag_id = bg.id
                                                                          WHERE b.id = gr.behandling_id));

DECLARE
  l_soeknad_id            NUMBER(19);
  l_antall_barn           NUMBER(3);
  l_foedsel_dato          VARCHAR2(15);
  l_bekreftet_hendelse_id NUMBER(19);
  sql_stmt                VARCHAR2(4000);
  CURSOR c_foedsler IS
    SELECT
           s.id,
           f.antall_barn,
           to_char(f.foedsel_dato, 'dd.mm.yyyy') AS foedsel_dato,
           gr.bekreftet_familie_hendelse_id
    FROM BEHANDLING b
           INNER JOIN BEHANDLING_GRUNNLAG bg ON b.behandling_grunnlag_id = bg.id
           INNER JOIN GR_FAMILIE_HENDELSE gr ON gr.behandling_id = b.id
           INNER JOIN Foedsel f ON f.behandling_grunnlag_id = bg.id
           INNER JOIN SOEKNAD s ON s.behandling_grunnlag_id = f.behandling_grunnlag_id
    WHERE NOT EXISTS(SELECT 1
                     FROM UIDENTIFISERT_BARN
                     WHERE UIDENTIFISERT_BARN.FAMILIE_HENDELSE_ID = gr.bekreftet_familie_hendelse_id)
      AND f.kilde_saksbehandler = 'N';
BEGIN
  OPEN c_foedsler;
  LOOP
    FETCH c_foedsler INTO l_soeknad_id, l_antall_barn, l_foedsel_dato, l_bekreftet_hendelse_id;
    EXIT WHEN c_foedsler%NOTFOUND;
    FOR loop_count IN 1..l_antall_barn
    LOOP
      sql_stmt :=
      'INSERT INTO UIDENTIFISERT_BARN (id, soeknad_id, foedsel_dato, barn_nummer, familie_hendelse_id) VALUES (SEQ_UIDENTIFISERT_BARN.nextval, '
      ||
      l_soeknad_id || ', to_date(''' || l_foedsel_dato || ''',''dd.mm.yyyy''),' || loop_count || ', ' ||
      l_bekreftet_hendelse_id || ')';
      DBMS_OUTPUT.put_line(sql_stmt);
      EXECUTE IMMEDIATE sql_stmt;
    END LOOP;
  END LOOP;
  CLOSE c_foedsler;
END;
/

MERGE INTO FAMILIE_HENDELSE fh
USING (SELECT
              fh.id as FAMILIE_HENDELSE_ID,
              'FODSL'       AS type,
              f.antall_barn AS ab
       FROM FOEDSEL f INNER JOIN FAMILIE_HENDELSE fh ON fh.TMP_behandling_grunnlag_id = f.behandling_grunnlag_id
       WHERE f.antall_barn = 0) fo
ON (fh.id = fo.FAMILIE_HENDELSE_ID)
WHEN MATCHED THEN UPDATE SET fh.familie_hendelse_type = fo.type, fh.antall_barn = fo.ab;

-- ## Overstyrt "nivå"
UPDATE FAMILIE_HENDELSE
SET TMP_behandling_grunnlag_id = NULL;

INSERT INTO FAMILIE_HENDELSE (id, TMP_behandling_grunnlag_id, familie_hendelse_type)
SELECT
       SEQ_FAMILIE_HENDELSE.NEXTVAL AS id,
       grunnlag_id,
       '-'                          AS type
FROM
     (SELECT DISTINCT
                      b.id  AS behandling_id,
                      bg.id AS grunnlag_id
      FROM BEHANDLING b
             INNER JOIN BEHANDLING_GRUNNLAG bg ON b.behandling_grunnlag_id = bg.id
     );
UPDATE GR_FAMILIE_HENDELSE gr
SET overstyrt_familie_hendelse_id = (SELECT f.id
                                     FROM FAMILIE_HENDELSE f
                                     WHERE TMP_behandling_grunnlag_id IS NOT NULL AND
                                           TMP_behandling_grunnlag_id IN (SELECT bg.id
                                                                          FROM Behandling_grunnlag bg
                                                                                 INNER JOIN BEHANDLING b
                                                                                   ON b.behandling_grunnlag_id = bg.id
                                                                          WHERE b.id = gr.behandling_id));



UPDATE ADOPSJON a
SET omsorg_vilkaar_type = '-',
  familie_hendelse_id   = (SELECT gr.overstyrt_familie_hendelse_id
                           FROM GR_FAMILIE_HENDELSE gr
                             INNER JOIN BEHANDLING b ON gr.behandling_id = b.id
                             INNER JOIN BEHANDLING_GRUNNLAG bg ON b.behandling_grunnlag_id = bg.id
                           WHERE bg.id = a.behandling_grunnlag_id
                                 AND gr.aktiv = 'J')
WHERE familie_hendelse_id IS NULL;

INSERT INTO ADOPSJON (id, familie_hendelse_id, behandling_grunnlag_id, OMSORGSOVERTAKELSE_DATO, adopterer_alene, ektefelles_barn, omsorg_vilkaar_type)
  SELECT
    SEQ_ADOPSJON.NEXTVAL   AS id,
    familie_hendelse_id    AS familie_hendelse_id,
    behandling_grunnlag_id AS behandling_grunnlag_id,
    omsorgovertakelse_dato AS OMSORGSOVERTAKELSE_DATO,
    'N'                    AS ektefelles_barn,
    'N'                    AS adopterer_alene,
    omsorgsvilkår          AS omsorg_vilkaar_type
  FROM
    (
      SELECT DISTINCT
        b.id                             AS behandling_id,
        bg.id                            AS behandling_grunnlag_id,
        gr.overstyrt_familie_hendelse_id AS familie_hendelse_id,
        s.omsorgsovertakelse_dato        AS omsorgovertakelse_dato,
        s.vilkar_type                    AS omsorgsvilkår
      FROM BEHANDLING b
        INNER JOIN BEHANDLING_GRUNNLAG bg ON b.behandling_grunnlag_id = bg.id
        INNER JOIN GR_FAMILIE_HENDELSE gr ON gr.behandling_id = b.id
        LEFT OUTER JOIN OMSORGSOVERTAKELSE s ON s.behandling_grunnlag_id = bg.id
      WHERE s.vilkar_type IS NOT NULL AND s.omsorgsovertakelse_dato IS NOT NULL
    );
INSERT INTO UIDENTIFISERT_BARN (id, soeknad_id, barn_nummer, familie_hendelse_id, foedsel_dato)
  SELECT
    SEQ_UIDENTIFISERT_BARN.nextval AS id,
    soeknad_id                     AS soeknad_id,
    barn_nummer                    AS barn_nummer,
    familie_hendelse_id            AS familie_hendelse_id,
    foedsel_dato                   AS foedsel_dato
  FROM (SELECT
          s.id                  AS soeknad_id,
          ab.barn_nummer        AS barn_nummer,
          ab.foedsel_dato       AS foedsel_dato,
          a.familie_hendelse_id AS familie_hendelse_id
        FROM ADOPSJON_BARN ab
          INNER JOIN ADOPSJON a ON a.id = ab.adopsjon_id
          INNER JOIN SOEKNAD s ON s.behandling_grunnlag_id = a.BEHANDLING_GRUNNLAG_ID
  );

UPDATE TERMINBEKREFTELSE t
SET familie_hendelse_id = (SELECT gr.overstyrt_familie_hendelse_id
                           FROM GR_FAMILIE_HENDELSE gr
                             INNER JOIN BEHANDLING b ON gr.behandling_id = b.id
                             INNER JOIN BEHANDLING_GRUNNLAG bg ON b.behandling_grunnlag_id = bg.id
                           WHERE bg.id = t.behandling_grunnlag_id
                                 AND gr.aktiv = 'J')
WHERE t.familie_hendelse_id IS NULL;

DECLARE
  l_soeknad_id            NUMBER(19);
  l_antall_barn           NUMBER(3);
  l_foedsel_dato          VARCHAR2(15);
  l_bekreftet_hendelse_id NUMBER(19);
  sql_stmt                VARCHAR2(4000);
  CURSOR c_foedsler IS
    SELECT
      s.id,
      f.antall_barn,
      to_char(f.foedsel_dato, 'dd.mm.yyyy') AS foedsel_dato,
      gr.overstyrt_familie_hendelse_id
    FROM BEHANDLING b
      INNER JOIN BEHANDLING_GRUNNLAG bg ON b.behandling_grunnlag_id = bg.id
      INNER JOIN GR_FAMILIE_HENDELSE gr ON gr.behandling_id = b.id
      INNER JOIN Foedsel f ON f.behandling_grunnlag_id = bg.id
      INNER JOIN SOEKNAD s ON s.behandling_grunnlag_id = f.behandling_grunnlag_id
    WHERE NOT EXISTS(SELECT 1
                     FROM UIDENTIFISERT_BARN
                     WHERE UIDENTIFISERT_BARN.FAMILIE_HENDELSE_ID = gr.overstyrt_familie_hendelse_id);
BEGIN
  OPEN c_foedsler;
  LOOP
    FETCH c_foedsler INTO l_soeknad_id, l_antall_barn, l_foedsel_dato, l_bekreftet_hendelse_id;
    EXIT WHEN c_foedsler%NOTFOUND;
    FOR loop_count IN 1..l_antall_barn
    LOOP
      sql_stmt :=
      'INSERT INTO UIDENTIFISERT_BARN (id, soeknad_id, foedsel_dato, barn_nummer, familie_hendelse_id) VALUES (SEQ_UIDENTIFISERT_BARN.nextval, '
      ||
      l_soeknad_id || ', to_date(''' || l_foedsel_dato || ''',''dd.mm.yyyy''),' || loop_count || ', ' ||
      l_bekreftet_hendelse_id || ')';
      DBMS_OUTPUT.put_line(sql_stmt);
      EXECUTE IMMEDIATE sql_stmt;
    END LOOP;
  END LOOP;
  CLOSE c_foedsler;
END;
/

MERGE INTO FAMILIE_HENDELSE fh
USING (SELECT
         fh.id as FAMILIE_HENDELSE_ID,
         'FODSL'       AS type,
         f.antall_barn AS ab
       FROM FOEDSEL f INNER JOIN FAMILIE_HENDELSE fh ON fh.TMP_behandling_grunnlag_id = f.behandling_grunnlag_id
       WHERE f.antall_barn = 0) fo
ON (fh.id = fo.FAMILIE_HENDELSE_ID)
WHEN MATCHED THEN UPDATE SET fh.familie_hendelse_type = fo.type, fh.antall_barn = fo.ab;

MERGE INTO FAMILIE_HENDELSE fh
USING (SELECT
         a.FAMILIE_HENDELSE_ID,
         CASE WHEN a.OMSORG_VILKAAR_TYPE = '-'
           THEN 'ADPSJN'
         ELSE 'OMSRGO' END AS type
       FROM ADOPSJON a) si
ON (fh.id = si.FAMILIE_HENDELSE_ID)
WHEN MATCHED THEN UPDATE SET fh.familie_hendelse_type = si.type;

MERGE INTO FAMILIE_HENDELSE fh
USING (SELECT DISTINCT
         a.FAMILIE_HENDELSE_ID,
         'FODSL' AS type
       FROM UIDENTIFISERT_BARN a INNER JOIN FAMILIE_HENDELSE FH2 ON a.FAMILIE_HENDELSE_ID = FH2.ID
       WHERE fh2.familie_hendelse_type = '-') si
ON (fh.id = si.FAMILIE_HENDELSE_ID)
WHEN MATCHED THEN UPDATE SET fh.familie_hendelse_type = si.type;

MERGE INTO FAMILIE_HENDELSE fh
USING (SELECT
         a.FAMILIE_HENDELSE_ID,
         'TERM' AS type
       FROM TERMINBEKREFTELSE a INNER JOIN FAMILIE_HENDELSE FH2 ON a.FAMILIE_HENDELSE_ID = FH2.ID
       WHERE fh2.familie_hendelse_type = '-') si
ON (fh.id = si.FAMILIE_HENDELSE_ID)
WHEN MATCHED THEN UPDATE SET fh.familie_hendelse_type = si.type;

MERGE INTO FAMILIE_HENDELSE fh
USING (SELECT
              GFH.bekreftet_familie_hendelse_id AS FAMILIE_HENDELSE_ID,
              'FODSL' AS type
       FROM FAMILIE_HENDELSE FH2 INNER JOIN GR_FAMILIE_HENDELSE GFH on FH2.id = GFH.bekreftet_familie_hendelse_id
       WHERE fh2.familie_hendelse_type = '-' AND fh2.antall_barn = 0) si
ON (fh.id = si.FAMILIE_HENDELSE_ID)
WHEN MATCHED THEN UPDATE SET fh.familie_hendelse_type = si.type;


UPDATE GR_FAMILIE_HENDELSE gr
SET overstyrt_familie_hendelse_id = NULL
WHERE EXISTS(SELECT fh2.id
             FROM FAMILIE_HENDELSE FH2
             WHERE fh2.familie_hendelse_type = '-' AND fh2.id = gr.overstyrt_familie_hendelse_id);

UPDATE GR_FAMILIE_HENDELSE gr
SET AKTIV = 'N'
WHERE EXISTS(SELECT fh2.id
             FROM FAMILIE_HENDELSE FH2
             WHERE fh2.familie_hendelse_type = '-' AND fh2.id = gr.SOEKNAD_FAMILIE_HENDELSE_ID);

-- Har blitt opprettet under migreringen men har ikke data. Så skal slettes
DELETE FAMILIE_HENDELSE fh
WHERE FAMILIE_HENDELSE_TYPE = '-' AND NOT EXISTS(SELECT gr.id
                                                 FROM GR_FAMILIE_HENDELSE gr
                                                 WHERE gr.soeknad_familie_hendelse_id = fh.id OR
                                                       gr.overstyrt_familie_hendelse_id = fh.id);
UPDATE FAMILIE_HENDELSE fh
SET ANTALL_BARN = (SELECT t.ANTALL_BARN_FRA_SOEKNAD FROM SOEKNAD t WHERE t.familie_hendelse_id = fh.id)
WHERE fh.ANTALL_BARN IS NULL AND exists(SELECT gr.id
                                        FROM GR_FAMILIE_HENDELSE gr
                                        WHERE gr.soeknad_familie_hendelse_id = fh.id);

UPDATE FAMILIE_HENDELSE fh
SET antall_barn = (SELECT t.antall_barn FROM TERMINBEKREFTELSE t WHERE t.familie_hendelse_id = fh.id)
WHERE fh.ANTALL_BARN IS NULL AND (SELECT t.antall_barn FROM TERMINBEKREFTELSE t WHERE t.familie_hendelse_id = fh.id) IS NOT NULL;

UPDATE FAMILIE_HENDELSE fh
SET antall_barn = (SELECT count(1) FROM UIDENTIFISERT_BARN u WHERE u.familie_hendelse_id = fh.id)
WHERE (fh.ANTALL_BARN IS NULL OR fh.antall_barn = 0)
  AND (SELECT count(1) FROM UIDENTIFISERT_BARN u WHERE u.familie_hendelse_id = fh.id) > 0;


MERGE INTO FAMILIE_HENDELSE fh
USING (SELECT
              fh.id as FAMILIE_HENDELSE_ID,
              f.antall_barn_til_beregning AS ab
       FROM OMSORGSOVERTAKELSE f INNER JOIN FAMILIE_HENDELSE fh ON fh.TMP_behandling_grunnlag_id = f.behandling_grunnlag_id
    WHERE fh.antall_barn IS NULL) fo
ON (fh.id = fo.FAMILIE_HENDELSE_ID)
WHEN MATCHED THEN UPDATE SET fh.antall_barn = fo.ab;

-- Drop gamle tabeller, kollonner, constraints, indexer osv.....
ALTER TABLE FAMILIE_HENDELSE
  DROP COLUMN TMP_behandling_grunnlag_id;
ALTER TABLE ADOPSJON
  DROP CONSTRAINT FK_ADOPSJON_1;
ALTER TABLE ADOPSJON
  ADD CONSTRAINT FK_ADOPSJON_1 FOREIGN KEY (familie_hendelse_id) REFERENCES FAMILIE_HENDELSE;
ALTER TABLE TERMINBEKREFTELSE
  DROP CONSTRAINT FK_TERMINBEKREFTELSE_1;
ALTER TABLE TERMINBEKREFTELSE
  ADD CONSTRAINT FK_TERMINBEKREFTELSE_1 FOREIGN KEY (familie_hendelse_id) REFERENCES FAMILIE_HENDELSE;
ALTER TABLE UIDENTIFISERT_BARN
  ADD CONSTRAINT FK_UIDENTIFISERT_BARN_1 FOREIGN KEY (familie_hendelse_id) REFERENCES FAMILIE_HENDELSE;
ALTER TABLE SOEKNAD
  MODIFY familie_hendelse_id NUMBER(19) NOT NULL;
ALTER TABLE SOEKNAD
  ADD CONSTRAINT FK_SOEKNAD_FAMILIE_HENDELSE FOREIGN KEY (familie_hendelse_id) REFERENCES FAMILIE_HENDELSE;
