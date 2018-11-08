UPDATE BEHANDLING_STEG_TYPE SET
  NAVN = 'Innhent registeropplysninger',
  BESKRIVELSE = 'Innhenting av registeropplysninger som vil benyttes til avklaring av fakta og vurdering av saken'
where KODE = 'INREG';

UPDATE VURDERINGSPUNKT_DEF SET
  NAVN = 'Innhent registeropplysninger - Inngang'
WHERE KODE = 'INREG.INN';

UPDATE VURDERINGSPUNKT_DEF SET
  NAVN = 'Innhent registeropplysninger - Utgang'
WHERE KODE = 'INREG.UT';