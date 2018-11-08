update kodeliste
set kode = '1041'
where kode = '1035'
  and navn = 'for lavt brutto beregningsgrunnlag'
  and kodeverk = 'AVSLAGSARSAK';

-- Nytt internt kodeverk vilkår_kategori med verdier (inngangsvilkår, beregningsvilkår)
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
values ('VILKAR_KATEGORI', 'N', 'N', 'Vilkår kategori', 'Kodeverk for vilkårskategorier');

insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'INNGANGSVILKÅR', 'inngangsvilkår', 'VILKAR_KATEGORI', to_date('2000-01-01', 'yyyy-mm-dd'));

insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'BEREGNINGSVILKÅR', 'beregningsvilkår', 'VILKAR_KATEGORI', to_date('2000-01-01', 'yyyy-mm-dd'));

-- Legge til i ekstra_data "kategori":"inngangsvilkår" for alle eksisterende vilkårtyper (i tillegg til lovreferanse)
update kodeliste
set ekstra_data = '{ "kategori": "inngangsvilkår", ' || substr(ekstra_data, 3)
where kodeverk = 'VILKAR_TYPE'
  and kode <> '-';

-- nytt kode FP_VK_41 navn Vilkår for beregningsgrunnlag
insert into kodeliste (id, kode, navn, beskrivelse, kodeverk, ekstra_data, gyldig_fom)
values (seq_kodeliste.nextval, 'FP_VK_41', 'Vilkår for beregningsgrunnlag', 'Beregningsregel § 14-7 Beregningsvilkår §8-3', 'VILKAR_TYPE', '{ "kategori": "beregningsvilkår", "lovreferanse": "§ 14-7" }', to_date('2000-01-01', 'yyyy-mm-dd'));

-- Tabell vilkar: Endre navn på kolonne inngangsvilkar_resultat_id til vilkar_resultat_id
ALTER TABLE VILKAR
  RENAME COLUMN inngangsvilkar_resultat_id to vilkar_resultat_id;

-- Endre navn på tabell inngangsvilkar_resultat til vilkar_resultat
ALTER TABLE INNGANGSVILKAR_RESULTAT
  RENAME TO VILKAR_RESULTAT;

RENAME SEQ_INNGANGSVILKAR_RESULTAT TO SEQ_VILKAR_RESULTAT;
