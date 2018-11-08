CREATE TABLE BR_KONSEKVENS_YTELSE (
  id                 NUMBER(19, 0)                     NOT NULL,
  konsekvens_ytelse VARCHAR2(100 char) NOT NULL,
  kl_konsekvens_ytelse VARCHAR2(100 char) AS ('KONSEKVENS_FOR_YTELSEN'),
  behandling_resultat_id NUMBER(19, 0)                     NOT NULL,
  versjon            NUMBER(19, 0) DEFAULT 0           NOT NULL,
  opprettet_av       VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid      TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av          VARCHAR2(20 CHAR),
  endret_tid         TIMESTAMP(3),
  CONSTRAINT PK_BR_KONSEKVENS_YTELSE PRIMARY KEY (id),
  CONSTRAINT FK_BR_KONSEKVENS_YTELSE_1 FOREIGN KEY (behandling_resultat_id) REFERENCES BEHANDLING_RESULTAT(id)
);

CREATE SEQUENCE SEQ_BR_KONSEKVENS_YTELSE MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

comment on table BR_KONSEKVENS_YTELSE is 'En revurderings konsekvenser for ytelsen';
comment on column BR_KONSEKVENS_YTELSE.konsekvens_ytelse is 'FK: KONSEKVENS_FOR_YTELSEN';
comment on column BR_KONSEKVENS_YTELSE.kl_konsekvens_ytelse is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column BR_KONSEKVENS_YTELSE.behandling_resultat_id is 'FK: BEHANDLING_RESULTAT';

alter table BR_KONSEKVENS_YTELSE add constraint fk_BR_KONSEKVENS_YTELSE_01 foreign key (konsekvens_ytelse, kl_konsekvens_ytelse) references kodeliste (kode, kodeverk);
create index IDX_BR_KONSEKVENS_YTELSE_1 ON BR_KONSEKVENS_YTELSE(behandling_resultat_id);
create index IDX_BR_KONSEKVENS_YTELSE_2 ON BR_KONSEKVENS_YTELSE(konsekvens_ytelse, kl_konsekvens_ytelse);

-- Move data from BEHANDLING_RESULTAT to BR_KONSEKVENS_YTELSE
INSERT INTO BR_KONSEKVENS_YTELSE (id, behandling_resultat_id, konsekvens_ytelse)
SELECT SEQ_BR_KONSEKVENS_YTELSE.nextval, id, konsekvens_for_ytelsen from BEHANDLING_RESULTAT
where konsekvens_for_ytelsen not in ('-', 'ENDRING_I_BEREGNING_OG_UTTAK');

INSERT INTO BR_KONSEKVENS_YTELSE (id, behandling_resultat_id, konsekvens_ytelse)
SELECT SEQ_BR_KONSEKVENS_YTELSE.nextval, id, 'ENDRING_I_BEREGNING' from BEHANDLING_RESULTAT
where konsekvens_for_ytelsen ='ENDRING_I_BEREGNING_OG_UTTAK';

INSERT INTO BR_KONSEKVENS_YTELSE (id, behandling_resultat_id, konsekvens_ytelse)
SELECT SEQ_BR_KONSEKVENS_YTELSE.nextval, id, 'ENDRING_I_UTTAK' from BEHANDLING_RESULTAT
where konsekvens_for_ytelsen ='ENDRING_I_BEREGNING_OG_UTTAK';

ALTER TABLE BEHANDLING_RESULTAT MODIFY konsekvens_for_ytelsen DEFAULT '-';

-- Contract part (of Expand-contract): Drop column BEHANDLING_RESULTAT.konsekvens_for_ytelsen
--ALTER TABLE BEHANDLING_RESULTAT DROP COLUMN konsekvens_for_ytelsen;