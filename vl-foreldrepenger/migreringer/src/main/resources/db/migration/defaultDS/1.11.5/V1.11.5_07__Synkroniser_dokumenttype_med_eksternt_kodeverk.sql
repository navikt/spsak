merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.KODE = '-')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, '-', null, 'Ikke definert', 'Ikke definert', 'DOKUMENT_TYPE_ID', TO_DATE('2000-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000003')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'SØKNAD_ENGANGSSTØNAD_FØDSEL', 'I000003', 'Søknad om engangsstønad ved fødsel', 'Søknad om engangsstønad ved fødsel', 'DOKUMENT_TYPE_ID', TO_DATE('2000-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000004')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'SØKNAD_ENGANGSSTØNAD_ADOPSJON', 'I000004', 'Søknad om engangsstønad ved adopsjon', 'Søknad om engangsstønad ved adopsjon', 'DOKUMENT_TYPE_ID', TO_DATE('2000-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000041')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL', 'I000041', 'Dokumentasjon av termin eller fødsel', 'Dokumentasjon av termindato (lev. kun av mor), fødsel eller dato for omsorgsovertakelse', 'DOKUMENT_TYPE_ID', TO_DATE('2000-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000042')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOKUMENTASJON_AV_OMSORGSOVERTAKELSE', 'I000042', 'Dokumentasjon av omsorgsovertakelse', 'Dokumentasjon av dato for overtakelse av omsorg', 'DOKUMENT_TYPE_ID', TO_DATE('2000-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000047')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'BRUKEROPPLASTET_DOKUMENTASJON', 'I000047', 'Brukeropplastet dokumentasjon', null, 'DOKUMENT_TYPE_ID', TO_DATE('2000-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000027')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'KLAGE_DOKUMENT', 'I000027', 'Klage', 'Klage', 'DOKUMENT_TYPE_ID', TO_DATE('2000-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000046')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'KVITTERING_DOKUMENTINNSENDING', 'I000046', 'Kvittering dokumentinnsending', 'Kvittering dokumentinnsending', 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000062')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'BEKREFTELSE_VENTET_FØDSELSDATO', 'I000062', 'Bekreftelse på ventet fødselsdato', 'Bekreftelse på ventet fødselsdato', 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000023')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'LEGEERKLÆRING', 'I000023', 'Legeerklæring', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000024')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'GJELDSBREV_GRUPPE_1', 'I000024', 'Gjeldsbrev gruppe 1', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000065')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'BEKREFTELSE_FRA_ARBEIDSGIVER', 'I000065', 'Bekreftelse fra arbeidsgiver', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-05-24 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000021')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'KOPI_VOGNKORT', 'I000021', 'Kopi av vognkort', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000066')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'KOPI_SKATTEMELDING', 'I000066', 'Kopi av likningsattest eller selvangivelse', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-05-24 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000022')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'KOPI_FØRERKORT', 'I000022', 'Kopi av førerkort', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000028')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'BREV_UTLAND', 'I000028', 'Brev - utland', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000025')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'GJELDSBREV_GRUPPE_2', 'I000025', 'Gjeldsbrev gruppe 2', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000026')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'INNTEKTSOPPLYSNINGER', 'I000026', 'Inntektsopplysninger for arbeidstaker som skal ha sykepenger, foreldrepenger, svangerskapspenger, pleie-/opplæringspenger', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000060')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'ANNET', 'I000060', 'Annet', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-05-24 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000063')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'FØDSELSATTEST', 'I000063', 'Fødselsattest', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-05-24 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000064')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'ELEVDOKUMENTASJON_LÆRESTED', 'I000064', 'Elevdokumentasjon fra lærested', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-05-24 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000020')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'KOPI_VERGEATTEST', 'I000020', 'Kopi av verge- eller hjelpeverge attest', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000061')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'BEKREFTELSE_FRA_STUDIESTED', 'I000061', 'Bekreftelse fra studiested/skole', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-05-24 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I500010')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'ETTERSENDT_SØKNAD_TILPASSNING_BIL', 'I500010', 'Ettersendelse til søknad om spesialutstyr og- tilpassing til bil', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-08-16 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I500057')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'TREKKOPPLYSNINGER_ETTERSENDT', 'I500057', 'Ettersendelse til trekkopplysninger for arbeidstaker som skal ha: sykepenger, foreldrepenger, svangerskapspenger, pleie-/opplæringspenger og omsorgspenger', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-08-16 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000029')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'ANNET_SKJEMA_UTLAND_IKKE_NAV', 'I000029', 'Annet skjema (ikke NAV-skjema) - utland', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000034')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'OPPPDRAGSKONTRAKT', 'I000034', 'Oppdragskontrakt', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000035')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'LØNNS_OG_TREKKOPPGAVE', 'I000035', 'Lønns- og trekkoppgave', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I001000')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'OPPHOLDSOPPLYSNINGER', 'I001000', 'Oppholdsopplysninger', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-26 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000032')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'RESULTATREGNSKAP', 'I000032', 'Resultatregnskap', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000033')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'LØNNSLIPP', 'I000033', 'Lønnsslipp', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000038')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_MORS_UTDANNING_ARBEID_SYKDOM', 'I000038', 'Dokumentasjon av mors utdanning, arbeid eller sykdom', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000039')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_MILITÆR_SIVIL_TJENESTE', 'I000039', 'Dokumentasjon av militær- eller siviltjeneste', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000036')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_FERIE', 'I000036', 'Dokumentasjon av ferie', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000037')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_INNLEGGELSE', 'I000037', 'Dokumentasjon av innleggelse i helseinstitusjon', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000030')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'NÆRINGSOPPGAVE', 'I000030', 'Næringsoppgave', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000031')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'PERSONINNTEKTSKJEMA', 'I000031', 'Personinntektsskjema', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I500027')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'I500027', 'I500027', 'Ettersendelse til klage/anke', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-08-16 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000045')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'BESKRIVELSE_FUNKSJONSNEDSETTELSE', 'I000045', 'Beskrivelse av funksjonsnedsettelse', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000001')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG', 'I000001', 'Søknad om svangerskapspenger til selvstendig næringsdrivende og frilanser', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000002')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'SØKNAD_FORELDREPENGER_ADOPSJON', 'I000002', 'Søknad om foreldrepenger, mødrekvote eller fedrekvote ved adopsjon', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000043')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_ARBEIDSFORHOLD', 'I000043', 'Dokumentasjon av arbeidsforhold', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000044')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_ETTERLØNN', 'I000044', 'Dokumentasjon av etterlønn/sluttvederlag', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000049')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'ANNET_SKJEMA_IKKE_NAV', 'I000049', 'Annet skjema (ikke NAV-skjema)', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000005')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'SØKNAD_FORELDREPENGER_FØDSEL', 'I000005', 'Søknad om foreldrepenger, mødrekvote eller fedrekvote ved fødsel', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000006')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'FLEKSIBELT_UTTAK_FORELDREPENGER', 'I000006', 'Utsettelse eller gradert uttak av foreldrepenger (fleksibelt uttak)', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000048')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'BREV', 'I000048', 'Brev', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000040')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_ASYL_DATO', 'I000040', 'Dokumentasjon av dato for asyl', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000009')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'SØKNAD_REISEUTGIFT_BIL', 'I000009', 'Søknad om refusjon av reiseutgifter til bil', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000007')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'INNTEKTSOPPLYSNING_SELVSTENDIG', 'I000007', 'Inntektsopplysninger om selvstendig næringsdrivende og/eller frilansere som skal ha foreldrepenger eller svangerskapspenger', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000008')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'SØKNAD_SKAFFE_BIL', 'I000008', 'Søknad om stønad til anskaffelse av motorkjøretøy', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000056')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_UTGIFT_BARNEPASS', 'I000056', 'Dokumentasjon av utgifter til stell og pass av barn', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000012')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'TILLEGGSJKJEMA_BIL', 'I000012', 'Tilleggskjema for bil', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000057')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'TREKKOPPLYSNING_ARBEIDSTAKER', 'I000057', 'Trekkopplysninger for arbeidstaker som skal ha: sykepenger, foreldrepenger, svangerskapspenger, pleie-/opplæringspenger og omsorgspenger', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000013')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'BEKREFTELSE_OPPMØTE', 'I000013', 'Bekreftelse på oppmøte', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000054')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_UTBETALING_FRA_ARBEIDSGIVER', 'I000054', 'Dokumentasjon av utbetalinger eller goder fra arbeidsgiver', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000010')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'SØKNAD_TILPASSNING_BIL', 'I000010', 'Søknad om spesialutstyr og -tilpassing til bil', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000055')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'BEKREFTELSE_OPPHOLDSTILLATELSE', 'I000055', 'Bekreftelse på oppholdstillatelse', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000011')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'LEGEERKLÆRING_EVNE_KJØRE_BIL', 'I000011', 'Legeerklæring om søkerens evne til å føre motorkjøretøy og om behovet for ekstra transport på grunn av funksjonshemmingen', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000016')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_INNTEKT', 'I000016', 'Dokumentasjon av inntekt', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000017')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_UTGIFT_REISE', 'I000017', 'Dokumentasjon av reiseutgifter', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000058')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_ANDRE_YTELSE', 'I000058', 'Dokumentasjon av andre ytelser', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-05-24 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000014')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_BEHOV_LEDSAGER', 'I000014', 'Dokumentasjon av behov for ledsager', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000059')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'TIMELISTER', 'I000059', 'Timelister', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-05-24 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000015')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_BEHOV_TRANSPORTMIDDEL', 'I000015', 'Dokumentasjon av behov for dyrere transportmiddel', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000052')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'SKJEMA_OPPLYSNING_INNTEKT', 'I000052', 'Inntektsopplysningsskjema', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I500050')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD', 'I500050', 'Ettersendelse til søknad om endring av uttak av foreldrepenger eller overføring av kvote', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-08-16 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000053')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_ANDRE_UTBETALINGER', 'I000053', 'Dokumentasjon av andre utbetalinger', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000050')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'FORELDREPENGER_ENDRING_SØKNAD', 'I000050', 'Søknad om endring av uttak av foreldrepenger eller overføring av kvote', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000051')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'BEKREFTELSE_DELTAR_KVALIFISERINGSPROGRAM', 'I000051', 'Bekreftelse på deltakelse i kvalifiseringsprogrammet', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-04-25 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I500002')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON', 'I500002', 'Ettersendelse til søknad om foreldrepenger, mødrekvote eller fedrekvote ved adopsjon', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-08-16 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I500003')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL', 'I500003', 'Ettersendelse til søknad om engangsstønad ved fødsel', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-08-16 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I500001')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG', 'I500001', 'Ettersendelse til søknad om svangerskapspenger til selvstendig næringsdrivende og frilanser', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-08-16 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I500006')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER', 'I500006', 'Ettersendelse til utsettelse eller gradert uttak av foreldrepenger (fleksibelt uttak)', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-08-16 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I500004')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON', 'I500004', 'Ettersendelse til søknad om engangsstønad ved adopsjon', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-08-16 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000018')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'SPESIALISTERKLÆRING', 'I000018', 'Spesialisterklæring', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I500005')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL', 'I500005', 'Ettersendelse til søknad om foreldrepenger, mødrekvote eller fedrekvote ved fødsel', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-08-16 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I000019')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'DOK_VEIFORHOLD', 'I000019', 'Dokumentasjon av veiforhold', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-03-22 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I500008')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'ETTERSENDT_SØKNAD_SKAFFE_BIL', 'I500008', 'Ettersendelse til søknad om stønad til anskaffelse av motorkjøretøy', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-08-16 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.offisiell_KODE = 'I500009')
when not matched then
INSERT (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'ETTERSENDT_SØKNAD_REISEUTGIFT_BIL', 'I500009', 'Ettersendelse til søknad om refusjon av reiseutgifter til bil', null, 'DOKUMENT_TYPE_ID', TO_DATE('2017-08-16 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
