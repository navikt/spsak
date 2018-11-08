package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAvklartSoeknadsperiodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OverføringÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.Årsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.BekreftetUttakPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.KontrollerFaktaPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.ManuellAvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.SlettetUttakPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakDokumentasjonDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import no.nav.fpsak.tidsserie.LocalDateInterval;

@ApplicationScoped
public class FaktaUttakHistorikkTjenesteImpl implements FaktaUttakHistorikkTjeneste{

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String PERIODE_SEPARATOR = " , ";
    private static final String PERIODE_SEPARATOR_ENDE = " og ";

    private HistorikkTjenesteAdapter historikkApplikasjonTjeneste;
    private KodeverkRepository kodeverkRepository;
    private UttakArbeidTjeneste uttakArbeidTjeneste;

    FaktaUttakHistorikkTjenesteImpl() {
        //FOR CDI proxy
    }

    @Inject
    public FaktaUttakHistorikkTjenesteImpl(HistorikkTjenesteAdapter historikkApplikasjonTjeneste,
                                           BehandlingRepositoryProvider behandlingRepositoryProvider,
                                           UttakArbeidTjeneste uttakArbeidTjeneste) {
        this.historikkApplikasjonTjeneste = historikkApplikasjonTjeneste;
        this.kodeverkRepository = behandlingRepositoryProvider.getKodeverkRepository();
        this.uttakArbeidTjeneste = uttakArbeidTjeneste;
    }

    @Override
    public void byggHistorikkinnslagForAvklarFakta(AvklarFaktaUttakDto avklarFaktaUttakDto, Behandling behandling) {
        byggHistorikkinnslag(avklarFaktaUttakDto.getBekreftedePerioder(), avklarFaktaUttakDto.getSlettedePerioder(), behandling);
    }

    @Override
    public void byggHistorikkinnslagForManuellAvklarFakta(ManuellAvklarFaktaUttakDto manuellAvklarFaktaUttakDto, Behandling behandling) {
        byggHistorikkinnslag(manuellAvklarFaktaUttakDto.getBekreftedePerioder(), manuellAvklarFaktaUttakDto.getSlettedePerioder(), behandling);
    }

    private void byggHistorikkinnslag(List<BekreftetUttakPeriodeDto> bekreftedePerioder,
                                 List<SlettetUttakPeriodeDto> slettedePerioder,
                                 Behandling behandling) {

        for (BekreftetUttakPeriodeDto bkftUttakPeriodeDto : bekreftedePerioder) {
            if (erNyPeriode(bkftUttakPeriodeDto)) {
                byggHistorikkinnslagForNyperiode(behandling, bkftUttakPeriodeDto.getBekreftetPeriode());
            } else {
                byggHistorikinnslagForAvklartSøknadsperiode(behandling, bkftUttakPeriodeDto);
            }
        }
        for (SlettetUttakPeriodeDto slettet : slettedePerioder) {
            byggHistorikkinnslagForSlettetperiode(behandling, slettet);
        }

    }

    private boolean erNyPeriode(BekreftetUttakPeriodeDto bkftUttakPeriodeDto) {
        return bkftUttakPeriodeDto.getOrginalFom() == null && bkftUttakPeriodeDto.getOrginalTom() == null;
    }

    private void byggHistorikkinnslagForSlettetperiode(Behandling behandling, SlettetUttakPeriodeDto dto) {
        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder()
            .medAvklartSøknadperiode(HistorikkAvklartSoeknadsperiodeType.SLETTET_SOEKNASPERIODE,
                formaterStringMedPeriodeType(finnUttakPeriodeType(dto.getUttakPeriodeType()).getNavn(), formaterPeriode(new LocalDateInterval(dto.getFom(), dto.getTom()))))
            .medBegrunnelse(dto.getBegrunnelse())
            .medSkjermlenke(SkjermlenkeType.FAKTA_OM_UTTAK);

        opprettHistorikkInnslag(behandling, tekstBuilder);
    }

    private void byggHistorikkinnslagForNyperiode(Behandling behandling, KontrollerFaktaPeriodeDto dto) {
        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder()
            .medAvklartSøknadperiode(HistorikkAvklartSoeknadsperiodeType.NY_SOEKNADSPERIODE,
                formaterStringMedPeriodeType(finnUttakPeriodeType(dto.getUttakPeriodeType()).getNavn(), formaterPeriode(new LocalDateInterval(dto.getFom(), dto.getTom()))))
            .medSkjermlenke(SkjermlenkeType.FAKTA_OM_UTTAK);

        opprettHistorikkInnslag(behandling, tekstBuilder);
    }

    private void byggHistorikinnslagForAvklartSøknadsperiode(Behandling behandling, BekreftetUttakPeriodeDto dto) {
        if (erGradering(dto)) {
            byggHistorikkinnslagForFerieEllerArbeid(behandling, dto, HistorikkAvklartSoeknadsperiodeType.GRADERING, true);
        } else if (erUtsettelse(dto.getBekreftetPeriode())) {
            byggHistorikkinnslagForUtsettelse(behandling, dto);
        } else if (erOverføring(dto.getBekreftetPeriode())) {
            byggHistorikkinnslagForOverføring(behandling, dto);
        } else {
            byggHistorikkinnslagForGeneraltSøknadsPeriode(behandling, dto, HistorikkAvklartSoeknadsperiodeType.UTTAK);
        }
    }

    private void byggHistorikkinnslagForOverføring(Behandling behandling, BekreftetUttakPeriodeDto dto) {
        Årsak overføringÅrsak = dto.getBekreftetPeriode().getOverføringÅrsak();
        if (OverføringÅrsak.INSTITUSJONSOPPHOLD_ANNEN_FORELDRE.equals(overføringÅrsak)) {
            byggHistorikkinnslagForSykdomEllerInnleggelse(behandling, dto, HistorikkAvklartSoeknadsperiodeType.OVERFOERING_INNLEGGELSE);
        } else if (OverføringÅrsak.SYKDOM_ANNEN_FORELDER.equals(overføringÅrsak)) {
            byggHistorikkinnslagForSykdomEllerInnleggelse(behandling, dto, HistorikkAvklartSoeknadsperiodeType.OVERFOERING_SKYDOM);
        }
    }

    private void byggHistorikkinnslagForUtsettelse(Behandling behandling, BekreftetUttakPeriodeDto dto) {
        Årsak utsettelseÅrsak = dto.getBekreftetPeriode().getUtsettelseÅrsak();
        if (UtsettelseÅrsak.FERIE.equals(utsettelseÅrsak)) {
            byggHistorikkinnslagForFerieEllerArbeid(behandling, dto, HistorikkAvklartSoeknadsperiodeType.UTSETTELSE_FERIE, false);
        } else if (UtsettelseÅrsak.ARBEID.equals(utsettelseÅrsak)) {
            byggHistorikkinnslagForFerieEllerArbeid(behandling, dto, HistorikkAvklartSoeknadsperiodeType.UTSETTELSE_ARBEID, false);
        } else if (UtsettelseÅrsak.SYKDOM.equals(utsettelseÅrsak)) {
            byggHistorikkinnslagForSykdomEllerInnleggelse(behandling, dto, HistorikkAvklartSoeknadsperiodeType.UTSETTELSE_SKYDOM);
        } else if (UtsettelseÅrsak.INSTITUSJON_BARN.equals(utsettelseÅrsak)) {
            byggHistorikkinnslagForSykdomEllerInnleggelse(behandling, dto, HistorikkAvklartSoeknadsperiodeType.UTSETTELSE_INSTITUSJON_BARN);
        } else if (UtsettelseÅrsak.INSTITUSJON_SØKER.equals(utsettelseÅrsak)) {
            byggHistorikkinnslagForSykdomEllerInnleggelse(behandling, dto, HistorikkAvklartSoeknadsperiodeType.UTSETTELSE_INSTITUSJON_SØKER);
        }
    }

    private boolean erGradering(BekreftetUttakPeriodeDto dto) {
        return dto.getBekreftetPeriode().getArbeidstidsprosent() != null && dto.getBekreftetPeriode().getArbeidstidsprosent().compareTo(BigDecimal.ZERO) > 0;
    }

    private void byggHistorikkinnslagForSykdomEllerInnleggelse(Behandling behandling, BekreftetUttakPeriodeDto dto, HistorikkAvklartSoeknadsperiodeType soeknadsperiodeType) {
        if (erEndretBegrunnelse(dto) || erEndretResultat(dto)) {
            LocalDateInterval orgPeriode = new LocalDateInterval(dto.getOrginalFom(), dto.getOrginalTom());
            KontrollerFaktaPeriodeDto bekreftetPeriode = dto.getBekreftetPeriode();
            List<UttakDokumentasjonDto> dokumentertePerioder = bekreftetPeriode.getDokumentertePerioder();

            List<LocalDateInterval> dokumenterteDatoer = dokumentertePerioder != null ? mapDokumentertPerioder(dokumentertePerioder) : Collections.emptyList();
            HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder()
                .medAvklartSøknadperiode(soeknadsperiodeType, formaterStringMedPeriodeType(finnUttakPeriodeType(bekreftetPeriode.getUttakPeriodeType()).getNavn(), formaterPeriode(orgPeriode)))
                .medSkjermlenke(SkjermlenkeType.FAKTA_OM_UTTAK);

            if (bekreftetPeriode.erAvklartDokumentert() && !dokumenterteDatoer.isEmpty()) {
                tekstBuilder.medEndretFelt(HistorikkEndretFeltType.FASTSETT_RESULTAT_PERIODEN, null, finnHistorikkFeltTypeForSykdomEllerInnleggelse(bekreftetPeriode, true));
                tekstBuilder.medEndretFelt(HistorikkEndretFeltType.AVKLART_PERIODE, null, konvertPerioderTilString(dokumenterteDatoer));
            } else {
                tekstBuilder.medEndretFelt(HistorikkEndretFeltType.FASTSETT_RESULTAT_PERIODEN, null, finnHistorikkFeltTypeForSykdomEllerInnleggelse(bekreftetPeriode, false));
            }
            opprettHistorikkInnslag(behandling, tekstBuilder);
        }
    }

    private void byggHistorikkinnslagForFerieEllerArbeid(Behandling behandling, BekreftetUttakPeriodeDto dto,
                                                         HistorikkAvklartSoeknadsperiodeType soeknadsperiodeType, boolean gradering) {
        if (erEndretPeriodeEllerArbeidsprosentEllerBegrunnelseEllerResultat(dto, gradering)) {
            String arbeidsgiverNavnOgOrgnr = "";
            List<Inntektsmelding> inntektsmeldinger = getInntektsmelding(behandling);
            if (!inntektsmeldinger.isEmpty()) {
                //TODO SOMMERFUGL Har er det vel feil ved flere arbeidsforhold?
                arbeidsgiverNavnOgOrgnr = inntektsmeldinger.get(0).getVirksomhet().getNavn() + "(" + inntektsmeldinger.get(0).getVirksomhet().getOrgnr() + ")";
            }

            LocalDateInterval orgPeriode = new LocalDateInterval(dto.getOrginalFom(), dto.getOrginalTom());
            KontrollerFaktaPeriodeDto bekreftetPeriode = dto.getBekreftetPeriode();
            LocalDateInterval bkftPeriode = new LocalDateInterval(bekreftetPeriode.getFom(), bekreftetPeriode.getTom());

            HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder()
                .medNavnVerdiOgAvklartSøknadperiode(soeknadsperiodeType, arbeidsgiverNavnOgOrgnr,
                    formaterStringMedPeriodeType(finnUttakPeriodeType(bekreftetPeriode.getUttakPeriodeType()).getNavn(), formaterPeriode(orgPeriode)))
                .medSkjermlenke(SkjermlenkeType.FAKTA_OM_UTTAK)
                .medEndretFelt(HistorikkEndretFeltType.FASTSETT_RESULTAT_PERIODEN, null, finnHistorikkFeltTypeForResultat(bekreftetPeriode.erAvklartDokumentert(), bekreftetPeriode.getResultat()));
            //hvis periode endret
            if (UttakPeriodeVurderingType.PERIODE_OK_ENDRET.equals(bekreftetPeriode.getResultat()) && erEndretPeriode(dto)) {
                tekstBuilder.medEndretFelt(HistorikkEndretFeltType.AVKLART_PERIODE, null, formaterPeriode(bkftPeriode));
            }
            //hvis arbeidsprosent endret
            if (gradering && erEndretArbeidstidsprosent(dto)) {
                tekstBuilder.medEndretFelt(HistorikkEndretFeltType.ANDEL_ARBEID, formaterArbeidstidsProsent(dto.getOriginalArbeidstidsprosent()), formaterArbeidstidsProsent(dto.getBekreftetPeriode().getArbeidstidsprosent()));
            }
            opprettHistorikkInnslag(behandling, tekstBuilder);
        }
    }

    private void byggHistorikkinnslagForGeneraltSøknadsPeriode(Behandling behandling, BekreftetUttakPeriodeDto dto,
                                                               HistorikkAvklartSoeknadsperiodeType soeknadsperiodeType) {
        if (erEndretPeriode(dto) || erEndretBegrunnelse(dto) || erEndretResultat(dto)) {

            LocalDateInterval orgPeriode = new LocalDateInterval(dto.getOrginalFom(), dto.getOrginalTom());
            KontrollerFaktaPeriodeDto bekreftetPeriode = dto.getBekreftetPeriode();
            LocalDateInterval bkftPeriode = new LocalDateInterval(bekreftetPeriode.getFom(), bekreftetPeriode.getTom());

            HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder()
                .medAvklartSøknadperiode(soeknadsperiodeType,
                    formaterStringMedPeriodeType(finnUttakPeriodeType(bekreftetPeriode.getUttakPeriodeType()).getNavn(), formaterPeriode(orgPeriode)))
                .medSkjermlenke(SkjermlenkeType.FAKTA_OM_UTTAK)
                .medEndretFelt(HistorikkEndretFeltType.FASTSETT_RESULTAT_PERIODEN, null, finnHistorikkFeltTypeForResultat(bekreftetPeriode.erAvklartDokumentert(), bekreftetPeriode.getResultat()));
            //hvis periode endret
            if (UttakPeriodeVurderingType.PERIODE_OK_ENDRET.equals(bekreftetPeriode.getResultat()) && erEndretPeriode(dto)) {
                tekstBuilder.medEndretFelt(HistorikkEndretFeltType.AVKLART_PERIODE, null, formaterPeriode(bkftPeriode));
            }
            opprettHistorikkInnslag(behandling, tekstBuilder);
        }
    }

    private String formaterStringMedPeriodeType(String s, String formattedPeriode) {
        return s + " " + formattedPeriode;
    }

    private String formaterArbeidstidsProsent(BigDecimal prosent) {
        return prosent != null ? prosent + "%" : null;
    }

    private void opprettHistorikkInnslag(Behandling behandling, HistorikkInnslagTekstBuilder tekstBuilder) {
        Historikkinnslag innslag = new Historikkinnslag();
        innslag.setType(HistorikkinnslagType.UTTAK);
        innslag.setAktør(HistorikkAktør.SAKSBEHANDLER);
        innslag.setBehandlingId(behandling.getId());
        tekstBuilder.build(innslag);
        historikkApplikasjonTjeneste.lagInnslag(innslag);
    }

    private boolean erEndretPeriodeEllerArbeidsprosentEllerBegrunnelseEllerResultat(BekreftetUttakPeriodeDto dto, boolean gradering) {

        boolean erEndret = erEndretPeriode(dto);
        if (gradering) {
            erEndret = erEndretArbeidstidsprosent(dto) || erEndret;
        }
        erEndret = erEndretBegrunnelse(dto) || erEndretResultat(dto) || erEndret;
        return erEndret;
    }

    private boolean erEndretBegrunnelse(BekreftetUttakPeriodeDto dto) {
        return !Objects.equals(dto.getOriginalBegrunnelse(), dto.getBekreftetPeriode().getBegrunnelse());
    }

    private boolean erEndretPeriode(BekreftetUttakPeriodeDto dto) {
        return !dto.getOrginalFom().equals(dto.getBekreftetPeriode().getFom()) ||
            !dto.getOrginalTom().equals(dto.getBekreftetPeriode().getTom());
    }

    private boolean erEndretResultat(BekreftetUttakPeriodeDto dto) {
        return !dto.getOriginalResultat().equals(dto.getBekreftetPeriode().getResultat());
    }

    private boolean erEndretArbeidstidsprosent(BekreftetUttakPeriodeDto dto) {
        return dto.getOriginalArbeidstidsprosent().compareTo(dto.getBekreftetPeriode().getArbeidstidsprosent()) != 0;
    }

    private UttakPeriodeType finnUttakPeriodeType(UttakPeriodeType periodeType) {
        return kodeverkRepository.finn(UttakPeriodeType.class, periodeType.getKode());
    }

    private Årsak finnUtsettelseÅrsak(Årsak årsak) {
        if (årsak != null) {
            return kodeverkRepository.finn(UtsettelseÅrsak.class, årsak.getKode());
        }
        return null;
    }

    private Årsak finnOverføringÅrsak(Årsak årsak) {
        if (årsak != null) {
            return kodeverkRepository.finn(OverføringÅrsak.class, årsak.getKode());
        }
        return null;
    }

    private List<Inntektsmelding> getInntektsmelding(Behandling behandling) {
        return uttakArbeidTjeneste.hentInntektsmeldinger(behandling);
    }

    private List<LocalDateInterval> mapDokumentertPerioder(List<UttakDokumentasjonDto> dokumentertePerioder) {
        return dokumentertePerioder.stream()
            .map(this::mapDokumentertPeriode)
            .collect(Collectors.toList());
    }

    private LocalDateInterval mapDokumentertPeriode(UttakDokumentasjonDto kontrollerFaktaPeriodeDto) {
        Objects.requireNonNull(kontrollerFaktaPeriodeDto, "kontrollerFaktaPeriodeDto"); // NOSONAR $NON-NLS-1$
        return new LocalDateInterval(kontrollerFaktaPeriodeDto.getFom(), kontrollerFaktaPeriodeDto.getTom());
    }

    private HistorikkEndretFeltVerdiType finnHistorikkFeltTypeForResultat(boolean avklartPeriode, UttakPeriodeVurderingType vurderingType) {
        if (avklartPeriode) {
            return finnBasertPåVurderingType(vurderingType);
        }
        return HistorikkEndretFeltVerdiType.FASTSETT_RESULTAT_PERIODEN_AVKLARES_IKKE;
    }

    private HistorikkEndretFeltVerdiType finnBasertPåVurderingType(UttakPeriodeVurderingType vurderingType) {
        return UttakPeriodeVurderingType.PERIODE_OK.equals(vurderingType) ? HistorikkEndretFeltVerdiType.FASTSETT_RESULTAT_GRADERING_AVKLARES : HistorikkEndretFeltVerdiType.FASTSETT_RESULTAT_ENDRE_SOEKNADSPERIODEN;
    }

    private HistorikkEndretFeltVerdiType finnHistorikkFeltTypeForSykdomEllerInnleggelse(KontrollerFaktaPeriodeDto bekreftetPeriode, boolean dokumentert) {
        if (erUtsettelseSkydom(bekreftetPeriode) || erOverføringSkydom(bekreftetPeriode)) {
            return dokumentert ? HistorikkEndretFeltVerdiType.FASTSETT_RESULTAT_PERIODEN_SYKDOM_DOKUMENTERT : HistorikkEndretFeltVerdiType.FASTSETT_RESULTAT_PERIODEN_SYKDOM_DOKUMENTERT_IKKE;
        }
        if (erUtsettelseInnleggelse(bekreftetPeriode) || erOverføringInnleggelse(bekreftetPeriode)) {
            return dokumentert ? HistorikkEndretFeltVerdiType.FASTSETT_RESULTAT_PERIODEN_INNLEGGELSEN_DOKUMENTERT : HistorikkEndretFeltVerdiType.FASTSETT_RESULTAT_PERIODEN_INNLEGGELSEN_DOKUMENTERT_IKKE;
        }
        return null;
    }

    private boolean erOverføringSkydom(KontrollerFaktaPeriodeDto bekreftetPeriode) {
        return erOverføring(bekreftetPeriode) && OverføringÅrsak.SYKDOM_ANNEN_FORELDER.equals(finnOverføringÅrsak(bekreftetPeriode.getOverføringÅrsak()));
    }

    private boolean erOverføring(KontrollerFaktaPeriodeDto bekreftetPeriode) {
        return bekreftetPeriode.getOverføringÅrsak() != null && !Årsak.UDEFINERT.getKode().equals(bekreftetPeriode.getOverføringÅrsak().getKode());
    }

    private boolean erOverføringInnleggelse(KontrollerFaktaPeriodeDto bekreftetPeriode) {
        return bekreftetPeriode.getOverføringÅrsak() != null && !Årsak.UDEFINERT.getKode().equals(bekreftetPeriode.getOverføringÅrsak().getKode()) && OverføringÅrsak.INSTITUSJONSOPPHOLD_ANNEN_FORELDRE.equals(finnOverføringÅrsak(bekreftetPeriode.getOverføringÅrsak()));
    }

    private boolean erUtsettelseSkydom(KontrollerFaktaPeriodeDto bekreftetPeriode) {
        return erUtsettelse(bekreftetPeriode) && UtsettelseÅrsak.SYKDOM.equals(finnUtsettelseÅrsak(bekreftetPeriode.getUtsettelseÅrsak()));
    }

    private boolean erUtsettelse(KontrollerFaktaPeriodeDto bekreftetPeriode) {
        return bekreftetPeriode.getUtsettelseÅrsak() != null && !Årsak.UDEFINERT.getKode().equals(bekreftetPeriode.getUtsettelseÅrsak().getKode());
    }

    private boolean erUtsettelseInnleggelse(KontrollerFaktaPeriodeDto bekreftetPeriode) {
        return bekreftetPeriode.getUtsettelseÅrsak() != null &&
            !Årsak.UDEFINERT.getKode().equals(bekreftetPeriode.getUtsettelseÅrsak().getKode()) &&
            (UtsettelseÅrsak.INSTITUSJON_SØKER.equals(finnUtsettelseÅrsak(bekreftetPeriode.getUtsettelseÅrsak())) || UtsettelseÅrsak.INSTITUSJON_BARN.equals(finnUtsettelseÅrsak(bekreftetPeriode.getUtsettelseÅrsak())));
    }

    private String formaterPeriode(LocalDateInterval periode) {
        return formatDate(periode.getFomDato()) + " - " + formatDate(periode.getTomDato());
    }

    private String formatDate(LocalDate localDate) {
        return DATE_FORMATTER.format(localDate);
    }

    private String konvertPerioderTilString(List<LocalDateInterval> perioder) {
        StringBuilder result = new StringBuilder();
        List<String> perioderList = perioder.stream().map(this::formaterPeriode).collect(Collectors.toList());
        int lastIndex = perioderList.size() - 1;
        result.append(lastIndex == 0 ? perioderList.get(lastIndex) :
            perioderList.subList(0, lastIndex).stream().collect(Collectors.joining(PERIODE_SEPARATOR)).concat(PERIODE_SEPARATOR_ENDE).concat(perioderList.get(lastIndex)));
        return result.toString();

    }
}
