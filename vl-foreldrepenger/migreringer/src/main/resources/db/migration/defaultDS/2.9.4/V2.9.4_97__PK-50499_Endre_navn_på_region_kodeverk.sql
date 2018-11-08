update kodeliste set navn = 'EU/EØS' where kodeverk = 'REGION' and kode = 'EOS';
update kodeliste set navn = '3.landsborger' where kodeverk = 'REGION' and kode in ('ANNET','-');
update kodeliste set navn = 'Nordisk' where kodeverk = 'REGION' and kode = 'NORDEN';
