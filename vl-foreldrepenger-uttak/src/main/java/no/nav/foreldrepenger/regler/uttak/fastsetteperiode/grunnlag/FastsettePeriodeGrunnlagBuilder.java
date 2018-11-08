package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public final class FastsettePeriodeGrunnlagBuilder {
    private Søknadstype søknadstype;
    private Behandlingtype behandlingtype;
    private LocalDate familiehendelseDato;
    private boolean søkerMor;
    private boolean farRett;
    private boolean morRett;
    private boolean samtykke;
    private boolean aleneomsorg;
    private List<UttakPeriode> uttakPerioder = new ArrayList<>();
    private List<FastsattPeriodeAnnenPart> uttakPerioderAnnenPart = new ArrayList<>();
    private List<GyldigGrunnPeriode> gyldigGrunnPerioder = new ArrayList<>();
    private List<PeriodeUtenOmsorg> perioderUtenOmsorg = new ArrayList<>();
    private List<PeriodeMedFerie> perioderMedFerie = new ArrayList<>();
    private List<PeriodeMedFulltArbeid> perioderMedFulltArbeid = new ArrayList<>();
    private List<PeriodeMedArbeid> perioderMedArbeid = new ArrayList<>();
    private List<PeriodeMedSykdomEllerSkade> perioderMedSykdomEllerSkade = new ArrayList<>();
    private List<PeriodeMedInnleggelse> perioderMedInnleggelse = new ArrayList<>();
    private List<PeriodeMedBarnInnlagt> perioderMedBarnInnlagt = new ArrayList<>();
    private Map<AktivitetIdentifikator, Map<Stønadskontotype, Integer>> kvoter = new HashMap<>();
    private LocalDate førsteLovligeUttaksdag;
    private Arbeidsprosenter arbeidListe = new Arbeidsprosenter();
    private List<AktivitetIdentifikator> aktivitetIdentifikatorer = new ArrayList<>();
    private LocalDate endringssøknadMottattdato;
    private LocalDate revurderingEndringsdato;


    private FastsettePeriodeGrunnlagBuilder() {
    }

    public static FastsettePeriodeGrunnlagBuilder create() {
        return new FastsettePeriodeGrunnlagBuilder();
    }

    public FastsettePeriodeGrunnlagBuilder medSøknadstype(Søknadstype søknadstype) {
        this.søknadstype = søknadstype;
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medBehandlingType(Behandlingtype behandlingtype) {
        this.behandlingtype = behandlingtype;
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medFamiliehendelseDato(LocalDate familiehendelseDato) {
        this.familiehendelseDato = familiehendelseDato;
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medSøkerMor(boolean søkerMor) {
        this.søkerMor = søkerMor;
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medSamtykke(boolean samtykke) {
        this.samtykke = samtykke;
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medFarRett(boolean farRett) {
        this.farRett = farRett;
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medMorRett(boolean morRett) {
        this.morRett = morRett;
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medUtsettelsePeriode(Stønadskontotype stønadskontotype,
                                                                PeriodeKilde periodeKilde,
                                                                LocalDate fom,
                                                                LocalDate tom,
                                                                Utsettelseårsaktype utsettelseårsaktype,
                                                                PeriodeVurderingType periodeResultat,
                                                                boolean samtidigUttak,
                                                                boolean flerbarnsdager) {
        this.uttakPerioder.add(new UtsettelsePeriode(stønadskontotype, periodeKilde, fom, tom, utsettelseårsaktype, periodeResultat,
                samtidigUttak, flerbarnsdager));
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medUtsettelsePeriode(Stønadskontotype stønadskontotype,
                                                                PeriodeKilde periodeKilde,
                                                                LocalDate fom,
                                                                LocalDate tom,
                                                                Utsettelseårsaktype utsettelseårsaktype,
                                                                PeriodeVurderingType periodeResultat) {
        return medUtsettelsePeriode(stønadskontotype, periodeKilde, fom, tom, utsettelseårsaktype, periodeResultat, false, false);
    }

    public FastsettePeriodeGrunnlagBuilder medStønadsPeriode(Stønadskontotype stønadskontotype, PeriodeKilde periodeKilde, LocalDate fom, LocalDate tom, PeriodeVurderingType vurderingType) {
        return medStønadsPeriode(stønadskontotype, periodeKilde, fom, tom, vurderingType, false, false);
    }

    public FastsettePeriodeGrunnlagBuilder medGradertStønadsPeriode(Stønadskontotype stønadskontotype,
                                                                    PeriodeKilde periodeKilde,
                                                                    LocalDate fom,
                                                                    LocalDate tom,
                                                                    List<AktivitetIdentifikator> graderteArbeidsforhold,
                                                                    BigDecimal arbeidsprosent,
                                                                    PeriodeVurderingType periodeResultat,
                                                                    boolean samtidigUttak,
                                                                    boolean flerbarnsdager) {
        this.uttakPerioder.add(StønadsPeriode.medGradering(stønadskontotype, periodeKilde, fom, tom, graderteArbeidsforhold, arbeidsprosent, periodeResultat,
                samtidigUttak, flerbarnsdager));
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medGradertStønadsPeriode(Stønadskontotype stønadskontotype,
                                                                    PeriodeKilde periodeKilde,
                                                                    LocalDate fom,
                                                                    LocalDate tom,
                                                                    List<AktivitetIdentifikator> graderteArbeidsforhold,
                                                                    BigDecimal arbeidsprosent,
                                                                    PeriodeVurderingType periodeResultat) {
        return medGradertStønadsPeriode(stønadskontotype, periodeKilde, fom, tom, graderteArbeidsforhold, arbeidsprosent, periodeResultat, false, false);
    }

    public FastsettePeriodeGrunnlagBuilder medOverføringAvKvote(Stønadskontotype stønadskontotype, PeriodeKilde periodeKilde, LocalDate fom, LocalDate tom,
                                                                OverføringÅrsak overføringÅrsak, PeriodeVurderingType periodeResultat) {
        return medOverføringAvKvote(stønadskontotype, periodeKilde, fom, tom, overføringÅrsak, periodeResultat, false, false);
    }

    public FastsettePeriodeGrunnlagBuilder medOverføringAvKvote(Stønadskontotype stønadskontotype,
                                                                PeriodeKilde periodeKilde,
                                                                LocalDate fom,
                                                                LocalDate tom,
                                                                OverføringÅrsak overføringÅrsak,
                                                                PeriodeVurderingType periodeResultat,
                                                                boolean samtidigUttak,
                                                                boolean flerbarnsdager) {
        this.uttakPerioder.add(StønadsPeriode.medOverføringAvKvote(stønadskontotype, periodeKilde, fom, tom, overføringÅrsak, periodeResultat, samtidigUttak, flerbarnsdager));
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medOppholdPeriode(Stønadskontotype stønadskontotype,
                                                             Oppholdårsaktype oppholdårsaktype, PeriodeKilde periodeKilde,
                                                             LocalDate fom,
                                                             LocalDate tom,
                                                             boolean samtidigUttak,
                                                             boolean flerbarnsdager) {
        this.uttakPerioder.add(new OppholdPeriode(stønadskontotype, periodeKilde, oppholdårsaktype, fom, tom, samtidigUttak, flerbarnsdager));
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medOppholdPeriode(Stønadskontotype stønadskontotype,
                                                             Oppholdårsaktype oppholdårsaktype,
                                                             PeriodeKilde periodeKilde,
                                                             LocalDate fom,
                                                             LocalDate tom) {
        return medOppholdPeriode(stønadskontotype, oppholdårsaktype, periodeKilde, fom, tom, false, false);
    }

    public FastsettePeriodeGrunnlagBuilder medOppholdPeriode(UttakPeriode uttakPeriode) {
        this.uttakPerioder.add(uttakPeriode);
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medSaldo(AktivitetIdentifikator aktivitetIdentifikator, Stønadskontotype stønadskontotype, int antallDager) {
        Map<Stønadskontotype, Integer> kvoterForAktivitet = kvoter.getOrDefault(aktivitetIdentifikator, new EnumMap<>(Stønadskontotype.class));
        kvoterForAktivitet.put(stønadskontotype, antallDager);
        this.kvoter.put(aktivitetIdentifikator, kvoterForAktivitet);
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medSaldo(Stønadskontotype stønadskontotype, int antallDager) {
        if (aktivitetIdentifikatorer.size() != 1) {
            throw new IllegalArgumentException("Kan ikke legge til saldo uten aktivitet hvis ikke akkurat 1 aktivitet. Aktiviteter: " + aktivitetIdentifikatorer);
        }
        return medSaldo(aktivitetIdentifikatorer.get(0), stønadskontotype, antallDager);
    }

    public FastsettePeriodeGrunnlagBuilder medGyldigGrunnForTidligOppstartPeriode(LocalDate fom, LocalDate tom) {
        this.gyldigGrunnPerioder.add(new GyldigGrunnPeriode(fom, tom));
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medPeriodeUtenOmsorg(LocalDate fom, LocalDate tom) {
        this.perioderUtenOmsorg.add(new PeriodeUtenOmsorg(fom, tom));
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medFørsteLovligeUttaksdag(LocalDate førsteLovligeUttaksdag) {
        this.førsteLovligeUttaksdag = førsteLovligeUttaksdag;
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medPeriodeMedFerie(PeriodeMedFerie periodeMedFerie) {
        this.perioderMedFerie.add(periodeMedFerie);
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medPeriodeMedFulltArbeid(PeriodeMedFulltArbeid periodeMedFulltArbeid) {
        this.perioderMedFulltArbeid.add(periodeMedFulltArbeid);
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medPeriodeMedArbeid(PeriodeMedArbeid periodeMedArbeid) {
        this.perioderMedArbeid.add(periodeMedArbeid);
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medPeriodeMedSykdomEllerSkade(PeriodeMedSykdomEllerSkade periodeMedSykdomEllerSkade) {
        this.perioderMedSykdomEllerSkade.add(periodeMedSykdomEllerSkade);
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medPeriodeMedInnleggelse(PeriodeMedInnleggelse periodeMedInnleggelse) {
        this.perioderMedInnleggelse.add(periodeMedInnleggelse);
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medPeriodeMedBarnInnlagt(PeriodeMedBarnInnlagt periodeMedBarnInnlagt) {
        this.perioderMedBarnInnlagt.add(periodeMedBarnInnlagt);
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medArbeid(AktivitetIdentifikator aktivitetIdentifikator, ArbeidTidslinje arbeidTidslinje) {
        arbeidListe.leggTil(aktivitetIdentifikator, arbeidTidslinje);
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medAktivitetIdentifikator(AktivitetIdentifikator aktivitetIdentifikator) {
        this.aktivitetIdentifikatorer = Collections.singletonList(aktivitetIdentifikator);
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medAktivitetIdentifikatorer(List<AktivitetIdentifikator> aktivitetIdentifikatorer) {
        this.aktivitetIdentifikatorer = aktivitetIdentifikatorer;
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medUttakPeriodeForAnnenPart(FastsattPeriodeAnnenPart uttakPeriode) {
        this.uttakPerioderAnnenPart.add(uttakPeriode);
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medEndringssøknadMottattdato(LocalDate endringssøknadMottattdato) {
        this.endringssøknadMottattdato = endringssøknadMottattdato;
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medStønadsPeriode(Stønadskontotype stønadskontotype,PeriodeKilde periodeKilde, LocalDate fom, LocalDate tom,PeriodeVurderingType periodeResultat,
                                                             boolean samtidigUttak,
                                                             boolean flerbarnsdager) {
        StønadsPeriode stønadsPeriode = new StønadsPeriode(stønadskontotype, periodeKilde, fom, tom, samtidigUttak, flerbarnsdager);
        stønadsPeriode.setPeriodeVurderingType(periodeResultat);
        this.uttakPerioder.add(stønadsPeriode);
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medRevurderingEndringsdato(LocalDate endringsdato) {
        this.revurderingEndringsdato = endringsdato;
        return this;
    }

    public FastsettePeriodeGrunnlagBuilder medAleneomsorg(boolean aleneomsorg) {
        this.aleneomsorg = aleneomsorg;
        return this;
    }

    public FastsettePeriodeGrunnlag build() {
        List<AktivitetIdentifikator> aktivitetIdentifikatorerAnnenPart = uttakPerioderAnnenPart.stream()
                .flatMap(p -> p.getUttakPeriodeAktiviteter().stream())
                .map(UttakPeriodeAktivitet::getAktivitetIdentifikator)
                .distinct()
                .collect(Collectors.toList());

        FastsettePeriodeGrunnlagImpl fastsettePeriodeGrunnlag = new FastsettePeriodeGrunnlagImpl();
        fastsettePeriodeGrunnlag.setSøknadstype(søknadstype);
        fastsettePeriodeGrunnlag.setBehandlingtype(behandlingtype);
        fastsettePeriodeGrunnlag.setFamiliehendelseDato(familiehendelseDato);
        fastsettePeriodeGrunnlag.setSøkerMor(søkerMor);
        fastsettePeriodeGrunnlag.setSamtykke(samtykke);
        fastsettePeriodeGrunnlag.setFarRett(farRett);
        fastsettePeriodeGrunnlag.setMorRett(morRett);
        fastsettePeriodeGrunnlag.setUttakPerioder(uttakPerioder);
        fastsettePeriodeGrunnlag.setTrekkdagertilstand(new Trekkdagertilstand(kvoter, aktivitetIdentifikatorer, aktivitetIdentifikatorerAnnenPart, uttakPerioderAnnenPart));
        fastsettePeriodeGrunnlag.setGyldigGrunnPerioder(gyldigGrunnPerioder);
        fastsettePeriodeGrunnlag.setPerioderUtenOmsorg(perioderUtenOmsorg);
        fastsettePeriodeGrunnlag.setFørsteLovligeUttaksdag(førsteLovligeUttaksdag);
        fastsettePeriodeGrunnlag.setPerioderMedFerie(perioderMedFerie);
        fastsettePeriodeGrunnlag.setPerioderMedFulltArbeid(perioderMedFulltArbeid);
        fastsettePeriodeGrunnlag.setPerioderMedArbeid(perioderMedArbeid);
        fastsettePeriodeGrunnlag.setPerioderMedSykdomEllerSkade(perioderMedSykdomEllerSkade);
        fastsettePeriodeGrunnlag.setPerioderMedInnleggelse(perioderMedInnleggelse);
        fastsettePeriodeGrunnlag.setPerioderMedBarnInnlagt(perioderMedBarnInnlagt);
        fastsettePeriodeGrunnlag.setArbeid(arbeidListe);
        fastsettePeriodeGrunnlag.setAktiviteter(aktivitetIdentifikatorer);
        fastsettePeriodeGrunnlag.setEndringssøknadMottattdato(endringssøknadMottattdato);
        fastsettePeriodeGrunnlag.setRevurderingEndringsdato(revurderingEndringsdato);
        fastsettePeriodeGrunnlag.setAleneomsorg(aleneomsorg);
        return fastsettePeriodeGrunnlag;
    }
}
