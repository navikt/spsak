package no.nav.foreldrepenger.behandling.revurdering.fp;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.revurdering.*;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.SykefraværRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;

@ApplicationScoped
public class RevurderingTjeneste {

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private RevurderingHistorikk revurderingHistorikk;
    private PersonopplysningRepository personopplysningRepository;
    private MedlemskapRepository medlemskapRepository;
    private AksjonspunktRepository aksjonspunktRepository;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private RevurderingTjenesteFelles revurderingTjenesteFelles;
    private MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository;
    private RevurderingEndring revurderingEndring;
    private SykefraværRepository sykefraværRepository;

    public RevurderingTjeneste() {
        // for CDI proxy
    }

    @Inject
    public RevurderingTjeneste(GrunnlagRepositoryProvider repositoryProvider, ResultatRepositoryProvider resultatRepositoryProvider,
                               BehandlingskontrollTjeneste behandlingskontrollTjeneste, RevurderingEndring revurderingEndring) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.revurderingHistorikk = new RevurderingHistorikk(repositoryProvider.getHistorikkRepository());
        this.personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.revurderingEndring = revurderingEndring;
        this.revurderingTjenesteFelles = new RevurderingTjenesteFelles(repositoryProvider);
        this.medlemskapVilkårPeriodeRepository = resultatRepositoryProvider.getMedlemskapVilkårPeriodeRepository();
        this.sykefraværRepository = repositoryProvider.getSykefraværRepository();
    }

    public Behandling opprettManuellRevurdering(Fagsak fagsak, BehandlingÅrsakType revurderingsÅrsak) {
        Behandling behandling = opprettRevurdering(fagsak, revurderingsÅrsak, true);
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.KONTROLL_AV_MANUELT_OPPRETTET_REVURDERINGSBEHANDLING);
        return behandling;
    }

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
        personopplysningRepository.kopierGrunnlagFraEksisterendeBehandling(origBehandling, revurdering);
        medlemskapRepository.kopierGrunnlagFraEksisterendeBehandling(origBehandling, revurdering);
        inntektArbeidYtelseRepository.kopierGrunnlagFraEksisterendeBehandling(origBehandling, revurdering);
        sykefraværRepository.kopierGrunnlagFraEksisterendeBehandling(origBehandling, revurdering);

        // Kopier aksjonspunkter
        aksjonspunktRepository.kopierAlleAksjonspunkterOgSettDemInaktive(origBehandling, revurdering);

        // Kopier vilkår (samme vilkår vurderes i Revurdering)
        return kopierVilkårsresultat(origBehandling, revurdering, kontekst);
    }

    private Behandling kopierVilkårsresultat(Behandling origBehandling, Behandling revurdering, BehandlingskontrollKontekst kontekst) {
        Behandlingsresultat originResult = behandlingRepository.hentResultat(origBehandling.getId());
        VilkårResultat origVilkårResultat = originResult.getVilkårResultat();
        Objects.requireNonNull(origVilkårResultat, "Vilkårsresultat må være satt på revurderingens originale behandling");

        VilkårResultat.Builder vilkårBuilder = VilkårResultat.builder();
        origVilkårResultat.getVilkårene()
            .forEach(vilkår -> vilkårBuilder
                .medUtfallManuelt(vilkår.getVilkårUtfallManuelt())
                .medUtfallOverstyrt(vilkår.getVilkårUtfallOverstyrt())
                .leggTilVilkårResultat(vilkår.getVilkårType(), VilkårUtfallType.IKKE_VURDERT, vilkår.getVilkårUtfallMerknad(),
                    vilkår.getMerknadParametere(), vilkår.getAvslagsårsak(), vilkår.erManueltVurdert(), vilkår.erOverstyrt(), vilkår.getRegelEvaluering(), vilkår.getRegelInput())
            );
        vilkårBuilder.medVilkårResultatType(VilkårResultatType.IKKE_FASTSATT);
        Behandlingsresultat revurderingResult = Behandlingsresultat.opprettFor(revurdering);
        VilkårResultat vilkårResultat = vilkårBuilder.buildFor(revurderingResult);
        behandlingRepository.lagre(vilkårResultat, kontekst.getSkriveLås());
        behandlingRepository.lagre(revurderingResult, kontekst.getSkriveLås());
        behandlingRepository.lagre(revurdering, kontekst.getSkriveLås());

        // MedlemskapsvilkårPerioder er tilknyttet vilkårresultat, ikke behandling
        medlemskapVilkårPeriodeRepository.kopierGrunnlagFraEksisterende(originResult, revurderingResult);

        return revurdering;
    }

    public Boolean kanRevurderingOpprettes(Fagsak fagsak) {
        return revurderingTjenesteFelles.kanRevurderingOpprettes(fagsak);
    }

    public boolean erRevurderingMedUendretUtfall(Behandling behandling) {
        if (behandling.erRevurdering()) {
            return revurderingEndring.erRevurderingMedUendretUtfall(behandling, behandlingRepository.hentResultat(behandling.getId()));
        }
        return false;
    }
}
