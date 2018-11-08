-- Nytt vilkår utfall for fødselsvilkåret
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '1026', '1026', 'Fødselsdato ikke oppgitt eller registrert', 'VILKAR_UTFALL_MERKNAD', to_date('2017-01-01', 'YYYY-MM-DD'));
-- Ny avslagsårsak for det nye vilkårsutfallet
INSERT INTO AVSLAGSARSAK (KODE, VILKAR_KODE, NAVN) VALUES ('1026', 'FP_VK_1', 'Fødselsdato ikke oppgitt eller registrert');
