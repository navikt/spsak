insert into KONFIG_VERDI_KODE(kode, navn, konfig_gruppe, konfig_type, beskrivelse)
values('inntekt.rapportering.frist.dato', 'Inntektsrapporteringsfrist','INGEN','INTEGER','Innsendingsfrist for inntekter for foregående måned');
insert into KONFIG_VERDI(id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
values(SEQ_KONFIG_VERDI.nextval, 'inntekt.rapportering.frist.dato', 'INGEN', '5', to_date('01.07.2016','dd.mm.yyyy'));

insert into VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type)
values ('FORS_BERGRUNN.UT', 'Foreslå beregningsgrunnlag - Utgang', 'FORS_BERGRUNN', 'UT');

insert into AKSJONSPUNKT_DEF(kode, navn, vurderingspunkt, beskrivelse, vilkar_type, aksjonspunkt_type, tilbakehopp_ved_gjenopptakelse,
lag_uten_historikk, totrinn_behandling_default, skjermlenke_type)
values ('7014', 'Vent på rapporteringsfrist for inntekt', 'FORS_BERGRUNN.UT', 'Vent til dagen etter rapporteringsfrist for månedsinntekt',
'-', 'AUTO', 'J', 'N', 'N','-');

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, beskrivelse, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VENT_AARSAK', 'VENT_INNTEKT_RAPPORTERINGSFRIST', null, 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'));
