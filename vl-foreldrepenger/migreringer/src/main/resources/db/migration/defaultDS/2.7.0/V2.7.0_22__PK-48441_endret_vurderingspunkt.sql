--Endrer vurderingspunkt på aksjonspunkt 5046 --
INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type)
VALUES ('KOFAKBER.UT', 'Kontroller fakta beregningsgrunnlag - Utgang', 'KOFAKBER', 'UT');

UPDATE AKSJONSPUNKT_DEF
set VURDERINGSPUNKT = 'KOFAKBER.UT'
where KODE = '5046';
