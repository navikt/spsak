-- Hvis schema har andre navn enn FPSAK og FPSAK_HIST så må sql endres
-- FPSAK_HIST må ha SELECT tilgang på AKSJONSPUNKT_DEF og KODELISTE i SCHEMA FPSAK 

grant SELECT on AKSJONSPUNKT_DEF to FPSAK_HIST;
grant SELECT on KODELISTE to FPSAK_HIST;
grant SELECT on BEHANDLING_STEG_TYPE to FPSAK_HIST;
grant SELECT on VURDERINGSPUNKT_DEF to FPSAK_HIST;
grant SELECT on KODELISTE_NAVN_I18N to FPSAK_HIST;

--------------------------------------------------------
--  DDL for View AKSJONSPUNKT_DEF_VIEW
--------------------------------------------------------

CREATE OR REPLACE FORCE VIEW FPSAK_HIST.AKSJONSPUNKT_DEF_VIEW (AKSJONSPUNKT_DEF_KODE, AKSJONSPUNKT_DEF_NAVN, AKSJONSPUNKT_DEF_BESKRIVELSE, VURDERINGSPUNKT, AKSJONSPUNKT_TYPE, OPPRETTET_DATO, ENDRET_DATO) AS 
  SELECT 
  ad.KODE , 
  ad.NAVN , 
  ad.BESKRIVELSE , 
  ad.VURDERINGSPUNKT, 
  ad.AKSJONSPUNKT_TYPE, 
  ad.OPPRETTET_TID, 
  ad.ENDRET_TID
FROM FPSAK.Aksjonspunkt_Def ad;


--------------------------------------------------------
--  DDL for View KODEVERK_VIEW
--------------------------------------------------------

CREATE OR REPLACE FORCE VIEW FPSAK_HIST.KODEVERK_VIEW (KODE, NAVN, BESK, NAV_KODE, TYPE_KODE, OPPRETTET_DATO, ENDRET_DATO, VIRKNING_FRA_DATO, VIRKNING_TIL_DATO) AS
  SELECT
    kl.KODE,
    KI.NAVN,
    kl.BESKRIVELSE,
    kl.OFFISIELL_KODE,
    kl.KODEVERK TYPE_KODE,
    kl.OPPRETTET_TID,
    kl.ENDRET_TID,
    kl.GYLDIG_FOM,
    kl.GYLDIG_TOM
  FROM FPSAK.Kodeliste kl LEFT JOIN FPSAK.KODELISTE_NAVN_I18N KI ON kl.KODE = KI.KL_KODE AND kl.KODEVERK = KI.KL_KODEVERK
  WHERE KI.SPRAK = 'NB';

--------------------------------------------------------
--  DDL for View BEHANDLING_STEG_TYPE_VIEW
--------------------------------------------------------
CREATE OR REPLACE VIEW FPSAK_HIST.BEHANDLING_STEG_TYPE_VIEW AS 
SELECT 
    KODE,
    NAVN,
    BEHANDLING_STATUS_DEF,
    BESKRIVELSE,
    OPPRETTET_AV,
    OPPRETTET_TID,
    ENDRET_AV,
    ENDRET_TID 
FROM FPSAK.BEHANDLING_STEG_TYPE; 

--------------------------------------------------------
--  DDL for View VURDERINGSPUNKT_DEF_VIEW
--------------------------------------------------------
CREATE OR REPLACE VIEW FPSAK_HIST.VURDERINGSPUNKT_DEF_VIEW AS 
SELECT 
    KODE, 
    BEHANDLING_STEG,
    VURDERINGSPUNKT_TYPE,
    NAVN,
    BESKRIVELSE,
    OPPRETTET_AV,
    OPPRETTET_TID, 
    ENDRET_AV, 
    ENDRET_TID 
 FROM FPSAK.VURDERINGSPUNKT_DEF;
