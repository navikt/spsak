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
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.registerinnhenting.EndringsresultatSjekker;
import no.nav.foreldrepenger.domene.ytelsefordeling.YtelseFordelingTjeneste;

@Dependent
class EndringsresultatSjekkerImpl implements EndringsresultatSjekker {

    private PersonopplysningTjeneste personopplysningTjeneste;
    private FamilieHendelseTjeneste familieHendelseTjeneste;
    private MedlemTjeneste medlemTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private YtelseFordelingTjeneste ytelseFordelingTjeneste;

    private OpptjeningRepository opptjeningRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private UttakRepository uttakRepository;

    EndringsresultatSjekkerImpl() {
        // For CDI
    }

    @Inject
    public EndringsresultatSjekkerImpl(PersonopplysningTjeneste personopplysningTjeneste,
                                       FamilieHendelseTjeneste familieHendelseTjeneste, MedlemTjeneste medlemTjeneste,
                                       InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                       YtelseFordelingTjeneste ytelseFordelingTjeneste,
                                       BehandlingRepositoryProvider provider) {
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.familieHendelseTjeneste = familieHendelseTjeneste;
        this.medlemTjeneste = medlemTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.ytelseFordelingTjeneste = ytelseFordelingTjeneste;
        this.opptjeningRepository = provider.getOpptjeningRepository();
        this.beregningsgrunnlagRepository = provider.getBeregningsgrunnlagRepository();
        this.uttakRepository = provider.getUttakRepository();
    }

    @Override
    public EndringsresultatSnapshot opprettEndringsresultatPåBehandlingsgrunnlagSnapshot(Behandling behandling) {
        EndringsresultatSnapshot snapshot = EndringsresultatSnapshot.opprett();
        snapshot.leggTil(personopplysningTjeneste.finnAktivGrunnlagId(behandling));
        snapshot.leggTil(familieHendelseTjeneste.finnAktivAggregatId(behandling));
        snapshot.leggTil(medlemTjeneste.finnAktivGrunnlagId(behandling));
        snapshot.leggTil(inntektArbeidYtelseTjeneste.finnAktivAggregatId(behandling));
        snapshot.leggTil(ytelseFordelingTjeneste.finnAktivAggregatId(behandling));

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
        EndringsresultatDiff sporedeEndringerDiff = EndringsresultatDiff.opprettForSporingsendringer();
        idDiff.hentDelresultat(PersonInformasjon.class).ifPresent(idEndring ->
            sporedeEndringerDiff.leggTilSporetEndring(idEndring, () -> personopplysningTjeneste.diffResultat(idEndring, ytelseType, kunSporedeEndringer)));
        idDiff.hentDelresultat(FamilieHendelseGrunnlag.class).ifPresent(idEndring ->
            sporedeEndringerDiff.leggTilSporetEndring(idEndring, () -> familieHendelseTjeneste.diffResultat(idEndring, ytelseType, kunSporedeEndringer)));
        idDiff.hentDelresultat(MedlemskapAggregat.class).ifPresent(idEndring ->
            sporedeEndringerDiff.leggTilSporetEndring(idEndring, () -> medlemTjeneste.diffResultat(idEndring, ytelseType, kunSporedeEndringer)));
        idDiff.hentDelresultat(InntektArbeidYtelseGrunnlag.class).ifPresent(idEndring ->
            sporedeEndringerDiff.leggTilSporetEndring(idEndring, () -> inntektArbeidYtelseTjeneste.diffResultat(idEndring, ytelseType, kunSporedeEndringer)));
        idDiff.hentDelresultat(YtelseFordelingAggregat.class).ifPresent(idEndring ->
            sporedeEndringerDiff.leggTilSporetEndring(idEndring, () -> ytelseFordelingTjeneste.diffResultat(idEndring, ytelseType, kunSporedeEndringer)));
        return sporedeEndringerDiff;
    }

    @Override
    public EndringsresultatSnapshot opprettEndringsresultatIdPåBehandlingSnapshot(Behandling behandling) {
        EndringsresultatSnapshot snapshot = opprettEndringsresultatPåBehandlingsgrunnlagSnapshot(behandling);

        snapshot.leggTil(opptjeningRepository.finnAktivGrunnlagId(behandling));
        snapshot.leggTil(beregningsgrunnlagRepository.finnAktivAggregatId(behandling));
        snapshot.leggTil(uttakRepository.finnAktivAggregatId(behandling));
        snapshot.leggTil(uttakRepository.finnAktivUttakPeriodeGrenseAggregatId(behandling));

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
       return Optional.ofNullable(behandling.getBehandlingsresultat())
                 .map(Behandlingsresultat::getVilkårResultat)
                 .map(vilkårResultat ->
                     EndringsresultatSnapshot.medSnapshot(VilkårResultat.class, hentLongVerdiAvEndretTid(vilkårResultat)))
                 .orElse(EndringsresultatSnapshot.utenSnapshot(VilkårResultat.class));
    }

    private EndringsresultatSnapshot lagBeregningResultatIdSnapshotAvTidsstempel(Behandling behandling) {
       return Optional.ofNullable(behandling.getBehandlingsresultat())
            .map(Behandlingsresultat::getBeregningResultat)
            .map(beregningResultat ->
                EndringsresultatSnapshot.medSnapshot(BeregningResultat.class, hentLongVerdiAvEndretTid(beregningResultat)))
            .orElse(EndringsresultatSnapshot.utenSnapshot(BeregningResultat.class));
    }

    private Long hentLongVerdiAvEndretTid(BaseEntitet entitet) {
       LocalDateTime endretTidspunkt = entitet.getOpprettetTidspunkt();
       if(entitet.getEndretTidspunkt()!=null){
           endretTidspunkt = entitet.getEndretTidspunkt();
       }
       return mapFraLocalDateTimeTilLong(endretTidspunkt);
    }

    static Long mapFraLocalDateTimeTilLong(LocalDateTime ldt){
        ZonedDateTime zdt = ldt.atZone(ZoneId.of("Europe/Paris"));
        return zdt.toInstant().toEpochMilli();
    }
}
