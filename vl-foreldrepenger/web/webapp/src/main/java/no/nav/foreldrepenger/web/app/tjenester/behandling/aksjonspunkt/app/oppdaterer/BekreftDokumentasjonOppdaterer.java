package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.familiehendelse.BekreftDokumentasjonAksjonspunktDto;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftDokumentertDatoAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = BekreftDokumentertDatoAksjonspunktDto.class, adapter = AksjonspunktOppdaterer.class)
public class BekreftDokumentasjonOppdaterer implements AksjonspunktOppdaterer<BekreftDokumentertDatoAksjonspunktDto> {

    private AksjonspunktRepository aksjonspunktRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private FamilieHendelseTjeneste hendelseTjeneste;
    private HistorikkTjenesteAdapter historikkAdapter;
    private BehandlingRepositoryProvider repositoryProvider;

    BekreftDokumentasjonOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public BekreftDokumentasjonOppdaterer(BehandlingRepositoryProvider repositoryProvider,
            HistorikkTjenesteAdapter historikkAdapter,
            SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
            FamilieHendelseTjeneste hendelseTjeneste) {
        this.repositoryProvider = repositoryProvider;
        this.historikkAdapter = historikkAdapter;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.hendelseTjeneste = hendelseTjeneste;
    }

    @Override
    public boolean skalReinnhenteRegisteropplysninger(Behandling behandling, LocalDate forrigeSkjæringstidspunkt) {
        return !skjæringstidspunktTjeneste.utledSkjæringstidspunktForRegisterInnhenting(behandling).equals(forrigeSkjæringstidspunkt);
    }

    @Override
    public OppdateringResultat oppdater(BekreftDokumentertDatoAksjonspunktDto dto, Behandling behandling) {

        håndterEndringHistorikk(dto, behandling);

        // beregn denne før vi oppdaterer grunnlag
        final LocalDate forrigeSkjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktForRegisterInnhenting(behandling);

        final BekreftDokumentasjonAksjonspunktDto adapter = new BekreftDokumentasjonAksjonspunktDto(dto.getOmsorgsovertakelseDato(),
                dto.getFodselsdatoer());
        hendelseTjeneste.aksjonspunktBekreftDokumentasjon(behandling, adapter);

        boolean skalReinnhente = skalReinnhenteRegisteropplysninger(behandling, forrigeSkjæringstidspunkt);

        // TODO (Maur): Må vi hoppe tilbake for å reinnhente registeropplysninger?  Virker ikke fornuftig.
        Aksjonspunkt aksjonspunkt = finnAksjonspunkt(behandling, dto);
        if (skalReinnhente) {
            aksjonspunktRepository.setSlettingVedRegisterinnhenting(aksjonspunkt, false);
            return OppdateringResultat.medTilbakehopp(BehandlingStegType.INNHENT_REGISTEROPP);
        } else {
            aksjonspunktRepository.setSlettingVedRegisterinnhenting(aksjonspunkt, true);
            return OppdateringResultat.utenOveropp();
        }
    }

    private void håndterEndringHistorikk(BekreftDokumentertDatoAksjonspunktDto dto, Behandling behandling) {
        boolean erEndret;
        final FamilieHendelseGrunnlag hendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling);

        LocalDate originalDato = getOmsorgsovertakelsesdatoForAdopsjon(
                hendelseGrunnlag.getGjeldendeAdopsjon().orElseThrow(IllegalStateException::new));
        erEndret = oppdaterVedEndretVerdi(HistorikkEndretFeltType.OMSORGSOVERTAKELSESDATO, originalDato, dto.getOmsorgsovertakelseDato());

        Map<Integer, LocalDate> orginaleFødselsdatoer = getAdopsjonFødselsdatoer(hendelseGrunnlag);
        Map<Integer, LocalDate> oppdaterteFødselsdatoer = dto.getFodselsdatoer();

        for (Map.Entry<Integer, LocalDate> entry : orginaleFødselsdatoer.entrySet()) {
            LocalDate oppdatertFødselsdato = oppdaterteFødselsdatoer.get(entry.getKey());
            erEndret = oppdaterVedEndretVerdi(HistorikkEndretFeltType.FODSELSDATO, entry.getValue(), oppdatertFødselsdato) || erEndret;
        }

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        historikkAdapter.tekstBuilder()
                .medBegrunnelse(dto.getBegrunnelse(),
                        aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon,
                                dto.getBegrunnelse()))
                .medSkjermlenke(aksjonspunktDefinisjon, behandling);

        if (erEndret) {
            aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);
        }
    }

    private LocalDate getOmsorgsovertakelsesdatoForAdopsjon(Adopsjon adopsjon) {
        return adopsjon.getOmsorgsovertakelseDato();
    }

    private Map<Integer, LocalDate> getAdopsjonFødselsdatoer(FamilieHendelseGrunnlag grunnlag) {
        return Optional.ofNullable(grunnlag.getGjeldendeBarna())
            .map(barna -> barna.stream()
                .collect(toMap(UidentifisertBarn::getBarnNummer, UidentifisertBarn::getFødselsdato)))
            .orElse(emptyMap());
    }

    private Aksjonspunkt finnAksjonspunkt(Behandling behandling, BekreftetAksjonspunktDto dto) {
        return behandling.getAksjonspunkter().stream()
                .filter(ap -> ap.getAksjonspunktDefinisjon().getKode().equals(dto.getKode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Aksjonspunkt som bekreftes må finnes på behandling."));
    }

    private boolean oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, Object original, Object bekreftet) {
        if (!Objects.equals(bekreftet, original)) {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, original, bekreftet);
            return true;
        }
        return false;
    }

}
