-- Steg "Kontroller fakta"/UT: Aksjonspunkter for faktakontroll som må avklares FØR vilkårsvurdering
UPDATE AKSJONSPUNKT_DEF SET
  VURDERINGSPUNKT = 'KOFAK.UT'
WHERE NAVN IN (
  'Avklar tilleggsopplysninger'
);
