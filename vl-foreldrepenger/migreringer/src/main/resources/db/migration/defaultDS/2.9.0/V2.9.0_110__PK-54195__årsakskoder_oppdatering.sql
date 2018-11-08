-- kodeliste INNVILGET_AARSAK
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '2023', '§14-12 tredje ledd: Overføring oppfylt, søker har aleneomsorg for barnet', '§14-12 tredje ledd: Overføring oppfylt, søker har aleneomsorg for barnet', 'INNVILGET_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-12"}}}');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '2010', '§14-11 første ledd bokstav a: Gyldig utsettelse pga. ferie', '§14-11 første ledd bokstav a: Gyldig utsettelse pga. ferie', 'INNVILGET_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-11"}}}');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '2011', '§14-11 første ledd bokstav b: Gyldig utsettelse pga. 100% arbeid', '§14-11 første ledd bokstav b: Gyldig utsettelse pga. 100% arbeid', 'INNVILGET_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-11"}}}');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '2012', '§14-11 første ledd bokstav c: Gyldig utsettelse pga. innleggelse', '§14-11 første ledd bokstav c: Gyldig utsettelse pga. innleggelse', 'INNVILGET_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-11"}}}');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '2013', '§14-11 første ledd bokstav d: Gyldig utsettelse pga. barn innlagt', '§14-11 første ledd bokstav d: Gyldig utsettelse pga. barn innlagt', 'INNVILGET_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-11"}}}');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '2014', '§14-11 første ledd bokstav c: Gyldig utsettelse pga. sykdom', '§14-11 første ledd bokstav c: Gyldig utsettelse pga. sykdom', 'INNVILGET_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-11"}}}');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '2020', '§14-12 tredje ledd: Overføring oppfylt, annen part har ikke rett til foreldrepenge', '§14-12 tredje ledd: Overføring oppfylt, annen part har ikke rett til foreldrepenge', 'INNVILGET_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-12"}}}');


-- kodeliste IKKE_OPPFYLT_AARSAK
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '4092', '§14-12: Avslag overføring - har ikke aleneomsorg for barnet', '§14-12: Avslag overføring - har ikke aleneomsorg for barnet', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-12"}}}');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '4093', '§14-16: Avslag gradering - søker er ikke i arbeid', '§14-16: Avslag gradering - søker er ikke i arbeid', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-16"}}}');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '4091', '§14-10: sjuende ledd: Hull mellom søknadsperiod etter siste utsettelse', '§14-10: sjuende ledd: Hull mellom søknadsperiod etter siste utsettelse', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-10"}}}');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '4090', '§14-10: sjuende ledd: Hull mellom søknadsperiod etter siste uttak', '§14-10: sjuende ledd: Hull mellom søknadsperiod etter siste uttak', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-10"}}}');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '4094', '§14-16 femte ledd, jf §21-3: Avslag graderingsavtale mangler - ikke dokumentert', '§14-16 femte ledd, jf §21-3: Avslag graderingsavtale mangler - ikke dokumentert', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-16,21-3"}}}');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '4088', '§14-13 første ledd bokstav f, jf §21-3: Aktivitetskrav – introprogram ikke dokumentert', '§14-13 første ledd bokstav f, jf §21-3: Aktivitetskrav – introprogram ikke dokumentert', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-13,21-3"}}}');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '4089', '§14-13 første ledd bokstav g, jf §21-3: Aktivitetskrav – KVP ikke dokumentert', '§14-13 første ledd bokstav g, jf §21-3: Aktivitetskrav – KVP ikke dokumentert', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-13,21-3"}}}');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '4087', '§14-2: Opphør medlemsskap', '§14-2: Opphør medlemsskap', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-2"}}}');


-- Opprydding
UPDATE kodeliste
SET kode = '4018', navn = '§14-10 andre ledd: Søkt uttak/utsettelse før omsorgsovertaksels', beskrivelse = '§14-10 andre ledd: Søkt uttak/utsettelse før omsorgsovertaksels', ekstra_data = '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-10"}}}'
WHERE kodeverk = 'IKKE_OPPFYLT_AARSAK' and kode = '4504';

UPDATE kodeliste
SET kodeverk = 'GRADERING_AVSLAG_AARSAK', kode = '4519'
WHERE kodeverk = 'IKKE_OPPFYLT_AARSAK' and kode = '4019';


--UPDATES
UPDATE kodeliste
SET ekstra_data = '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-16"}}}'
WHERE kodeverk = 'GRADERING_AVSLAG_AARSAK' and kode = '4504';

UPDATE kodeliste
SET navn = '§14-11 første ledd bokstav d, jf §21-3: Utsettelse barnets innleggelse - ikke dokumentert', beskrivelse = '§14-11 første ledd bokstav d, jf §21-3: Utsettelse barnets innleggelse - ikke dokumentert', ekstra_data = '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-11,21-3"}}}'
WHERE kodeverk = 'IKKE_OPPFYLT_AARSAK' and kode = '4065';

UPDATE kodeliste
SET navn = '§14-12 tredje ledd: Ikke rett til kvote fordi mor ikke har rett til foreldrepenger', beskrivelse = '§14-12 tredje ledd: Ikke rett til kvote fordi mor ikke har rett til foreldrepenger', ekstra_data = '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-12"}}}'
WHERE kodeverk = 'IKKE_OPPFYLT_AARSAK' and kode = '4073';

UPDATE kodeliste
SET navn = '§14-12 tredje ledd, jf §21-3: Avslag overføring kvote pga. sykdom/skade/innleggelse ikke dokumentert', beskrivelse = '§14-12 tredje ledd, jf §21-3: Avslag overføring kvote pga. sykdom/skade/innleggelse ikke dokumentert', ekstra_data = '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-12,21-3"}}}'
WHERE kodeverk = 'IKKE_OPPFYLT_AARSAK' and kode = '4074';

UPDATE kodeliste
SET ekstra_data = '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-16"}}}'
WHERE kodeverk = 'IKKE_OPPFYLT_AARSAK' and kode = '4080';

UPDATE kodeliste
SET ekstra_data = '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-10,14-11"}}}'
WHERE kodeverk = 'IKKE_OPPFYLT_AARSAK' and kode = '4086';
