package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.steg.beregnytelse.es.BarnInfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.OmsorgsovertakelseVilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.familiehendelse.omsorg.OmsorghendelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.omsorg.impl.OmsorgsvilkårKonfigurasjon;
import no.nav.foreldrepenger.domene.personopplysning.AvklarOmsorgOgForeldreansvarAksjonspunktData;
import no.nav.foreldrepenger.domene.personopplysning.AvklartDataBarnAdapter;
import no.nav.foreldrepenger.domene.personopplysning.AvklartDataForeldreAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.AvklartDataBarnDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto.class, adapter = AksjonspunktOppdaterer.class)
public class AvklarOmsorgOgForeldreansvarOppdaterer implements AksjonspunktOppdaterer<AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto> {

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;
    private VilkårKodeverkRepository vilkårKodeverkRepository;
    private OmsorghendelseTjeneste omsorghendelseTjeneste;
    private HistorikkTjenesteAdapter historikkAdapter;
    private KodeverkRepository kodeverkRepository;
    private BehandlingRepositoryProvider repositoryProvider;

    AvklarOmsorgOgForeldreansvarOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public AvklarOmsorgOgForeldreansvarOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                                  SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                                  OmsorghendelseTjeneste omsorghendelseTjeneste,
                                                  HistorikkTjenesteAdapter historikkAdapter) {
        this.repositoryProvider = repositoryProvider;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.vilkårKodeverkRepository = repositoryProvider.getVilkårKodeverkRepository();
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.omsorghendelseTjeneste = omsorghendelseTjeneste;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.historikkAdapter = historikkAdapter;
    }

    @Override
    public boolean skalReinnhenteRegisteropplysninger(Behandling behandling, LocalDate forrigeSkjæringstidspunkt) {
        return !skjæringstidspunktTjeneste.utledSkjæringstidspunktForRegisterInnhenting(behandling).equals(forrigeSkjæringstidspunkt);
    }

    @Override
    public OppdateringResultat oppdater(AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto, Behandling behandling,
                                        VilkårResultat.Builder vilkårBuilder) {

        håndterEndringHistorikk(dto, behandling);

        final LocalDate forrigeSkjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktForRegisterInnhenting(behandling);

        oppdaterAksjonspunktGrunnlag(dto, behandling);

        boolean skalReinnhenteRegisteropplysninger = skalReinnhenteRegisteropplysninger(behandling, forrigeSkjæringstidspunkt);

        // Aksjonspunkter
        settNyttVilkårOgAvbrytAndreOmsorgsovertakelseVilkårOgAksjonspunkter(dto, behandling, vilkårBuilder);

        return vurderSkalReinnhenteRegisteropplysninger(dto, behandling, skalReinnhenteRegisteropplysninger);
    }

    private void oppdaterAksjonspunktGrunnlag(AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto, Behandling behandling) {
        List<AvklartDataForeldreAdapter> foreldreAdapter = new ArrayList<>();
        dto.getForeldre().forEach(foreldre -> foreldreAdapter.add(new AvklartDataForeldreAdapter(foreldre.getAktorId(), foreldre.getDødsdato())));
        List<AvklartDataBarnAdapter> barnAdapter = new ArrayList<>();
        dto.getBarn().forEach(barn -> barnAdapter.add(new AvklartDataBarnAdapter(barn.getAktørId(), barn.getFodselsdato(), barn.getNummer())));

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());

        final AvklarOmsorgOgForeldreansvarAksjonspunktData data = new AvklarOmsorgOgForeldreansvarAksjonspunktData(dto.getVilkårType().getKode(),
            aksjonspunktDefinisjon, dto.getOmsorgsovertakelseDato(), dto.getAntallBarn(), barnAdapter);

        omsorghendelseTjeneste.aksjonspunktAvklarOmsorgOgForeldreansvar(behandling, data);
    }

    private OppdateringResultat vurderSkalReinnhenteRegisteropplysninger(AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto,
                                                                         Behandling behandling, boolean skalReinnhenteRegisteropplysninger) {
        Aksjonspunkt aksjonspunkt = finnAksjonspunkt(behandling, dto);
        // TODO (FC): Må vi hoppe tilbake for å hente inn registeropplysninger her? Det virker ikke fornuftig.
        if (skalReinnhenteRegisteropplysninger) {
            aksjonspunktRepository.setSlettingVedRegisterinnhenting(aksjonspunkt, false);
            return OppdateringResultat.medTilbakehopp(BehandlingStegType.INNHENT_REGISTEROPP);
        } else {
            aksjonspunktRepository.setSlettingVedRegisterinnhenting(aksjonspunkt, true);
            return OppdateringResultat.utenOveropp();
        }
    }

    private void settNyttVilkårOgAvbrytAndreOmsorgsovertakelseVilkårOgAksjonspunkter(AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto,
                                                                                     Behandling behandling, VilkårResultat.Builder vilkårBuilder) {

        // Omsorgsovertakelse
        OmsorgsovertakelseVilkårType omsorgsovertakelseVilkårType = vilkårKodeverkRepository.finnOmsorgsovertakelseVilkårtype(dto.getVilkårType().getKode());

        // Vilkår
        VilkårType vilkårType = vilkårKodeverkRepository.finnVilkårType(dto.getVilkårType().getKode());
        vilkårBuilder.leggTilVilkår(vilkårType, VilkårUtfallType.IKKE_VURDERT);

        // Rydd opp i eventuelle omsorgsvilkår som er tidligere lagt til
        if (behandling.getBehandlingsresultat() != null) {
            behandling.getBehandlingsresultat().getVilkårResultat().getVilkårene().stream()
                .filter(vilkår -> OmsorgsvilkårKonfigurasjon.getOmsorgsovertakelseVilkår().contains(vilkår.getVilkårType()))
                // Men uten å fjerne seg selv
                .filter(vilkår -> !vilkår.getVilkårType().getKode().equals(omsorgsovertakelseVilkårType.getKode()))
                .forEach(fjernet -> vilkårBuilder.fjernVilkår(fjernet.getVilkårType()));
        }
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        behandling.getAksjonspunkter().stream()
            .filter(ap -> OmsorgsvilkårKonfigurasjon.getOmsorgsovertakelseAksjonspunkter().contains(ap.getAksjonspunktDefinisjon()))
            .filter(ap -> !Objects.equals(ap.getAksjonspunktDefinisjon(), aksjonspunktDefinisjon)) // ikke avbryte seg selv
            .forEach(ap -> aksjonspunktRepository.setTilAvbrutt(ap));
    }

    private void håndterEndringHistorikk(AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto, Behandling behandling) {
        boolean erEndret;

        final FamilieHendelseGrunnlag hendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling);

        Optional<LocalDate> orginalOmsorgsovertakelseDato = getOriginalOmsorgsovertakelseDato(hendelseGrunnlag);
        erEndret = oppdaterVedEndretVerdi(HistorikkEndretFeltType.OMSORGSOVERTAKELSESDATO,
            orginalOmsorgsovertakelseDato.orElse(null), dto.getOmsorgsovertakelseDato());

        Integer orginalAntallBarn = getOrginalAntallBarnForOmsorgsovertakelse(hendelseGrunnlag);
        erEndret = oppdaterVedEndretVerdi(HistorikkEndretFeltType.ANTALL_BARN, orginalAntallBarn, dto.getAntallBarn()) || erEndret;

        List<BarnInfo> orginaleBarn = getOpprinneligeBarn(behandling);
        List<BarnInfo> oppdaterteBarn = getOppdaterteBarn(dto);
        erEndret = oppdaterVedEndringAvFødselsdatoer(orginaleBarn, oppdaterteBarn) || erEndret;

        VilkårType vilkårType = kodeverkRepository.finn(VilkårType.class, dto.getVilkårType().getKode());
        List<VilkårType> vilkårTyper = behandling.getBehandlingsresultat().getVilkårResultat().getVilkårene().stream()
            .map(Vilkår::getVilkårType)
            .collect(Collectors.toList());
        if (!vilkårTyper.contains(vilkårType)) {
            historikkAdapter.tekstBuilder().medEndretFelt(HistorikkEndretFeltType.VILKAR_SOM_ANVENDES, null, finnTekstBasertPåOmsorgsvilkår(vilkårType));
        }

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        historikkAdapter.tekstBuilder()
            .medBegrunnelse(dto.getBegrunnelse(), aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(
                behandling, aksjonspunktDefinisjon, dto.getBegrunnelse()))
            .medSkjermlenke(aksjonspunktDefinisjon, behandling);

        if (erEndret) {
            aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);
        }
    }

    private Aksjonspunkt finnAksjonspunkt(Behandling behandling, BekreftetAksjonspunktDto dto) {
        return behandling.getAksjonspunkter().stream()
            .filter(ap -> ap.getAksjonspunktDefinisjon().getKode().equals(dto.getKode()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Aksjonspunkt som bekreftes må finnes på behandling."));
    }

    private HistorikkEndretFeltVerdiType finnTekstBasertPåOmsorgsvilkår(VilkårType vilkårType) {
        if (VilkårType.OMSORGSVILKÅRET.equals(vilkårType)) {
            return HistorikkEndretFeltVerdiType.OMSORGSVILKARET_TITTEL;
        } else if (VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD.equals(vilkårType)) {
            return HistorikkEndretFeltVerdiType.FORELDREANSVAR_2_TITTEL;
        } else if (VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD.equals(vilkårType)) {
            return HistorikkEndretFeltVerdiType.FORELDREANSVAR_4_TITTEL;
        }
        return null;
    }

    private List<BarnInfo> getOpprinneligeBarn(Behandling behandling) {
        List<BarnInfo> oppgitteBarn = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling).getGjeldendeVersjon()
            .getBarna().stream()
            .map(barn -> new BarnInfo(barn.getBarnNummer(), barn.getFødselsdato(), null))
            .collect(toList());

        return oppgitteBarn;
    }

    private List<BarnInfo> getOppdaterteBarn(AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto) {
        List<AvklartDataBarnDto> barna = dto.getBarn();
        if (barna != null) {
            return barna.stream()
                .map(barn -> new BarnInfo(barn.getNummer(), barn.getFodselsdato(), null))
                .collect(toList());
        } else {
            return Collections.emptyList();
        }
    }

    private Integer getOrginalAntallBarnForOmsorgsovertakelse(FamilieHendelseGrunnlag grunnlag) {
        return grunnlag.getGjeldendeAntallBarn();
    }

    private Optional<LocalDate> getOriginalOmsorgsovertakelseDato(FamilieHendelseGrunnlag grunnlag) {
        return grunnlag.getGjeldendeAdopsjon().map(Adopsjon::getOmsorgsovertakelseDato);
    }


    private boolean oppdaterVedEndringAvFødselsdatoer(List<BarnInfo> orginalBarn, List<BarnInfo> oppdaterteBarn) {
        boolean erEndret = false;

        // Endrede
        for (UidentifisertBarn opprinnelig : orginalBarn) {
            Optional<BarnInfo> endret = oppdaterteBarn.stream()
                .filter(oppdatert -> opprinnelig.getBarnNummer() != null && oppdatert.getBarnNummer() != null) // Kan
                // bare
                // spore
                // endringer
                // på
                // barn
                // med
                // nummer
                .filter(oppdatert -> opprinnelig.getBarnNummer().equals(oppdatert.getBarnNummer()))
                .filter(oppdatert -> !opprinnelig.getFødselsdato().equals(oppdatert.getFødselsdato()))
                .findFirst();

            if (endret.isPresent()) {
                erEndret = oppdaterVedEndretVerdi(HistorikkEndretFeltType.FODSELSDATO, opprinnelig.getFødselsdato(), endret.get().getFødselsdato())
                    || erEndret;
            }
        }
        return erEndret;
    }


    private boolean oppdaterVedEndretVerdi(HistorikkEndretFeltType type, Object original, Object bekreftet) {
        if (!Objects.equals(bekreftet, original)) {
            historikkAdapter.tekstBuilder().medEndretFelt(type, original, bekreftet);
            return true;
        }
        return false;
    }

}
