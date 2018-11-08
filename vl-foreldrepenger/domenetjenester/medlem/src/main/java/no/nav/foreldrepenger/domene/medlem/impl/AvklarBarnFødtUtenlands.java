package no.nav.foreldrepenger.domene.medlem.impl;

import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.JA;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.NEI;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOpphold;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytning;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;


class AvklarBarnFødtUtenlands {

    private MedlemskapRepository medlemskapRepository;
    private FamilieHendelseRepository familieHendelseRepository;

    AvklarBarnFødtUtenlands(BehandlingRepositoryProvider repositoryProvider) {
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
    }

    //TODO(OJR) skal denne endres til å støtte vurderingsdato? tror svaret er ja...
    Optional<MedlemResultat> utled(Behandling behandling, LocalDate vurderingsdato) {
        FamilieHendelse bekreftetFH = familieHendelseRepository.hentAggregat(behandling).getBekreftetVersjon().orElse(null);

        if (!((erSøktPåBakgrunnAvFødselsdato(behandling) == JA) || erFødselBekreftet(bekreftetFH) == JA)) {
            return Optional.empty();
        } else {
            if ((erFødselsdatoFraTpsInnenforEnOppgittUtlandsperiode(bekreftetFH, behandling) == JA)
                || (erFødselsdatoFraSøknadInnenforEnOppgittUtlandsperiode(behandling) == JA)) {
                return Optional.of(MedlemResultat.AVKLAR_OM_ER_BOSATT);
            }
        }
        return Optional.empty();
    }

    private Utfall erSøktPåBakgrunnAvFødselsdato(Behandling behandling) {
        FamilieHendelseGrunnlag grunnlag = familieHendelseRepository.hentAggregat(behandling);

        if (!grunnlag.getGjeldendeVersjon().getTerminbekreftelse().isPresent() && !grunnlag.getGjeldendeVersjon().getAdopsjon().isPresent()) {
            return JA;
        }
        return NEI;
    }

    private Utfall erFødselBekreftet(FamilieHendelse bekreftet) {
        return bekreftet != null && ! bekreftet.getBarna().isEmpty() ? JA : NEI;
    }

    private Utfall erFødselsdatoFraTpsInnenforEnOppgittUtlandsperiode(FamilieHendelse bekreftet, Behandling behandling) {
        Optional<MedlemskapAggregat> aggregat = medlemskapRepository.hentMedlemskap(behandling);
        if (!aggregat.isPresent() || bekreftet == null) {
            return NEI;
        }
        MedlemskapAggregat medlemskapAggregat = aggregat.get();
        Optional<Set<OppgittLandOpphold>> utenlandsopphold = getOppgittUtenlandsOpphold(medlemskapAggregat);
        if (!utenlandsopphold.isPresent()) {
            return NEI;
        }

        for (UidentifisertBarn barnet : bekreftet.getBarna()) {
            if (erFødselsdatoInnenforEtUtenlandsopphold(barnet.getFødselsdato(), utenlandsopphold)) {
                return JA;
            }
        }
        return NEI;
    }

    private Utfall erFødselsdatoFraSøknadInnenforEnOppgittUtlandsperiode(Behandling behandling) {
        FamilieHendelseGrunnlag grunnlag = familieHendelseRepository.hentAggregat(behandling);

        Optional<MedlemskapAggregat> aggregat = medlemskapRepository.hentMedlemskap(behandling);
        if (!aggregat.isPresent()) {
            return NEI;
        }
        MedlemskapAggregat medlemskapAggregat = aggregat.get();
        Optional<Set<OppgittLandOpphold>> utenlandsopphold = getOppgittUtenlandsOpphold(medlemskapAggregat);
        if (!utenlandsopphold.isPresent()) {
            return NEI;
        }
        List<UidentifisertBarn> barnFraSøknad = grunnlag.getGjeldendeVersjon().getBarna();

        for (UidentifisertBarn barnet : barnFraSøknad) {
            if (erFødselsdatoInnenforEtUtenlandsopphold(barnet.getFødselsdato(), utenlandsopphold)) {
                return JA;
            }
        }
        return NEI;
    }

    private Optional<Set<OppgittLandOpphold>> getOppgittUtenlandsOpphold(MedlemskapAggregat medlemskapAggregat) {
        Optional<Set<OppgittLandOpphold>> opphold = medlemskapAggregat.getOppgittTilknytning().map(OppgittTilknytning::getOpphold);
        if (!opphold.isPresent()) {
            return Optional.empty();
        }
        Set<OppgittLandOpphold> utenlandsOpphold = opphold.get().stream().filter(o -> !o.getLand().equals(Landkoder.NOR)).collect(Collectors.toSet());
        if (utenlandsOpphold.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(utenlandsOpphold);
    }

    private boolean erFødselsdatoInnenforEtUtenlandsopphold(LocalDate barnetsFødselsdato, Optional<Set<OppgittLandOpphold>> utenlandsopphold) {
        for (OppgittLandOpphold utenlandsoppholdet : utenlandsopphold.get()) {
            if (erBarnetFødtUnderDetteUtenlandsoppholdet(barnetsFødselsdato, utenlandsoppholdet.getPeriodeFom(), utenlandsoppholdet.getPeriodeTom())) {
                return true;
            }
        }
        return false;
    }

    private boolean erBarnetFødtUnderDetteUtenlandsoppholdet(LocalDate barnetsFødselsdato, LocalDate startUtenlandsopphold, LocalDate sluttUtenlandsopphold) {
        return barnetsFødselsdato.isAfter(startUtenlandsopphold) && barnetsFødselsdato.isBefore(sluttUtenlandsopphold);
    }
}
