-- PK-43157

create table KONFIG_VERDI_GRUPPE(
kode varchar2(50 char)  not null,
navn varchar2(50 char) not null,
beskrivelse varchar2(255 char),
opprettet_av VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
endret_av VARCHAR2(20 CHAR),
endret_tid TIMESTAMP(3),
CONSTRAINT PK_KONFIG_VERDI_GRUPPE PRIMARY KEY (kode)
);

COMMENT ON TABLE KONFIG_VERDI_GRUPPE IS 'Angir en gruppe konfigurerbare verdier tilhører. Det åpner for å kunne ha lister og Maps av konfigurerbare verdier';
COMMENT ON COLUMN KONFIG_VERDI_GRUPPE.kode IS 'Primary Key for gruppen';
COMMENT ON COLUMN KONFIG_VERDI_GRUPPE.navn IS 'Angir et visningsnavn for gruppen';
COMMENT ON COLUMN KONFIG_VERDI_GRUPPE.beskrivelse IS 'Beskrivelse av formålet med gruppen';

INSERT INTO KONFIG_VERDI_GRUPPE(kode, navn , beskrivelse) values ('INGEN', '-', 'Ingen gruppe definert (default).  Brukes istdf. NULL siden dette inngår i en Primary Key. Koder som ikke er del av en gruppe må alltid være unike.');

create table KONFIG_VERDI_TYPE(
kode varchar2(50 char)  not null,
navn varchar2(50 char) not null,
beskrivelse varchar2(255 char),
opprettet_av VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
endret_av VARCHAR2(20 CHAR),
endret_tid TIMESTAMP(3),
CONSTRAINT PK_KONFIG_VERDI_TYPE PRIMARY KEY (kode)
);


COMMENT ON TABLE KONFIG_VERDI_TYPE IS 'Angir type den konfigurerbare verdien er av slik at dette kan brukes til validering og fremstilling.';
COMMENT ON COLUMN KONFIG_VERDI_TYPE.kode IS 'Primary Key';
COMMENT ON COLUMN KONFIG_VERDI_TYPE.navn IS 'Angir et visningsnavn for typen';
COMMENT ON COLUMN KONFIG_VERDI_TYPE.beskrivelse IS 'Beskrivelse av bruk av typen';

insert into KONFIG_VERDI_TYPE (kode, navn, beskrivelse) values ('BOOLEAN', 'Boolske verdier', 'Støtter J(a) / N(ei) flagg');
insert into KONFIG_VERDI_TYPE (kode, navn, beskrivelse) values ('PERIOD', 'Periode verdier', 'ISO 8601 Periode verdier.  Eks. P10M (10 måneder), P1D (1 dag) ');
insert into KONFIG_VERDI_TYPE (kode, navn, beskrivelse) values ('DURATION', 'Periode verdier', 'ISO 8601 Duration (tid) verdier.  Eks. PT1H (1 time), PT1M (1 minutt) ');
insert into KONFIG_VERDI_TYPE (kode, navn, beskrivelse) values ('INTEGER', 'Heltall', 'Heltallsverdier (positiv/negativ)');
insert into KONFIG_VERDI_TYPE (kode, navn, beskrivelse) values ('STRING', 'Streng verdier', '');
insert into KONFIG_VERDI_TYPE (kode, navn, beskrivelse) values ('URI', 'Uniform Resource Identifier', 'URI for å angi id til en ressurs');

create table KONFIG_VERDI_KODE(
kode varchar2(50 char)  not null,
konfig_gruppe varchar2(50 char) default 'INGEN' not null,
navn varchar2(50 char) not null,
konfig_type varchar2(50 char) not null,
beskrivelse varchar2(255 char),
opprettet_av VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
endret_av VARCHAR2(20 CHAR),
endret_tid TIMESTAMP(3),
CONSTRAINT PK_KONFIG_VERDI_KODE PRIMARY KEY (kode, konfig_gruppe)
);

alter table KONFIG_VERDI_KODE add constraint FK_KONFIG_VERDI_KODE_1 foreign key (konfig_type) references KONFIG_VERDI_TYPE;
alter table KONFIG_VERDI_KODE add constraint FK_KONFIG_VERDI_KODE_2 foreign key (konfig_gruppe) references KONFIG_VERDI_GRUPPE;

COMMENT ON TABLE KONFIG_VERDI_KODE IS 'Angir unik kode for en konfigurerbar verdi for validering og utlisting av tilgjengelige koder.';
COMMENT ON COLUMN KONFIG_VERDI_KODE.kode IS 'Primary Key';
COMMENT ON COLUMN KONFIG_VERDI_KODE.navn IS 'Angir et visningsnavn';
COMMENT ON COLUMN KONFIG_VERDI_KODE.beskrivelse IS 'Beskrivelse av formålet den konfigurerbare verdien';
COMMENT ON COLUMN KONFIG_VERDI_KODE.konfig_type IS 'Type angivelse for koden';
COMMENT ON COLUMN KONFIG_VERDI_KODE.konfig_gruppe IS 'Angir gruppe en konfigurerbar verdi kode tilhører (hvis noen - kan også spesifiseres som INGEN).';

create table KONFIG_VERDI (   
id number(19, 0) not null,
konfig_kode varchar2(50 char)  not null,
konfig_gruppe varchar2(50 char)  not null,
konfig_verdi varchar2(255 char),
gyldig_fom date default sysdate not null,
gyldig_tom date default to_date('31.12.9999','dd.mm.yyyy') not null,
opprettet_av VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
endret_av VARCHAR2(20 CHAR),
endret_tid TIMESTAMP(3),
CONSTRAINT PK_KONFIG_VERDI PRIMARY KEY (id)
);

COMMENT ON TABLE KONFIG_VERDI IS 'Angir konfigurerbare verdier med kode, eventuelt tilhørende gruppe.';
COMMENT ON COLUMN KONFIG_VERDI.id IS 'Primary Key';
COMMENT ON COLUMN KONFIG_VERDI.konfig_kode IS 'Angir kode som identifiserer en konfigurerbar verdi. ';
COMMENT ON COLUMN KONFIG_VERDI.konfig_verdi IS 'Angir verdi';
COMMENT ON COLUMN KONFIG_VERDI.gyldig_fom IS 'Gydlig fra-og-med dato';
COMMENT ON COLUMN KONFIG_VERDI.gyldig_tom IS 'Gydlig til-og-med dato';

create index IDX_KONFIG_VERDI_1 ON KONFIG_VERDI (gyldig_fom, gyldig_tom);
create index IDX_KONFIG_VERDI_3 ON KONFIG_VERDI (konfig_kode);
create index IDX_KONFIG_VERDI_2 ON KONFIG_VERDI (konfig_gruppe);

-- har ikke range constraints i Oracle, men følgende fanger der det oprettes verdier med samme kode, gruppe og åpen slutt (31.12.9999)
create unique index UIDX_KONFIG_VERDI_1 ON KONFIG_VERDI (konfig_gruppe, gyldig_tom, konfig_kode); 

alter table KONFIG_VERDI add constraint FK_KONFIG_VERDI_1 foreign key (konfig_kode, konfig_gruppe) references KONFIG_VERDI_KODE;

CREATE SEQUENCE SEQ_KONFIG_VERDI MINVALUE 1 START WITH 100000 INCREMENT BY 50 NOCACHE NOCYCLE;
