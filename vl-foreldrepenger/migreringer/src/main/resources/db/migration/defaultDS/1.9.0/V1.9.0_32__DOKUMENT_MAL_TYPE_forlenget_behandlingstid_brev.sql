INSERT INTO DOKUMENT_MAL_RESTRIKSJON (kode, navn) VALUES ('ÅPEN_BEHANDLING', 'Brev kan bare sendes fra en åpen behandling');

INSERT INTO DOKUMENT_MAL_TYPE (kode, navn, DOKUMENT_MAL_RESTRIKSJON, generisk) VALUES ('000056', 'Forlenget saksbehandlingstid', 'ÅPEN_BEHANDLING', 'Y');
