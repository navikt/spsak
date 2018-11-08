ALTER TABLE IAY_INNTEKTSMELDING
ADD foerste_fravaersdag DATE;

COMMENT ON COLUMN IAY_INNTEKTSMELDING.FOERSTE_FRAVAERSDAG IS 'Dato for når arbeidstager er borte fra arbeidsplassen';
