
UPDATE kodeliste
SET ekstra_data = '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-5"}}}'
WHERE kodeverk = 'AVSLAGSARSAK' and kode = '1031';

UPDATE kodeliste
SET EKSTRA_DATA = '{"fagsakYtelseType": [{"ES": [{"kategori": "FP_VK1", "lovreferanse": "§ 14-17 1. ledd"}, {"kategori": "FP_VK4", "lovreferanse": "§ 14-17 1. ledd"}, {"kategori": "FP_VK5", "lovreferanse": "§ 14-17 3. ledd"}]}, {"FP": [{"kategori": "FP_VK_8", "lovreferanse": "14-5"}]}]}'
WHERE kodeverk = 'AVSLAGSARSAK' and kode = '1032';
