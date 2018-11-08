-- PK-52839 Hjemmel for beregning

INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('BG_HJEMMEL', 'N', 'N', 'Hjemmel for beregningsgrunnlag', 'Internt kodeverk for hjemmel for beregningsgrunnlag');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'BG_HJEMMEL', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'F_14_7', 'folketrygdloven § 14-7', 'folketrygdloven § 14-7', 'BG_HJEMMEL', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'F_14_7_8_30', 'folketrygdloven §§ 14-7 og 8-30', 'folketrygdloven §§ 14-7 og 8-30', 'BG_HJEMMEL', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'F_14_7_8_35', 'folketrygdloven §§ 14-7 og 8-35', 'folketrygdloven §§ 14-7 og 8-35', 'BG_HJEMMEL', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'F_14_7_8_38', 'folketrygdloven §§ 14-7 og 8-38', 'folketrygdloven §§ 14-7 og 8-38', 'BG_HJEMMEL', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'F_14_7_8_40', 'folketrygdloven §§ 14-7 og 8-40', 'folketrygdloven §§ 14-7 og 8-40', 'BG_HJEMMEL', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'F_14_7_8_41', 'folketrygdloven §§ 14-7 og 8-41', 'folketrygdloven §§ 14-7 og 8-41', 'BG_HJEMMEL', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'F_14_7_8_42', 'folketrygdloven §§ 14-7 og 8-42', 'folketrygdloven §§ 14-7 og 8-42', 'BG_HJEMMEL', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'F_14_7_8_43', 'folketrygdloven §§ 14-7 og 8-43', 'folketrygdloven §§ 14-7 og 8-43', 'BG_HJEMMEL', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'F_14_7_8_47', 'folketrygdloven §§ 14-7 og 8-47', 'folketrygdloven §§ 14-7 og 8-47', 'BG_HJEMMEL', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'F_14_7_8_49', 'folketrygdloven §§ 14-7 og 8-49', 'folketrygdloven §§ 14-7 og 8-49', 'BG_HJEMMEL', to_date('2000-01-01', 'YYYY-MM-DD'));

ALTER TABLE BG_AKTIVITET_STATUS ADD HJEMMEL VARCHAR2(100 CHAR) DEFAULT '-' NOT NULL;
ALTER TABLE BG_AKTIVITET_STATUS ADD KL_HJEMMEL VARCHAR2(100 CHAR) GENERATED ALWAYS AS ('BG_HJEMMEL') VIRTUAL;

ALTER TABLE BG_AKTIVITET_STATUS ADD CONSTRAINT FK_BG_AKTIVITET_STATUS_3 FOREIGN KEY (HJEMMEL, KL_HJEMMEL)
REFERENCES KODELISTE (KODE, KODEVERK);

CREATE INDEX IDX_BG_AKTIVITET_STATUS_03 ON BG_AKTIVITET_STATUS(HJEMMEL);

COMMENT ON COLUMN BG_AKTIVITET_STATUS.HJEMMEL IS 'Hjemmel for beregningsgrunnlag';

