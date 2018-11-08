
-- Legger til innsendingsårsak i kodeverk
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
values ('INNTEKTSMELDING_INNSENDINGSAARSAK', 'N', 'N', 'Inntektsmelding innsendingsårsak', 'Begrunnelse for innsending av inntektsmelding');

--Legger til årsaker i kodeliste
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'NY', 'NY', 'Ny inntektsmelding', to_date('2000-01-01', 'YYYY-MM-DD'),
        'INNTEKTSMELDING_INNSENDINGSAARSAK');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'ENDRING', 'ENDRING', 'Endret inntektsmelding', to_date('2000-01-01', 'YYYY-MM-DD'),
        'INNTEKTSMELDING_INNSENDINGSAARSAK');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, '-', 'UDEFINERT', 'Udefinert inntektsmelding', to_date('2000-01-01', 'YYYY-MM-DD'),
        'INNTEKTSMELDING_INNSENDINGSAARSAK');


-- Legger til innsendingsårsak på inntektsmelding
alter table IAY_INNTEKTSMELDING add INNSENDINGSAARSAK varchar2(100 char) default '-' not null;
alter table IAY_INNTEKTSMELDING add KL_INNSENDINGSAARSAK varchar2(100 char) as ('INNTEKTSMELDING_INNSENDINGSAARSAK');
alter table IAY_INNTEKTSMELDING add constraint FK_IAY_INNTEKTSMELDING_1 foreign key (KL_INNSENDINGSAARSAK, INNSENDINGSAARSAK) references KODELISTE (KODEVERK, KODE);
create index IDX_IAY_INNTEKTSMELDING_7 on IAY_INNTEKTSMELDING(INNSENDINGSAARSAK);

update STARTPUNKT_TYPE set BEHANDLING_STEG='KOFAKBER' where KODE='BEREGNING';

