-------------------------------------------
-- Fjerner søknaden fra grunnlaget
-------------------------------------------
ALTER TABLE SOEKNAD_ANNEN_PART RENAME TO SO_ANNEN_PART;
ALTER TABLE GR_PERSONOPPLYSNING ADD SO_ANNEN_PART_ID NUMBER(19);
ALTER TABLE GR_PERSONOPPLYSNING ADD CONSTRAINT FK_GR_PERSONOPPL_ANNE_PRT FOREIGN KEY (SO_ANNEN_PART_ID) REFERENCES SO_ANNEN_PART;

DECLARE

CURSOR c_hent_po_grunnlag IS
  select * from GR_PERSONOPPLYSNING;

CURSOR c_hent_annenpart(grp_grunnlag_id NUMBER) IS
  select soanp.id as id from SO_ANNEN_PART soanp
  inner join SOEKNAD s ON s.ANNEN_PART_ID = soanp.id
  inner join BEHANDLING_GRUNNLAG BG ON s.BEHANDLING_GRUNNLAG_ID = BG.ID
  inner join behandling b on B.BEHANDLING_GRUNNLAG_ID = bg.id
  inner join GR_PERSONOPPLYSNING grp on grp.BEHANDLING_ID = b.id
  where grp.id = grp_grunnlag_id;

PROCEDURE p_kopier_annen_part IS
    BEGIN
      FOR po_grp IN c_hent_po_grunnlag
      LOOP
        FOR anpa IN c_hent_annenpart(po_grp.id)
        LOOP
          update GR_PERSONOPPLYSNING set SO_ANNEN_PART_ID = anpa.id
          where id = po_grp.id;
        END LOOP;
      END LOOP;
    END;

--------------------------------------------------------------
-------------------------- M A I N ---------------------------
--------------------------------------------------------------
BEGIN
    p_kopier_annen_part;
END;
/
