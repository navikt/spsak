Delete from BEHANDLING_TYPE_STEG_SEKV
where BEHANDLING_STEG_TYPE = 'VRSLREV'
      and   FAGSAK_YTELSE_TYPE = 'FP';

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT,SKJERMLENKE_TYPE)
VALUES ('5055','Kontroller revurderingsbehandling','FORVEDSTEG.UT',
        'Generelt aksjonspunkt for å kontrollere revurderingsbehandling, slik at saksbehandler for eksempel kan sende varslingsbrev','-','N','-');

update DOKUMENT_MAL_TYPE set NAVN ='Varsel om revurdering' where DOKSYS_KODE = 000058;
COMMIT;
