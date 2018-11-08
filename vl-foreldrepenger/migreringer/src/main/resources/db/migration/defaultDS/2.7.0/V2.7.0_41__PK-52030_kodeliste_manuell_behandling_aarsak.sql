-- Kodeliste MANUELL_BEHANDLING_AARSAK
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5015', 'Ugyldig stønadskonto - Foreldrepenger før fødsel starter for tidlig eller slutter for sent', 'Ugyldig stønadskonto - Foreldrepenger før fødsel starter for tidlig eller slutter for sent', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5017', 'Ugyldig stønadskonto - Far/medmor søkt om foreldrepenger før fødsel', 'Ugyldig stønadskonto - Far/medmor søkt om foreldrepenger før fødsel', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5005', 'Søker har ikke søkt om periode, og \"hullet\" er tidligere enn \"gyldig peride\', 'Søker har ikke søkt om periode, og \"hullet\" er tidligere enn \"gyldig peride\', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5006', 'Manuell behandling av utsetteklse', 'Manuell behandling av utsettelse', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5002', 'Ingen disponible dager igjen på kvote', 'Ingen disponible dager igjen på kvote', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5021', 'Søkt for sent', 'Søkt for sent', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5003', 'Søker har ikke omsorg', 'Søker har ikke omsorg', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5098', 'Adopsjon ikke implementert, må behandles manuelt', 'Adopsjon ikke implementert, må behandles manuelt', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5099', 'Foreldrepenger ikke implementert, må behandles manuelt', 'Foreldrepenger ikke implementert, må behandles manuelt', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
