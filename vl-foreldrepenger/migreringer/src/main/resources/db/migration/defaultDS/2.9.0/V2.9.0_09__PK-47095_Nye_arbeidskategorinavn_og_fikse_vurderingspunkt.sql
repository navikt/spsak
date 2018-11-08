UPDATE KODELISTE
SET navn = 'Selvstendig næringsdrivende - Jordbruker'
WHERE kode = 'JORDBRUKER' and kodeverk='ARBEIDSKATEGORI';

UPDATE KODELISTE
SET navn = 'Selvstendig næringsdrivende - Fisker'
WHERE kode = 'FISKER' and kodeverk='ARBEIDSKATEGORI';

UPDATE KODELISTE
SET navn = 'Selvstendig næringsdrivende - Dagmamma'
WHERE kode = 'DAGMAMMA' and kodeverk='ARBEIDSKATEGORI';

UPDATE KODELISTE
SET navn = 'Tilstøtende ytelse - dagpenger'
WHERE kode = 'DAGPENGER' and kodeverk='ARBEIDSKATEGORI';

UPDATE KODELISTE
SET navn = 'Arbeidstaker - sjømann'
WHERE kode = 'SJØMANN' and kodeverk='ARBEIDSKATEGORI';

UPDATE KODELISTE
SET navn = 'Kombinasjon arbeidstaker og dagpenger'
WHERE kode = 'KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER' and kodeverk='ARBEIDSKATEGORI';

UPDATE KODELISTE
SET navn = 'Kombinasjon arbeidstaker og selvstendig næringsdrivende - fisker'
WHERE kode = 'KOMBINASJON_ARBEIDSTAKER_OG_FISKER' and kodeverk='ARBEIDSKATEGORI';

UPDATE KODELISTE
SET navn = 'Kombinasjon arbeidstaker og selvstendig næringsdrivende - jordbruker'
WHERE kode = 'KOMBINASJON_ARBEIDSTAKER_OG_JORDBRUKER' and kodeverk='ARBEIDSKATEGORI';

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'FORS_BERGRUNN_INN', 'Foreslå beregningsgrunnlag - Inngang', 'Foreslå beregningsgrunnlag - Inngang', to_date('2000-01-01', 'YYYY-MM-DD'), 'BEREGNINGSGRUNNLAG_TILSTAND');

update AKSJONSPUNKT_DEF
set VURDERINGSPUNKT = 'KOFAKBER.UT'
where VURDERINGSPUNKT = 'FORS_BERGRUNN.INN';
