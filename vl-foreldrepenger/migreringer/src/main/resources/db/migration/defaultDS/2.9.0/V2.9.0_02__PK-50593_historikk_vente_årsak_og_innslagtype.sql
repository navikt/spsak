-- Vent aarsak
INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'VENT_AARSAK', 'VENT_TIDLIGERE_BEHANDLING', 'Venter på iverksettelse av en tidligere behandling i denne saken', 'Venter på iverksettelse av en tidligere behandling i denne saken', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'VENT_AARSAK', 'VENT_INFOTRYGD', 'Venter på en ytelse i Infotrygd', 'Venter på en ytelse i Infotrygd', to_date('2000-01-01', 'YYYY-MM-DD'));

--Historikkinnslag type
INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
VALUES(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'IVERKSETTELSE_VENT', 'Behandlingen venter på iverksettelse', to_date('2017-01-01', 'YYYY-MM-DD'), '{"mal": "TYPE4"}');
