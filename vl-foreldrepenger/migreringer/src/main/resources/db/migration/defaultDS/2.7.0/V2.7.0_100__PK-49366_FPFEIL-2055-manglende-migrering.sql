/*
* Retter opp manglende migrering av relasjoner for annenpart. Resten av migreringen ligger
* i V2.7.0_98__PK-49366_migrering.sql.
*/

DECLARE

  ------------------------------------------------------------
  -- Alle barn til annenpart
  ------------------------------------------------------------
  CURSOR c_relasjonsdata IS
    SELECT
      po2.AKTOER_ID                 AS fra_aktoer_id,
      po3.AKTOER_ID                 AS til_aktoer_id,
      grp.REGISTRERT_INFORMASJON_ID AS po_informasjon_id,
      fr2.RELASJONSROLLE            AS relasjonsrolle,
      fr2.HAR_SAMME_BOSTED          AS har_samme_bosted,
      fr2.OPPRETTET_AV              AS opprettet_av,
      fr2.OPPRETTET_TID             AS opprettet_tid,
      fr2.ENDRET_AV                 AS endret_av,
      fr2.ENDRET_TID                AS endret_tid
    FROM FAMILIERELASJON fr2
      INNER JOIN PERSONOPPLYSNING po2 ON po2.id = fr2.FRAPERSON
      INNER JOIN PERSONOPPLYSNING po3 ON po3.id = fr2.TILPERSON
      INNER JOIN SO_ANNEN_PART sop ON sop.AKTOER_ID = po2.AKTOER_ID
      INNER JOIN GR_PERSONOPPLYSNING grp ON grp.SO_ANNEN_PART_ID = sop.id
    WHERE fr2.FRAPERSON IN (
      SELECT fr.FRAPERSON annenpart_po_id
      FROM FAMILIERELASJON fr
        INNER JOIN PERSONOPPLYSNING po ON po.id = fr.FRAPERSON
      WHERE fr.RELASJONSROLLE = 'HOVS'
            AND po.AKTOER_ID NOT IN (
        SELECT br.AKTOER_ID hovedsoeker
        FROM BEHANDLING b INNER JOIN FAGSAK fs ON fs.id = b.FAGSAK_ID
          INNER JOIN bruker br ON br.id = fs.BRUKER_ID
      )
    ) AND fr2.RELASJONSROLLE <> 'HOVS'
    ORDER BY fr2.FRAPERSON;


  PROCEDURE kjoer_migrering IS
    BEGIN
      FOR rel_data IN c_relasjonsdata
      LOOP
        INSERT INTO PO_RELASJON (
          id, fra_aktoer_id, til_aktoer_id,
          po_informasjon_id, relasjonsrolle, har_samme_bosted,
          opprettet_av, opprettet_tid, endret_av, endret_tid)
        VALUES (
          SEQ_PO_RELASJON.nextval,
          rel_data.fra_aktoer_id,
          rel_data.til_aktoer_id,
          rel_data.po_informasjon_id,
          rel_data.relasjonsrolle,
          rel_data.har_samme_bosted,
          rel_data.opprettet_av,
          rel_data.opprettet_tid,
          rel_data.endret_av,
          rel_data.endret_tid
        );
      END LOOP;
    END;

  --------------------------------------------------------------
  -------------------------- M A I N ---------------------------
  --------------------------------------------------------------
BEGIN
  kjoer_migrering;
END;
/
