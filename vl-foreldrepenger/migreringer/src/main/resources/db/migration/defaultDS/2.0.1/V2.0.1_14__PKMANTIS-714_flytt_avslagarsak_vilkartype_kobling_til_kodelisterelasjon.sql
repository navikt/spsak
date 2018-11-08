-- Oppdater kodeliste_relasjon tabell med data fra VILKAR_TYPE_AVSLAGSARSAK_KOBLE.
INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2)
    SELECT seq_kodeliste_relasjon.nextval, vtak.KL_VILKAR_TYPE, vtak.VILKAR_TYPE_KODE, vtak.KL_AVSLAGSARSAK, vtak.AVSLAGSARSAK_KODE
      FROM VILKAR_TYPE_AVSLAGSARSAK_KOBLE vtak;

-- Fjern tabell som ikke lenger skal være i bruk
DROP TABLE VILKAR_TYPE_AVSLAGSARSAK_KOBLE;
