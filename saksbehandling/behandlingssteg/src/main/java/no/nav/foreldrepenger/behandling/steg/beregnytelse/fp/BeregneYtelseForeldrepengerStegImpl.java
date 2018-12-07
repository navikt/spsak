package no.nav.foreldrepenger.behandling.steg.beregnytelse.fp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.beregnytelse.api.BeregneYtelseSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.SykefraværGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.SykefraværRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.domene.beregning.ytelse.BeregnFeriepengerTjeneste;
import no.nav.foreldrepenger.domene.beregning.ytelse.FastsettBeregningsresultatTjeneste;
import no.nav.foreldrepenger.domene.beregning.ytelse.FinnEndringsdatoBeregningsresultatFPTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

/**
 * Steg for å beregne tilkjent ytelse (for Foreldrepenger).
 */
@BehandlingStegRef(kode = "BERYT")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class BeregneYtelseForeldrepengerStegImpl implements BeregneYtelseSteg {

    private BehandlingRepository behandlingRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private BeregningsresultatFPRepository beregningsresultatFPRepository;
    private FastsettBeregningsresultatTjeneste fastsettBeregningsresultatTjeneste;
    private UttakRepository uttakRepository;
    private BeregnFeriepengerTjeneste beregnFeriepengerTjeneste;
    private FinnEndringsdatoBeregningsresultatFPTjeneste finnEndringsdatoBeregningsresultatFPTjeneste;
    private SykefraværRepository sykefraværRepository;

    BeregneYtelseForeldrepengerStegImpl() {
        // for CDI proxy
    }

    @Inject
    BeregneYtelseForeldrepengerStegImpl(BehandlingRepositoryProvider repositoryProvider,
                                        FastsettBeregningsresultatTjeneste fastsettBeregningsresultatTjeneste,
                                        BeregnFeriepengerTjeneste beregnFeriepengerTjeneste,
                                        FinnEndringsdatoBeregningsresultatFPTjeneste finnEndringsdatoBeregningsresultatFPTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
        this.fastsettBeregningsresultatTjeneste = fastsettBeregningsresultatTjeneste;
        this.finnEndringsdatoBeregningsresultatFPTjeneste = finnEndringsdatoBeregningsresultatFPTjeneste;
        this.uttakRepository = repositoryProvider.getUttakRepository();
        this.sykefraværRepository = repositoryProvider.getSykefraværRepository();
        this.beregnFeriepengerTjeneste = beregnFeriepengerTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {

        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);

        // FIXME SP : Gjør dette midlertidig for å få til et vedtak..
        lagreNedPerioderFraSøknaden(behandling);

        UttakResultatEntitet uttakResultat = uttakRepository.hentUttakResultat(behandling);

        // Kalle regeltjeneste
        BeregningsresultatFP beregningsresultat = fastsettBeregningsresultatTjeneste.fastsettBeregningsresultat(beregningsgrunnlag, uttakResultat, behandling);

        // Beregn feriepenger
        beregnFeriepengerTjeneste.beregnFeriepenger(behandling, beregningsresultat, beregningsgrunnlag);

        // Sett endringsdato
        if (behandling.erRevurdering()) {
            Optional<LocalDate> endringsDato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(behandling, beregningsresultat);
            endringsDato.ifPresent(endringsdato -> BeregningsresultatFP.builder(beregningsresultat).medEndringsdato(endringsdato));
        }

        // Lagre beregningsresultat
        beregningsresultatFPRepository.lagre(behandling, beregningsresultat);

        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    private void lagreNedPerioderFraSøknaden(Behandling behandling) {
        Optional<UttakResultatEntitet> uttakResultatEntitet = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        if (uttakResultatEntitet.isPresent()) {
            return;
        }
        Optional<SykefraværGrunnlag> sykefraværGrunnlagOpt = sykefraværRepository.hentHvisEksistererFor(behandling.getId());
        if (!sykefraværGrunnlagOpt.isPresent()) {
            return;
        }
        SykefraværGrunnlag sykefraværGrunnlag = sykefraværGrunnlagOpt.get();
        UttakResultatPerioderEntitet resultatPerioder = new UttakResultatPerioderEntitet();
        List<SykefraværPeriode> perioder = sykefraværGrunnlag.getSykefravær().getPerioder();
        perioder.stream().filter(p -> SykefraværPeriodeType.SYKEMELDT.equals(p.getType())).forEach(pp -> {
            UttakResultatPeriodeEntitet periode = new UttakResultatPeriodeEntitet.Builder(pp.getPeriode().getFomDato(), pp.getPeriode().getTomDato())
                .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
                .build();

            UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
                .medArbeidsforhold((VirksomhetEntitet) pp.getArbeidsgiver().getVirksomhet(), ArbeidsforholdRef.ref(""))
                .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
                .build();
            UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(periode, uttakAktivitet)
                .medTrekkdager(Long.valueOf(utledTrekkDager(pp.getPeriode())).intValue())
                .medArbeidsprosent(pp.getArbeidsgrad().getVerdi())
                .medUtbetalingsprosent(BigDecimal.valueOf(100))
                .build();
            periode.leggTilAktivitet(periodeAktivitet);
            resultatPerioder.leggTilPeriode(periode);
        });
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, resultatPerioder);
    }

    private long utledTrekkDager(DatoIntervallEntitet periode) {
        return Periode.of(periode.getFomDato(), periode.getTomDato()).getVarighetDager();
    }

    @Override
    public void vedHoppOverBakover(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell, BehandlingStegType tilSteg, BehandlingStegType fraSteg) {
        beregningsresultatFPRepository.deaktiverBeregningsresultatFP(behandling, kontekst.getSkriveLås());
    }


}
