package no.nav.foreldrepenger.behandling.steg.beregnytelse.es;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;

class BarnFinner {

    private FamilieHendelseRepository familieGrunnlagRepository;

    BarnFinner(BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.familieGrunnlagRepository = behandlingRepositoryProvider.getFamilieGrunnlagRepository();
    }

    int finnAntallBarn(Behandling behandling, int maksStønadsalderAdopsjon) {

        final FamilieHendelseGrunnlag grunnlag = familieGrunnlagRepository.hentAggregat(behandling);
        List<BarnInfo> barnSøktFor = getBarnInfoer(grunnlag);
        return finnAntallBarn(maksStønadsalderAdopsjon, grunnlag, barnSøktFor);
    }

    private int finnAntallBarn(int maksStønadsalderAdopsjon, final FamilieHendelseGrunnlag grunnlag,
                               List<BarnInfo> barnSøktFor) {
        List<BarnInfo> barnKvalifisertForYtelse = Objects.equals(FamilieHendelseType.ADOPSJON, grunnlag.getGjeldendeVersjon().getType())
            ? barnKvalifisertForAdopsjon(maksStønadsalderAdopsjon, grunnlag, barnSøktFor)
            : barnSøktFor;

        if (barnKvalifisertForYtelse.isEmpty()) {
            throw BeregneYtelseFeil.FACTORY.beregningsstegIkkeStøttetForBehandling().toException();
        }
        return barnKvalifisertForYtelse.size();
    }

    private List<BarnInfo> barnKvalifisertForAdopsjon(int maksStønadsalderAdopsjon, final FamilieHendelseGrunnlag grunnlag, List<BarnInfo> barnSøktFor) {
        Optional<Adopsjon> gjeldendeAdopsjon = grunnlag.getGjeldendeAdopsjon();
        if (!gjeldendeAdopsjon.isPresent()) {
            // skal aldri kunne skje, men logikken for å sjekke ifPresent er basert på negativ testing hvilket kan være ustabilt.
            // legger derfor på her
            throw new IllegalStateException("Mangler grunnlag#getGjeldendeAdopsjon i " + grunnlag);
        }

        Adopsjon adopsjon = gjeldendeAdopsjon.get();
        LocalDate eldsteFristForOmsorgsovertakelse = adopsjon.getOmsorgsovertakelseDato().minusYears(maksStønadsalderAdopsjon);

        return barnSøktFor.stream()
            .filter(barn -> {
                LocalDate fødselsdato = barn.getFødselsdato();
                return fødselsdato.isAfter(eldsteFristForOmsorgsovertakelse);
            })
            .collect(Collectors.toList());
    }

    private List<BarnInfo> getBarnInfoer(FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        final FamilieHendelseType type = familieHendelseGrunnlag.getGjeldendeVersjon().getType();
        if (Objects.equals(FamilieHendelseType.FØDSEL, type) || Objects.equals(FamilieHendelseType.TERMIN, type)) {
            return fødselsvilkårTilBarnInfoer(familieHendelseGrunnlag);
        } else if (Objects.equals(FamilieHendelseType.ADOPSJON, type)) {
            return adopsjonsvilkårTilBarnInfoer(familieHendelseGrunnlag);
        } else if (Objects.equals(FamilieHendelseType.OMSORG, type)) {
            return adopsjonsvilkårTilBarnInfoer(familieHendelseGrunnlag);
        } else {
            return Collections.emptyList();
        }
    }

    private List<BarnInfo> fødselsvilkårTilBarnInfoer(FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        final FamilieHendelse gjeldendeVersjon = familieHendelseGrunnlag.getGjeldendeVersjon();
        final List<UidentifisertBarn> gjeldendeBarn = familieHendelseGrunnlag.getGjeldendeBarna();

        if (FamilieHendelseType.FØDSEL.equals(gjeldendeVersjon.getType()) && !gjeldendeBarn.isEmpty()) {
            return gjeldendeBarn.stream()
                .map(it -> new BarnInfo(it.getBarnNummer(), it.getFødselsdato(), null))
                .collect(Collectors.toList());
        } else {
            Optional<Terminbekreftelse> gjeldendeTerminbekreftelse = familieHendelseGrunnlag.getGjeldendeTerminbekreftelse();
            if (gjeldendeTerminbekreftelse.isPresent()) {
                Terminbekreftelse terminbekreftelse = gjeldendeTerminbekreftelse.get();
                Integer antallBarn = gjeldendeVersjon.getAntallBarn();
                List<BarnInfo> barnInfoer = new ArrayList<>();
                for (int i = 0; i < antallBarn; i++) {
                    barnInfoer.add(new BarnInfo(i, terminbekreftelse.getTermindato(), null));
                }
                return barnInfoer;
            } else {
                return Collections.emptyList();
            }
        }
    }

    private List<BarnInfo> adopsjonsvilkårTilBarnInfoer(FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        return familieHendelseGrunnlag.getGjeldendeBarna().stream()
            .map(adopsjonBarn -> new BarnInfo(adopsjonBarn.getBarnNummer(), adopsjonBarn.getFødselsdato(), null))
            .collect(Collectors.toList());
    }
}
