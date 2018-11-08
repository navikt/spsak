INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) VALUES ('bruker.gruppenavn.saksbehandler', 'Gruppenavn for rolle saksbehandler', 'INGEN', 'STRING', 'Gruppenavn for rolle saksbehandler');
INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) VALUES (SEQ_KONFIG_VERDI.nextval, 'bruker.gruppenavn.saksbehandler', 'INGEN', '0000-GA-fpsak-saksbehandler', to_date('01.01.2016', 'dd.mm.yyyy'));

INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) VALUES ('bruker.gruppenavn.veileder', 'Gruppenavn for rolle veileder', 'INGEN', 'STRING', 'Gruppenavn for rolle veileder');
INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) VALUES (SEQ_KONFIG_VERDI.nextval, 'bruker.gruppenavn.veileder', 'INGEN', '0000-GA-fpsak-veileder', to_date('01.01.2016', 'dd.mm.yyyy'));

INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) VALUES ('bruker.gruppenavn.beslutter', 'Gruppenavn for rolle beslutter', 'INGEN', 'STRING', 'Gruppenavn for rolle beslutter');
INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) VALUES (SEQ_KONFIG_VERDI.nextval, 'bruker.gruppenavn.beslutter', 'INGEN', '0000-GA-fpsak-beslutter', to_date('01.01.2016', 'dd.mm.yyyy'));

INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) VALUES ('bruker.gruppenavn.overstyrer', 'Gruppenavn for rolle overstyrer', 'INGEN', 'STRING', 'Gruppenavn for rolle overstyrer');
INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) VALUES (SEQ_KONFIG_VERDI.nextval, 'bruker.gruppenavn.overstyrer', 'INGEN', '0000-GA-fpsak-manuelt-overstyrer', to_date('01.01.2016', 'dd.mm.yyyy'));

INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) VALUES ('bruker.gruppenavn.egenansatt', 'Gruppenavn for rolle egen ansatt', 'INGEN', 'STRING', 'Gruppenavn for rolle egen ansatt');
INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) VALUES (SEQ_KONFIG_VERDI.nextval, 'bruker.gruppenavn.egenansatt', 'INGEN', '0000-GA-GOSYS_UTVIDET', to_date('01.01.2016', 'dd.mm.yyyy'));

INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) VALUES ('bruker.gruppenavn.kode6', 'Gruppenavn for rolle kode 6', 'INGEN', 'STRING', 'Gruppenavn for rolle kode 6');
INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) VALUES (SEQ_KONFIG_VERDI.nextval, 'bruker.gruppenavn.kode6', 'INGEN', '0000-GA-GOSYS_KODE6', to_date('01.01.2016', 'dd.mm.yyyy'));

INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) VALUES ('bruker.gruppenavn.kode7', 'Gruppenavn for rolle kode 7', 'INGEN', 'STRING', 'Gruppenavn for rolle kode 7');
INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) VALUES (SEQ_KONFIG_VERDI.nextval, 'bruker.gruppenavn.kode7', 'INGEN', '0000-GA-GOSYS_KODE7', to_date('01.01.2016', 'dd.mm.yyyy'));
