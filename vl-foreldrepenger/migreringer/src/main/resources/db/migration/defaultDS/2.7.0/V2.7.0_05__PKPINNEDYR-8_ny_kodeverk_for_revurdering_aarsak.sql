-- Nytt kodeverk for årsak til revurdering
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
values ('REVURDERING_VARSLING_AARSAK', 'N', 'N', 'Årsak til revurdering', 'Kodeverk for årsak til revurdering');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'BARNIKKEREG', 'Barn er ikke registrert i folkeregisteret', 'Barn er ikke registrert i folkeregisteret', 'REVURDERING_VARSLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'JOBBFULLTID', 'Arbeid i stønadsperioden', 'Arbeid i stønadsperioden', 'REVURDERING_VARSLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'IKKEOPPTJENT', 'Beregningsgrunnlaget er under 1/2 G', 'Beregningsgrunnlaget er under 1/2 G', 'REVURDERING_VARSLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'UTVANDRET', 'Bruker er registrert utvandret', 'Bruker er registrert utvandret', 'REVURDERING_VARSLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'JOBBUTLAND', 'Arbeid i utlandet', 'Arbeid i utlandet', 'REVURDERING_VARSLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'IKKEOPPHOLD', 'Ikke lovlig opphold', 'Ikke lovlig opphold', 'REVURDERING_VARSLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'JOBB6MND', 'Opptjeningsvilkår ikke oppfylt', 'Opptjeningsvilkår ikke oppfylt', 'REVURDERING_VARSLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'AKTIVITET', 'Mors aktivitetskrav er ikke oppfylt', 'Mors aktivitetskrav er ikke oppfylt', 'REVURDERING_VARSLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'ANNET', 'Annet', 'Annet', 'REVURDERING_VARSLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
