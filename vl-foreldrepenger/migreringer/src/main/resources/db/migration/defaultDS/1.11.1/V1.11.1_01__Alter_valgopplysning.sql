-- bruk valgt_opplysning.personstatus
ALTER TABLE PERSONOPPLYSNING DROP CONSTRAINT FK_PERSONOPPLYSNING_85;
ALTER TABLE PERSONOPPLYSNING DROP COLUMN overstyrt_personstatus_type;

-- legg til doedsdato som en opplysning saksbehandler ska ta stilling til
ALTER TABLE VALGT_OPPLYSNING ADD doedsdato DATE NULL;
CREATE INDEX IDX_VALGT_OPPLYSNING_4 ON VALGT_OPPLYSNING(doedsdato);
COMMENT ON COLUMN VALGT_OPPLYSNING.doedsdato IS 'Dødsdato til personen';
