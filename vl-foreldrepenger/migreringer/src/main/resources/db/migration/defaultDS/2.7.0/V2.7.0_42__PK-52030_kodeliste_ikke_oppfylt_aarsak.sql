-- kodeverk IKKE_OPPFYLT_AARSAK
INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('IKKE_OPPFYLT_AARSAK', 'N', 'N', 'Årsak til ikke oppfylt stønadsperiode', 'Årsak til ikke oppfylt stønadsperiode');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4011', 'Far søker de 6 første ukene - mors sykdom/skade eller innleggelse ugyldig', 'Far søker de 6 første ukene - mors sykdom/skade eller innleggelse ugyldig', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4005', 'Hull mellom stønadsperioder', 'Hull mellom stønadsperioder', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4002', 'Ikke Stønadsdager igjen', 'Ikke Stønadsdager igjen', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4020', 'Brudd på søknadsfrist', 'Brudd på søknadsfrist', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4022', 'Barnet er over 3 år', 'Barnet er over 3 år', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4012', 'Far har ikke omsorg', 'Far har ikke omsorg', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4003', 'Mor har ikke omsorg', 'Mor har ikke omsorg', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4001', 'Mødrekvote før termin/fødsel', 'Mødrekvote før termin/fødsel', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4013', 'Mor søker fellesperiode før 12 uker før termin/fødsel', 'Mor søker fellesperiode før 12 uker før termin/fødsel', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4010', 'Far søker før termin/fødsel', 'Far søker før termin/fødsel', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4060', 'Samtidig uttak - ikke gyldig kombinasjon', 'Samtidig uttak - ikke gyldig kombinasjon', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4014', 'Mor søker fellesperiode før uke 7', 'Mor søker fellesperiode før uke 7', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4007', 'Den andre part syk/skadet ikke oppfylt', 'Den andre part syk/skadet ikke oppfylt', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4008', 'Den andre part innleggelse ikke oppfylt', 'Den andre part innleggelse ikke oppfylt', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4033', 'Ikke lovbestemt ferie', 'Ikke lovbestemt ferie', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4032', 'Ferie - selvstendig næringsdrivende/frilanser', 'Ferie - selvstendig næringsdrivende/frilanser', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4037', 'Ikke heltidsarbeid', 'Ikke heltidsarbeid', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4038', 'Søkers sykdom/skade ikke oppfylt', 'Søkers sykdom/skade ikke oppfylt', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4039', 'Søkers innleggelse ikke oppfylt', 'Søkers innleggelse ikke oppfylt', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4040', 'Barnets innleggelse ikke oppfylt', 'Barnets innleggelse ikke oppfylt', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4031', 'Ferie innenfor de første 6 ukene', 'Ferie innenfor de første 6 ukene', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4034', 'Avslag utsettelse - ingen stønadsdager igjen', 'Avslag utsettelse - ingen stønadsdager igjen', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4035', 'Far aleneomsorg, mor fyller ikke aktivitetskravet', 'Far aleneomsorg, mor fyller ikke aktivitetskravet', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4030', 'Avslag utsettelse før termin/fødsel', 'Avslag utsettelse før termin/fødsel', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4041', 'Avslag utsettelse ferie på bevegelig helligdag', 'Avslag utsettelse ferie på bevegelig helligdag', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4050', 'Aktivitetskravet arbeid ikke oppfylt', 'Aktivitetskravet arbeid ikke oppfylt', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4051', 'Aktivitetskravet offentlig godkjent utdanning ikke oppfylt', 'Aktivitetskravet offentlig godkjent utdanning ikke oppfylt', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4052', 'Aktivitetskravet offentlig godkjent utdanning i kombinasjon med arbeid ikke oppfylt', 'Aktivitetskravet offentlig godkjent utdanning i kombinasjon med arbeid ikke oppfylt', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4053', 'Aktivitetskravet mors sykdom/skade ikke oppfylt', 'Aktivitetskravet mors sykdom/skade ikke oppfylt', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4054', 'Aktivitetskravet mors innleggelse ikke oppfylt', 'Aktivitetskravet mors innleggelse ikke oppfylt', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4055', 'Aktivitetskravet mors deltakelse på introduksjonssprogram ikke oppfylt', 'Aktivitetskravet mors deltakelse på introduksjonssprogram ikke oppfylt', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4056', 'Aktivitetskravet mors deltakelse på kvalifiseringsprogram ikke oppfylt', 'Aktivitetskravet mors deltakelse på kvalifiseringsprogram ikke oppfylt', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4057', 'Unntak for Aktivitetskravet, mors mottak av uføretrygd ikke oppfylt', 'Unntak for Aktivitetskravet, mors mottak av uføretrygd ikke oppfylt', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4058', 'Unntak for Aktivitetskravet, stebarnsadopsjon - ikke nok dager', 'Unntak for Aktivitetskravet, stebarnsadopsjon - ikke nok dager', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4059', 'Unntak for Aktivitetskravet, flerbarnsfødsel - ikke nok dager', 'Unntak for Aktivitetskravet, flerbarnsfødsel - ikke nok dager', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
