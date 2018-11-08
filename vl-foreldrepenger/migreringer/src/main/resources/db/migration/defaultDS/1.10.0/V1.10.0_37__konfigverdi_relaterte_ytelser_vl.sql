insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('relaterte.ytelser.vl.periode.start', 'Relaterte ytelser fra vedtaksløsning periode start','INGEN', 'PERIOD', 'Periode bakover i tid fra dagens dato det skal søkes etter relaterte ytelser i Vedtaksløsning. Default P36M (36 måneder) før dagens dato');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'relaterte.ytelser.vl.periode.start', 'INGEN', 'P36M', to_date('01.01.2016', 'dd.mm.yyyy'));
