insert into KONFIG_VERDI_KODE (KODE, NAVN, KONFIG_GRUPPE, KONFIG_TYPE, BESKRIVELSE)
values ('vise.detaljerte.feilmeldinger', 'Om detaljerte feilmeldinger skal vises i GUI', 'INGEN', 'BOOLEAN',
        'Boolean som angir om nedetidsboksen på søkesiden skal vises, og om det skal vises en link til detaljene på feilmeldinger på den røde linja øverst.');

insert into KONFIG_VERDI (ID, KONFIG_KODE, KONFIG_GRUPPE, KONFIG_VERDI, GYLDIG_FOM)
values (SEQ_KONFIG_VERDI.NEXTVAL, 'vise.detaljerte.feilmeldinger', 'INGEN', 'true', to_date('01.01.2017', 'dd.mm.yyyy'));
