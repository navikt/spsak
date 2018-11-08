-- Tabell RELATERTE_YTELSER_STATUS
CREATE TABLE RELATERTE_YTELSER_STATUS (
    kode varchar2(20 char) not null,
    navn varchar2(50 char) not null,
    beskrivelse varchar2(4000 char),
    opprettet_av    VARCHAR2(20 char) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 char),
    endret_tid      TIMESTAMP(3),
	CONSTRAINT PK_RELATERTE_YTELSER_STATUS PRIMARY KEY (kode)
);

INSERT INTO RELATERTE_YTELSER_STATUS (kode, navn) VALUES ('IKKE_INNHENTET', 'Ikke innhentet');
INSERT INTO RELATERTE_YTELSER_STATUS (kode, navn) VALUES ('UNDER_INNHENTING', 'Under innhenting');
INSERT INTO RELATERTE_YTELSER_STATUS (kode, navn) VALUES ('INNHENTET', 'Innhentet');

-- Ny kolonne i BEHANDLING_GRUNNLAG
ALTER TABLE BEHANDLING_GRUNNLAG ADD ytelser_infotrygd_status VARCHAR2(20 char) DEFAULT 'IKKE_INNHENTET' NOT NULL;
ALTER TABLE BEHANDLING_GRUNNLAG ADD CONSTRAINT FK_RELATERTE_YTELSER_STATUS FOREIGN KEY (ytelser_infotrygd_status) REFERENCES RELATERTE_YTELSER_STATUS;

-- Nye saksopplysningskoder
INSERT INTO SAKSOPPLYSNING_TYPE (kode, navn) VALUES ('RYSOKER', 'Relaterte ytelser for søker');
INSERT INTO SAKSOPPLYSNING_TYPE (kode, navn) VALUES ('RYANNEN', 'Relaterte ytelser for annen forelder');
