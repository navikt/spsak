UPDATE STARTPUNKT_TYPE set RANGERING = RANGERING + 1 where KODE != '-';

INSERT INTO STARTPUNKT_TYPE (KODE, NAVN, BEHANDLING_STEG, RANGERING, BESKRIVELSE)
VALUES ('KONTROLLER_ARBEIDSFORHOLD', 'Startpunkt kontroller arbeidsforhold', 'KOARB', 1, 'Startpunkt kontroller arbeidsforhold');

