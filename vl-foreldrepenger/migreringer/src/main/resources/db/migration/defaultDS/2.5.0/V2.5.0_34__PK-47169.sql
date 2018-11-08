INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT,  AKSJONSPUNKT_TYPE)
VALUES ('5054', 'Avklar fakta for foreldreansvarsvilkåret for FP',  'VURDERSRB.INN', 'Kontroller opplysningene og gjør eventuelle endringer før du bekrefter', 'FP_VK_8', 'N'  , 'MANU');

ALTER TABLE ADOPSJON ADD foreldreansvar_oppfylt_dato DATE NULL;


-- nr3 kun for es
UPDATE KODELISTE SET EKSTRA_DATA = '{ "fagsakYtelseType" : { "ES" : { "kategori": "vilkår", "lovreferanse": "§ 22-13, 2. ledd" }} }'
WHERE kodeverk = 'VILKAR_TYPE' AND KODE = 'FP_VK_3';

-- nr4 kun for es
UPDATE KODELISTE SET EKSTRA_DATA = '{ "fagsakYtelseType" : { "ES" : { "kategori": "vilkår", "lovreferanse": "§ 14-17, 1. ledd" } } }'
WHERE kodeverk = 'VILKAR_TYPE' AND KODE = 'FP_VK_4';

-- nr5 kun for es
UPDATE KODELISTE SET EKSTRA_DATA = '{ "fagsakYtelseType" : { "ES" : { "kategori": "vilkår", "lovreferanse": "§ 14-17, 3. ledd" }} }'
WHERE kodeverk = 'VILKAR_TYPE' AND KODE = 'FP_VK_5';

-- nr8 endret lovref for fp
UPDATE KODELISTE SET EKSTRA_DATA = '{ "fagsakYtelseType" : { "ES" : { "kategori": "vilkår", "lovreferanse": "§ 14-17, 2. ledd" } , "FP" : { "kategori": "vilkår", "lovreferanse": "§ 14-5, 2. ledd" } } }'
WHERE kodeverk = 'VILKAR_TYPE' AND KODE = 'FP_VK_8';

-- nr33 kun es
UPDATE KODELISTE SET EKSTRA_DATA = '{ "fagsakYtelseType" : { "ES" : { "kategori": "vilkår", "lovreferanse": "§ 14-17, 4. ledd" } } }'
WHERE kodeverk = 'VILKAR_TYPE' AND KODE = 'FP_VK_33';

-- nr23 kun for fp
UPDATE KODELISTE SET EKSTRA_DATA = '{ "fagsakYtelseType" : {"FP" : { "kategori": "vilkår", "lovreferanse": "§ 14-6" } } }'
WHERE kodeverk = 'VILKAR_TYPE' AND KODE = 'FP_VK_23';

-- nr41 Beregningsgrunnlag kun for fp
UPDATE KODELISTE SET EKSTRA_DATA = '{ "fagsakYtelseType" :  "FP" : { "kategori": "vilkår", "lovreferanse": "§ 14-7" } } }'
WHERE kodeverk = 'VILKAR_TYPE' AND KODE = 'FP_VK_41';

-- nr21 kun for fp
UPDATE KODELISTE SET EKSTRA_DATA = '{ "fagsakYtelseType" : {  "FP" : { "kategori": "vilkår", "lovreferanse": "§ 14-6 og 14-10" } } }'
WHERE kodeverk = 'VILKAR_TYPE' AND KODE = 'FP_VK_21';




