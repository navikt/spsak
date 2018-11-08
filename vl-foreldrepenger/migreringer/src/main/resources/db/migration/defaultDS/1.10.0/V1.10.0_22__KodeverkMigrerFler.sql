alter table kodeliste add ekstra_data varchar2(4000 char);

comment on column kodeliste.ekstra_data is '(Optional) Tilleggsdata brukt av kodeverket.  Format er kodeverk spesifikt - eks. kan være tekst, json, key-value, etc.';

insert into kodeverk (kode, navn, beskrivelse ) values ('VILKAR_TYPE', 'VilkarType', '');
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk, ekstra_data) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'VILKAR_TYPE', '{ "lovreferanse": "'||lov_referanse||'" }' from VILKAR_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) values (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'), 'VILKAR_TYPE');

begin 
    migrer_KODELISTE_fk('VILKAR_TYPE', 'VILKAR_TYPE');
end;
/

drop table VILKAR_TYPE cascade constraints;
update AKSJONSPUNKT_DEF set vilkar_type='-' where vilkar_type IS NULL;

-----
update kodeverk set kodeverk_eier='Arena' where kode='RELATERT_YTELSE_RESULTAT';
update kodeverk set kodeverk_eier='Arena' where kode='RELATERT_YTELSE_SAKSTYPE';

-----
insert into kodeverk (kode, navn, beskrivelse, kodeverk_eier ) values ('RELATERT_YTELSE_BEH_TEMA', 'RelatertYtelseBehandlingTema', '', 'Arena');
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'RELATERT_YTELSE_BEH_TEMA' from RELATERT_YTELSE_BEHANDLTEMA;

update KODELISTE SET offisiell_kode='Arbeidsavklaringspenger' where kode='AAP' and kodeverk='RELATERT_YTELSE_BEH_TEMA';
update KODELISTE SET offisiell_kode='Dagp. v/perm fra fiskeindustri' where kode='FISK' and kodeverk='RELATERT_YTELSE_BEH_TEMA';
update KODELISTE SET offisiell_kode='Dagpenger under permitteringer' where kode='PERM' and kodeverk='RELATERT_YTELSE_BEH_TEMA';
update KODELISTE SET offisiell_kode='Lønnsgarantimidler - dagpenger' where kode='LONN' and kodeverk='RELATERT_YTELSE_BEH_TEMA';
update KODELISTE SET offisiell_kode='Ordinære dagpenger' where kode='DAGO' and kodeverk='RELATERT_YTELSE_BEH_TEMA';
update KODELISTE SET offisiell_kode='Tiltakspenger (basisytelse før 2014)' where kode='BASI' and kodeverk='RELATERT_YTELSE_BEH_TEMA';
;
begin 
    migrer_KODELISTE_fk('RELATERT_YTELSE_BEHANDLTEMA', 'RELATERT_YTELSE_BEH_TEMA');
end;
/

drop table RELATERT_YTELSE_BEHANDLTEMA cascade constraints;
------

insert into kodeverk (kode, navn, beskrivelse, kodeverk_eier ) values ('RELATERT_YTELSE_STATUS', 'RelatertYtelseStatus', '', 'Arena');
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'RELATERT_YTELSE_STATUS' from RELATERT_YTELSE_STATUS;

update KODELISTE SET OFFISIELL_KODE='Avsluttet' where kode='AVSLU' AND kodeverk='RELATERT_YTELSE_STATUS';
update KODELISTE SET OFFISIELL_KODE='Godkjent' where kode='GODKJ' AND kodeverk='RELATERT_YTELSE_STATUS';
update KODELISTE SET OFFISIELL_KODE='Innstilt' where kode='INNST' AND kodeverk='RELATERT_YTELSE_STATUS';
update KODELISTE SET OFFISIELL_KODE='Iverksatt' where kode='IVERK' AND kodeverk='RELATERT_YTELSE_STATUS';
update KODELISTE SET OFFISIELL_KODE='Mottatt' where kode='MOTAT' AND kodeverk='RELATERT_YTELSE_STATUS';
update KODELISTE SET OFFISIELL_KODE='Opprettet' where kode='OPPRE' AND kodeverk='RELATERT_YTELSE_STATUS';
update KODELISTE SET OFFISIELL_KODE='Registrert' where kode='REGIS' and kodeverk='RELATERT_YTELSE_STATUS';

update KODELISTE SET OFFISIELL_KODE=KODE where offisiell_kode Is null and kodeverk='RELATERT_YTELSE_STATUS';

update KODELISTE SET ekstra_data='{"infotrygdOppe:"true"}' where offisiell_kode in ('IP', 'UB', 'SG', 'UK', 'RT', 'ST', 'VD', 'VI', 'VT') 
and kodeverk='RELATERT_YTELSE_STATUS';



begin 
    migrer_KODELISTE_fk('RELATERT_YTELSE_STATUS', 'RELATERT_YTELSE_STATUS');
end;
/

drop table RELATERT_YTELSE_STATUS cascade constraints;
------

insert into kodeverk (kode, navn, beskrivelse, kodeverk_eier ) values ('RELATERT_YTELSE_TEMA', 'RelatertYtelseTema', '', 'Arena');
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'RELATERT_YTELSE_TEMA' from RELATERT_YTELSE_TEMA;

update KODELISTE SET OFFISIELL_KODE='Arbeidsavklaringspenger' where kode='AA' AND kodeverk='RELATERT_YTELSE_TEMA';
update KODELISTE SET OFFISIELL_KODE='Dagpenger' where kode='DAGP' AND kodeverk='RELATERT_YTELSE_TEMA';
update KODELISTE SET OFFISIELL_KODE='Individstønad' where kode='INDIV' AND kodeverk='RELATERT_YTELSE_TEMA';

begin 
    migrer_KODELISTE_fk('RELATERT_YTELSE_TEMA', 'RELATERT_YTELSE_TEMA');
end;
/

drop table RELATERT_YTELSE_TEMA cascade constraints;
------