alter table IAY_EGEN_NAERING
  add naer_relasjon VARCHAR2(1 CHAR);
UPDATE IAY_EGEN_NAERING
set naer_relasjon = 'N';
COMMENT ON COLUMN IAY_EGEN_NAERING.naer_relasjon
IS 'Om det i søknaden er angitt nær relasjon for egen næring';
ALTER TABLE IAY_EGEN_NAERING
  MODIFY (naer_relasjon NOT NULL);
