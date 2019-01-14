package no.nav.foreldrepenger.web.app.tjenester.behandling.opptjening;


import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.behandling.aksjonspunkt.Overstyringshåndterer;
import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetKlassifisering;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.InngangsvilkårTjeneste;
import no.nav.foreldrepenger.domene.inngangsvilkaar.overstyring.InngangsvilkårOverstyringshåndterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringOpptjeningsvilkåretDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = OverstyringOpptjeningsvilkåretDto.class, adapter = Overstyringshåndterer.class)
public class OpptjeningsvilkåretOverstyringshåndterer extends InngangsvilkårOverstyringshåndterer<OverstyringOpptjeningsvilkåretDto> {

    private OpptjeningRepository opptjeningRepository;
    private BehandlingRepository behandlingRepository;

    OpptjeningsvilkåretOverstyringshåndterer() {
        // for CDI proxy
    }

    @Inject
    public OpptjeningsvilkåretOverstyringshåndterer(GrunnlagRepositoryProvider repositoryProvider,
                                                    ResultatRepositoryProvider resultatRepositoryProvider,
                                                    HistorikkTjenesteAdapter historikkAdapter,
                                                    InngangsvilkårTjeneste inngangsvilkårTjeneste) {
        super(repositoryProvider, historikkAdapter,
            AksjonspunktDefinisjon.OVERSTYRING_AV_OPPTJENINGSVILKÅRET,
            VilkårType.OPPTJENINGSVILKÅRET,
            inngangsvilkårTjeneste);
        this.opptjeningRepository = resultatRepositoryProvider.getOpptjeningRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
    }

    @Override
    protected void lagHistorikkInnslag(Behandling behandling, OverstyringOpptjeningsvilkåretDto dto) {
        lagHistorikkInnslagForOverstyrtVilkår(dto.getBegrunnelse(), dto.getErVilkarOk(), dto.getKode(), behandling);
    }

    @Override
    protected void precondition(Behandling behandling, OverstyringOpptjeningsvilkåretDto dto) {
        if (dto.getErVilkarOk()) {
            Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
            final Optional<Opptjening> opptjening = opptjeningRepository.finnOpptjening(behandlingsresultat);
            if (opptjening.isPresent()) {
                final long antall = opptjening.get().getOpptjeningAktivitet().stream()
                    .filter(oa -> erGodkjent(oa) && !oa.getAktivitetType().equals(OpptjeningAktivitetType.UTENLANDSK_ARBEIDSFORHOLD)).count();
                if (antall > 0) {
                    return;
                }
            }
            throw OverstyringFeil.FACTORY.opptjeningPreconditionFailed().toException();
        }
    }

    private boolean erGodkjent(OpptjeningAktivitet oa) {
        OpptjeningAktivitetKlassifisering klassifisering = oa.getKlassifisering();
        return klassifisering.equals(OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT) ||
            klassifisering.equals(OpptjeningAktivitetKlassifisering.ANTATT_GODKJENT) ||
            klassifisering.equals(OpptjeningAktivitetKlassifisering.MELLOMLIGGENDE_PERIODE);
    }
}
