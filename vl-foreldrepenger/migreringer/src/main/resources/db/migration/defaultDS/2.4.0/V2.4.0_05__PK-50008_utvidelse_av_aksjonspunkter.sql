alter table AKSJONSPUNKT add AKTIVT varchar2(1) default 'J' not null;
alter table AKSJONSPUNKT add MANUELT_OPPRETTET varchar2(1) default 'N' not null;

update AKSJONSPUNKT set MANUELT_OPPRETTET = 'J' where AKSJONSPUNKT_DEF in (
  select KODE from AKSJONSPUNKT_DEF where AKSJONSPUNKT_TYPE in ('OVST', 'SAOV'));

comment on column AKSJONSPUNKT.AKTIVT is 'Angir om aksjonspunktet er aktivt. Inaktive aksjonspunkter er historiske som ble kopiert når en revurdering ble opprettet. De eksisterer for å kunne vise den opprinnelige begrunnelsen, uten at saksbehandler må ta stilling til det på nytt.';
comment on column AKSJONSPUNKT.MANUELT_OPPRETTET is 'Angir om aksjonspunktet ble opprettet manuelt. Typisk skjer dette ved overstyring, og når saksbehandler manuelt reaktiverer et historisk aksjonspunkt i en revurdering. Brukes når Behandlingskontroll skal rydde ved hopp.';
