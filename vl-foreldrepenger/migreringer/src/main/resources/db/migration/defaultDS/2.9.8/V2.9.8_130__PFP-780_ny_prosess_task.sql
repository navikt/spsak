insert into PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, beskrivelse)
values ('oppgavebehandling.opprettOppgaveSakTilInfotrygd', 'Oppgave i GSAK for å ta over saken', 3, 'Saker der skjæringstidspunkt inntreffer før 2019-01-01 må behandles av Infrotrygd da VL ikke besitter gamle bereningsregler.');

insert into KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn) values (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'AVSLAGSARSAK',	'1099',	'NB',	'Ingen beregningsregler tilgjengelig i løsningen');
