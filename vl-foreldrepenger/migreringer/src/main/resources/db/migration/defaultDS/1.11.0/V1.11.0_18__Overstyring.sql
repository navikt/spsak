alter table VILKAR add OPPRINNELIG_VILKAR_UTFALL varchar2(30 char);
alter table VILKAR add constraint FK_VILKAR_6 foreign key (KL_VILKAR_UTFALL_TYPE, OPPRINNELIG_VILKAR_UTFALL) references KODELISTE (KODEVERK, KODE);
create index IDX_VILKAR_4 on VILKAR(OPPRINNELIG_VILKAR_UTFALL);
comment on column VILKAR.OPPRINNELIG_VILKAR_UTFALL is 'FK: KODELISTE. Opprinnelig (første) utfall dersom utfallet er overstyrt. Vil ikke endres ved påfølgende overstyringer.';

alter table BEREGNING add OPPR_BEREGNET_TILKJENT_YTELSE number(10,0);
comment on column BEREGNING.OPPR_BEREGNET_TILKJENT_YTELSE is 'Opprinnelig beregnet tilkjent ytelse dersom beregningen er overstyrt.';

insert into AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, BESKRIVELSE, VILKAR_TYPE, AKSJONSPUNKT_TYPE)
values ('6003', 'Overstyring av fødselsvilkåret', 'VURDERBV.UT', 'J', 'Opprettes hvis fødselsvilkåret overstyres', 'FP_VK_1', 'OVST');

insert into AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, BESKRIVELSE, VILKAR_TYPE, AKSJONSPUNKT_TYPE)
values ('6004', 'Overstyring av adopsjonsvilkåret', 'VURDERBV.UT', 'J', 'Opprettes hvis adopsjonsvilkåret overstyres', 'FP_VK_4', 'OVST');

insert into AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, BESKRIVELSE, VILKAR_TYPE, AKSJONSPUNKT_TYPE)
values ('6005', 'Overstyring av medlemskapsvilkåret', 'VURDERMV.UT', 'J', 'Opprettes hvis medlemskapsvilkåret overstyres', 'FP_VK_2', 'OVST');

insert into AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, BESKRIVELSE, VILKAR_TYPE, AKSJONSPUNKT_TYPE)
values ('6006', 'Overstyring av søknadsfristvilkåret', 'VURDERSFV.UT', 'J', 'Opprettes hvis søknadsfristvilkåret overstyres', 'FP_VK_3', 'OVST');

insert into AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, BESKRIVELSE, VILKAR_TYPE, AKSJONSPUNKT_TYPE)
values ('6007', 'Overstyring av beregning', 'BERYT.INN', 'J', 'Opprettes hvis beregningen overstyres', '-', 'OVST');

update KODELISTE set BESKRIVELSE = 'Et punkt som er laget automatisk og løses manuelt' where KODE = 'MANU' and KODEVERK = 'AKSJONSPUNKT_TYPE';
update KODELISTE set BESKRIVELSE = 'Et punkt som er laget automatisk og løses automatisk' where KODE = 'AUTO' and KODEVERK = 'AKSJONSPUNKT_TYPE';
update KODELISTE set BESKRIVELSE = 'Et punkt som er laget manuelt og løses manuelt. Krever overstyringsrollen.' where KODE = 'OVST' and KODEVERK = 'AKSJONSPUNKT_TYPE';
insert into KODELISTE (ID, KODE, OFFISIELL_KODE, NAVN, KODEVERK, GYLDIG_FOM, BESKRIVELSE) values (seq_kodeliste.nextval, 'SAOV', 'Saksbehandleroverstyring', 'Saksbehandleroverstyring', 'AKSJONSPUNKT_TYPE', to_date('2000-01-01', 'yyyy-mm-dd'), 'Et punkt som er laget manuelt og løses manuelt. Krever ikke overstyringsrollen.');
update AKSJONSPUNKT_DEF set AKSJONSPUNKT_TYPE = 'SAOV' where KODE = '6002';
