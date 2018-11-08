MERGE INTO VURDERINGSPUNKT_DEF vd USING dual on (dual.dummy is not null and vd.kode = 'FORS_BERGRUNN.UT')
when not matched then
insert (kode, navn, behandling_steg, vurderingspunkt_type) VALUES ('FORS_BERGRUNN.UT', 'Foreslå beregningsgrunnlag - Utgang', 'FORS_BERGRUNN', 'UT');

MERGE INTO aksjonspunkt_def ad USING dual on (dual.dummy is not null
                                              and ad.beskrivelse = 'Fastsette beregningsgrunnlag for arbeidstaker/frilanser skjønnsmessig')
when not matched then
insert (kode, navn, vurderingspunkt, beskrivelse, VILKAR_TYPE, totrinn_behandling_default)
values (5038, 'Fastsette beregningsgrunnlag for arbeidstaker/frilanser skjønnsmessig', 'FORS_BERGRUNN.UT',
        'Fastsette beregningsgrunnlag for arbeidstaker/frilanser skjønnsmessig', '-', 'N');
