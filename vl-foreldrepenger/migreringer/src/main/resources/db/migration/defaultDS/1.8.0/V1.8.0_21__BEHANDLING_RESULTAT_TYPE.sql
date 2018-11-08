-- Tabell BEHANDLING_RESULTAT_TYPE
CREATE TABLE BEHANDLING_RESULTAT_TYPE (
    kode varchar2(30 char) not null,
    navn varchar2(50 char) not null,
    beskrivelse varchar2(4000 char),
    opprettet_av    VARCHAR2(20 char) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 char),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_BEHANDLING_RESULTAT_TYPE PRIMARY KEY (kode)
);

INSERT INTO BEHANDLING_RESULTAT_TYPE (kode, navn, beskrivelse) VALUES ('IKKE_FASTSATT', 'Ikke fastsatt','Under behandling, ikke fastsatt');
INSERT INTO BEHANDLING_RESULTAT_TYPE (kode, navn, beskrivelse) VALUES ('INNVILGET', 'Innvilget','Søknaden er innvilget');
INSERT INTO BEHANDLING_RESULTAT_TYPE (kode, navn, beskrivelse) VALUES ('AVSLÅTT', 'Avslått','Søknaden er avslått');
INSERT INTO BEHANDLING_RESULTAT_TYPE (kode, navn, beskrivelse) VALUES ('HENLAGT_SØKNAD_TRUKKET', 'Henlagt, søknaden er trukket','Søknaden er henlagt, trukket av bruker');
INSERT INTO BEHANDLING_RESULTAT_TYPE (kode, navn, beskrivelse) VALUES ('HENLAGT_FEILOPPRETTET', 'Henlagt, søknaden er feilopprettet','Søknaden er henlagt, feilopprettet');
INSERT INTO BEHANDLING_RESULTAT_TYPE (kode, navn, beskrivelse) VALUES ('HENLAGT_BRUKER_DØD', 'Henlagt, brukeren er død','Søknaden er henlagt, bruker er død');

ALTER TABLE behandling_resultat DROP COLUMN behandling_resultat CASCADE CONSTRAINTS;

ALTER TABLE behandling_resultat ADD behandling_resultat_type VARCHAR2(30 char) DEFAULT 'IKKE_FASTSATT' NOT NULL;

ALTER TABLE behandling_resultat ADD CONSTRAINT FK_BEHANDLING_RESULTAT_4 FOREIGN KEY (behandling_resultat_type) REFERENCES BEHANDLING_RESULTAT_TYPE (kode);
CREATE INDEX IDX_BEHANDLING_RESULTAT_4 ON BEHANDLING_RESULTAT(behandling_resultat_type);
