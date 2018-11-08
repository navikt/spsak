insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('behandling.venter.frist.lengde', 'Frist - Behandling venter', 'INGEN', 'PERIOD', 'Sett behandling på vent  (i en angitt periode, eks. P2W = 2 uker');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'behandling.venter.frist.lengde', 'INGEN', 'P2W', to_date('01.01.2016', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('virtuell.saksbehandler.navn', 'Virtuell saksbehandler navn', 'INGEN', 'STRING', 'Setter saksbehandler navn når prosessen er gått automatisk. Kun for visning internt i løsning');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'virtuell.saksbehandler.navn', 'INGEN', 'Vedtaksløsning Prosess', to_date('01.01.2016', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('relaterte.ytelser.periode.start', 'Relaterte ytelser periode start','INGEN', 'PERIOD', 'Periode bakover i tid fra dagens dato det skal søkes etter relaterte ytelser i Infotrygd og Arena. Default P10M (10 måneder) før dagens dato');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'relaterte.ytelser.periode.start', 'INGEN', 'P10M', to_date('01.01.2016', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('vedtak.klagefrist.uker', 'Vedtak klagefrist', 'INGEN', 'INTEGER', ' Klagefrist i uker (positivt heltall), sendes i vedtaksbrev til brukeren');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'vedtak.klagefrist.uker', 'INGEN', '6', to_date('01.01.2016', 'dd.mm.yyyy'));
