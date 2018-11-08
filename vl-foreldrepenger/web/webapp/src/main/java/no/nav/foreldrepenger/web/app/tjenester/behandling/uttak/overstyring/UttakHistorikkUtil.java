package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.overstyring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakResultatPeriodeAktivitetLagreDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakResultatPeriodeLagreDto;
import no.nav.fpsak.tidsserie.LocalDateInterval;

public final class UttakHistorikkUtil {

    private HistorikkinnslagType historikkinnslagTypeSplitt;
    private HistorikkinnslagType historikkinnslagTypeEndring;

    private UttakHistorikkUtil(HistorikkinnslagType historikkinnslagTypeSplitt, HistorikkinnslagType historikkinnslagTypeEndring) {
        this.historikkinnslagTypeSplitt = historikkinnslagTypeSplitt;
        this.historikkinnslagTypeEndring = historikkinnslagTypeEndring;
    }

    public static UttakHistorikkUtil forFastsetting() {
        return new UttakHistorikkUtil(HistorikkinnslagType.FASTSATT_UTTAK_SPLITT, HistorikkinnslagType.FASTSATT_UTTAK);
    }

    public List<Historikkinnslag> lagHistorikkinnslag(Behandling behandling,
                                                      AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                                      List<UttakResultatPeriodeLagreDto> uttakResultat,
                                                      UttakResultatPerioderEntitet gjeldende) {
        ArrayList<Historikkinnslag> historikkinnslag = new ArrayList<>();
        historikkinnslag.addAll(lagHistorikkinnslagFraSplitting(behandling, aksjonspunktDefinisjon, uttakResultat, gjeldende));
        historikkinnslag.addAll(lagHistorikkinnslagFraPeriodeEndringer(behandling, aksjonspunktDefinisjon, uttakResultat, gjeldende));
        return historikkinnslag;
    }

    private List<Historikkinnslag> lagHistorikkinnslagFraPeriodeEndringer(Behandling behandling,
                                                                          AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                                                          List<UttakResultatPeriodeLagreDto> grupper,
                                                                          UttakResultatPerioderEntitet gjeldende) {
        return grupper
            .stream()
            .map(gruppe -> lagHistorikkinnslagForPeriode(behandling, aksjonspunktDefinisjon, gruppe, gjeldende))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private List<Historikkinnslag> lagHistorikkinnslagFraSplitting(Behandling behandling,
                                                                   AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                                                   List<UttakResultatPeriodeLagreDto> grupper,
                                                                   UttakResultatPerioderEntitet gjeldende) {
        if (grupper.size() == gjeldende.getPerioder().size()) {
            return Collections.emptyList();
        }
        List<UttakOverstyringsPeriodeSplitt> splittet = finnSplittet(grupper, gjeldende);
        return splittet.stream().map(split -> lagHistorikkinnslag(behandling, aksjonspunktDefinisjon, split)).collect(Collectors.toList());
    }

    private Historikkinnslag lagHistorikkinnslag(Behandling behandling,
                                                 AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                                 UttakOverstyringsPeriodeSplitt split) {
        Historikkinnslag historikkinnslag = new Historikkinnslag.Builder()
            .medType(historikkinnslagTypeSplitt)
            .medFagsakId(behandling.getFagsakId())
            .medBehandlingId(behandling.getId())
            .medAktør(HistorikkAktør.SAKSBEHANDLER)
            .build();
        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder()
            .medSkjermlenke(aksjonspunktDefinisjon, behandling)
            .medHendelse(historikkinnslagTypeSplitt);
        for (LocalDateInterval splittetPeriode : split.getSplittet()) {
            byggEndringFelt(tekstBuilder, HistorikkEndretFeltType.UTTAK_SPLITT_TIDSPERIODE, split.getOpprinnelig(), splittetPeriode);
        }
        historikkinnslag.setHistorikkinnslagDeler(tekstBuilder.build(historikkinnslag));
        return historikkinnslag;
    }

    private List<UttakOverstyringsPeriodeSplitt> finnSplittet(List<UttakResultatPeriodeLagreDto> perioder,
                                                              UttakResultatPerioderEntitet gjeldende) {

        Map<UttakResultatPeriodeEntitet, UttakOverstyringsPeriodeSplitt.Builder> map = new HashMap<>();
        for (UttakResultatPeriodeLagreDto periode : perioder) {
            LocalDateInterval periodeInterval = new LocalDateInterval(periode.getFom(), periode.getTom());
            UttakResultatPeriodeEntitet matchendeGjeldendePeriode = EndreUttakUtil.finnGjeldendePeriodeFor(gjeldende, periodeInterval);
            if (!likeTidsperioder(periode, matchendeGjeldendePeriode)) {
                map.computeIfAbsent(matchendeGjeldendePeriode, m -> new UttakOverstyringsPeriodeSplitt.Builder()
                    .medOpprinnelig(new LocalDateInterval(m.getFom(), m.getTom())));
                map.get(matchendeGjeldendePeriode).leggTil(periodeInterval);
            }
        }

        return map.values().stream().map(UttakOverstyringsPeriodeSplitt.Builder::build).collect(Collectors.toList());
    }

    private boolean likeTidsperioder(UttakResultatPeriodeLagreDto periode, UttakResultatPeriodeEntitet matchendeGjeldendePeriode) {
        return matchendeGjeldendePeriode.getFom().isEqual(periode.getFom()) &&
            matchendeGjeldendePeriode.getTom().isEqual(periode.getTom());
    }

    private List<Historikkinnslag> lagHistorikkinnslagForPeriode(Behandling behandling,
                                                                 AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                                                 UttakResultatPeriodeLagreDto periode,
                                                                 UttakResultatPerioderEntitet gjeldende) {
        List<Historikkinnslag> list = new ArrayList<>();
        for (UttakResultatPeriodeAktivitetLagreDto aktivitet : periode.getAktiviteter()) {
            if (aktivitetHarEndringer(behandling, aksjonspunktDefinisjon, gjeldende, periode, aktivitet)) {
                list.add(lagHistorikkinnslag(behandling, aksjonspunktDefinisjon, gjeldende, periode, aktivitet));
            }
        }
        return list;
    }

    private Historikkinnslag lagHistorikkinnslag(Behandling behandling,
                                                 AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                                 UttakResultatPerioderEntitet gjeldende,
                                                 UttakResultatPeriodeLagreDto nyGruppe,
                                                 UttakResultatPeriodeAktivitetLagreDto nyPeriode) {
        Historikkinnslag historikkinnslag = new Historikkinnslag.Builder()
            .medAktør(HistorikkAktør.SAKSBEHANDLER)
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId())
            .medType(historikkinnslagTypeEndring)
            .build();
        HistorikkInnslagTekstBuilder tekstBuilder = lagHistorikkinnslagTekst(behandling, aksjonspunktDefinisjon, gjeldende, nyGruppe, nyPeriode);
        historikkinnslag.setHistorikkinnslagDeler(tekstBuilder.build(historikkinnslag));
        return historikkinnslag;
    }

    private boolean aktivitetHarEndringer(Behandling behandling,
                                          AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                          UttakResultatPerioderEntitet gjeldende,
                                          UttakResultatPeriodeLagreDto nyGruppe,
                                          UttakResultatPeriodeAktivitetLagreDto nyAktivitet) {
        return lagHistorikkinnslagTekst(behandling, aksjonspunktDefinisjon, gjeldende, nyGruppe, nyAktivitet).antallEndredeFelter() != 0;
    }

    private HistorikkInnslagTekstBuilder lagHistorikkinnslagTekst(Behandling behandling,
                                                                  AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                                                  UttakResultatPerioderEntitet gjeldende,
                                                                  UttakResultatPeriodeLagreDto nyPeriode,
                                                                  UttakResultatPeriodeAktivitetLagreDto nyAktivitet) {
        UttakResultatPeriodeEntitet gjeldendePeriode = EndreUttakUtil.finnGjeldendePeriodeFor(gjeldende, new LocalDateInterval(nyPeriode.getFom(), nyPeriode.getTom()));
        UttakResultatPeriodeAktivitetEntitet gjeldendeAktivitet = EndreUttakUtil.finnGjeldendeAktivitetFor(gjeldendePeriode, nyAktivitet.getArbeidsforholdId(),
            nyAktivitet.getArbeidsforholdOrgnr(), nyAktivitet.getUttakArbeidType());
        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder()
            .medSkjermlenke(aksjonspunktDefinisjon, behandling)
            .medHendelse(historikkinnslagTypeEndring)
            .medBegrunnelse(nyPeriode.getBegrunnelse())
            .medOpplysning(HistorikkOpplysningType.UTTAK_PERIODE_FOM, nyPeriode.getFom())
            .medOpplysning(HistorikkOpplysningType.UTTAK_PERIODE_TOM, nyPeriode.getTom());

        byggEndringFelt(builder, HistorikkEndretFeltType.UTTAK_TREKKDAGER, gjeldendeAktivitet.getTrekkdager(), nyAktivitet.getTrekkdager());
        byggEndringFelt(builder, HistorikkEndretFeltType.UTTAK_STØNADSKONTOTYPE, returnereNullHvisUdefinert(gjeldendeAktivitet.getTrekkonto()), nyAktivitet.getStønadskontoType());
        byggEndringFelt(builder, HistorikkEndretFeltType.UTTAK_PERIODE_RESULTAT_TYPE, gjeldendePeriode.getPeriodeResultatType(), nyPeriode.getPeriodeResultatType());
        byggEndringFelt(builder, HistorikkEndretFeltType.UTTAK_PROSENT_UTBETALING, gjeldendeAktivitet.getUtbetalingsprosent(), nyAktivitet.getUtbetalingsgrad());
        byggEndringFelt(builder, HistorikkEndretFeltType.UTTAK_PERIODE_RESULTAT_ÅRSAK, gjeldendePeriode.getPeriodeResultatÅrsak(), nyPeriode.getPeriodeResultatÅrsak());
        byggEndringFelt(builder, HistorikkEndretFeltType.UTTAK_TREKKDAGER_FLERBARN_KVOTE, gjeldendePeriode.isFlerbarnsdager(), nyPeriode.isFlerbarnsdager());
        byggEndringFelt(builder, HistorikkEndretFeltType.UTTAK_SAMTIDIG_UTTAK, gjeldendePeriode.isSamtidigUttak(), nyPeriode.isSamtidigUttak());
        byggEndringFelt(builder, HistorikkEndretFeltType.UTTAK_SAMTIDIG_UTTAK, gjeldendePeriode.getSamtidigUttaksprosent(), nyPeriode.getSamtidigUttaksprosent());
        return builder;
    }

    private static <T> void byggEndringFelt(HistorikkInnslagTekstBuilder builder,
                                            HistorikkEndretFeltType type,
                                            T fraVerdi,
                                            T tilVerdi) {
        if (!Objects.equals(fraVerdi, tilVerdi)) {
            builder.medEndretFelt(type, fraVerdi, tilVerdi);
        }
    }

    private StønadskontoType returnereNullHvisUdefinert(StønadskontoType stønadskontoType) {
        return StønadskontoType.UDEFINERT.equals(stønadskontoType) ? null : stønadskontoType;
    }

    public static UttakHistorikkUtil forOverstyring() {
        return new UttakHistorikkUtil(HistorikkinnslagType.OVST_UTTAK_SPLITT, HistorikkinnslagType.OVST_UTTAK);
    }
}
