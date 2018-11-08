-- b) fyll de som har SOEKER satt til N (sjekk om dette funker)
DECLARE
  CURSOR AKT IS
    SELECT
      SEQ_AKTOER_YTELSE.NEXTVAL AS RY_ID,
      akt_id,
      iay_id
    FROM (
      SELECT
        b.AKTOER_ID                  AS akt_id,
        gr.INNTEKT_ARBEID_YTELSER_ID AS iay_id
      FROM BEHANDLING b
        INNER JOIN GR_ARBEID_INNTEKT gr ON b.id = gr.BEHANDLING_ID
        INNER JOIN FAGSAK f ON f.id = b.FAGSAK_ID
        INNER JOIN Bruker b ON b.id = f.BRUKER_ID
      WHERE b.id IN
            (SELECT DISTINCT B.ORIGINAL_BEHANDLING_ID AS B_ID
             FROM BEHANDLING_REL_YTELSER BRY
               INNER JOIN BEHANDLING_GRUNNLAG B
                 ON B.ID = BRY.BEHANDLING_GRUNNLAG_ID
             WHERE BRY.SOEKER = 'N'
            )
    );
BEGIN
  FOR LOOP_AKT IN AKT LOOP
    INSERT INTO AKTOER_YTELSE (ID, INNTEKT_ARBEID_YTELSER_ID, AKTOER_ID)
    VALUES (LOOP_AKT.RY_ID, LOOP_AKT.iay_id, LOOP_AKT.akt_id);
  END LOOP;
END;
