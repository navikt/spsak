package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.web.app.tjenester.behandling.SøknadType;

/**
 * Bygger et sammen satt resultat av avklarte data for en Familiehendelse (fødsel, adopsjon, omsorgsovertagelse)
 */
@ApplicationScoped
public class FamiliehendelseDataDtoTjenesteImpl implements FamiliehendelseDataDtoTjeneste {

    // TODO (OJR) Bør denne hardkodast her? FC: NOPE
    private static final Integer ANTALL_UKER_I_SVANGERSKAP = 40;

    private BehandlingRepositoryProvider repositoryProvider;

    FamiliehendelseDataDtoTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public FamiliehendelseDataDtoTjenesteImpl(BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.repositoryProvider = behandlingRepositoryProvider;
    }

    @Override
    public Optional<FamiliehendelseDto> mapFra(Behandling behandling) {
        return mapFraType(behandling);
    }

    private Optional<FamiliehendelseDto> mapFraType(Behandling behandling) {
        final Optional<FamilieHendelseGrunnlag> grunnlagOpt1 = repositoryProvider.getFamilieGrunnlagRepository()
            .hentAggregatHvisEksisterer(behandling);

        if (grunnlagOpt1.isPresent()) {
            FamilieHendelseGrunnlag grunnlag = grunnlagOpt1.get();

            if (grunnlag.getGjeldendeVersjon().getGjelderFødsel()) {
                return lagFodselDto(grunnlag, behandling);
            } else if (FamilieHendelseType.ADOPSJON.equals(grunnlag.getGjeldendeVersjon().getType())) {
                return lagAdopsjonDto(grunnlag);
            } else if (FamilieHendelseType.OMSORG.equals(grunnlag.getGjeldendeVersjon().getType())) {
                return lagOmsorgDto(grunnlag);
            }
        }
        return Optional.empty();
    }

    private Optional<FamiliehendelseDto> lagFodselDto(FamilieHendelseGrunnlag grunnlag, Behandling behandling) {
        AvklartDataFodselDto dto = new AvklartDataFodselDto();
        grunnlag.getGjeldendeBekreftetVersjon().ifPresent(hendelse -> {
            hendelse.getTerminbekreftelse().ifPresent(terminbekreftelse -> {
                dto.setTermindato(terminbekreftelse.getTermindato());
                dto.setUtstedtdato(terminbekreftelse.getUtstedtdato());
                dto.setAntallBarnTermin(hendelse.getAntallBarn());
                finnUkerUtISvangerskapet(terminbekreftelse, behandling.getOriginalVedtaksDato()).ifPresent(dto::setVedtaksDatoSomSvangerskapsuke);
            });
            hendelse.getFødselsdato().ifPresent(dato -> {
                dto.setFodselsdato(dato);
                dto.setAntallBarnFødt(hendelse.getAntallBarn());
            });
            if (grunnlag.getHarOverstyrteData() && grunnlag.getOverstyrtVersjon().get().getType().equals(FamilieHendelseType.FØDSEL)) {
                final boolean brukAntallBarnFraTps = harValgtSammeSomBekreftet(grunnlag);
                dto.setBrukAntallBarnFraTps(brukAntallBarnFraTps);
                dto.setErOverstyrt(!brukAntallBarnFraTps);
            }
            dto.setMorForSykVedFodsel(hendelse.erMorForSykVedFødsel());
            dto.setSkjæringstidspunkt(hendelse.getSkjæringstidspunkt());
        });
        return Optional.of(dto);
    }

    private Optional<Long> finnUkerUtISvangerskapet(Terminbekreftelse terminbekreftelse, LocalDate originalVedtaksDato) {
        LocalDate termindato = terminbekreftelse.getTermindato();

        if (originalVedtaksDato != null && termindato != null) {
            LocalDate termindatoMinusUkerISvangerskap = termindato.minusWeeks(ANTALL_UKER_I_SVANGERSKAP);
            return Optional.of(ChronoUnit.WEEKS.between(termindatoMinusUkerISvangerskap, originalVedtaksDato) + 1);
        } else {
            return Optional.empty();
        }
    }

    private boolean harValgtSammeSomBekreftet(FamilieHendelseGrunnlag grunnlag) {
        final Optional<FamilieHendelse> bekreftet = grunnlag.getBekreftetVersjon();
        final FamilieHendelse overstyrt = grunnlag.getOverstyrtVersjon().get(); // NOSONAR

        boolean antallBarnLike = false;
        boolean fødselsdatoLike = false;
        if (bekreftet.isPresent()) {
            antallBarnLike = Objects.equals(bekreftet.get().getAntallBarn(), overstyrt.getAntallBarn());
            fødselsdatoLike = Objects.equals(bekreftet.get().getFødselsdato(), overstyrt.getFødselsdato());
        }
        return (antallBarnLike && fødselsdatoLike) || (!bekreftet.isPresent() && overstyrt.getBarna().isEmpty());
    }

    private Optional<FamiliehendelseDto> lagAdopsjonDto(FamilieHendelseGrunnlag grunnlag) {
        AvklartDataAdopsjonDto dto = new AvklartDataAdopsjonDto();

        Optional<FamilieHendelse> gjeldendeBekreftetVersjon = grunnlag.getGjeldendeBekreftetVersjon();
        if (gjeldendeBekreftetVersjon.isPresent()) {
            FamilieHendelse bekreftet = gjeldendeBekreftetVersjon.get();
            Map<Integer, LocalDate> fødselsdatoer = bekreftet.getBarna().stream()
                .collect(Collectors.toMap(UidentifisertBarn::getBarnNummer, UidentifisertBarn::getFødselsdato));

            bekreftet.getAdopsjon().ifPresent(adopsjon -> {
                dto.setEktefellesBarn(adopsjon.getErEktefellesBarn());
                dto.setMannAdoptererAlene(adopsjon.getAdoptererAlene());
                dto.setOmsorgsovertakelseDato(adopsjon.getOmsorgsovertakelseDato());
                dto.setAnkomstNorge(adopsjon.getAnkomstNorgeDato());
                dto.setAdopsjonFodelsedatoer(fødselsdatoer);
            });
            return Optional.of(dto);
        }
        return Optional.of(dto);
    }

    private Optional<FamiliehendelseDto> lagOmsorgDto(FamilieHendelseGrunnlag grunnlag) {
        SøknadType søknadType = SøknadType.fra(grunnlag.getGjeldendeVersjon());
        AvklartDataOmsorgDto dto = new AvklartDataOmsorgDto(søknadType);
        grunnlag.getGjeldendeBekreftetVersjon().ifPresent(hendelse -> {
            hendelse.getAdopsjon().ifPresent(adopsjon -> {
                dto.setOmsorgsovertakelseDato(adopsjon.getOmsorgsovertakelseDato());
                dto.setForeldreansvarDato(adopsjon.getForeldreansvarDato());
                dto.setVilkarType(adopsjon.getOmsorgovertakelseVilkår());
            });
            dto.setAntallBarnTilBeregning(hendelse.getAntallBarn());
        });
        return Optional.of(dto);
    }
}
