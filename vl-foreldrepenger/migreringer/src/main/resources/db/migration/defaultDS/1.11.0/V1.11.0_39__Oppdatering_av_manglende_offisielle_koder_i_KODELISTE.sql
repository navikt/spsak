-- Sjekket opp mot https://modapp.adeo.no/kodeverksklient/allekodeverk og T11 for å validere at internkode er den samme som i det offisielle kodeverket
UPDATE KODELISTE
SET OFFISIELL_KODE = KODE
WHERE id IN (
    SELECT 
      kl.id
    FROM KODELISTE kl
      INNER JOIN KODEVERK kv ON kv.KODE = kl.KODEVERK
    WHERE 
      kv.KODEVERK_EIER      = 'Kodeverkforvaltning'
      AND kl.KODEVERK       IN ('DOKUMENT_TYPE', 'DISKRESJONSKODE', 'OPPLYSNING_ADRESSE_TYPE', 'SIVILSTAND_TYPE', 'ARKIV_FILTYPE', 'SPRAAK_KODE', 'RELASJONSROLLE_TYPE')
      AND kl.OFFISIELL_KODE IS NULL
      AND kl.KODE NOT       IN ('-','ANNET_DOKUMENT','ANPA','HOVS','MMOR') -- Koder som ikke finnes i det offisielle kodeverket
  );
