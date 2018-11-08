package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonFeil.FACTORY;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.familiehendelse.AvklarManglendeFødselAksjonspunktDto;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.SjekkManglendeFodselDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = SjekkManglendeFodselDto.class, adapter = AksjonspunktOppdaterer.class)
public class SjekkManglendeFødselOppdaterer implements AksjonspunktOppdaterer<SjekkManglendeFodselDto> {

    private AksjonspunktRepository aksjonspunktRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private FamilieHendelseTjeneste hendelseTjeneste;
    private HistorikkTjenesteAdapter historikkAdapter;

    SjekkManglendeFødselOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public SjekkManglendeFødselOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                          HistorikkTjenesteAdapter historikkAdapter,
                                          SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                          FamilieHendelseTjeneste hendelseTjeneste) {
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
    public OppdateringResultat oppdater(SjekkManglendeFodselDto dto, Behandling behandling) {
        håndterEndringHistorikk(dto, behandling);
        // sjekk før vi modifiserer
        AntallBarnOgFødselsdato utledetResultat = utledFødselsdata(dto, behandling);

        final LocalDate forrigeSkjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktForRegisterInnhenting(behandling);
        // TODO: Legge inn dødsdatoer for eventuelle døde barn. Trengs endring i GUI for å støtte dette.
        final AvklarManglendeFødselAksjonspunktDto adapter = new AvklarManglendeFødselAksjonspunktDto(dto.getKode(),
            utledetResultat.fødselsdato,
            utledetResultat.antallBarn);

        hendelseTjeneste.aksjonspunktAvklarManglendeFødsel(behandling, adapter);
        boolean skalReinnhenteRegisteropplysninger = skalReinnhenteRegisteropplysninger(behandling, forrigeSkjæringstidspunkt);

        Aksjonspunkt aksjonspunkt = finnAksjonspunkt(behandling, dto);
        if (skalReinnhenteRegisteropplysninger) {
            aksjonspunktRepository.setSlettingVedRegisterinnhenting(aksjonspunkt, false);
            return OppdateringResultat.medTilbakehopp(BehandlingStegType.INNHENT_REGISTEROPP);
        } else {
            aksjonspunktRepository.setSlettingVedRegisterinnhenting(aksjonspunkt, true);
            return OppdateringResultat.utenOveropp();
        }
    }

    private void håndterEndringHistorikk(SjekkManglendeFodselDto dto, Behandling behandling) {
        final FamilieHendelseGrunnlag grunnlag = hendelseTjeneste.hentAggregat(behandling);
        AntallBarnOgFødselsdato utledetResultat = utledFødselsdata(dto, behandling);
        Boolean originalDokumentasjonForeligger = hentOrginalDokumentasjonForeligger(grunnlag);
        boolean erEndret = oppdaterVedEndretVerdi(HistorikkEndretFeltType.DOKUMENTASJON_FORELIGGER,
            originalDokumentasjonForeligger, dto.getDokumentasjonForeligger());

        int gjeldendeAntallBarn;

        erEndret = sjekkFødselsDatoOgAntallBarnEndret(grunnlag, utledetResultat, erEndret);
        gjeldendeAntallBarn = dto.getDokumentasjonForeligger() ? dto.getAntallBarnFodt() : grunnlag.getBekreftetVersjon().map(FamilieHendelse::getAntallBarn).orElse(0);

        opprettetInnslagforFeltBrukAntallBarnITps(dto, behandling);

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        historikkAdapter.tekstBuilder().medOpplysning(HistorikkOpplysningType.ANTALL_BARN, gjeldendeAntallBarn)
            .medBegrunnelse(dto.getBegrunnelse(),
                aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon,
                    dto.getBegrunnelse()))
            .medSkjermlenke(aksjonspunktDefinisjon, behandling);

        if (erEndret) {
            aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);
        }
    }

    private AntallBarnOgFødselsdato utledFødselsdata(SjekkManglendeFodselDto dto, Behandling behandling) {
        final FamilieHendelseGrunnlag grunnlag = hendelseTjeneste.hentAggregat(behandling);

        return getGjeldendeAntallBarnOgFødselsdato(dto, grunnlag);
    }

    private AntallBarnOgFødselsdato getGjeldendeAntallBarnOgFødselsdato(SjekkManglendeFodselDto dto,
                                                                        FamilieHendelseGrunnlag grunnlag) {
        AntallBarnOgFødselsdato resultat;
        final Optional<FamilieHendelse> bekreftetVersjon = grunnlag.getBekreftetVersjon();

        boolean brukAntallBarnISøknad = dto.getDokumentasjonForeligger() && !dto.isBrukAntallBarnITps();
        int gjeldendeAntallBarn = brukAntallBarnISøknad ? dto.getAntallBarnFodt() : bekreftetVersjon.map(FamilieHendelse::getAntallBarn).orElse(0);
        LocalDate gjeldendeFødselsdato = brukAntallBarnISøknad ? dto.getFodselsdato() : grunnlag.getGjeldendeBekreftetVersjon()
            .flatMap(FamilieHendelse::getFødselsdato)
            .orElse(grunnlag.getSøknadVersjon().getFødselsdato().orElse(grunnlag.getSøknadVersjon().getSkjæringstidspunkt()));
        resultat = new AntallBarnOgFødselsdato(gjeldendeFødselsdato, gjeldendeAntallBarn);

        if (resultat.fødselsdato == null) {
            throw FACTORY.kanIkkeUtledeGjeldendeFødselsdato().toException();
        }
        return resultat;
    }

    private Aksjonspunkt finnAksjonspunkt(Behandling behandling, BekreftetAksjonspunktDto dto) {
        return behandling.getAksjonspunkter().stream()
            .filter(ap -> ap.getAksjonspunktDefinisjon().getKode().equals(dto.getKode()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Aksjonspunkt som bekreftes må finnes på behandling.")); //$NON-NLS-1$
    }

    private Boolean hentOrginalDokumentasjonForeligger(FamilieHendelseGrunnlag grunnlag) {
        Optional<FamilieHendelse> overstyrtVersjon = grunnlag.getOverstyrtVersjon();
        FamilieHendelse overstyrt = overstyrtVersjon.orElse(null);

        if (overstyrt != null && overstyrt.getType().equals(FamilieHendelseType.FØDSEL)) {
            Optional<FamilieHendelse> bekreftet = grunnlag.getBekreftetVersjon();
            if (bekreftet.isPresent()) {
                FamilieHendelse familieHendelse = bekreftet.get();
                boolean antallBarnLike = Objects.equals(familieHendelse.getAntallBarn(), overstyrt.getAntallBarn());
                boolean fødselsdatoLike = Objects.equals(familieHendelse.getFødselsdato(), overstyrt.getFødselsdato());
                return (antallBarnLike && fødselsdatoLike);
            } else {
                return !overstyrt.getBarna().isEmpty();
            }
        }
        return null; // $NON-NLS-1$ //NOSONAR
    }

    private Integer getAntallBarnVedSøknadFødsel(FamilieHendelseGrunnlag grunnlag) {
        return grunnlag.getGjeldendeAntallBarn();
    }

    private boolean sjekkFødselsDatoOgAntallBarnEndret(FamilieHendelseGrunnlag behandlingsgrunnlag, AntallBarnOgFødselsdato dto,
                                                       boolean erEndret) {
        boolean erEndretTemp = erEndret;
        LocalDate orginalFødselsdato = getFødselsdato(behandlingsgrunnlag);
        erEndretTemp = oppdaterVedEndretVerdi(HistorikkEndretFeltType.FODSELSDATO, orginalFødselsdato, dto.fødselsdato) || erEndretTemp;
        Integer opprinneligAntallBarn = getAntallBarnVedSøknadFødsel(behandlingsgrunnlag);
        erEndretTemp = oppdaterVedEndretVerdi(HistorikkEndretFeltType.ANTALL_BARN, opprinneligAntallBarn, dto.antallBarn) || erEndretTemp;
        return erEndretTemp;
    }

    private void opprettetInnslagforFeltBrukAntallBarnITps(SjekkManglendeFodselDto dto, Behandling behandling) {
        HistorikkEndretFeltType feltNavn = dto.isBrukAntallBarnITps()
            ? HistorikkEndretFeltType.BRUK_ANTALL_I_TPS
            : (BehandlingType.REVURDERING.equals(behandling.getType())
            ? HistorikkEndretFeltType.BRUK_ANTALL_I_VEDTAKET
            : HistorikkEndretFeltType.BRUK_ANTALL_I_SOKNAD);

        if (dto.getDokumentasjonForeligger()) {
            historikkAdapter.tekstBuilder().medEndretFelt(feltNavn, null, true);
        }
    }

    private boolean oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, Object original, Object bekreftet) {
        if (!Objects.equals(bekreftet, original)) {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, original, bekreftet);
            return true;
        }
        return false;
    }

    private LocalDate getFødselsdato(FamilieHendelseGrunnlag grunnlag) {
        final Optional<LocalDate> fødselsdato = grunnlag.getGjeldendeBarna().stream()
            .map(UidentifisertBarn::getFødselsdato).findFirst();

        return fødselsdato.orElse(null);
    }

    private static class AntallBarnOgFødselsdato {
        final LocalDate fødselsdato;
        final int antallBarn;

        AntallBarnOgFødselsdato(LocalDate fødselsdato, int antallBarn) {
            this.fødselsdato = fødselsdato;
            this.antallBarn = antallBarn;
        }
    }

}
