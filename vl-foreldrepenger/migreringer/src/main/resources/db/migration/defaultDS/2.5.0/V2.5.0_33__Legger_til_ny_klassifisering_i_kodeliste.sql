INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_KLASSIFISERING', 'MELLOMLIGGENDE_PERIODE', 'Mellomliggende periode', 'Aktivitet er godkjent som en mellomliggende periode.', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
