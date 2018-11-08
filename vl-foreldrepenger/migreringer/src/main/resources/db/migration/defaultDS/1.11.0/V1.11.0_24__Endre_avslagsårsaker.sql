insert into kodeverk (kode, kodeverk_eier, navn, beskrivelse) values ('AVSLAGSARSAK', 'VL', 'Avslagsårsak', 'Kodetabell som definerer avslagsårsaken på bakgrunn av et vilkår.');

insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1001', 'søkt for tidlig', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1002', 'søker er medmor', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1003', 'søker er far', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1004', 'barn over 15 år', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1005', 'ektefelles/samboers barn', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1006', 'mann adopterer ikke alene', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1007', 'søkt for sent', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1008', 'søker er ikke barnets far', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1009', 'mor ikke død', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1010', 'mor ikke død ved fødsel/omsorg', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1011', 'engangsstønad er allerede utbetalt til mor', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1012', 'far har ikke omsorg for barnet', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1013', 'barn ikke under 15 år', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1014', 'søker har ikke foreldreansvar', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1015', 'søker har hatt vanlig samvær med barnet', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1016', 'søker er ikke barnets far', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1017', 'omsorgsovertakelse etter 56 uker', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1018', 'ikke foreldreansvar alene etter barnelova', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1019', 'manglende dokumentasjon', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1020', 'søker er ikke medlem', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1021', 'søker er utvandret', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1023', 'søker har ikke lovlig opphold', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1024', 'søker har ikke oppholdsrett', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1025', 'søker er ikke bosatt', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1026', 'fødselsdato ikke oppgitt eller registrert', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1032', 'foreldrepenger er allerede utbetalt til mor', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1033', 'engangsstønad er allerede utbetalt til far/medmor ', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1034', 'foreldrepenger er allerede utbetalt til far/medmor', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '-', 'Ikke definert', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));

create table vilkar_type_avslagsarsak_koble (
  vilkar_type_kode          varchar2(100 char) not null,
  avslagsarsak_kode         varchar2(20 char) not null,
  kl_vilkar_type            varchar(20 char)  default 'VILKAR_TYPE',
  kl_avslagsarsak           varchar(20 char)  default 'AVSLAGSARSAK',
  constraint fk_vilkar_type_kode foreign key (kl_vilkar_type, vilkar_type_kode) references kodeliste(KODEVERK, KODE),
  constraint fk_avslagsarsak foreign key (kl_avslagsarsak, avslagsarsak_kode) references kodeliste(KODEVERK, KODE)
);

insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1001', 'FP_VK_1');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1002', 'FP_VK_1');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1003', 'FP_VK_1');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1004', 'FP_VK_4');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1005', 'FP_VK_4');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1006', 'FP_VK_4');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1007', 'FP_VK_3');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1008', 'FP_VK_5');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1009', 'FP_VK_5');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1010', 'FP_VK_5');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1012', 'FP_VK_5');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1013', 'FP_VK_8');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1014', 'FP_VK_8');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1015', 'FP_VK_8');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1016', 'FP_VK_33');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1017', 'FP_VK_33');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1018', 'FP_VK_33');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1019', 'FP_VK_34');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1020', 'FP_VK_2');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1021', 'FP_VK_2');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1023', 'FP_VK_2');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1024', 'FP_VK_2');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1025', 'FP_VK_2');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1026', 'FP_VK_1');

insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1011', 'FP_VK_1');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1011', 'FP_VK_4');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1011', 'FP_VK_5');

insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1032', 'FP_VK_1');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1032', 'FP_VK_4');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1032', 'FP_VK_5');

insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1033', 'FP_VK_4');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1033', 'FP_VK_5');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1033', 'FP_VK_8');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1033', 'FP_VK_33');

insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1034', 'FP_VK_4');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1034', 'FP_VK_5');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1034', 'FP_VK_8');
insert into vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) values ('1034', 'FP_VK_33');

ALTER TABLE BEHANDLING_RESULTAT ADD KL_AVSLAGSARSAK varchar(20 char) default 'AVSLAGSARSAK';
ALTER TABLE VILKAR ADD KL_AVSLAGSARSAK varchar(20 char) default 'AVSLAGSARSAK';

ALTER TABLE BEHANDLING_RESULTAT drop CONSTRAINT FK_BEHANDLING_RESULTAT_6;
ALTER TABLE VILKAR drop CONSTRAINT FK_VILKAR_5;

ALTER TABLE BEHANDLING_RESULTAT Add CONSTRAINT FK_BEHANDLING_RESULTAT_6 FOREIGN KEY (KL_AVSLAGSARSAK, AVSLAG_ARSAK) References kodeliste(KODEVERK, KODE);
ALTER TABLE VILKAR Add CONSTRAINT FK_VILKAR_5 FOREIGN KEY (KL_AVSLAGSARSAK, AVSLAG_KODE) References kodeliste(KODEVERK, KODE);

drop table avslagsarsak;
