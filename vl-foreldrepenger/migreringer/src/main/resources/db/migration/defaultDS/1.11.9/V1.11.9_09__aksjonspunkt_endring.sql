ALTER TABLE AKSJONSPUNKT_DEF MODIFY (NAVN VARCHAR2(70 CHAR));

UPDATE AKSJONSPUNKT_DEF SET NAVN = 'Vurder om ytelse allerede er innvilget'
  , BESKRIVELSE = 'Saksbehandler kontroller opplysninger, kan endre grunnlag, vurderinger eller overstyre før oversending til godkjenning.'
WHERE KODE = '5015';

UPDATE AKSJONSPUNKT_DEF SET NAVN = 'Vurder søkers opplysningsplikt ved ufullstendig/ikke-komplett søknad'
  , BESKRIVELSE = 'Papirsøknad er ikke fullstendig, eller innsendt søknad mangler påkrevd vedlegg.'
WHERE KODE = '5017';

UPDATE AKSJONSPUNKT_DEF SET NAVN = 'Saksbehandler initierer kontroll av søkers opplysningsplikt'
  , BESKRIVELSE = 'Saksbehandler oppretter overstyringspunkt for kontroll av søkers opplysningsplikt '
WHERE KODE = '6002';

UPDATE AKSJONSPUNKT_DEF SET NAVN = 'Varsel om revurdering opprettet manuelt'
  , BESKRIVELSE = 'Ta stilling til om varsel skal sendes etter manuelt opprettet revurdering.'
WHERE KODE = '5026';

UPDATE AKSJONSPUNKT_DEF SET NAVN = 'Vent på fødsel ved avklaring av søkers relasjon til barnet'
  , BESKRIVELSE = 'Vent på registeroppdatering av fødsel, ved avklaring av søkers relasjon til barnet når fødsel er oppgitt og inneværende periode er fra fødsel til 14 dager etter fødsel'
WHERE KODE = '7002';

UPDATE AKSJONSPUNKT_DEF SET NAVN = 'Vent på fødsel ved avklaring av medlemskap'
  , BESKRIVELSE = 'Vent på registeroppdatering av fødsel, ved avklaring av medlemskap når dette er usikkert'
WHERE KODE = '7004';
