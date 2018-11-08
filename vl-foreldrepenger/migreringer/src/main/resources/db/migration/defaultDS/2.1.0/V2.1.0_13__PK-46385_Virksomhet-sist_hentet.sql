ALTER TABLE VIRKSOMHET ADD opplysninger_oppdatert_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL;

ALTER TABLE VIRKSOMHET MODIFY opplysninger_oppdatert_tid DEFAULT NULL;
