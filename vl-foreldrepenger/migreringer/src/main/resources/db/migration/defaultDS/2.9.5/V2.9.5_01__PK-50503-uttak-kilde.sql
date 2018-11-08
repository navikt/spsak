INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES('FORDELING_PERIODE_KILDE', 'Kodeverk over mulige kilder til fordeling perioder', 'Kodeverk over mulige kilder til fordeling perioder.', 'VL');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'SØKNAD', 'Kilde er søknad', 'Perioden kommer fra søknaden.', 'FORDELING_PERIODE_KILDE',
        to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'TIDLIGERE_VEDTAK', 'Kilde er tidligere vedtatt behandling.', 'Perioden er hentet fra tidligere vedtatt behandling.', 'FORDELING_PERIODE_KILDE',
        to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'FORDELING_PERIODE_KILDE',
        to_date('2000-01-01', 'YYYY-MM-DD'));

ALTER TABLE YF_FORDELING_PERIODE ADD FORDELING_PERIODE_KILDE VARCHAR2(100 CHAR) DEFAULT '-';

ALTER TABLE YF_FORDELING_PERIODE ADD KL_FORDELING_PERIODE_KILDE  VARCHAR2(100 CHAR) GENERATED ALWAYS AS ('FORDELING_PERIODE_KILDE') VIRTUAL;

ALTER TABLE YF_FORDELING_PERIODE ADD CONSTRAINT FK_YF_FORDELING_PERIODE_3 FOREIGN KEY (KL_FORDELING_PERIODE_KILDE, FORDELING_PERIODE_KILDE) REFERENCES KODELISTE (kodeverk, kode);

CREATE INDEX IDX_YF_FORDELING_PERIODE_7 ON YF_FORDELING_PERIODE (FORDELING_PERIODE_KILDE);

UPDATE YF_FORDELING_PERIODE SET FORDELING_PERIODE_KILDE = 'SØKNAD';

COMMENT ON COLUMN YF_FORDELING_PERIODE.FORDELING_PERIODE_KILDE IS 'Kilden til denne perioden, fra søknad eller tidligere vedtak';

