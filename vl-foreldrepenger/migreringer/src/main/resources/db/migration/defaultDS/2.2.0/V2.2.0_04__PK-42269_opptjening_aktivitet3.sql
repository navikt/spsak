
update kodeliste set kodeverk='VILKAR_UTFALL_MERKNAD' where kodeverk='VILKAR_UTFALL_TYPE' and kode='1035';

update kodeliste set kode = 'BEKREFTET_GODKJENT' where kodeverk='OPPTJENING_AKTIVITET_KLASSIFISERING' and kode='BEKREFET_GODKJENT';

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'AVSLAGSARSAK', '1035', 'Ikke tilstrekkelig opptjening', 'Ikke tilstrekkelig opptjening', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
