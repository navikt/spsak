INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, ekstra_data)
VALUES (seq_kodeliste.nextval, 'BEH_MAN_GJEN', 'Gjenoppta behandling','Brukes når saksbehandler bruker behandlingsmenyen til å gjenoppta en behandling', 'HISTORIKKINNSLAG_TYPE', to_date('2018-05-22', 'YYYY-MM-DD'), '{"mal": "TYPE1"}');
