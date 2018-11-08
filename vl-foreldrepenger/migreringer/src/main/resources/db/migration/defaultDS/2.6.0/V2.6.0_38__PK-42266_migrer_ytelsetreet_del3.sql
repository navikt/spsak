CREATE OR REPLACE FUNCTION FinnDatoTom(dato IN DATE)
  RETURN DATE
IS
  cDate DATE;
  BEGIN
    cDate := dato;
    IF (cDate IS NULL)
    THEN cDate := to_date('31.12.9999', 'dd.mm.yyyy');
    ELSIF (MOD(TO_CHAR(dato, 'J'), 7) + 1 = 6)
      THEN cDate := dato - 1;
    ELSIF (MOD(TO_CHAR(dato, 'J'), 7) + 1 = 7)
      THEN cDate := dato - 2;
    END IF;
    RETURN cDate;
  END FinnDatoTom;
/
CREATE OR REPLACE FUNCTION FinnDatoFom(regDato IN DATE, iDato IN DATE, vDato IN DATE)
  RETURN DATE
IS
  cDate DATE;
  BEGIN

    IF (iDato IS NOT NULL)
    THEN cDate := iDato;
    ELSIF (vDato IS NOT NULL)
      THEN cDate := vDato;
    ELSIF (regDato IS NOT NULL)
      THEN cDate := regDato;
    END IF;
    RETURN cDate;
  END FinnDatoFom;
/
CREATE OR REPLACE FUNCTION FinnTema(kilde IN VARCHAR2, tema IN VARCHAR2)
  RETURN VARCHAR2
IS
  cTema VARCHAR2(10);
  BEGIN

    IF (kilde = 'ARENA')
    THEN cTema := '-';
    ELSE
      cTema := tema;
    END IF;
    RETURN cTema;
  END FinnTema;
/
-- c) fyll ytelse
DECLARE
  CURSOR C_YTELSE IS
    SELECT
      bry.RELATERT_YTELSE_TYPE                                                    AS ytelsetype,
      FinnDatoFom(bry.REGISTRERING_DATO, bry.IVERKSETTELSE_DATO, bry.VEDTAK_DATO) AS fom,
      FinnDatoTom(bry.OPPHOER_FOM)                                                AS tom,
      bry.RELATERT_YTELSE_TILSTAND                                                AS tilstand,
      bry.SAKSNUMMER                                                              AS saksnummer,
      bry.SAKSOPPLYSNING_KILDE                                                    AS kilde,
      FinnTema(bry.SAKSOPPLYSNING_KILDE, bry.RELATERT_YTELSE_BEHANDL_TEMA)        AS behandlingstema,
      ay.id                                                                       AS ayid,
      SEQ_YTELSE.NEXTVAL                                                          AS YT_ID,
      bry.OPPRETTET_TID                                                           AS o_tid,
      bry.opprettet_av                                                            AS o_av
    FROM BEHANDLING_REL_YTELSER bry
      INNER JOIN BEHANDLING_GRUNNLAG bg ON bry.BEHANDLING_GRUNNLAG_ID = bg.id
      INNER JOIN GR_ARBEID_INNTEKT gai ON gai.BEHANDLING_ID = bg.ORIGINAL_BEHANDLING_ID
      INNER JOIN AKTOER_YTELSE ay ON ay.INNTEKT_ARBEID_YTELSER_ID = gai.INNTEKT_ARBEID_YTELSER_ID;
BEGIN
  FOR LOOP_Y IN C_YTELSE LOOP
    INSERT INTO YTELSE (ID, AKTOER_YTELSE_ID, YTELSE_TYPE, FOM, TOM, STATUS, SAKSNUMMER, KILDE, TEMAUNDERKATEGORI, OPPRETTET_TID, OPPRETTET_AV)
    VALUES (
      LOOP_Y.YT_ID,
      LOOP_Y.ayid,
      LOOP_Y.ytelsetype,
      LOOP_Y.fom,
      LOOP_Y.tom,
      LOOP_Y.tilstand,
      LOOP_Y.saksnummer,
      LOOP_Y.kilde,
      LOOP_Y.behandlingstema,
      LOOP_Y.o_tid,
      LOOP_Y.o_av
    );
  END LOOP;
END;
/
-- trenger ikke denne mer
DROP FUNCTION FinnDatoTom;
DROP FUNCTION FinnDatoFom;
DROP FUNCTION FinnTema;
