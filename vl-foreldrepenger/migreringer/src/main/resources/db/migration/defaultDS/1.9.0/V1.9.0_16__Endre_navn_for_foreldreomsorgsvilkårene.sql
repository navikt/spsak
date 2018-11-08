alter table AKSJONSPUNKT_DEF modify (navn varchar2(55 char));

UPDATE AKSJONSPUNKT_DEF 
	SET NAVN = 'Manuell vurdering av foreldreansvarsvilkåret 2.ledd'
	WHERE KODE = '5013';

UPDATE AKSJONSPUNKT_DEF 
	SET NAVN = 'Manuell vurdering av foreldreansvarsvilkåret 4.ledd'
	WHERE KODE = '5014';

UPDATE VILKAR_TYPE 
 	SET NAVN = 'Foreldreansvarsvilkåret 2.ledd'
	WHERE KODE = 'FP_VK_8';
	
UPDATE VILKAR_TYPE 
 	SET NAVN = 'Foreldreansvarsvilkåret 4.ledd'
	WHERE KODE = 'FP_VK_33';