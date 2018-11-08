ALTER TABLE AKSJONSPUNKT DROP CONSTRAINT CHK_AKTIVT;
alter table AKSJONSPUNKT MODIFY AKTIVT varchar2(100 char) default 'AKTIV';
alter table AKSJONSPUNKT RENAME COLUMN AKTIVT TO  REAKTIVERING_STATUS;
alter table AKSJONSPUNKT ADD KL_REAKTIVERING_STATUS varchar2(100 char) as ('REAKTIVERING_STATUS') NOT NULL;

-- ny kodeverk AKTIVITET_STATUS for beregning, med kodeverdier
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('REAKTIVERING_STATUS', 'N', 'N', 'Aksjonspunktets reaktiveringsstatus', 'Hvilken reaktiveringsstatus som aksjonspunktet har. Default aktiv ved opprettelse, men starter som inaktiv ved revurdering.');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'AKTIV', 'Aktiv', 'Aktiv (opprettes som aktiv)', 'REAKTIVERING_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'INAKTIV', 'Inaktiv', 'Inaktiv (etter kopiering fra original behandling)', 'REAKTIVERING_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'SLETTET', 'Inaktiv og slettet', 'Inaktiv og slettet (etter kopiering fra original behandling)', 'REAKTIVERING_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));

update AKSJONSPUNKT set REAKTIVERING_STATUS = 'AKTIV' where REAKTIVERING_STATUS = 'J';
update AKSJONSPUNKT set REAKTIVERING_STATUS = 'INAKTIV' where REAKTIVERING_STATUS = 'N';

alter table AKSJONSPUNKT ADD constraint FK_AKSJONSPUNKT_6 foreign key (REAKTIVERING_STATUS, KL_REAKTIVERING_STATUS) references KODELISTE (KODE, KODEVERK);

create index IDX_AKSJONSPUNKT_DEF_10 on AKSJONSPUNKT(REAKTIVERING_STATUS);
