-- Retter skrivefeil i KODELISTE
UPDATE KODELISTE SET BESKRIVELSE = '§14-10 femte ledd: Arbeider i uttaksperioden mer enn 0%' WHERE KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KODE = '4023';
UPDATE KODELISTE SET BESKRIVELSE = '§14-10 femte ledd: Arbeider i uttaksperioden mer enn 0%' WHERE KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KODE = '4023';

-- retter skrivefeil i KODELISTE_NAVN_I18N
UPDATE KODELISTE_NAVN_I18N SET NAVN = '§14-13 første ledd bokstav f: Aktivitetskravet mors deltakelse på introduksjonsprogram ikke oppfylt' WHERE KL_KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KL_KODE = '4055';
UPDATE KODELISTE_NAVN_I18N SET NAVN = '§14-13 første ledd bokstav f: Aktivitetskravet mors deltakelse på introduksjonsprogram ikke oppfylt' WHERE KL_KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KL_KODE = '4055';
