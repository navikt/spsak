
INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk, ekstra_data)
VALUES (nextval('seq_kodeliste'), 'SP_VK_5', 'Søknadsfristvilkåret §22-13', to_date('2000-01-01', 'YYYY-MM-DD'),
        'VILKAR_TYPE', '{ "fagsakYtelseType" : {"SP" : { "kategori": "vilkår", "lovreferanse": "§ 22-13" } } }');
