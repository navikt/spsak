package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse;

import static no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse.BehandlingRelaterteYtelserMapper.RELATERT_YTELSE_TYPER_FOR_SØKER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdWrapper;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.ArbeidsforholdAdministrasjonTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.konfig.Tid;

@ApplicationScoped
public class InntektArbeidYtelseDtoMapper {

    private ArbeidsforholdAdministrasjonTjeneste inntektArbeidYtelseTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private PersonopplysningTjeneste personopplysningTjeneste;

    public InntektArbeidYtelseDtoMapper() {
        // for CDI proxy
    }

    @Inject
    public InntektArbeidYtelseDtoMapper(PersonopplysningTjeneste personopplysningTjeneste,
                                        ArbeidsforholdAdministrasjonTjeneste inntektArbeidYtelseTjeneste,
                                        InntektArbeidYtelseTjeneste iayTjeneste, BehandlingRepository behandlingRepository) {

        this.personopplysningTjeneste = personopplysningTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.iayTjeneste = iayTjeneste;
    }

    public InntektArbeidYtelseDto mapFra(Behandling behandling) {
        InntektArbeidYtelseDto dto = new InntektArbeidYtelseDto();

        mapRelaterteYtelser(dto, behandling);
        mapArbeidsforhold(dto, behandling);

        dto.setInntektsmeldinger(lagInntektsmeldingDto(behandling));
        return dto;
    }

    private void mapArbeidsforhold(InntektArbeidYtelseDto dto, Behandling behandling) {
        dto.setArbeidsforhold(inntektArbeidYtelseTjeneste.hentArbeidsforholdFerdigUtledet(behandling).stream()
            .map(wrapper -> {
                ArbeidsforholdDto arbeidsforholdDto = new ArbeidsforholdDto();
                arbeidsforholdDto.setId(lagId(wrapper));
                arbeidsforholdDto.setFomDato(wrapper.getFomDato());
                arbeidsforholdDto.setTomDato(wrapper.getTomDato() != null && wrapper.getTomDato().equals(Tid.TIDENES_ENDE) ? null : wrapper.getTomDato());
                arbeidsforholdDto.setNavn(wrapper.getNavn());
                mapArbeidsgiverIdentifikator(wrapper, arbeidsforholdDto);
                arbeidsforholdDto.setBrukArbeidsforholdet(wrapper.getBrukArbeidsforholdet());
                if (wrapper.getBrukArbeidsforholdet() != null && wrapper.getBrukArbeidsforholdet()) {
                    arbeidsforholdDto.setErNyttArbeidsforhold(wrapper.getErNyttArbeidsforhold());
                    arbeidsforholdDto.setFortsettBehandlingUtenInntektsmelding(wrapper.getFortsettBehandlingUtenInntektsmelding());
                }
                arbeidsforholdDto.setArbeidsforholdId(wrapper.getArbeidsforholdId());
                arbeidsforholdDto.setIkkeRegistrertIAaRegister(wrapper.getIkkeRegistrertIAaRegister());
                arbeidsforholdDto.setHarErstattetEttEllerFlere(wrapper.getHarErsattetEttEllerFlere());
                arbeidsforholdDto.setErstatterArbeidsforholdId(wrapper.getErstatterArbeidsforhold());
                arbeidsforholdDto.setKilde(wrapper.getKilde());
                arbeidsforholdDto.setMottattDatoInntektsmelding(wrapper.getMottattDatoInntektsmelding());
                arbeidsforholdDto.setTilVurdering(wrapper.isHarAksjonspunkt());
                arbeidsforholdDto.setBeskrivelse(wrapper.getBeskrivelse());
                arbeidsforholdDto.setVurderOmSkalErstattes(wrapper.getVurderOmSkalErstattes());
                arbeidsforholdDto.setStillingsprosent(wrapper.getStillingsprosent());
                arbeidsforholdDto.setErSlettet(wrapper.getErSlettet());
                arbeidsforholdDto.setErEndret(wrapper.getErEndret());
                return arbeidsforholdDto;
            }).collect(Collectors.toList()));
    }

    private void mapArbeidsgiverIdentifikator(ArbeidsforholdWrapper wrapper, ArbeidsforholdDto arbeidsforholdDto) {
        arbeidsforholdDto.setArbeidsgiverIdentifikator(wrapper.getArbeidsgiverIdentifikator());
        if (gjelderVirksomhet(wrapper)) {
            arbeidsforholdDto.setArbeidsgiverIdentifiktorGUI(wrapper.getArbeidsgiverIdentifikator());
        } else {
            arbeidsforholdDto.setArbeidsgiverIdentifiktorGUI(wrapper.getPersonArbeidsgiverFnr());
        }
    }

    private boolean gjelderVirksomhet(ArbeidsforholdWrapper wrapper) {
        return wrapper.getPersonArbeidsgiverFnr() == null;
    }

    private String lagId(ArbeidsforholdWrapper wrapper) {
        return wrapper.getArbeidsgiverIdentifikator() + "-" + wrapper.getArbeidsforholdId();
    }

    private List<InntektsmeldingDto> lagInntektsmeldingDto(Behandling behandling) {
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlagOptional = iayTjeneste.hentAggregatHvisEksisterer(behandling);
        if (inntektArbeidYtelseGrunnlagOptional.isPresent()) {
            Optional<InntektsmeldingAggregat> inntektsmeldingerOptional = inntektArbeidYtelseGrunnlagOptional.get().getInntektsmeldinger();
            if (inntektsmeldingerOptional.isPresent()) {
                return inntektsmeldingerOptional.get().getInntektsmeldinger().stream()
                    .map(InntektsmeldingDto::new)
                    .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private void mapRelaterteYtelser(InntektArbeidYtelseDto dto, Behandling behandling) {
        dto.setRelatertTilgrensendeYtelserForSoker(mapTilDtoSøker(hentRelaterteYtelser(behandling, behandling.getAktørId())));

    }

    private List<RelaterteYtelserDto> mapTilDtoSøker(List<TilgrensendeYtelserDto> tilgrensendeYtelserDtos) {
        return BehandlingRelaterteYtelserMapper.samleYtelserBasertPåYtelseType(tilgrensendeYtelserDtos, RELATERT_YTELSE_TYPER_FOR_SØKER);
    }

    private List<TilgrensendeYtelserDto> hentRelaterteYtelser(Behandling behandling, AktørId aktørId) {
        final List<TilgrensendeYtelserDto> relatertYtelser = new ArrayList<>();

        // Relaterte yteleser fra InntektArbeidYtelseAggregatet
        iayTjeneste.hentAggregatHvisEksisterer(behandling)
            .ifPresent(inntektArbeidYtelseGrunnlag -> relatertYtelser.addAll(mapYtelseForAktørTilTilgrensedeYtelser(inntektArbeidYtelseGrunnlag, aktørId)));

        return relatertYtelser;
    }

    private List<TilgrensendeYtelserDto> mapYtelseForAktørTilTilgrensedeYtelser(InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag, AktørId aktørId) {
        Optional<AktørYtelse> aktørYtelse = inntektArbeidYtelseGrunnlag.getAktørYtelseFørStp(aktørId);
        List<TilgrensendeYtelserDto> tilgrensedeYtelser = new ArrayList<>();
        aktørYtelse
            .ifPresent(aktYtelse -> tilgrensedeYtelser.addAll(BehandlingRelaterteYtelserMapper.mapFraBehandlingRelaterteYtelser(aktYtelse.getYtelser())));
        return tilgrensedeYtelser;
    }


}
