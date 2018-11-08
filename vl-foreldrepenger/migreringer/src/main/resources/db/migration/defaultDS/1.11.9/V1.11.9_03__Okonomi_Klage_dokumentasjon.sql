-- OPPDRAG_KONTROLL
COMMENT ON TABLE OPPDRAG_KONTROLL IS 'Inneholder kvittering/svar melding fra Økonomi';
COMMENT ON COLUMN OPPDRAG_KONTROLL.VENTER_KVITTERING IS 'om oppdragskvittering er mottatt eller ikke';

-- OKO_OPPDRAG_ENHET_120
COMMENT ON TABLE OKO_OPPDRAG_ENHET_120 IS 'Inneholder de relevante verdier som mappes og sender med i request melding. (Tabell fra Økonomi)';
COMMENT ON COLUMN OKO_OPPDRAG_ENHET_120.TYPE_ENHET IS 'Angir hva slags type enhet som mottas (f.eks bosted, behandlende)';
COMMENT ON COLUMN OKO_OPPDRAG_ENHET_120.ENHET IS 'Identifiserer den aktuelle enheten (tknr evt. orgnr + avd)';
COMMENT ON COLUMN OKO_OPPDRAG_ENHET_120.DATO_ENHET_FOM IS 'Angir når ny oppdragsenhet gjelder fra';

-- OKO_OPPDRAG_110
COMMENT ON TABLE OKO_OPPDRAG_110 IS 'Inneholder de relevante verdier som mappes og sender med i request melding. (Tabell fra Økonomi)';
COMMENT ON COLUMN OKO_OPPDRAG_110.KODE_AKSJON IS 'Aksjonskode 1 betyr at oppdragssystemet skal oppdateres. Aksjonskode 3 medfører en reell beregning, men det foretas ingen oppdatering av systemet.';
COMMENT ON COLUMN OKO_OPPDRAG_110.KODE_ENDRING IS ' benyttes for at systemet skal vite om det er et nytt oppdrag, endring i eksisterende oppdrag eller om det ikke er endring i informasjonen på oppdragsnivå (ny, endring, uendret)';
COMMENT ON COLUMN OKO_OPPDRAG_110.KODE_FAGOMRADE IS 'Fagrutine';
COMMENT ON COLUMN OKO_OPPDRAG_110.FAGSYSTEM_ID IS 'Fagsystemets identifikasjon av stønaden/oppdraget';
COMMENT ON COLUMN OKO_OPPDRAG_110.UTBET_FREKVENS IS 'Angir med hvilken frekvens oppdraget skal beregnes/utbetales (DAG, UKE, MND, etc.)';
COMMENT ON COLUMN OKO_OPPDRAG_110.OPPDRAG_GJELDER_ID IS 'Angir hvem som saken/vedtaket er registrert på i fagrutinen, og må inneholde et gyldig fødselsnummer eller organisasjonsnummer';
COMMENT ON COLUMN OKO_OPPDRAG_110.SAKSBEH_ID IS 'Må fylles ut for at Oppdragssystemet skal ha sporbarhet på hvem som har gjort endringer i data knyttet til det enkelte oppdrag';

-- OKO_OPPDRAG_LINJE_150
COMMENT ON TABLE OKO_OPPDRAG_LINJE_150 IS 'Inneholder de relevante verdier som mappes og sender med i request melding. (Tabell fra Økonomi)';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.ID IS 'Primary Key';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.KODE_ENDRING_LINJE IS 'Benyttes for at systemet skal vite om det er en ny oppdragslinje eller endring i eksisterende linje';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.KODE_STATUS_LINJE IS 'Benyttes for å påvirke behandlingen av den enkelte delytelsen. (opphør, hvilende, sperret, reaktiver)';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.DATO_STATUS_FOM IS 'Er hvilken beregningsperiode for delytelsen som statusen skal gjelde fra';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.VEDTAK_ID IS 'Vedtaksdato';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.DELYTELSE_ID IS 'Basert på fagsystemets id med start fra 100 for tre siste sifre i id';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.DATO_VEDTAK_FOM IS 'Utbetaling/vedtak fom';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.DATO_VEDTAK_TOM IS 'Utbetaling/vedtak tom';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.KODE_KLASSIFIK IS 'Skal entydig bestemme hvilket kontonummer delytelsen regnskapsføres på, og må defineres i nært samarbeid med Oppdragssystemet etterhvert som nye fagområder tilknyttes systemet.';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.SATS IS 'Engangsstønad sats som ble brukt til beregning';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.FRADRAG_TILLEGG IS 'Angir om satsen går til fradag eller utbetaling (T = tillegg, F = fradrag))';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.TYPE_SATS IS 'satstype (engangs, dag, 14-dag, uke, mnd, år)';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.BRUK_KJORE_PLAN IS 'Angir om utbetaling skal skje i dag eller i henhold til kjøreplan';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.SAKSBEH_ID IS 'Ansvarlig saksbehandler';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.UTBETALES_TIL_ID IS 'Utbetalingsmottaker (fnr/orgnr)';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.REF_FAGSYSTEM_ID IS 'Oppdragsbasens identifikasjon av vedtaket som endres';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.REF_DELYTELSE_ID IS 'Identifikasjon av delytelsen som endres (fagsystemets id)';

-- OKO_AVSTEMMING_115
COMMENT ON TABLE OKO_AVSTEMMING_115 IS 'Inneholder de relevante verdier som mappes og sender med i request melding. (Tabell fra Økonomi)';
COMMENT ON COLUMN OKO_AVSTEMMING_115.KODE_KOMPONENT IS 'Identifiserer avleverende komponent av dataene (brukes ved avstemming)';
COMMENT ON COLUMN OKO_AVSTEMMING_115.NOKKEL_AVSTEMMING IS 'Brukes til å identifisere data som skal avstemmes';
COMMENT ON COLUMN OKO_AVSTEMMING_115.TIDSPNKT_MELDING IS 'Når meldingen ble sendt';

-- OKO_ATTESTANT_180
COMMENT ON TABLE OKO_ATTESTANT_180 IS 'Inneholder de relevante verdier som mappes og sender med i request melding. (Tabell fra Økonomi)';

-- KLAGE_VURDERING_RESULTAT
COMMENT ON TABLE KLAGE_VURDERING_RESULTAT IS 'Inneholder vurdering av klage gjort av NK/NFP';
COMMENT ON COLUMN KLAGE_VURDERING_RESULTAT.BEHANDLING_ID IS 'FK: BEHANDLING';
COMMENT ON COLUMN KLAGE_VURDERING_RESULTAT.KLAGE_VURDERT_AV IS 'Angir hvem som har vurdert klage (NK = Nav Klageinstans, NFP = Nav Familie og Pensjon)';
COMMENT ON COLUMN KLAGE_VURDERING_RESULTAT.KLAGEVURDERING IS 'Angir vurdering av klage (avvist, medhold, stadfeste, oppheve)';
COMMENT ON COLUMN KLAGE_VURDERING_RESULTAT.BEGRUNNELSE IS 'Begrunnelse for vurdering gjort i klage';
COMMENT ON COLUMN KLAGE_VURDERING_RESULTAT.KLAGE_AVVIST_AARSAK IS 'Angir årsak dersom vurdering er avvist (klage for sent, klage ugyldig)';
COMMENT ON COLUMN KLAGE_VURDERING_RESULTAT.KLAGE_MEDHOLD_AARSAK IS 'Angir årsak dersom vurdering er medhold (nye opplysninger, ulik regelverkstolkning, ulik vurdering, prosessuell feil)';






