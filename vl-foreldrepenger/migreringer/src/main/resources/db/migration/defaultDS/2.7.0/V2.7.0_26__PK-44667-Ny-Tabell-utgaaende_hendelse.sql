create table UTGAAENDE_HENDELSE (
ID                   number(19,0) not null,
SEKVENSNUMMER        number(19,0) NOT NULL,
OUTPUT_FEED_KODE     varchar2(50 char) not null,
TYPE                 varchar2(100 char) not null,
PAYLOAD              clob not null,
AKTOER_ID            NUMBER(19)         NOT NULL,
OPPRETTET_AV         varchar2(20 char) default 'VL',
OPPRETTET_TID        timestamp(3)      default systimestamp,
ENDRET_AV            varchar2(20 char),
ENDRET_TID           timestamp(3),

CONSTRAINT PK_UTGAAENDE_HENDELSE PRIMARY KEY (ID)
);

comment on table UTGAAENDE_HENDELSE is 'Definerer JSON_FEEDs som leses fra';
comment on column UTGAAENDE_HENDELSE.OUTPUT_FEED_KODE is 'Fremmednøkkel til OUTPUT_FEED';
comment on column UTGAAENDE_HENDELSE.TYPE is 'Hendelsetype';
comment on column UTGAAENDE_HENDELSE.PAYLOAD is 'Innhold i hendelse';
comment on column UTGAAENDE_HENDELSE.SEKVENSNUMMER is 'Sekvensnummer for hendelsen';

CREATE SEQUENCE SEQ_UTGAAENDE_HENDELSE_ID MINVALUE 10000000 START WITH 10000000 INCREMENT BY 1000000 NOCACHE NOCYCLE;

