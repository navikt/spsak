-- Ta bort eksisterende referanser til kodeverket
alter TABLE MOTTATT_DOKUMENT DROP CONSTRAINT FK_MOTTATTE_DOKUMENT_80;
ALTER TABLE MOTTATT_DOKUMENT DROP COLUMN KL_DOKUMENT_TYPE;

-- Rydd opp i kodeverket
INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('DOKUMENT_TYPE_ID',  'DokumentTypeId-er','Typen til et mottatt dokument. Dette er et subset av DokumentTyper; inngående dokumenter, for eksempel søknad, terminbekreftelse o.l', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/DokumentTypeId-er',  2 , 'DokumentTypeId-er','J', 'N');

update KODELISTE set  kodeverk = 'DOKUMENT_TYPE_ID', kode ='SØKNAD_ENGANGSSTØNAD_FØDSEL', beskrivelse = 'Søknad om engangsstønad ved fødsel' where kodeverk  = 'DOKUMENT_TYPE' and OFFISIELL_KODE = 'I000003';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE_ID', kode ='KLAGE_DOKUMENT', beskrivelse = 'Klage/anke' where kodeverk  = 'DOKUMENT_TYPE' and OFFISIELL_KODE = 'I000027';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE_ID', kode ='BRUKEROPPLASTET_DOKUMENTASJON', beskrivelse = 'Brukeropplastet dokumentasjon' where kodeverk  = 'DOKUMENT_TYPE' and OFFISIELL_KODE = 'I000047';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE_ID', kode ='SØKNAD_ENGANGSSTØNAD_ADOPSJON', beskrivelse = 'Søknad om engangsstønad ved adopsjon' where kodeverk  = 'DOKUMENT_TYPE' and OFFISIELL_KODE = 'I000004';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE_ID', kode ='DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL', beskrivelse = 'Dokumentasjon av termindato (lev. kun av mor), fødsel eller dato for omsorgsovertakelse' where kodeverk  = 'DOKUMENT_TYPE' and OFFISIELL_KODE = 'I000041';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE_ID', kode ='DOKUMENTASJON_AV_OMSORGSOVERTAKELSE', beskrivelse = 'Dokumentasjon av dato for overtakelse av omsorg' where kodeverk  = 'DOKUMENT_TYPE' and OFFISIELL_KODE = 'I000042';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE_ID', kode ='BEKREFTELSE_VENTET_FØDSELSDATO', beskrivelse = 'Bekreftelse på ventet fødselsdato' where kodeverk  = 'DOKUMENT_TYPE' and OFFISIELL_KODE = 'I000062';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE_ID', kode ='KVITTERING_DOKUMENTINNSENDING', beskrivelse = 'Kvittering dokumentinnsending' where kodeverk  = 'DOKUMENT_TYPE' and OFFISIELL_KODE = 'I000046';


update KODELISTE set  kodeverk = 'DOKUMENT_TYPE_ID' where kodeverk  = 'DOKUMENT_TYPE' and KODE = '-';

DELETE FROM  KODELISTE where kodeverk = 'DOKUMENT_TYPE' and kode = 'ANNET_DOKUMENT';

DELETE FROM kodeverk where kode='DOKUMENT_TYPE';

INSERT INTO kodeverk (kode, navn, kodeverk_eier, kodeverk_eier_ver, kodeverk_eier_navn, beskrivelse)
VALUES ('DOKUMENT_TYPE', 'DokumentType', 'Dokkat', '2', 'Dokumenttyper','Unik identifikator av dokumenttype på tvers av fagsystemer. Finnes ikke i felles kodeverk, men kan hentes fra et av endepunktene her: https://fasit.adeo.no/search/DokumenttypeInfo_v3?type=resource');

update KODELISTE set  kodeverk = 'DOKUMENT_TYPE', kode ='INNHENT', beskrivelse = 'Innhente opplysninger' where kodeverk  = 'DOKUMENT_TYPE_WORKAROUND' and OFFISIELL_KODE = '000049';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE', kode ='KLAGE_OVERFØRT', beskrivelse = 'Overføring til NAV Klageinstans' where kodeverk  = 'DOKUMENT_TYPE_WORKAROUND' and OFFISIELL_KODE = '000060';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE', kode ='KLAG_NY_BEH', beskrivelse = 'Vedtak opphevet, sendt til ny behandling' where kodeverk  = 'DOKUMENT_TYPE_WORKAROUND' and OFFISIELL_KODE = '000059';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE', kode ='KLAGE_STADFESTET', beskrivelse = 'Vedtak om stadfestelse' where kodeverk  = 'DOKUMENT_TYPE_WORKAROUND' and OFFISIELL_KODE = '000055';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE', kode ='KLAGE_AVVIST', beskrivelse = 'Vedtak om avvist klage' where kodeverk  = 'DOKUMENT_TYPE_WORKAROUND' and OFFISIELL_KODE = '000054';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE', kode ='FORLENGET_SAKSBEHANDLINGSTID', beskrivelse = 'Forlenget saksbehandlingstid' where kodeverk  = 'DOKUMENT_TYPE_WORKAROUND' and OFFISIELL_KODE = '000056';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE', kode ='REVURDERING', beskrivelse = 'Varsel om revurdering' where kodeverk  = 'DOKUMENT_TYPE_WORKAROUND' and OFFISIELL_KODE = '000058';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE', kode ='UENDRET_UTFALL', beskrivelse = 'Informasjon om ingen endring etter revurdering' where kodeverk  = 'DOKUMENT_TYPE_WORKAROUND' and OFFISIELL_KODE = '000052';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE', kode ='AVSLAG', beskrivelse = 'Vedtak om avslag på engangsstønad' where kodeverk  = 'DOKUMENT_TYPE_WORKAROUND' and OFFISIELL_KODE = '000051';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE', kode ='HENLEGGELSE', beskrivelse = 'Informasjon om at saken er henlagt' where kodeverk  = 'DOKUMENT_TYPE_WORKAROUND' and OFFISIELL_KODE = '000050';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE', kode ='POSITIVT_VEDTAK', beskrivelse = 'Vedtak om engangsstønad' where kodeverk  = 'DOKUMENT_TYPE_WORKAROUND' and OFFISIELL_KODE = '000048';
update KODELISTE set  kodeverk = 'DOKUMENT_TYPE' where kodeverk  = 'DOKUMENT_TYPE_WORKAROUND' and KODE = '-';

DELETE FROM kodeverk where kode='DOKUMENT_TYPE_WORKAROUND';

-- Ta i bruk oppryddet kodeverk

update mottatt_dokument set type='SØKNAD_ENGANGSSTØNAD_FØDSEL' where type='I000003';
update mottatt_dokument set type='KLAGE_DOKUMENT' where type='I000027';
update mottatt_dokument set type='BRUKEROPPLASTET_DOKUMENTASJON' where type='I000047';
update mottatt_dokument set type='SØKNAD_ENGANGSSTØNAD_ADOPSJON' where type='I000004';
update mottatt_dokument set type='DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL' where type='I000041';
update mottatt_dokument set type='DOKUMENTASJON_AV_OMSORGSOVERTAKELSE' where type='I000042';

ALTER TABLE MOTTATT_DOKUMENT ADD KL_DOKUMENT_TYPE_ID VARCHAR(100) generated always as ('DOKUMENT_TYPE_ID') virtual;
ALTER TABLE MOTTATT_DOKUMENT ADD CONSTRAINT FK_MOTTATT_DOKUMENT_01 FOREIGN KEY (type, KL_DOKUMENT_TYPE_ID) REFERENCES KODELISTE(kode, kodeverk);

-- Avslutt med en liten fiks på navnet til eksisterende FK

alter TABLE MOTTATT_DOKUMENT DROP CONSTRAINT FK_MOTTATTE_DOKUMENT_2;
ALTER TABLE MOTTATT_DOKUMENT ADD CONSTRAINT FK_MOTTATT_DOKUMENT_02 FOREIGN KEY (BEHANDLING_ID) REFERENCES BEHANDLING(ID);


