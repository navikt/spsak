package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.behandling.aksjonspunkt.OppdateringResultat;
import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdOverstyringBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.ArbeidsforholdAdministrasjonTjeneste;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.ArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AvklarArbeidsforholdDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklarArbeidsforholdDto.class, adapter = AksjonspunktOppdaterer.class)
public class AvklarArbeidsforholdOppdaterer implements AksjonspunktOppdaterer<AvklarArbeidsforholdDto> {

    private ArbeidsforholdAdministrasjonTjeneste arbeidsforholdTjeneste;
    private TpsTjeneste tpsTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;
    private HistorikkTjenesteAdapter historikkAdapter;
    private KodeverkRepository kodeverkRepository;
    private VirksomhetRepository virksomhetRepository;

    AvklarArbeidsforholdOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public AvklarArbeidsforholdOppdaterer(ArbeidsforholdAdministrasjonTjeneste arbeidsforholdTjeneste,
                                          TpsTjeneste tpsTjeneste,
                                          VirksomhetTjeneste virksomhetTjeneste,
                                          HistorikkTjenesteAdapter historikkAdapter,
                                          GrunnlagRepositoryProvider grunnlagRepositoryProvider) {
        this.arbeidsforholdTjeneste = arbeidsforholdTjeneste;
        this.tpsTjeneste = tpsTjeneste;
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.historikkAdapter = historikkAdapter;
        this.kodeverkRepository = grunnlagRepositoryProvider.getKodeverkRepository();
        this.virksomhetRepository = grunnlagRepositoryProvider.getVirksomhetRepository();
    }

    @Override
    public OppdateringResultat oppdater(AvklarArbeidsforholdDto dto, Behandling behandling) {
        Map<String, ArbeidsforholdHandlingType> handlingTyper = kodeverkRepository.hentAlle(ArbeidsforholdHandlingType.class)
            .stream().collect(Collectors.toMap(ArbeidsforholdHandlingType::getKode, Function.identity()));

        final ArbeidsforholdInformasjonBuilder informasjonBuilder = arbeidsforholdTjeneste.opprettBuilderFor(behandling);
        informasjonBuilder.tilbakestillOverstyringer();

        List<ArbeidsforholdDto> arbeidsforhold = dto.getArbeidsforhold();
        for (ArbeidsforholdDto arbeidsforholdDto : filtrerUtArbeidsforholdSomHarBlittErsattet(arbeidsforhold)) {
            final ArbeidsforholdHandlingType handling = utledHandling(arbeidsforholdDto, handlingTyper);
            final Arbeidsgiver arbeidsgiver = hentArbeidsgiver(arbeidsforholdDto);
            final ArbeidsforholdRef ref = ArbeidsforholdRef.ref(arbeidsforholdDto.getArbeidsforholdId());

            if (skalErstatteAnnenInntektsmelding(arbeidsforholdDto)) {
                ArbeidsforholdRef gammelRef = utledArbeidsforholdIdSomSkalErstattes(arbeidsforholdDto.getErstatterArbeidsforholdId(), arbeidsforhold);
                informasjonBuilder.erstattArbedsforhold(arbeidsgiver, gammelRef, ref);
                final ArbeidsforholdOverstyringBuilder erstattBuilder = informasjonBuilder.getOverstyringBuilderFor(arbeidsgiver, gammelRef);
                erstattBuilder.medNyArbeidsforholdRef(ref);
                erstattBuilder.medHandling(handling);
                informasjonBuilder.leggTil(erstattBuilder);
            }
            ArbeidsforholdOverstyringBuilder builder = informasjonBuilder.getOverstyringBuilderFor(arbeidsgiver, ref);
            builder.medHandling(handling.equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET) ? ArbeidsforholdHandlingType.BRUK : handling)
                .medBeskrivelse(arbeidsforholdDto.getBeskrivelse());
            informasjonBuilder.leggTil(builder);
            lagHistorikkinnslagDel(behandling, lagNavn(arbeidsforholdDto), null, handling, dto.getBegrunnelse());
        }
        arbeidsforholdTjeneste.lagre(behandling, informasjonBuilder);

        return OppdateringResultat.utenOveropp();
    }

    String lagNavn(ArbeidsforholdDto dto) {
        String arbeidsgiversNavn = finnArbeidsgiversNavn(dto);
        return arbeidsgiversNavn +
            "(" +
            dto.getArbeidsgiverIdentifikator() +
            ")" +
            (dto.getArbeidsforholdId() != null
                ? "..." + dto.getArbeidsforholdId().substring(dto.getArbeidsforholdId().length() - 4, dto.getArbeidsforholdId().length())
                : "");
    }

    private String finnArbeidsgiversNavn(ArbeidsforholdDto dto) {
        // ArbeidsgiverIdentifikator er AktørId (13-tall) for person-arbeidsgiver, og orgnr (9-tall) for virksomhet
        if (OrganisasjonsNummerValidator.erGyldig(dto.getArbeidsgiverIdentifikator())){
            Virksomhet virksomhet = virksomhetRepository.hent(dto.getArbeidsgiverIdentifikator()).orElse(null);
            return virksomhet != null ? virksomhet.getNavn() : "";
        } else {
            Personinfo personinfo = tpsTjeneste.hentBrukerForAktør(new AktørId(dto.getArbeidsgiverIdentifikator())).orElse(null);
            return personinfo != null ? personinfo.getNavn() : "";
        }
    }

    private ArbeidsforholdRef utledArbeidsforholdIdSomSkalErstattes(String erstatterArbeidsforhold, List<ArbeidsforholdDto> arbeidsforhold) {
        final String arbeidsforholdId = arbeidsforhold.stream()
            .filter(af -> af.getId().equalsIgnoreCase(erstatterArbeidsforhold))
            .findAny()
            .map(ArbeidsforholdDto::getArbeidsforholdId)
            .orElse(null);
        return ArbeidsforholdRef.ref(arbeidsforholdId);
    }

    private List<ArbeidsforholdDto> filtrerUtArbeidsforholdSomHarBlittErsattet(List<ArbeidsforholdDto> arbeidsforhold) {
        Set<String> filtrertUt = arbeidsforhold.stream().map(ArbeidsforholdDto::getErstatterArbeidsforholdId).collect(Collectors.toSet());
        return arbeidsforhold.stream().filter(a -> !filtrertUt.contains(a.getId())).collect(Collectors.toList());
    }

    private void lagHistorikkinnslagDel(Behandling behandling, String sammenSattNavn, String fraVerdi, ArbeidsforholdHandlingType handlingType,
                                        String begrunnelse) {
        if (!ArbeidsforholdHandlingType.BRUK.equals(handlingType)) {
            HistorikkInnslagTekstBuilder historikkInnslagTekstBuilder = historikkAdapter.tekstBuilder();
            historikkInnslagTekstBuilder
                .medEndretFelt(HistorikkEndretFeltType.ARBEIDSFORHOLD, sammenSattNavn,
                    fraVerdi, handlingType.getNavn())
                .medSkjermlenke(SkjermlenkeType.FAKTA_OM_ARBEIDSFORHOLD)
                .medBegrunnelse(begrunnelse);

            historikkAdapter.opprettHistorikkInnslag(behandling, HistorikkinnslagType.FAKTA_ENDRET);
        }
    }

    private boolean skalErstatteAnnenInntektsmelding(ArbeidsforholdDto arbeidsforholdDto) {
        return arbeidsforholdDto.getErstatterArbeidsforholdId() != null && !arbeidsforholdDto.getErstatterArbeidsforholdId().isEmpty();
    }

    private ArbeidsforholdHandlingType utledHandling(ArbeidsforholdDto arbeidsforholdDto, Map<String, ArbeidsforholdHandlingType> typer) {
        if (arbeidsforholdDto.getFortsettBehandlingUtenInntektsmelding() != null && arbeidsforholdDto.getFortsettBehandlingUtenInntektsmelding()) {
            return typer.get(ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING.getKode());
        } else if (brukArbeidsforholdet(arbeidsforholdDto)
            && skalErstatteAnnenInntektsmelding(arbeidsforholdDto)) {
            return typer.get(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET.getKode());
        } else if (brukArbeidsforholdet(arbeidsforholdDto) && erNyttArbeidsforhold(arbeidsforholdDto)) {
            return typer.get(ArbeidsforholdHandlingType.NYTT_ARBEIDSFORHOLD.getKode());
        } else if (brukArbeidsforholdet(arbeidsforholdDto)) {
            return typer.get(ArbeidsforholdHandlingType.BRUK.getKode());
        }
        return typer.get(ArbeidsforholdHandlingType.IKKE_BRUK.getKode());
    }

    private Boolean erNyttArbeidsforhold(ArbeidsforholdDto arbeidsforholdDto) {
        return arbeidsforholdDto.getErNyttArbeidsforhold()  != null && arbeidsforholdDto.getErNyttArbeidsforhold();
    }

    private boolean brukArbeidsforholdet(ArbeidsforholdDto arbeidsforholdDto) {
        return arbeidsforholdDto.getBrukArbeidsforholdet() != null && arbeidsforholdDto.getBrukArbeidsforholdet();
    }

    private Arbeidsgiver hentArbeidsgiver(ArbeidsforholdDto dto) {
        return virksomhetTjeneste.finnOrganisasjon(dto.getArbeidsgiverIdentifikator())
            .map(Arbeidsgiver::virksomhet)
            .orElse(Arbeidsgiver.person(new AktørId(dto.getArbeidsgiverIdentifikator())));
    }
}
