insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('etterkontroll.førsøknad.periode', 'Etterkontroll før søknad', 'INGEN', 'PERIOD', 'Periode før søknadsdato hvor det skal etterkontrolleres barn er født');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'etterkontroll.førsøknad.periode', 'INGEN', 'P1W', to_date('01.01.2016', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('etterkontroll.ettertermin.periode', 'Etterkontroll etter termin', 'INGEN', 'PERIOD', 'Periode etter termindato hvor det skal etterkontrolleres barn er født');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'etterkontroll.ettertermin.periode', 'INGEN', 'P4W', to_date('01.01.2016', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('etterkontroll.tpsregistrering.periode', 'TPS registrering før termindato', 'INGEN', 'PERIOD', ' Periode før termin hvor dødfødsel kan være registrert i TPS');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'etterkontroll.tpsregistrering.periode', 'INGEN', 'P11W', to_date('01.01.2016', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('etterkontroll.tid.tilbake', 'tids tilbake for etterkontroll', 'INGEN', 'PERIOD', 'Tid etter innvilgelsesdato før en fagsak vurderes for etterkontroll');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'etterkontroll.tid.tilbake', 'INGEN', 'P60D', to_date('01.01.2016', 'dd.mm.yyyy'));
