
update KODELISTE set gyldig_fom = to_date('2006-07-01', 'YYYY-MM-DD') where KODEVERK = 'ARKIV_FILTYPE' and kode = 'XML';
update KODELISTE set gyldig_fom = to_date('2006-07-01', 'YYYY-MM-DD') where KODEVERK = 'ARKIV_FILTYPE' and kode = 'PDF';
update KODELISTE set gyldig_fom = to_date('2006-07-01', 'YYYY-MM-DD') where KODEVERK = 'ARKIV_FILTYPE' and kode = 'PDFA';

insert into KODELISTE (id, kode, offisiell_kode,  navn, beskrivelse, kodeverk, gyldig_fom) values
  (seq_kodeliste.nextval, 'AFP', 'AFP', 'AFP', 'Filtype AFP', 'ARKIV_FILTYPE', to_date('2006-07-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom) values
  (seq_kodeliste.nextval, 'AXML', 'AXML', 'AXML', 'Filtype AXML', 'ARKIV_FILTYPE', to_date('2017-07-06', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom) values
  (seq_kodeliste.nextval, 'DLF', 'DLF', 'DLF', 'Filtype DLF', 'ARKIV_FILTYPE', to_date('2006-07-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom) values
  (seq_kodeliste.nextval, 'DOC', 'DOC', 'DOC', 'Filtype DOC', 'ARKIV_FILTYPE', to_date('2006-07-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom) values
  (seq_kodeliste.nextval, 'DOCX', 'DOCX', 'DOCX', 'Filtype DOCX', 'ARKIV_FILTYPE', to_date('2006-07-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom) values
  (seq_kodeliste.nextval, 'JPEG', 'JPEG', 'JPEG', 'Filtype JPEG', 'ARKIV_FILTYPE', to_date('2006-07-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom) values
  (seq_kodeliste.nextval, 'RTF', 'RTF', 'RTF', 'Filtype RTF', 'ARKIV_FILTYPE', to_date('2006-07-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom) values
  (seq_kodeliste.nextval, 'TIFF', 'TIFF', 'TIFF', 'Filtype TIFF', 'ARKIV_FILTYPE', to_date('2006-07-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom) values
  (seq_kodeliste.nextval, 'XLS', 'XLS', 'XLS', 'Filtype XLS', 'ARKIV_FILTYPE', to_date('2006-07-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom) values
  (seq_kodeliste.nextval, 'XLSX', 'XLSX', 'XLSX', 'Filtype XLSX', 'ARKIV_FILTYPE', to_date('2006-07-01', 'YYYY-MM-DD'));

