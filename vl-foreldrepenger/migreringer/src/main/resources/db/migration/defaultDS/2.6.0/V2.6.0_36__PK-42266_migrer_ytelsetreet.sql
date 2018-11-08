-- forusetter at alle gr_arbeid_inntekt og inntekt_arbeid_ytelser har blitt opprettet riktig
-- (ble gjort litt tidligere da man endret ytelser første gang)

-- steg 1 fyll Aktør_ytelse med data (krever at det er mulig å finne aktørID!)
-- a) fyll de som har SOEKER satt til J
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
             WHERE BRY.SOEKER = 'J'
            )
    );
BEGIN
  FOR LOOP_AKT IN AKT LOOP
    INSERT INTO AKTOER_YTELSE (ID, INNTEKT_ARBEID_YTELSER_ID, AKTOER_ID)
    VALUES (LOOP_AKT.RY_ID, LOOP_AKT.iay_id, LOOP_AKT.akt_id);
  END LOOP;
END;
/
INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES ('TEMA_UNDERKATEGORI', 'Kodeverk for tema underkategori i ytelse', 'Kodeverk for tema underkategori i ytelse','VL');

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'FØ','Foreldrepenger fødsel','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'AP','Foreldrepenger adopsjon','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'SV','Svangerskapspenger','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'AE','Adopsjon engangsstønad','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'FE','Fødsel engangsstønad','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'FU','Foreldrepenger fødsel, utland','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'RS','Forsikr.risiko sykefravær','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'RT','Reisetilskudd','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'SP','Sykepenger','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'SU','Sykepenger utenlandsopphold','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'BT','Stønad til barnetilsyn','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'FL','Tilskudd til flytting','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'OG','Overgangsstønad','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'UT','Skolepenger','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'OM','Pårørende omsorgsmpenger','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'OP','Pårørende opplæringspenger','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'PB','Pårørende pleietrengende sykt barn','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'PI','Pårørende pleietrengende','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'PP','Pårørende pleietrengende pårørende','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', 'PN','Pårørende pleiepenger','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM) VALUES (SEQ_KODELISTE.NEXTVAL ,'TEMA_UNDERKATEGORI', '-','Udefinert','','NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

