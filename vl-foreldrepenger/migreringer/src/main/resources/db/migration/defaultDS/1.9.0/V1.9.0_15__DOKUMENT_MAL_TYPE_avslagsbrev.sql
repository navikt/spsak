
INSERT INTO dokument_mal_type (kode, navn, generisk) VALUES ('000051', 'Avslagsbrev', 'N');

-- Endring i VILKAR_TYPE tabell
update VILKAR_TYPE set lov_referanse = '§ 14-17, 1. ledd' where kode = 'FP_VK_1';
update VILKAR_TYPE set lov_referanse = '§ 14-2' where kode = 'FP_VK_2';
update VILKAR_TYPE set lov_referanse = '§ 22-13, 2. ledd' where kode = 'FP_VK_3';
update VILKAR_TYPE set lov_referanse = '§ 14-17, 1. ledd' where kode = 'FP_VK_4';
update VILKAR_TYPE set lov_referanse = '§ 14-17, 3. ledd' where kode = 'FP_VK_5';
update VILKAR_TYPE set lov_referanse = 'Utgår' where kode = 'FP_VK_6';
update VILKAR_TYPE set lov_referanse = 'Utgår' where kode = 'FP_VK_7';
update VILKAR_TYPE set lov_referanse = '§ 14-17, 2. ledd' where kode = 'FP_VK_8';
update VILKAR_TYPE set lov_referanse = '§ 14-17, 4. ledd' where kode = 'FP_VK_33';
update VILKAR_TYPE set lov_referanse = '§§ 21-3 og 21-7' where kode = 'FP_VK_34';
alter table VILKAR_TYPE modify (lov_referanse NOT NULL);

