-- VilkarType for Opptjeningsperiodevilkår
INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom, EKSTRA_DATA) VALUES
  (seq_kodeliste.nextval, 'VILKAR_TYPE', 'FP_VK_21', 'Opptjeningsperiode', 'Fastsett opptjeningsperiode 14-6',
   to_date('2000-01-01', 'YYYY-MM-DD'),'{ "kategori": "inngangsvilkår", "lovreferanse": "§ 14-6 og 14-10" }');

-- Tabell OPPTJENINGSPERIODE
CREATE TABLE OPPTJENINGSPERIODE (
  id                 NUMBER(19, 0)                     NOT NULL,
  vilkar_resultat_id NUMBER(19, 0)                     NOT NULL,
  fom                DATE                              NOT NULL,
  tom                DATE                              NOT NULL,
  versjon            NUMBER(19, 0) DEFAULT 0           NOT NULL,
  opprettet_av       VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid      TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av          VARCHAR2(20 CHAR),
  endret_tid         TIMESTAMP(3),
  CONSTRAINT PK_OPPTJENINGSPERIODE PRIMARY KEY (id),
  CONSTRAINT FK_OPPTJENINGSPERIODE FOREIGN KEY (vilkar_resultat_id) REFERENCES VILKAR_RESULTAT
);
CREATE INDEX IDX_OPPTJENINGSPERIODE_01
  ON OPPTJENINGSPERIODE (vilkar_resultat_id);
CREATE SEQUENCE SEQ_OPPTJENINGSPERIODE MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE OPPTJENINGSPERIODE IS 'Opptjeningsperiode som fastsatt gjennom Inngangsvilkåret';
COMMENT ON COLUMN OPPTJENINGSPERIODE.ID IS 'Primary Key';
COMMENT ON COLUMN OPPTJENINGSPERIODE.vilkar_resultat_id IS 'FK: VILKAR_RESULTAT';
