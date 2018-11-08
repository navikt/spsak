-- nr41 Beregningsgrunnlag kun for fp
UPDATE KODELISTE SET EKSTRA_DATA = '{ "fagsakYtelseType" : { "FP" : { "kategori": "vilkår", "lovreferanse": "§ 14-7" } } }'
WHERE kodeverk = 'VILKAR_TYPE' AND KODE = 'FP_VK_41';
