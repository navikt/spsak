alter table BEHANDLING_ARSAK add MANUELT_OPPRETTET varchar2(1 char) default 'N' not null;
comment on column BEHANDLING_ARSAK.MANUELT_OPPRETTET is 'Angir om behandlingsårsaken oppstod når en behandling ble manuelt opprettet. Brukes til å utlede om behandlingen ble manuelt opprettet.';
