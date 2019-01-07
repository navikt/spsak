package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.KopierBeregningsgrunnlagUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.RegelmodellOversetter;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.wrapper.BeregningsgrunnlagRegelResultat;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class ForeslåBeregningsgrunnlag {

    private MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel;
    private MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel;
    private AksjonspunktRepository aksjonspunktRepository;
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;
    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;

    private JacksonJsonConfig jacksonJsonConfig = new JacksonJsonConfig();

    ForeslåBeregningsgrunnlag() {
        //for CDI proxy
    }

    @Inject
    public ForeslåBeregningsgrunnlag(MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel,
                                     MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel,
                                     GrunnlagRepositoryProvider repositoryProvider, KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste,
                                     HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste) {
        this.oversetterTilRegel = oversetterTilRegel;
        this.oversetterFraRegel = oversetterFraRegel;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.kontrollerFaktaBeregningTjeneste = kontrollerFaktaBeregningTjeneste;
        this.hentGrunnlagsdataTjeneste = hentGrunnlagsdataTjeneste;
    }

    public BeregningsgrunnlagRegelResultat foreslåBeregningsgrunnlag(Behandling behandling, no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag beregningsgrunnlag) {
        // Innhent grunnlagsdata
        if (hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling)) {
            hentGrunnlagsdataTjeneste.innhentInntektsInformasjonBeregningOgSammenligning(behandling);
        }

        // Oversetter initielt Beregningsgrunnlag -> regelmodell
        Beregningsgrunnlag regelmodellBeregningsgrunnlag = oversetterTilRegel.map(behandling, beregningsgrunnlag);
        opprettPerioderForKortvarigeArbeidsforhold(behandling, regelmodellBeregningsgrunnlag);
        String input = toJson(regelmodellBeregningsgrunnlag);

        // Evaluerer hver BeregningsgrunnlagPeriode fra initielt Beregningsgrunnlag
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : regelmodellBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(periode).evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation));
        }

        // Oversett endelig resultat av regelmodell til foreslått Beregningsgrunnlag  (+ spore input -> evaluation)
        no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag foreslåttBeregningsgrunnlag =
            oversetterFraRegel.mapForeslåBeregningsgrunnlag(regelmodellBeregningsgrunnlag, input, regelResultater, beregningsgrunnlag);
        List<AksjonspunktDefinisjon> aksjonspunkter = aksjonspunkterSomSkalOpprettes(regelResultater);
        return new BeregningsgrunnlagRegelResultat(foreslåttBeregningsgrunnlag, aksjonspunkter);
    }

    private void opprettPerioderForKortvarigeArbeidsforhold(Behandling behandling, Beregningsgrunnlag grunnlag) {
        Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> kortvarigeAktiviteter = kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(behandling);
        kortvarigeAktiviteter.entrySet().stream()
            .filter(entry -> entry.getKey().getBgAndelArbeidsforhold().filter(a -> Boolean.TRUE.equals(a.getErTidsbegrensetArbeidsforhold())).isPresent())
            .map(Map.Entry::getValue)
            .forEach(yrkesaktivitet -> {
                Optional<AktivitetsAvtale> aktivitetsAvtale = yrkesaktivitet.getAnsettelsesPeriode();
                aktivitetsAvtale.ifPresent(aa -> {
                    LocalDate kortvarigArbeidsforholdTom = aa.getTilOgMed();
                    List<BeregningsgrunnlagPeriode> eksisterendePerioder = grunnlag.getBeregningsgrunnlagPerioder();
                    ListIterator<BeregningsgrunnlagPeriode> periodeIterator = eksisterendePerioder.listIterator();
                    while (periodeIterator.hasNext()) {
                        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = periodeIterator.next();
                        Periode bgPeriode = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriode();
                        PeriodeÅrsak nyPeriodeÅrsak = PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET;
                        if (bgPeriode.getTom().equals(kortvarigArbeidsforholdTom)) {
                            oppdaterPeriodeÅrsakForNestePeriode(eksisterendePerioder, periodeIterator, nyPeriodeÅrsak);
                        } else if (bgPeriode.inneholder(kortvarigArbeidsforholdTom)) {
                            splitBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode, kortvarigArbeidsforholdTom, nyPeriodeÅrsak);
                        }
                    }
                });
            });
    }

    private void oppdaterPeriodeÅrsakForNestePeriode(List<BeregningsgrunnlagPeriode> eksisterendePerioder, ListIterator<BeregningsgrunnlagPeriode> periodeIterator, PeriodeÅrsak nyPeriodeÅrsak) {
        if (periodeIterator.hasNext()) {
            BeregningsgrunnlagPeriode nestePeriode = eksisterendePerioder.get(periodeIterator.nextIndex());
            BeregningsgrunnlagPeriode.builder(nestePeriode)
                .leggTilPeriodeÅrsak(nyPeriodeÅrsak)
                .build();
        }
    }

    private BeregningsgrunnlagPeriode splitBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, LocalDate nyPeriodeTom, PeriodeÅrsak periodeÅrsak) {
        LocalDate eksisterendePeriodeTom = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriode().getTom();
        BeregningsgrunnlagPeriode.builder(beregningsgrunnlagPeriode)
            .medPeriode(Periode.of(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriode().getFom(), nyPeriodeTom))
            .build();
        BeregningsgrunnlagPeriode nyPeriode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(nyPeriodeTom.plusDays(1), eksisterendePeriodeTom))
            .leggTilPeriodeÅrsak(periodeÅrsak)
            .build();
        KopierBeregningsgrunnlagUtil.kopierBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode, nyPeriode);
        leggTilPeriodeIBeregningsgrunnlag(beregningsgrunnlagPeriode.getBeregningsgrunnlag(), nyPeriode);
        return nyPeriode;
    }

    private void leggTilPeriodeIBeregningsgrunnlag(Beregningsgrunnlag beregningsgrunnlag, BeregningsgrunnlagPeriode nyPeriode) {
        Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medBeregningsgrunnlagPeriode(nyPeriode)
            .build();
    }

    private List<AksjonspunktDefinisjon> aksjonspunkterSomSkalOpprettes(List<RegelResultat> regelResultater) {
        return regelResultater.stream().flatMap(r -> r.getMerknader().stream())
            .distinct()
            .map(this::mapRegelMerknadTilAksjonspunktDef)
            .collect(Collectors.toList());
    }

    private AksjonspunktDefinisjon mapRegelMerknadTilAksjonspunktDef(RegelMerknad regelMerknad) {
        return aksjonspunktRepository.finnAksjonspunktDefinisjon(regelMerknad.getMerknadKode());
    }

    private String toJson(Beregningsgrunnlag beregningsgrunnlagRegel) {
        return jacksonJsonConfig.toJson(beregningsgrunnlagRegel, RegelFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }

    interface RegelFeil extends DeklarerteFeil {
        RegelFeil FEILFACTORY = FeilFactory.create(RegelFeil.class);

        @TekniskFeil(feilkode = "FP-370602", feilmelding = "Kunne ikke serialisere regelinput for beregningsgrunnlag.", logLevel = LogLevel.WARN)
        Feil kanIkkeSerialisereRegelinput(JsonProcessingException e);
    }

}
