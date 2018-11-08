-- ubrukt
ALTER TABLE IAY_INNTEKTSMELDING DROP COLUMN foerste_fravaersdag;

ALTER TABLE IAY_INNTEKTSMELDING
ADD innsendingstidspunkt TIMESTAMP;

COMMENT ON COLUMN IAY_INNTEKTSMELDING.innsendingstidspunkt IS 'Innsendingstidspunkt fra LPS-system. For Altinn bruker kjøretidspunkt';

