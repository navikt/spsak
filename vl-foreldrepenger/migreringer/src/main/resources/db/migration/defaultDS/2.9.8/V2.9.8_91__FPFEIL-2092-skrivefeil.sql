-- Retter skrivefeil i KODELISTE
UPDATE KODELISTE SET BESKRIVELSE = '§14-10 andre ledd: Søkt uttak/utsettelse før omsorgsovertakelse' WHERE KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KODE = '4018';

UPDATE KODELISTE SET BESKRIVELSE = '§14-10: sjuende ledd: Hull mellom søknadsperioder etter siste utsettelse' WHERE KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KODE = '4091';

UPDATE KODELISTE SET BESKRIVELSE = '§14-10: sjuende ledd: Hull mellom søknadsperioder etter siste uttak' WHERE KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KODE = '4090';

UPDATE KODELISTE SET BESKRIVELSE = '§14-13 første ledd bokstav a, jf §21-3: Aktivitetskrav - arbeid ikke dokumentert' WHERE KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KODE = '4066';

UPDATE KODELISTE SET BESKRIVELSE = '§14-14 tredje ledd: Unntak for aktivitetskravet, mors mottak av uføretrygd ikke oppfylt' WHERE KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KODE = '4057';

UPDATE KODELISTE SET BESKRIVELSE = '§14-9: Ikke stønadsdager igjen på stønadskonto' WHERE KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KODE = '4002';

-- retter skrivefeil i KODELISTE_NAVN_I18N
UPDATE KODELISTE_NAVN_I18N SET NAVN = '§14-10 andre ledd: Søkt uttak/utsettelse før omsorgsovertakelse' WHERE KL_KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KL_KODE = '4018';

UPDATE KODELISTE_NAVN_I18N SET NAVN = '§14-10: sjuende ledd: Hull mellom søknadsperioder etter siste utsettelse' WHERE KL_KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KL_KODE = '4091';

UPDATE KODELISTE_NAVN_I18N SET NAVN = '§14-10: sjuende ledd: Hull mellom søknadsperioder etter siste uttak' WHERE KL_KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KL_KODE = '4090';

UPDATE KODELISTE_NAVN_I18N SET NAVN = '§14-13 første ledd bokstav a, jf §21-3: Aktivitetskrav - arbeid ikke dokumentert' WHERE KL_KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KL_KODE = '4066';

UPDATE KODELISTE_NAVN_I18N SET NAVN = '§14-14 tredje ledd: Unntak for aktivitetskravet, mors mottak av uføretrygd ikke oppfylt' WHERE KL_KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KL_KODE = '4057';

UPDATE KODELISTE_NAVN_I18N SET NAVN = '§14-9: Ikke stønadsdager igjen på stønadskonto' WHERE KL_KODEVERK = 'IKKE_OPPFYLT_AARSAK' and KL_KODE = '4002';
