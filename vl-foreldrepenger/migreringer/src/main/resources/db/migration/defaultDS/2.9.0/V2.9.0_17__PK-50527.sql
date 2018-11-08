alter table BEHANDLING add AAPNET_FOR_ENDRING varchar2(1 char) default 'N' not null;
comment on column BEHANDLING.AAPNET_FOR_ENDRING is 'Flagget settes når menyvalget "Åpne behandling for endringer" kjøres.';

alter table AKSJONSPUNKT add REVURDERING varchar2(1 char) default 'N' not null;
comment on column AKSJONSPUNKT.REVURDERING is 'Flagget settes på aksjonspunkter som kopieres i det en revurdering opprettes. Trengs for å kunne vurdere om aksjonspunktet er kandidat for totrinnskontroll dersom det har blitt en endring i aksjonspunktet under revurderingen.';
