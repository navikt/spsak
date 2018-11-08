

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT,SKJERMLENKE_TYPE)
VALUES ('5056','Kontroll av manuelt opprettet revurderingsbehandling','FORVEDSTEG.UT',
        'Aksjonspunkt for å manuelt opprette revurderingsbehandling','-','N','-');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'RE-KLAG', 'Realitetsbehandling/klage', 'Revurdering pga. realitetsbehandling/klage', 'BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'RE-MDL', 'Opplysninger om medlemskap', 'Revurdering pga. opplysninger om medlemskap', 'BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'RE-OPTJ', 'Opplysninger om opptjening', 'Revurdering pga. opplysninger om opptjening', 'BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'RE-FRDLING', 'Opplysninger om fordeling av stønadsperiode', 'Revurdering pga. opplysninger om fordeling av stønadsperiode', 'BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'RE-INNTK', 'Opplysninger om inntekt', 'Revurdering pga. opplysninger om inntekt', 'BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'RE-DØD', 'Opplysninger om død', 'Revurdering pga. opplysninger om død','BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'RE-SRTB', 'Opplysninger om søkers relasjon til barnet', 'Revurdering pga. opplysninger om søkers relasjon til barnet', 'BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'RE-FRIST', 'Opplysninger om søknadsfrist', 'Revurdering pga. opplysninger om søknadsfrist', 'BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'RE-BER-GRUN', 'Opplysninger om beregningsgrunnlag', 'Revurdering pga. opplysninger om beregningsgrunnlag', 'BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

COMMIT;

