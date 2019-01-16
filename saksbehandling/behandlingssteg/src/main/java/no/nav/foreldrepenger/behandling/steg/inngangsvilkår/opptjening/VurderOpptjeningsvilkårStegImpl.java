package no.nav.foreldrepenger.behandling.steg.inngangsvilkår.opptjening;

import static java.util.Collections.singletonList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.inngangsvilkår.InngangsvilkårStegImpl;
import no.nav.foreldrepenger.behandlingskontroll.*;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetKlassifisering;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.RegelOrkestrerer;
import no.nav.foreldrepenger.domene.inngangsvilkaar.RegelResultat;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.OpptjeningsvilkårResultat;
import no.nav.vedtak.util.FPDateUtil;

@BehandlingStegRef(kode = "VURDER_OPPTJ")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class VurderOpptjeningsvilkårStegImpl extends InngangsvilkårStegImpl {

    private static final VilkårType OPPTJENINGSVILKÅRET = VilkårType.OPPTJENINGSVILKÅRET;
    private static List<VilkårType> STØTTEDE_VILKÅR = singletonList(OPPTJENINGSVILKÅRET);
    private final GrunnlagRepositoryProvider repositoryProvider;
    private final OpptjeningRepository opptjeningRepository;
    private final AksjonspunktRepository aksjonspunktRepository;
    private ResultatRepositoryProvider resultatRepositoryProvider;

    @Inject
    public VurderOpptjeningsvilkårStegImpl(GrunnlagRepositoryProvider repositoryProvider, ResultatRepositoryProvider resultatRepositoryProvider, RegelOrkestrerer regelOrkestrerer) {
        super(repositoryProvider, regelOrkestrerer, BehandlingStegType.VURDER_OPPTJENINGSVILKÅR);
        this.repositoryProvider = repositoryProvider;
        this.opptjeningRepository = resultatRepositoryProvider.getOpptjeningRepository();
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.resultatRepositoryProvider = resultatRepositoryProvider;
    }

    @Override
    protected void utførtRegler(BehandlingskontrollKontekst kontekst, Behandling behandling, RegelResultat regelResultat) {
        Behandlingsresultat behandlingsresultat = repositoryProvider.getBehandlingRepository().hentResultat(behandling.getId());
        if (vilkårErVurdert(regelResultat)) {
            OpptjeningsvilkårResultat opres = getVilkårresultat(behandling, regelResultat);

            Period totalOpptjeningResultat = opres.getResultatOpptjent();

            MapTilOpptjeningAktiviteter mapper = new MapTilOpptjeningAktiviteter(opptjeningRepository);
            List<OpptjeningAktivitet> aktiviteter = new ArrayList<>();
            aktiviteter.addAll(mapper.map(opres.getUnderkjentePerioder(), OpptjeningAktivitetKlassifisering.BEKREFTET_AVVIST));
            aktiviteter.addAll(mapper.map(opres.getAntattGodkjentePerioder(), OpptjeningAktivitetKlassifisering.ANTATT_GODKJENT));
            aktiviteter.addAll(mapper.map(opres.getBekreftetGodkjentePerioder(), OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));

            aktiviteter.addAll(mapper.map(opres.getAkseptertMellomliggendePerioder(), OpptjeningAktivitetKlassifisering.MELLOMLIGGENDE_PERIODE));

            opptjeningRepository.lagreOpptjeningResultat(behandlingsresultat, totalOpptjeningResultat, aktiviteter);

        } else {
            // rydd bort tidligere aktiviteter
            opptjeningRepository.lagreOpptjeningResultat(behandlingsresultat, null, Collections.emptyList());
        }
    }

    /**
     * Overstyr stegresultat og sett en frist dersom vi må vente på opptjeningsopplysninger.
     */
    @Override
    protected BehandleStegResultat stegResultat(RegelResultat regelResultat) {
        BehandleStegResultat stegResultat = super.stegResultat(regelResultat);
        AksjonspunktDefinisjon apDef = AksjonspunktDefinisjon.AUTO_VENT_PÅ_OPPTJENINGSOPPLYSNINGER;

        if (regelResultat.getAksjonspunktDefinisjoner().contains(apDef)) {
            LocalDateTime frist = getVentPåOpptjeningsopplysningerFrist(regelResultat);

            return stegResultat.medAksjonspunktResultat(
                AksjonspunktResultat.opprettForAksjonspunktMedCallback(apDef,
                    ap -> aksjonspunktRepository.setFrist(ap, frist, Venteårsak.VENT_OPPTJENING_OPPLYSNINGER)));
        }

        return stegResultat;
    }

    private LocalDateTime getVentPåOpptjeningsopplysningerFrist(RegelResultat regelResultat) {
        Optional<OpptjeningsvilkårResultat> resultat = regelResultat.getEkstraResultat(OPPTJENINGSVILKÅRET);
        LocalDate now = LocalDate.now(FPDateUtil.getOffset());
        LocalDate fristDato = resultat.isPresent() ? resultat.get().getFrist() : now;
        LocalDateTime frist = LocalDateTime.of(fristDato, LocalTime.now(FPDateUtil.getOffset()));
        return frist;
    }

    @Override
    public void vedTransisjon(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell, TransisjonType transisjonType, BehandlingStegType førsteSteg, BehandlingStegType sisteSteg, TransisjonType skalTil) {
        if (transisjonType.equals(TransisjonType.HOPP_OVER_BAKOVER)) {
            if (!(BehandlingStegType.VURDER_OPPTJENINGSVILKÅR.equals(førsteSteg) && skalTil.equals(TransisjonType.ETTER_UTGANG))) {
                new RyddOpptjening(repositoryProvider, resultatRepositoryProvider, behandling, kontekst).ryddOppAktiviteter();
            }
        }
    }

    private OpptjeningsvilkårResultat getVilkårresultat(Behandling behandling, RegelResultat regelResultat) {
        OpptjeningsvilkårResultat op = (OpptjeningsvilkårResultat) regelResultat.getEkstraResultater()
            .get(OPPTJENINGSVILKÅRET);
        if (op == null) {
            throw new IllegalArgumentException(
                "Utvikler-feil: finner ikke resultat fra evaluering av Inngangsvilkår/Opptjeningsvilkåret:" + behandling.getId());
        }
        return op;
    }

    private boolean vilkårErVurdert(RegelResultat regelResultat) {
        Optional<Vilkår> opptjeningsvilkår = regelResultat.getVilkårResultat().getVilkårene().stream()
            .filter(v -> v.getVilkårType().equals(OPPTJENINGSVILKÅRET))
            .findFirst();
        return opptjeningsvilkår.map(v -> !v.getGjeldendeVilkårUtfall().equals(VilkårUtfallType.IKKE_VURDERT))
            .orElse(Boolean.FALSE);
    }

    @Override
    public void vedHoppOverBakover(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell, BehandlingStegType tilSteg, BehandlingStegType fraSteg) {
        new RyddOpptjening(repositoryProvider, resultatRepositoryProvider, behandling, kontekst).ryddOppAktiviteter();
    }

    @Override
    public List<VilkårType> vilkårHåndtertAvSteg() {
        return STØTTEDE_VILKÅR;
    }
}
