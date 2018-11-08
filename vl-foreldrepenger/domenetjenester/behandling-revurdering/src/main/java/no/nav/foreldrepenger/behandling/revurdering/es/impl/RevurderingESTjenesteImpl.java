package no.nav.foreldrepenger.behandling.revurdering.es.impl;

import java.util.List;
import java.util.Optional;

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
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;

@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class RevurderingESTjenesteImpl implements RevurderingTjeneste {
    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private RevurderingHistorikk revurderingHistorikk;
    private FamilieHendelseRepository familieHendelseRepository;
    private PersonopplysningRepository personopplysningRepository;
    private MedlemskapRepository medlemskapRepository;
    private SøknadRepository søknadRepository;
    private RevurderingTjenesteFelles revurderingTjenesteFelles;
    private RevurderingEndring revurderingEndring;

    public RevurderingESTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public RevurderingESTjenesteImpl(BehandlingRepositoryProvider repositoryProvider, BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                     HistorikkRepository historikkRepository, @FagsakYtelseTypeRef("ES") RevurderingEndring revurderingEndring) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.revurderingHistorikk = new RevurderingHistorikk(historikkRepository);
        this.familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.revurderingEndring = revurderingEndring;
        this.revurderingTjenesteFelles = new RevurderingTjenesteFelles(repositoryProvider);
    }

    @Override
    public Behandling opprettManuellRevurdering(Fagsak fagsak, BehandlingÅrsakType revurderingsÅrsak) {
        return opprettRevurdering(fagsak, revurderingsÅrsak, true);
    }

    @Override
    public Behandling opprettAutomatiskRevurdering(Fagsak fagsak, BehandlingÅrsakType revurderingsÅrsak) {
        return opprettRevurdering(fagsak, revurderingsÅrsak, false);
    }

    private Behandling opprettRevurdering(Fagsak fagsak, BehandlingÅrsakType revurderingÅrsakType, boolean manueltOpprettet) {
        Optional<Behandling> opprinneligBehandlingOptional = behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(fagsak.getId());
        if (!opprinneligBehandlingOptional.isPresent()) {
            throw RevurderingFeil.FACTORY.tjenesteFinnerIkkeBehandlingForRevurdering(fagsak.getId()).toException();
        }
        Behandling opprinneligBehandling = opprinneligBehandlingOptional.get();
        // lås original behandling først slik at ingen andre forsøker på samme
        behandlingskontrollTjeneste.initBehandlingskontroll(opprinneligBehandling);

        // deretter opprett kontekst for revurdering og opprett
        Behandling revurderingBehandling = revurderingTjenesteFelles.opprettRevurderingsbehandling(revurderingÅrsakType, opprinneligBehandling, manueltOpprettet);
        revurderingHistorikk.opprettHistorikkinnslagOmRevurdering(revurderingBehandling, revurderingÅrsakType, manueltOpprettet);
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(revurderingBehandling);
        behandlingskontrollTjeneste.opprettBehandling(kontekst, revurderingBehandling);

        // Kopier søknadsdata
        final Optional<Søknad> søknadOptional = søknadRepository.hentSøknadHvisEksisterer(opprinneligBehandling);
        if (søknadOptional.isPresent()) {
            final Søknad søknad = søknadOptional.get();

            familieHendelseRepository.kopierGrunnlagFraEksisterendeBehandlingForRevurdering(opprinneligBehandling, revurderingBehandling);
            final Optional<FamilieHendelseGrunnlag> familieHendelseAggregat = familieHendelseRepository.hentAggregatHvisEksisterer(revurderingBehandling);
            personopplysningRepository.kopierGrunnlagFraEksisterendeBehandlingForRevurdering(opprinneligBehandling, revurderingBehandling);
            final Optional<PersonopplysningGrunnlag> personopplysningerAggregat = personopplysningRepository
                .hentPersonopplysningerHvisEksisterer(revurderingBehandling);
            medlemskapRepository.kopierGrunnlagFraEksisterendeBehandlingForRevurdering(opprinneligBehandling, revurderingBehandling);
            final Optional<MedlemskapAggregat> medlemskapAggregat = medlemskapRepository.hentMedlemskap(revurderingBehandling);

            final SøknadEntitet.Builder builder = new SøknadEntitet.Builder(søknad);
            familieHendelseAggregat.ifPresent(aggregat -> builder.medFamilieHendelse(aggregat.getSøknadVersjon()));
            personopplysningerAggregat.flatMap(PersonopplysningGrunnlag::getOppgittAnnenPart).ifPresent(builder::medSøknadAnnenPart);
            medlemskapAggregat.flatMap(MedlemskapAggregat::getOppgittTilknytning).ifPresent(builder::medOppgittTilknytning);
            søknadRepository.lagreOgFlush(revurderingBehandling, builder.build());
        }

        return revurderingBehandling;
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
