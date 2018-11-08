update KODELISTE set offisiell_kode = 'Overstyring' where kode = 'OVST' and kodeverk = 'AKSJONSPUNKT_TYPE';
update KODELISTE set offisiell_kode = 'Manuell' where kode = 'MANU' and kodeverk = 'AKSJONSPUNKT_TYPE';
update KODELISTE set offisiell_kode = 'Auto' where kode = 'AUTO' and kodeverk = 'AKSJONSPUNKT_TYPE';
update KODELISTE set offisiell_kode = 'Udefinert' where kode = '-' and kodeverk = 'AKSJONSPUNKT_TYPE';
