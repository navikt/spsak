-- Aksjonspunkter for overstyring
insert into AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, BESKRIVELSE, VILKAR_TYPE, AKSJONSPUNKT_TYPE, SKJERMLENKE_TYPE)
values ('6009', 'Overstyring av fødselsvilkåret for far/medmor', 'VURDERSRB.UT', 'J', 'Opprettes hvis fødselsvilkåret for far/medmor overstyres', 'FP_VK_11', 'OVST', 'PUNKT_FOR_FOEDSEL');

insert into AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, BESKRIVELSE, VILKAR_TYPE, AKSJONSPUNKT_TYPE, SKJERMLENKE_TYPE)
values ('6010', 'Overstyring av adopsjonsvilkåret for foreldrepenger', 'VURDERSRB.UT', 'J', 'Opprettes hvis adopsjonsvilkåret overstyres for foreldrepenger', 'FP_VK_16_1', 'OVST', 'PUNKT_FOR_ADOPSJON');

insert into AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, BESKRIVELSE, VILKAR_TYPE, AKSJONSPUNKT_TYPE, SKJERMLENKE_TYPE)
values ('6011', 'Overstyring av opptjeningsvilkåret', 'VURDER_OPPTJ.UT', 'J', 'Opprettes hvis opptjeningsvilkåret overstyres', 'FP_VK_23', 'OVST', 'FAKTA_FOR_OPPTJENING');

-- Avslagsårsak 1051- Stebarnsadopsjon ikke flere dager igjen
insert into KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, '1051', 'Stebarnsadopsjon ikke flere dager igjen', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));

INSERT INTO KODELISTE_RELASJON(ID, KODEVERK1, KODE1, KODEVERK2, KODE2, GYLDIG_FOM, GYLDIG_TOM)
values(seq_kodeliste_relasjon.nextval, 'VILKAR_TYPE', 'FP_VK_16_1', 'AVSLAGSARSAK', '1051', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));

-- Fjerne kobling mellom avslagsårsaker og vilkår som ikke hører sammen
DELETE FROM KODELISTE_RELASJON
where KODEVERK2 = 'AVSLAGSARSAK'
and KODE1 = 'FP_VK_16_1'
and KODE2 = '1005';

-- Relasjon mellom vilkår FP_VK_21 og aksjonspunkt 1035
INSERT INTO KODELISTE_RELASJON(ID, KODEVERK1, KODE1, KODEVERK2, KODE2, GYLDIG_FOM, GYLDIG_TOM)
values(seq_kodeliste_relasjon.nextval, 'VILKAR_TYPE', 'FP_VK_21', 'AVSLAGSARSAK', '1035',
to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));

-- Endre navn så far vises med liten forbokstav i GUI
update KODELISTE
set navn = 'Fødselsvilkår far', beskrivelse = 'Fødselsvilkår far'
where kode = 'FP_VK_11';
