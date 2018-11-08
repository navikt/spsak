package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingEndring;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingFeil;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingHistorikk;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingTjenesteFelles;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.FødtBarnInfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class RevurderingFPTjenesteImpl implements RevurderingTjeneste {

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private RevurderingHistorikk revurderingHistorikk;
    private FamilieHendelseRepository familieHendelseRepository;
    private PersonopplysningRepository personopplysningRepository;
    private MedlemskapRepository medlemskapRepository;
    private AksjonspunktRepository aksjonspunktRepository;
    private YtelsesFordelingRepository ytelsesFordelingRepository;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private RevurderingTjenesteFelles revurderingTjenesteFelles;
    private MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository;
    private RevurderingEndring revurderingEndring;

    public RevurderingFPTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public RevurderingFPTjenesteImpl(BehandlingRepositoryProvider repositoryProvider, BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                     HistorikkRepository historikkRepository, @FagsakYtelseTypeRef("FP") RevurderingEndring revurderingEndring) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
        this.revurderingHistorikk = new RevurderingHistorikk(historikkRepository);
        this.familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.revurderingEndring = revurderingEndring;
        this.revurderingTjenesteFelles = new RevurderingTjenesteFelles(repositoryProvider);
        this.medlemskapVilkårPeriodeRepository = repositoryProvider.getMedlemskapVilkårPeriodeRepository();
    }

    @Override
    public Behandling opprettManuellRevurdering(Fagsak fagsak, BehandlingÅrsakType revurderingsÅrsak){
        Behandling behandling = opprettRevurdering(fagsak, revurderingsÅrsak, true);
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.KONTROLL_AV_MANUELT_OPPRETTET_REVURDERINGSBEHANDLING);
        return behandling;
    }

    @Override
    public Behandling opprettAutomatiskRevurdering(Fagsak fagsak, BehandlingÅrsakType revurderingsÅrsak) {
        return opprettRevurdering(fagsak, revurderingsÅrsak, false);
    }

    private Behandling opprettRevurdering(Fagsak fagsak, BehandlingÅrsakType revurderingsÅrsak, boolean manueltOpprettet) {
        Behandling origBehandling = behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(fagsak.getId())
            .orElseThrow(() -> RevurderingFeil.FACTORY.tjenesteFinnerIkkeBehandlingForRevurdering(fagsak.getId()).toException());

         // lås original behandling først
         behandlingskontrollTjeneste.initBehandlingskontroll(origBehandling);

         // deretter opprett revurdering
        Behandling revurdering = revurderingTjenesteFelles.opprettRevurderingsbehandling(revurderingsÅrsak, origBehandling, manueltOpprettet);
        revurderingHistorikk.opprettHistorikkinnslagOmRevurdering(revurdering, revurderingsÅrsak, manueltOpprettet);
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(revurdering);
        behandlingskontrollTjeneste.opprettBehandling(kontekst, revurdering);

        // Kopier grunnlagsdata
        familieHendelseRepository.kopierGrunnlagFraEksisterendeBehandling(origBehandling, revurdering);
        personopplysningRepository.kopierGrunnlagFraEksisterendeBehandling(origBehandling, revurdering);
        medlemskapRepository.kopierGrunnlagFraEksisterendeBehandling(origBehandling, revurdering);
        ytelsesFordelingRepository.kopierGrunnlagFraEksisterendeBehandling(origBehandling, revurdering);
        inntektArbeidYtelseRepository.kopierGrunnlagFraEksisterendeBehandling(origBehandling, revurdering);

        // Kopier aksjonspunkter
        aksjonspunktRepository.kopierAlleAksjonspunkterOgSettDemInaktive(origBehandling, revurdering);

        // Kopier vilkår (samme vilkår vurderes i Revurdering)
        return kopierVilkårsresultat(origBehandling, revurdering, kontekst);
    }

    private Behandling kopierVilkårsresultat(Behandling origBehandling, Behandling revurdering, BehandlingskontrollKontekst kontekst) {
        VilkårResultat origVilkårResultat = origBehandling.getBehandlingsresultat().getVilkårResultat();
        Objects.requireNonNull(origVilkårResultat, "Vilkårsresultat må være satt på revurderingens originale behandling");

        VilkårResultat.Builder vilkårBuilder = VilkårResultat.builder();
        origVilkårResultat.getVilkårene().stream()
            .forEach(vilkår -> vilkårBuilder
                    .medUtfallManuelt(vilkår.getVilkårUtfallManuelt())
                    .medUtfallOverstyrt(vilkår.getVilkårUtfallOverstyrt())
                    .leggTilVilkårResultat(vilkår.getVilkårType(), VilkårUtfallType.IKKE_VURDERT, vilkår.getVilkårUtfallMerknad(),
                vilkår.getMerknadParametere(), vilkår.getAvslagsårsak(), vilkår.erManueltVurdert(), vilkår.erOverstyrt(), vilkår.getRegelEvaluering(), vilkår.getRegelInput())
            );
        vilkårBuilder.medVilkårResultatType(VilkårResultatType.IKKE_FASTSATT);
        VilkårResultat vilkårResultat = vilkårBuilder.buildFor(revurdering);
        behandlingRepository.lagre(vilkårResultat, kontekst.getSkriveLås());
        behandlingRepository.lagre(revurdering, kontekst.getSkriveLås());

        // MedlemskapsvilkårPerioder er tilknyttet vilkårresultat, ikke behandling
        medlemskapVilkårPeriodeRepository.kopierGrunnlagFraEksisterendeBehandling(origBehandling, revurdering);

        return revurdering;
    }


    @Override
    public void opprettHistorikkinnslagForFødsler(Behandling behandling, List<FødtBarnInfo> barnFødtIPeriode) {
        revurderingHistorikk.opprettHistorikkinnslagForFødsler(behandling, barnFødtIPeriode);
    }

    @Override
    public Boolean kanRevurderingOpprettes(Fagsak fagsak) {
        return revurderingTjenesteFelles.kanRevurderingOpprettes(fagsak);
    }

    @Override
    public boolean erRevurderingMedUendretUtfall(Behandling behandling) {
        return revurderingEndring.erRevurderingMedUendretUtfall(behandling);
    }
}
