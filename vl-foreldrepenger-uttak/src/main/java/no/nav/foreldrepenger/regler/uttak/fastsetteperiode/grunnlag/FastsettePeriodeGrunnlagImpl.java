package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

class FastsettePeriodeGrunnlagImpl implements FastsettePeriodeGrunnlag {

    private Søknadstype søknadstype;
    private Behandlingtype behandlingtype;
    private LocalDate familiehendelseDato;
    private LocalDate førsteLovligeUttaksdag;
    private boolean søkerMor;
    private boolean farRett;
    private boolean morRett;
    private boolean samtykke;
    private boolean aleneomsorg;

    private List<UttakPeriode> uttakPerioder = new ArrayList<>();
    private DokumentasjonPerioder dokumentasjonPerioder = new DokumentasjonPerioder();
    private List<PeriodeMedFulltArbeid> perioderMedFulltArbeid = new ArrayList<>();
    private List<PeriodeMedArbeid> perioderMedArbeid = new ArrayList<>();
    private Arbeidsprosenter arbeidsprosenter = new Arbeidsprosenter();
    private List<AktivitetIdentifikator> aktiviteter;
    private Trekkdagertilstand trekkdagertilstand;

    private int aktuellPeriodeIndex = 0;
    private LocalDate endringssøknadMottattdato;
    private LocalDate revurderingEndringsdato;

    @Override
    public Optional<UttakPeriode> getAktuellPeriode() {
        if (aktuellPeriodeIndex >= uttakPerioder.size()) {
            return Optional.empty();
        }
        return Optional.of(uttakPerioder.get(aktuellPeriodeIndex));
    }

    @Override
    public UttakPeriode hentPeriodeUnderBehandling() {
        return getAktuellPeriode().orElseThrow(() -> new IllegalArgumentException("Fant ikke periode som skal avklares."));
    }

    @Override
    public List<GyldigGrunnPeriode> getAktuelleGyldigeGrunnPerioder() {
        Optional<UttakPeriode> aktuellPeriodeOptional = getAktuellPeriode();
        if (aktuellPeriodeOptional.isPresent()) {
            UttakPeriode aktuellPeriode = aktuellPeriodeOptional.get();

            return dokumentasjonPerioder.getGyldigGrunnPerioder().stream()
                    .filter(p -> p.overlapper(aktuellPeriode))
                    .sorted(Comparator.comparing(Periode::getFom))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<AktivitetIdentifikator> getAktiviteter() {
        return aktiviteter;
    }

    @Override
    public Arbeidsprosenter getArbeidsprosenter() {
        return arbeidsprosenter;
    }

    @Override
    public PeriodeMedFulltArbeid[] getPerioderMedFulltArbeid() {
        return perioderMedFulltArbeid.toArray(new PeriodeMedFulltArbeid[0]);
    }

    @Override
    public PeriodeMedArbeid[] getPerioderMedArbeid() {
        return perioderMedArbeid.toArray(new PeriodeMedArbeid[0]);
    }

    @Override
    public PeriodeMedFerie[] getPerioderMedFerie() {
        return dokumentasjonPerioder.getPerioderMedFerie().toArray(new PeriodeMedFerie[0]);
    }

    @Override
    public PeriodeMedSykdomEllerSkade[] getPerioderMedSykdomEllerSkade() {
        return dokumentasjonPerioder.getPerioderMedSykdomEllerSkade().toArray(new PeriodeMedSykdomEllerSkade[0]);
    }

    @Override
    public PeriodeMedInnleggelse[] getPerioderMedInnleggelse() {
        return dokumentasjonPerioder.getPerioderMedInnleggelse().toArray(new PeriodeMedInnleggelse[0]);
    }

    @Override
    public PeriodeMedBarnInnlagt[] getPerioderMedBarnInnlagt() {
        return dokumentasjonPerioder.getPerioderMedBarnInnlagt().toArray(new PeriodeMedBarnInnlagt[0]);
    }

    @Override
    public List<UttakPeriode> getPerioderMedAnnenForelderInnlagt() {
        return getPerioderMedOverføringÅrsak(OverføringÅrsak.INNLEGGELSE);
    }

    @Override
    public List<UttakPeriode> getPerioderMedAnnenForelderSykdomEllerSkade() {
        return getPerioderMedOverføringÅrsak(OverføringÅrsak.SYKDOM_ELLER_SKADE);
    }

    private List<UttakPeriode> getPerioderMedOverføringÅrsak(OverføringÅrsak årsak) {
        return uttakPerioder.stream().filter(periode -> Objects.equals(periode.getOverføringÅrsak(), årsak)
                && PeriodeVurderingType.avklart(periode.getPeriodeVurderingType())).collect(Collectors.toList());
    }


    @Override
    public Stønadskontotype getStønadskontotype() {
        Optional<UttakPeriode> aktuellPeriode = getAktuellPeriode();
        if (!aktuellPeriode.isPresent()) {
            return Stønadskontotype.UKJENT;
        }
        return aktuellPeriode.get().getStønadskontotype();
    }

    @Override
    public Søknadstype getSøknadstype() {
        return søknadstype;
    }

    @Override
    public Behandlingtype getBehandlingtype() {
        return behandlingtype;
    }

    @Override
    public LocalDate getFamiliehendelse() {
        return familiehendelseDato;
    }

    @Override
    public boolean isSøkerMor() {
        return søkerMor;
    }

    @Override
    public boolean isSamtykke() {
        return samtykke;
    }

    @Override
    public boolean isFarRett() {
        return farRett;
    }

    @Override
    public boolean isMorRett() {
        return morRett;
    }

    @Override
    public void medOppholdPerioder(List<OppholdPeriode> oppholdPerioder) {
        if (aktuellPeriodeIndex > 0) {
            throw new IllegalStateException("Kan ikke legge til oppholdPerioder etter at fastsettelse av uttaksperioder har begynt");
        }

        List<UttakPeriode> opprinneligePerioder = new ArrayList<>();
        opprinneligePerioder.addAll(uttakPerioder);

        uttakPerioder.clear();
        uttakPerioder.addAll(Stream.of(opprinneligePerioder, oppholdPerioder)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(Periode::getFom))
                .collect(Collectors.toList()));
    }

    @Override
    public void knekkPerioder(Collection<LocalDate> knekkpunkter) {
        this.uttakPerioder = knekkPerioder(uttakPerioder, knekkpunkter);
    }

    @Override
    public void knekkPeriode(LocalDate knekkpunkt, Perioderesultattype perioderesultattype, Årsak årsak) {
        knekkPeriode(knekkpunkt, perioderesultattype, årsak, null);
    }

    @Override
    public void knekkPeriode(LocalDate knekkpunkt, Perioderesultattype perioderesultattype, Årsak årsak, Avkortingårsaktype avkortingårsaktype) {
        UttakPeriode aktuellUttakPeriode = hentPeriodeUnderBehandling();
        if (!aktuellUttakPeriode.overlapper(knekkpunkt)) {
            throw new IllegalArgumentException("Knekkpunkt utenfor aktuell periode. Knekkpunkt: " + knekkpunkt + ". Aktuell periode: " + aktuellUttakPeriode);
        }
        if (knekkpunkt.isEqual(aktuellUttakPeriode.getFom())) {
            throw new IllegalArgumentException("Knekkpunkt kan ikke være samme dato som fom. for aktuell periode " + aktuellUttakPeriode.getFom());
        }
        UttakPeriode førKnekk = aktuellUttakPeriode.kopiMedNyPeriode(aktuellUttakPeriode.getFom(), knekkpunkt.minusDays(1));
        førKnekk.setPerioderesultattype(perioderesultattype);
        førKnekk.setAvkortingårsaktype(avkortingårsaktype);
        førKnekk.setÅrsak(årsak);
        uttakPerioder.set(aktuellPeriodeIndex, førKnekk);

        UttakPeriode etterKnekk = aktuellUttakPeriode.kopiMedNyPeriode(knekkpunkt, aktuellUttakPeriode.getTom());
        uttakPerioder.add(aktuellPeriodeIndex + 1, etterKnekk);
    }

    void setSøknadstype(Søknadstype søknadstype) {
        this.søknadstype = søknadstype;
    }

    void setBehandlingtype(Behandlingtype behandlingtype) {
        this.behandlingtype = behandlingtype;
    }

    void setFamiliehendelseDato(LocalDate familiehendelseDato) {
        this.familiehendelseDato = familiehendelseDato;
    }

    void setSøkerMor(boolean søkerMor) {
        this.søkerMor = søkerMor;
    }

    void setSamtykke(boolean samtykke) {
        this.samtykke = samtykke;
    }

    void setUttakPerioder(List<UttakPeriode> uttakPerioder) {
        this.uttakPerioder = uttakPerioder;
    }

    void setTrekkdagertilstand(Trekkdagertilstand trekkdagertilstand) {
        this.trekkdagertilstand = trekkdagertilstand;
    }

    @Override
    public UttakPeriode[] getUttakPerioder() {
        return this.uttakPerioder.toArray(new UttakPeriode[0]);
    }

    void setGyldigGrunnPerioder(List<GyldigGrunnPeriode> gyldigGrunnPerioder) {
        dokumentasjonPerioder.setGyldigGrunnPerioder(gyldigGrunnPerioder);
    }

    @Override
    public GyldigGrunnPeriode[] getGyldigGrunnPerioder() {
        return dokumentasjonPerioder.getGyldigGrunnPerioder().toArray(new GyldigGrunnPeriode[0]);
    }

    public void setPerioderUtenOmsorg(List<PeriodeUtenOmsorg> perioderUtenOmsorg) {
        dokumentasjonPerioder.setPerioderUtenOmsorg(perioderUtenOmsorg);
    }

    @Override
    public PeriodeUtenOmsorg[] getPerioderUtenOmsorg() {
        return dokumentasjonPerioder.getPerioderUtenOmsorg().toArray(new PeriodeUtenOmsorg[0]);
    }

    @Override
    public LocalDate getFørsteLovligeUttaksdag() {
        return førsteLovligeUttaksdag;
    }

    void setFørsteLovligeUttaksdag(LocalDate førsteLovligeUttaksdag) {
        this.førsteLovligeUttaksdag = førsteLovligeUttaksdag;
    }

    void setPerioderMedFerie(List<PeriodeMedFerie> perioderMedFerie) {
        this.dokumentasjonPerioder.setPerioderMedFerie(perioderMedFerie);
    }

    void setPerioderMedFulltArbeid(List<PeriodeMedFulltArbeid> perioderMedFulltArbeid) {
        this.perioderMedFulltArbeid = perioderMedFulltArbeid;
    }

    void setPerioderMedArbeid(List<PeriodeMedArbeid> perioderMedArbeid) {
        this.perioderMedArbeid = perioderMedArbeid;
    }

    void setPerioderMedSykdomEllerSkade(List<PeriodeMedSykdomEllerSkade> perioderMedSykdomEllerSkade) {
        dokumentasjonPerioder.setPerioderMedSykdomEllerSkade(perioderMedSykdomEllerSkade);
    }

    void setPerioderMedInnleggelse(List<PeriodeMedInnleggelse> perioderMedInnleggelse) {
        dokumentasjonPerioder.setPerioderMedInnleggelse(perioderMedInnleggelse);
    }

    @Override
    public LocalDate getMaksgrenseForLovligeUttaksdag(Konfigurasjon konfigurasjon) {
        Period maksGrenseRelativTilFamiliehendelse = konfigurasjon.getParameter(Parametertype.GRENSE_ETTER_FØDSELSDATO, Period.class, familiehendelseDato);
        return familiehendelseDato.plus(maksGrenseRelativTilFamiliehendelse);
    }

    void setPerioderMedBarnInnlagt(List<PeriodeMedBarnInnlagt> perioderMedBarnInnlagt) {
        dokumentasjonPerioder.setPerioderMedBarnInnlagt(perioderMedBarnInnlagt);
    }

    void setArbeid(Arbeidsprosenter arbeidsprosenter) {
        this.arbeidsprosenter = arbeidsprosenter;
    }

    void setAktiviteter(List<AktivitetIdentifikator> aktiviteter) {
        this.aktiviteter = aktiviteter;
    }

    @Override
    public void nestePeriode() {
        aktuellPeriodeIndex++;
    }

    static List<UttakPeriode> knekkPerioder(List<UttakPeriode> uttakPerioder, Collection<LocalDate> knekkpunkter) {
        TreeSet<LocalDate> sorterteKnekkpunkter = new TreeSet<>(knekkpunkter);
        List<UttakPeriode> resultat = new ArrayList<>(uttakPerioder.size() + sorterteKnekkpunkter.size());
        for (UttakPeriode periode : uttakPerioder) {
            LocalDate nesteFom = periode.getFom();
            for (LocalDate knekk : sorterteKnekkpunkter.subSet(periode.getFom(), false, periode.getTom(), true)) {
                resultat.add(periode.kopiMedNyPeriode(nesteFom, knekk.minusDays(1)));
                nesteFom = knekk;
            }
            resultat.add(periode.kopiMedNyPeriode(nesteFom, periode.getTom()));
        }
        return resultat;
    }

    @Override
    public Set<Stønadskontotype> getGyldigeStønadskontotyper() {
        return trekkdagertilstand.getStønadskontotyper();
    }

    @Override
    public boolean erEndringssøknad() {
        return endringssøknadMottattdato != null;
    }

    @Override
    public LocalDate getRevurderingEndringsdato() {
        return revurderingEndringsdato;
    }

    @Override
    public boolean erRevurdering() {
        return revurderingEndringsdato != null;
    }

    void setFarRett(boolean farRett) {
        this.farRett = farRett;
    }

    void setMorRett(boolean morRett) {
        this.morRett = morRett;
    }

    public void setEndringssøknadMottattdato(LocalDate endringssøknadMottattdato) {
        this.endringssøknadMottattdato = endringssøknadMottattdato;
    }

    @Override
    public LocalDate getEndringssøknadMottattdato() {
        return endringssøknadMottattdato;
    }

    public void setRevurderingEndringsdato(LocalDate revurderingEndringsdato) {
        this.revurderingEndringsdato = revurderingEndringsdato;
    }

    @Override
    public Trekkdagertilstand getTrekkdagertilstand() {
        return trekkdagertilstand;
    }

    @Override
    public boolean harAleneomsorg() {
        return aleneomsorg;
    }

    public void setAleneomsorg(boolean aleneomsorg) {
        this.aleneomsorg = aleneomsorg;
    }
}
