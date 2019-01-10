package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.registerinnhenting.EndringsresultatSjekker;

@Dependent
class EndringsresultatSjekkerImpl implements EndringsresultatSjekker {

    private PersonopplysningTjeneste personopplysningTjeneste;
    private MedlemTjeneste medlemTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    private OpptjeningRepository opptjeningRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private UttakRepository uttakRepository;
    private BeregningsresultatRepository beregningsresultatRepository;
    private BehandlingRepository behandlingRepository;

    EndringsresultatSjekkerImpl() {
        // For CDI
    }

    @Inject
    public EndringsresultatSjekkerImpl(PersonopplysningTjeneste personopplysningTjeneste,
                                       MedlemTjeneste medlemTjeneste,
                                       InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                       ResultatRepositoryProvider resultatProvider) {
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.medlemTjeneste = medlemTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.opptjeningRepository = resultatProvider.getOpptjeningRepository();
        this.beregningsgrunnlagRepository = resultatProvider.getBeregningsgrunnlagRepository();
        this.uttakRepository = resultatProvider.getUttakRepository();
        this.beregningsresultatRepository = resultatProvider.getBeregningsresultatRepository();
        this.behandlingRepository = resultatProvider.getBehandlingRepository();
    }

    static Long mapFraLocalDateTimeTilLong(LocalDateTime ldt) {
        ZonedDateTime zdt = ldt.atZone(ZoneId.of("Europe/Paris"));
        return zdt.toInstant().toEpochMilli();
    }

    @Override
    public EndringsresultatSnapshot opprettEndringsresultatPåBehandlingsgrunnlagSnapshot(Behandling behandling) {
        EndringsresultatSnapshot snapshot = EndringsresultatSnapshot.opprett();
        snapshot.leggTil(personopplysningTjeneste.finnAktivGrunnlagId(behandling));
        snapshot.leggTil(medlemTjeneste.finnAktivGrunnlagId(behandling));
        snapshot.leggTil(inntektArbeidYtelseTjeneste.finnAktivAggregatId(behandling));

        return snapshot;
    }

    @Override
    public EndringsresultatDiff finnSporedeEndringerPåBehandlingsgrunnlag(Behandling behandling, EndringsresultatSnapshot idSnapshotFør) {
        FagsakYtelseType ytelseType = behandling.getFagsak().getYtelseType();
        boolean kunSporedeEndringer = true;
        // Del 1: Finn diff mellom grunnlagets id før og etter oppdatering
        EndringsresultatSnapshot idSnapshotNå = opprettEndringsresultatPåBehandlingsgrunnlagSnapshot(behandling);
        EndringsresultatDiff idDiff = idSnapshotNå.minus(idSnapshotFør);

        // Del 2: Transformer diff på grunnlagets id til diff på grunnlagets sporede endringer (@ChangeTracked)
        // FIXME SP : Legge inn sykemelding ?
        EndringsresultatDiff sporedeEndringerDiff = EndringsresultatDiff.opprettForSporingsendringer();
        idDiff.hentDelresultat(PersonInformasjon.class).ifPresent(idEndring ->
            sporedeEndringerDiff.leggTilSporetEndring(idEndring, () -> personopplysningTjeneste.diffResultat(idEndring, ytelseType, kunSporedeEndringer)));
        idDiff.hentDelresultat(MedlemskapAggregat.class).ifPresent(idEndring ->
            sporedeEndringerDiff.leggTilSporetEndring(idEndring, () -> medlemTjeneste.diffResultat(idEndring, ytelseType, kunSporedeEndringer)));
        idDiff.hentDelresultat(InntektArbeidYtelseGrunnlag.class).ifPresent(idEndring ->
            sporedeEndringerDiff.leggTilSporetEndring(idEndring, () -> inntektArbeidYtelseTjeneste.diffResultat(idEndring, ytelseType, kunSporedeEndringer)));
        return sporedeEndringerDiff;
    }

    @Override
    public EndringsresultatSnapshot opprettEndringsresultatIdPåBehandlingSnapshot(Behandling behandling) {
        EndringsresultatSnapshot snapshot = opprettEndringsresultatPåBehandlingsgrunnlagSnapshot(behandling);

        Optional<Behandlingsresultat> behandlingsresultat = behandlingRepository.hentResultatHvisEksisterer(behandling.getId());
        snapshot.leggTil(opptjeningRepository.finnAktivGrunnlagId(behandlingsresultat.orElse(null)));
        snapshot.leggTil(beregningsgrunnlagRepository.finnAktivAggregatId(behandling));
        snapshot.leggTil(uttakRepository.finnAktivAggregatId(behandling));
        snapshot.leggTil(uttakRepository.finnAktivUttakPeriodeGrenseAggregatId(behandlingsresultat.orElse(null)));

        // Resultatstrukturene nedenfor støtter ikke paradigme med "aktivt" grunnlag som kan identifisere med id
        // Aksepterer her at endringssjekk heller utledes av deres tidsstempel forutsatt at metoden ikke brukes i
        // kritiske endringssjekker. Håp om at de i fremtiden vil støtte paradigme.
        snapshot.leggTil(lagVilkårResultatIdSnapshotAvTidsstempel(behandling));
        snapshot.leggTil(lagBeregningResultatIdSnapshotAvTidsstempel(behandling));

        return snapshot;
    }

    @Override
    public EndringsresultatDiff finnIdEndringerPåBehandling(Behandling behandling, EndringsresultatSnapshot idSnapshotFør) {
        EndringsresultatSnapshot idSnapshotNå = opprettEndringsresultatIdPåBehandlingSnapshot(behandling);
        return idSnapshotNå.minus(idSnapshotFør);
    }

    private EndringsresultatSnapshot lagVilkårResultatIdSnapshotAvTidsstempel(Behandling behandling) {
        return behandlingRepository.hentResultatHvisEksisterer(behandling.getId())
            .map(Behandlingsresultat::getVilkårResultat)
            .map(vilkårResultat ->
                EndringsresultatSnapshot.medSnapshot(VilkårResultat.class, hentLongVerdiAvEndretTid(vilkårResultat)))
            .orElse(EndringsresultatSnapshot.utenSnapshot(VilkårResultat.class));
    }

    private EndringsresultatSnapshot lagBeregningResultatIdSnapshotAvTidsstempel(Behandling behandling) {
        return behandlingRepository.hentResultatHvisEksisterer(behandling.getId())
            .flatMap(br -> beregningsresultatRepository.hentHvisEksistererFor(br))
            .map(beregningResultat ->
                EndringsresultatSnapshot.medSnapshot(BeregningsResultat.class, hentLongVerdiAvEndretTid(beregningResultat)))
            .orElse(EndringsresultatSnapshot.utenSnapshot(BeregningsResultat.class));
    }

    private Long hentLongVerdiAvEndretTid(BaseEntitet entitet) {
        LocalDateTime endretTidspunkt = entitet.getOpprettetTidspunkt();
        if (entitet.getEndretTidspunkt() != null) {
            endretTidspunkt = entitet.getEndretTidspunkt();
        }
        return mapFraLocalDateTimeTilLong(endretTidspunkt);
    }
}
