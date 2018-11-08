package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.TerminbekreftelseAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftTerminbekreftelseAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftTerminbekreftelseValidator;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
@DtoTilServiceAdapter(dto = BekreftTerminbekreftelseAksjonspunktDto.class, adapter = AksjonspunktOppdaterer.class)
public class BekreftTerminbekreftelseOppdaterer implements AksjonspunktOppdaterer<BekreftTerminbekreftelseAksjonspunktDto> {

    private Period antallDagerOverTermindatoSjekkFødsel;
    private FamilieHendelseTjeneste hendelseTjeneste;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private BekreftTerminbekreftelseValidator bekreftTerminbekreftelseValidator;
    private AksjonspunktRepository aksjonspunktRepository;
    private HistorikkTjenesteAdapter historikkAdapter;
    private BehandlingRepositoryProvider repositoryProvider;

    BekreftTerminbekreftelseOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public BekreftTerminbekreftelseOppdaterer(BehandlingRepositoryProvider repositoryProvider, HistorikkTjenesteAdapter historikkAdapter,
            @KonfigVerdi(value = "aksjonspunkt.dager.etter.termin.sjekk.fødsel") Period antallDagerOverTermindatoSjekkFødsel,
            FamilieHendelseTjeneste hendelseTjeneste,
            SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
            BekreftTerminbekreftelseValidator bekreftTerminbekreftelseValidator) {
        this.repositoryProvider = repositoryProvider;
        this.historikkAdapter = historikkAdapter;
        this.antallDagerOverTermindatoSjekkFødsel = antallDagerOverTermindatoSjekkFødsel;
        this.hendelseTjeneste = hendelseTjeneste;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.bekreftTerminbekreftelseValidator = bekreftTerminbekreftelseValidator;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();

    }

    @Override
    public boolean skalReinnhenteRegisteropplysninger(Behandling behandling, LocalDate forrigeSkjæringstidspunkt) {
        return !skjæringstidspunktTjeneste.utledSkjæringstidspunktForRegisterInnhenting(behandling).equals(forrigeSkjæringstidspunkt);
    }

    @Override
    public OppdateringResultat oppdater(BekreftTerminbekreftelseAksjonspunktDto dto, Behandling behandling) {
        final FamilieHendelseGrunnlag grunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling);

        LocalDate orginalTermindato = getTermindato(grunnlag);
        boolean erEndret = oppdaterVedEndretVerdi(HistorikkEndretFeltType.TERMINDATO, orginalTermindato, dto.getTermindato());

        LocalDate orginalUtstedtDato = getUtstedtdato(grunnlag);
        erEndret = oppdaterVedEndretVerdi(HistorikkEndretFeltType.UTSTEDTDATO, orginalUtstedtDato, dto.getUtstedtdato()) || erEndret;

        Integer opprinneligAntallBarn = getAntallBarnVedSøknadTerminbekreftelse(grunnlag);
        erEndret = oppdaterVedEndretVerdi(HistorikkEndretFeltType.ANTALL_BARN, opprinneligAntallBarn, dto.getAntallBarn()) || erEndret;

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        historikkAdapter.tekstBuilder()
                .medBegrunnelse(dto.getBegrunnelse(),
                        aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon,
                                dto.getBegrunnelse()))
                .medSkjermlenke(aksjonspunktDefinisjon, behandling);

        if (erEndret) {
            aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);
        }

        final LocalDate forrigeSkjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktForRegisterInnhenting(behandling);

        // Map til adapter
        final TerminbekreftelseAksjonspunktDto adapter = new TerminbekreftelseAksjonspunktDto(dto.getTermindato(),
                dto.getUtstedtdato(), dto.getAntallBarn(), dto.getKode());

        if (hendelseTjeneste.erUtstedtdatoEllerTermindatoEndret(behandling, adapter)) {
            bekreftTerminbekreftelseValidator.validerOpplysninger(dto);
        }

        hendelseTjeneste.aksjonspunktBekreftTerminbekreftelse(behandling, adapter);
        boolean skalReinnhente = skalReinnhenteRegisteropplysninger(behandling, forrigeSkjæringstidspunkt);

        Aksjonspunkt aksjonspunkt = finnAksjonspunkt(behandling, dto);
        // TODO (Maur): Må vi hoppe tilbake for å hente inn registeropplysninger her? Det virker ikke fornuftig.
        if (skalReinnhente) {
            aksjonspunktRepository.setSlettingVedRegisterinnhenting(aksjonspunkt, false);
            return OppdateringResultat.medTilbakehopp(BehandlingStegType.INNHENT_REGISTEROPP);
        } else {
            aksjonspunktRepository.setSlettingVedRegisterinnhenting(aksjonspunkt, true);
        }
        if (harSattTidligereTermindatoEnnKonfigurertVerdi(dto)) {
            // Må kontrollere fakta på nytt for å sjekke om fødsel skulle ha inntruffet.
            return OppdateringResultat.medTilbakehopp(BehandlingStegType.KONTROLLER_FAKTA);
        }

        return OppdateringResultat.utenOveropp();
    }

    private Aksjonspunkt finnAksjonspunkt(Behandling behandling, BekreftTerminbekreftelseAksjonspunktDto dto) {
        return behandling.getAksjonspunkter().stream()
            .filter(ap -> ap.getAksjonspunktDefinisjon().getKode().equals(dto.getKode()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Aksjonspunkt som bekreftes må finnes på behandling."));
    }

    private boolean harSattTidligereTermindatoEnnKonfigurertVerdi(BekreftTerminbekreftelseAksjonspunktDto dto) {
        return dto.getTermindato().isBefore(LocalDate.now(FPDateUtil.getOffset()).minus(antallDagerOverTermindatoSjekkFødsel));
    }

    private Integer getAntallBarnVedSøknadTerminbekreftelse(FamilieHendelseGrunnlag grunnlag) {
        return grunnlag.getGjeldendeAntallBarn();
    }

    private LocalDate getTermindato(FamilieHendelseGrunnlag grunnlag) {
        return getGjeldendeTerminbekreftelse(grunnlag).getTermindato();
    }

    private Terminbekreftelse getGjeldendeTerminbekreftelse(FamilieHendelseGrunnlag grunnlag) {
        return grunnlag.getGjeldendeTerminbekreftelse()
                .orElseThrow(() -> new IllegalStateException("Har ikke terminbekreftelse når forventet"));
    }

    private LocalDate getUtstedtdato(FamilieHendelseGrunnlag grunnlag) {
        return getGjeldendeTerminbekreftelse(grunnlag).getUtstedtdato();
    }

    private boolean oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, Object original, Object bekreftet) {
        if (!Objects.equals(bekreftet, original)) {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, original, bekreftet);
            return true;
        }
        return false;
    }
}
