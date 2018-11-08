--PK-43460 nye tabeller
create table KODEVERK(
kode VARCHAR2(100 CHAR)  NOT NULL,
kodeverk_eier varchar2(100 char) default 'VL' not null,
kodeverk_eier_ref varchar2(1000 char),
kodeverk_eier_ver varchar2(20 char),
kodeverk_eier_navn VARCHAR2(100 CHAR),
kodeverk_synk_nye CHAR(1) DEFAULT 'J'  NOT NULL CHECK (kodeverk_synk_nye IN ('J', 'N')),
kodeverk_synk_eksisterende char(1) DEFAULT 'J'  NOT NULL check (kodeverk_synk_eksisterende IN ('J', 'N')),
navn varchar2(256 char) not null,
beskrivelse varchar2(4000 char),
opprettet_av VARCHAR2(200 CHAR) DEFAULT 'VL' NOT NULL,
opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
endret_av VARCHAR2(200 CHAR),
endret_tid TIMESTAMP(3),
CONSTRAINT PK_KODEVERK PRIMARY KEY (kode)
);

COMMENT ON TABLE KODEVERK IS 'Registrerte kodeverk. Representerer grupperinger av koder';
comment on column KODEVERK.KODE is 'PK - definerer kodeverk';
comment on column KODEVERK.kodeverk_eier is 'Offisielt kodeverk eier (kode)';
comment on column KODEVERK.kodeverk_eier_ref is 'Offisielt kodeverk referanse (url)';
comment on column KODEVERK.kodeverk_eier_ver is 'Offisielt kodeverk versjon';
comment on column KODEVERK.kodeverk_eier_navn is 'Offisielt kodeverk navn';
comment on column KODEVERK.kodeverk_synk_nye is 'Hvorvidt nye koder fra kodeverkeier skal legges til ved oppdatering';
comment on column KODEVERK.kodeverk_synk_nye is 'Hvorvidt eksisterende koder fra kodeverkeier skal oppdateres med eks. nytt offisielt navn, periode ved oppdatering.';
comment on column KODEVERK.NAVN is 'Navn på kodeverk';
comment on column KODEVERK.BESKRIVELSE is 'Beskrivelse av kodeverk';


create table KODELISTE(   
id number(19, 0) not null,
kodeverk varchar2(100 char) not null,
kode varchar2(100 char) not null,
offisiell_kode varchar2(1000 char) null,
navn varchar2(256 char) ,
beskrivelse varchar2(4000 char),
sprak varchar(3 char) default 'NB',
gyldig_fom date default sysdate not null,
gyldig_tom date default to_date('31.12.9999','dd.mm.yyyy') not null,
opprettet_av VARCHAR2(200 CHAR) DEFAULT 'VL' NOT NULL,
opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
endret_av VARCHAR2(200 CHAR),
endret_tid TIMESTAMP(3),
CONSTRAINT PK_KODELISTE PRIMARY KEY (ID)
);

create sequence SEQ_KODELISTE MINVALUE 1 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
alter table KODELISTE add constraint  FK_KODELISTE_01 foreign key (kodeverk) references KODEVERK;

create index idx_KODELISTE_1 on KODELISTE (kode);
create index idx_KODELISTE_2 on KODELISTE (offisiell_kode);
create index idx_KODELISTE_3 on KODELISTE (gyldig_fom);
create unique index uidx_KODELISTE_1 ON KODELISTE (kode, kodeverk);
alter table KODELISTE add constraint CHK_UNIQUE_KODELISTE UNIQUE (kode, kodeverk);

COMMENT ON TABLE KODELISTE IS 'Inneholder lister av koder for alle Kodeverk som benyttes i applikasjonen.  Både offisielle (synkronisert fra sentralt hold i Nav) såvel som interne Kodeverk.  Offisielle koder skiller seg ut ved at nav_offisiell_kode er populert. Følgelig vil gyldig_tom/fom, navn, språk og beskrivelse lastes ned fra Kodeverkklienten eller annen kilde sentralt';

comment on column KODELISTE.kodeverk is '(PK) og FK - kodeverk';
comment on column KODELISTE.kode is '(PK) Unik kode innenfor kodeverk. Denne koden er alltid brukt internt';
comment on column KODELISTE.offisiell_kode is '(Optional) Offisiell kode hos kodeverkeier. Denne kan avvike fra kode der systemet har egne koder. Kan brukes til å veksle inn kode i offisiell kode når det trengs for integrasjon med andre systemer';
comment on column KODELISTE.navn is 'Navn på Kodeverket. Offsielt navn synkes dersom Offsiell kode er satt';
comment on column KODELISTE.beskrivelse is 'Beskrivelse av koden';
comment on column KODELISTE.sprak is 'Språk Kodeverket er definert for, default NB (norsk bokmål). Bruker ISO 639-1 standard men med store bokstaver siden det representert slik i NAVs offisielle Kodeverk';
comment on column KODELISTE.gyldig_fom is 'Dato Kodeverket er gyldig fra og med';
comment on column KODELISTE.gyldig_tom is 'Dato Kodeverket er gyldig til og med';






