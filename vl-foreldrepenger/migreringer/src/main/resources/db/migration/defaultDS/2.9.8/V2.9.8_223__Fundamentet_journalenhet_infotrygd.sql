-- Journalførende enhet - for å kunne legge vurder dokument til enheten som har sendt til scanning
ALTER TABLE MOTTATT_DOKUMENT ADD JOURNAL_ENHET varchar2(10 char);

COMMENT ON COLUMN MOTTATT_DOKUMENT.JOURNAL_ENHET IS 'Journalførende enhet fra forside dersom satt';

-- Håndtere feilsituasjoner der Infotrygd returnerer ukjente koder og kodeverksdrevet oppslag
update kodeliste set offisiell_kode=kode
where kode<>'-' and OFFISIELL_KODE is null
      and kodeverk in ('RELATERT_YTELSE_RESULTAT', 'RELATERT_YTELSE_SAKSTYPE', 'RELATERT_YTELSE_STATUS',
                       'RELATERT_YTELSE_TEMA', 'TEMA_UNDERKATEGORI') ;
