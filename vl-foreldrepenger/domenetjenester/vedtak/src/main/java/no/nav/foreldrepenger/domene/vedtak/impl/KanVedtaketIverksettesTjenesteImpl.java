package no.nav.foreldrepenger.domene.vedtak.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.hendelse.InfotrygdHendelse;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.hendelse.InfotrygdHendelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.hendelse.Meldingstype;
import no.nav.foreldrepenger.domene.vedtak.KanVedtaketIverksettesTjeneste;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;

@ApplicationScoped
public class KanVedtaketIverksettesTjenesteImpl implements KanVedtaketIverksettesTjeneste {

    private static final Logger log = LoggerFactory.getLogger(KanVedtaketIverksettesTjenesteImpl.class);
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private InfotrygdHendelseTjeneste infotrygdHendelseTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private BeregningsresultatFPRepository beregningsresultatFPRepository;

    KanVedtaketIverksettesTjenesteImpl() {
        // for CDI
    }

    @Inject
    public KanVedtaketIverksettesTjenesteImpl(BehandlingRepositoryProvider repositoryProvider, InfotrygdHendelseTjeneste infotrygdHendelseTjeneste, InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
        this.infotrygdHendelseTjeneste = infotrygdHendelseTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
    }

    @Override
    public boolean kanVedtaketIverksettes(Behandling behandling) {
        Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentAggregat(behandling);
        LocalDate skjæringstidspunkt = bg.getSkjæringstidspunkt();
        List<InfotrygdHendelse> hendelseListe = infotrygdHendelseTjeneste.hentHendelsesListFraInfotrygdFeed(behandling);

        List<Ytelse> ytelseList = hentRegisterDataFraInfotrygd(behandling, skjæringstidspunkt);

        boolean finnesMatchMedInnvilIRegisterData = sjekkMotRegisterData(hendelseListe, ytelseList);
        if (finnesMatchMedInnvilIRegisterData) {
            log.info("Det var en hendelse av type ANNULERT fra infotrygd feed som korrelerer med en INNVILGET ytelse i registerdata");
        }
        Optional<InfotrygdHendelse> nyesteHendelse = finnDenNyesteHendelsen(hendelseListe);

        return finnOmVedtaketKanIverksettes(nyesteHendelse, ytelseList, skjæringstidspunkt);
    }

    private List<Ytelse> hentRegisterDataFraInfotrygd(Behandling behandling, LocalDate skjæringstidspunkt) {
        LocalDate sisteUttaksdato = hentSisteUttaksdato(behandling);
        if (sisteUttaksdato.isBefore(skjæringstidspunkt)) {
            throw new IllegalStateException("Siste uttaksdato kan ikke være før skjæringstidspunktet.");
        }
        ÅpenDatoIntervallEntitet datoIntervall = ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(skjæringstidspunkt, sisteUttaksdato);
        Optional<AktørYtelse> aktørYtelseOpt = inntektArbeidYtelseTjeneste.hentAggregat(behandling).getAktørYtelseFørStp(behandling.getAktørId());
        return aktørYtelseOpt.isPresent() ? aktørYtelseOpt.get().getYtelser().stream()
            .filter(ytelse -> ytelse.getKilde().equals(Fagsystem.INFOTRYGD))
            .filter(ytelse -> datoIntervall.overlapper(ytelse.getPeriode()))
            .filter(ytelse -> Arrays.asList(RelatertYtelseTilstand.LØPENDE, RelatertYtelseTilstand.AVSLUTTET).contains(ytelse.getStatus()))
            .collect(Collectors.toList()) : Collections.emptyList();
    }

    private LocalDate hentSisteUttaksdato(Behandling behandling) {
        BeregningsresultatFP beregningsresultat = beregningsresultatFPRepository.hentBeregningsresultatFP(behandling)
            .orElseThrow(() -> new IllegalStateException("Fant ikke beregningsresultat for behandling med id " + behandling.getId()));
        return beregningsresultat.getBeregningsresultatPerioder().stream()
            .map(BeregningsresultatPeriode::getBeregningsresultatPeriodeTom)
            .max(Comparator.comparing(Function.identity()))
            .orElseThrow(() -> new IllegalStateException("Fant ikke siste uttaksdato for behandling med id " + behandling.getId()));
    }


    private boolean sjekkMotRegisterData(List<InfotrygdHendelse> hendelseListe, List<Ytelse> ytelseList) {
        Map<String, List<InfotrygdHendelse>> groupedByType = hendelseListe.stream()
            .collect(Collectors.groupingBy(InfotrygdHendelse::getType));
        List<InfotrygdHendelse> listAnnulSammenMedOpphInnv = fjernAnnulertSomErKorrelertMedInnvEllerOpph(groupedByType);
        List<InfotrygdHendelse> listMedKunAnnul = listAnnulSammenMedOpphInnv.stream().
            filter(infotrygdHendelse -> infotrygdHendelse.getType().equals(Meldingstype.INFOTRYGD_ANNULLERT.getType())).collect(Collectors.toList());

        for (InfotrygdHendelse hendelse : listMedKunAnnul) {
            LocalDate identDato = konverterTilLocalDate(hendelse.getIdentDato());
            boolean finnesInnvilMatchIRegData = ytelseList.stream().anyMatch(ytelse -> ytelse.getPeriode().getFomDato().equals(identDato));
            if (finnesInnvilMatchIRegData) {
                return true;
            }
        }
        return false;
    }

    private Optional<InfotrygdHendelse> finnDenNyesteHendelsen(List<InfotrygdHendelse> hendelseListe) {

        Map<String, List<InfotrygdHendelse>> groupedByType = hendelseListe.stream()
            .collect(Collectors.groupingBy(InfotrygdHendelse::getType));
        List<InfotrygdHendelse> filtrertList = fjernAnnulertSomErKorrelertMedInnvEllerOpph(groupedByType);
        filtrertList.removeIf(infotrygdHendelse -> infotrygdHendelse.getType().equals(Meldingstype.INFOTRYGD_ANNULLERT.getType()));
        filtrertList.removeIf(infotrygdHendelse -> infotrygdHendelse.getType().equals(Meldingstype.INFOTRYGD_ENDRET.getType()));

        return filtrertList.stream().max(Comparator.comparing(InfotrygdHendelse::getSekvensnummer));
    }

    private List<InfotrygdHendelse> fjernAnnulertSomErKorrelertMedInnvEllerOpph(Map<String, List<InfotrygdHendelse>> groupedByType) {
        String keyANNUL = Meldingstype.INFOTRYGD_ANNULLERT.getType();
        List<InfotrygdHendelse> toRemove = new ArrayList<>();
        List<InfotrygdHendelse> alleHendelser = groupedByType.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        if (!groupedByType.isEmpty() && groupedByType.containsKey(keyANNUL)) {
            for (InfotrygdHendelse hendelse : groupedByType.get(keyANNUL)) {
                boolean matchMedOpphørt = fjernAnnulertOgOpphørtSomMatches(groupedByType, hendelse, toRemove);
                if (!matchMedOpphørt) {
                    fjernAnnulertOgInnvilgetSomMatches(groupedByType, hendelse, toRemove);
                }
            }
            for (InfotrygdHendelse ih : toRemove) {
                alleHendelser.remove(ih);
            }
        }
        return alleHendelser;
    }

    private boolean fjernAnnulertOgOpphørtSomMatches(Map<String, List<InfotrygdHendelse>> groupedByType, InfotrygdHendelse hendelse, List<InfotrygdHendelse> toRemove) {
        String keyOPPH = Meldingstype.INFOTRYGD_OPPHOERT.getType();
        Optional<InfotrygdHendelse> opphOpt = groupedByType.containsKey(keyOPPH) ? groupedByType.get(keyOPPH)
            .stream()
            .filter(ih -> ih.getIdentDato().equals(hendelse.getIdentDato()) && ih.getTypeYtelse().equals(hendelse.getTypeYtelse()))
            .findFirst() : Optional.empty();

        if (opphOpt.isPresent()) {
            toRemove.add(opphOpt.get());
            return toRemove.add(hendelse);
        }
        return false;
    }

    private void fjernAnnulertOgInnvilgetSomMatches(Map<String, List<InfotrygdHendelse>> groupedByType, InfotrygdHendelse hendelse, List<InfotrygdHendelse> toRemove) {
        String keyINNVIL = Meldingstype.INFOTRYGD_INNVILGET.getType();
        Optional<InfotrygdHendelse> innvilOpt = groupedByType.containsKey(keyINNVIL) ? groupedByType.get(keyINNVIL)
            .stream()
            .filter(ih -> ih.getIdentDato().equals(hendelse.getIdentDato()) && ih.getTypeYtelse().equals(hendelse.getTypeYtelse()))
            .findFirst() : Optional.empty();

        if (innvilOpt.isPresent()) {
            toRemove.add(innvilOpt.get());
            toRemove.add(hendelse);
        }
    }

    private LocalDate konverterTilLocalDate(String identDatoStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.parse(identDatoStr, formatter);
    }

    private boolean finnOmVedtaketKanIverksettes(Optional<InfotrygdHendelse> nyesteHendelsen, List<Ytelse> ytelseList, LocalDate skjæringstidspunkt) {
        if (!ytelseList.isEmpty()) {
            return nyesteHendelsen.map(infotrygdHendelse -> !infotrygdHendelse.getType().equals(Meldingstype.INFOTRYGD_INNVILGET.getType())
                && !infotrygdHendelse.getFom().isAfter(skjæringstidspunkt)).orElse(false);
        } else {
            return nyesteHendelsen.map(infotrygdHendelse -> !infotrygdHendelse.getType().equals(Meldingstype.INFOTRYGD_INNVILGET.getType())
                && !infotrygdHendelse.getFom().isAfter(skjæringstidspunkt)).orElse(true);
        }

    }

}
