INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
VALUES ('terminbekreftelse.tidligst.utstedelse.før.termin', 'Tidligst utstedt terminbekreft. etter termindato', 'INGEN', 'PERIOD', 'Periode for tidligst utstedelse av terminbekreftelse etter termindato');
INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
VALUES (SEQ_KONFIG_VERDI.nextval, 'terminbekreftelse.tidligst.utstedelse.før.termin', 'INGEN', 'P14W4D', to_date('05.12.2017', 'dd.mm.yyyy'));

INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
VALUES ('behandling.default.ventefrist.periode', 'Default ventefrist for behandling', 'INGEN', 'PERIOD', 'Default ventefrist for behandling');
INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
VALUES (SEQ_KONFIG_VERDI.nextval, 'behandling.default.ventefrist.periode', 'INGEN', 'P4W', to_date('05.12.2017', 'dd.mm.yyyy'));

INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
VALUES ('aksjonspunkt.dager.etter.termin.sjekk.fødsel', 'Restart behandling/sjekk fødsel etter termindato', 'INGEN', 'PERIOD', 'Maks antall dager etter hvilket behandling restartes/fødsel sjekkes ifm terminbekreftelse');
INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
VALUES (SEQ_KONFIG_VERDI.nextval, 'aksjonspunkt.dager.etter.termin.sjekk.fødsel', 'INGEN', 'P25D', to_date('05.12.2017', 'dd.mm.yyyy'));

INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
VALUES ('norg2.kontakt.telefonnummer', 'Norg2 kontakttelefonnummer', 'INGEN', 'STRING', 'Norg2 kontakttelefonnummer');
INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
VALUES (SEQ_KONFIG_VERDI.nextval, 'norg2.kontakt.telefonnummer', 'INGEN', '55553333', to_date('05.12.2017', 'dd.mm.yyyy'));
