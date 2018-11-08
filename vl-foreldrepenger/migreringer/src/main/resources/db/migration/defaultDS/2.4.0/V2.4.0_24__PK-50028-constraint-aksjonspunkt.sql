--guard for at det ikke opprettes både aktive og inaktive aksjonspunkter av samme aksjonspunkt-def på samme behandling

ALTER TABLE AKSJONSPUNKT
  ADD CONSTRAINT CHK_UNIQUE_BEH_AD UNIQUE (BEHANDLING_ID, AKSJONSPUNKT_DEF);


