MERGE INTO VURDERINGSPUNKT_DEF vd USING dual on (dual.dummy is not null and vd.kode = 'FAST_BERGRUNN.INN')
when not matched then
insert (kode, navn, behandling_steg, vurderingspunkt_type) VALUES ('FAST_BERGRUNN.INN', 'Fastsett beregningsgrunnlag - inngang', 'FAST_BERGRUNN', 'INN');

UPDATE AKSJONSPUNKT_DEF SET VURDERINGSPUNKT= 'FAST_BERGRUNN.INN' WHERE VURDERINGSPUNKT= 'FORS_BERGRUNN.UT';

DELETE FROM VURDERINGSPUNKT_DEF WHERE KODE = 'FORS_BERGRUNN.UT';
