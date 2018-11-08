INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'RELATERT_YTELSE_TILSTAND', 'IKKESTARTET', 'Ikke startet', 'Sak eller vedtak er ikke startet', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'RELATERT_YTELSE_STATUS', 'I', 'I', 'Ikke startet', 'Ikke startet', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

ALTER TABLE YTELSE ADD (temaUnderkategori VARCHAR2(20));
ALTER TABLE YTELSE ADD (kl_temaUnderkategori VARCHAR2(100) AS ('TEMA_UNDERKATEGORI'));
ALTER TABLE YTELSE ADD CONSTRAINT FK_YTELSE_5 foreign key (temaUnderkategori, kl_temaUnderkategori) references KODELISTE(kode, kodeverk);

