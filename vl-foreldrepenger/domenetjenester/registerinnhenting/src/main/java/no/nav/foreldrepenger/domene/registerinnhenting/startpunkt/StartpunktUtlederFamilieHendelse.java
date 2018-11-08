package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;

@ApplicationScoped
@GrunnlagRef("FamilieHendelseGrunnlag")
class StartpunktUtlederFamilieHendelse implements StartpunktUtleder {

    private FamilieHendelseRepository familieGrunnlagRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;

    StartpunktUtlederFamilieHendelse() {
        // For CDI
    }

    @Inject
    StartpunktUtlederFamilieHendelse(FamilieHendelseRepository familieGrunnlagRepository, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.familieGrunnlagRepository = familieGrunnlagRepository;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public StartpunktType utledStartpunkt(Behandling behandling, Long id1, Long id2) {
        if (erSkjæringstidspunktEndret(behandling)) {
            FellesStartpunktUtlederLogger.loggEndringSomFørteTilStartpunkt(this.getClass().getSimpleName(), StartpunktType.INNGANGSVILKÅR_OPPLYSNINGSPLIKT, "skjæringstidspunkt", id1, id2);
            return StartpunktType.INNGANGSVILKÅR_OPPLYSNINGSPLIKT;
        }
        if (erAntallBekreftedeBarnEndret(id1, id2)) {
            FellesStartpunktUtlederLogger.loggEndringSomFørteTilStartpunkt(this.getClass().getSimpleName(), StartpunktType.SØKERS_RELASJON_TIL_BARNET, "antall barn", id1, id2);
            return StartpunktType.SØKERS_RELASJON_TIL_BARNET;
        }

        FellesStartpunktUtlederLogger.loggEndringSomFørteTilStartpunkt(this.getClass().getSimpleName(), StartpunktType.UTTAKSVILKÅR, "familiehendelse", id1, id2);
        return StartpunktType.UTTAKSVILKÅR;
    }

    private boolean erSkjæringstidspunktEndret(Behandling behandling) {
        LocalDate nySkjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);
        Optional<LocalDate> origSkjæringstidspunkt = behandling.getOriginalBehandling()
            .map(orig -> skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(orig));
        Optional<LocalDate> nyBekreftetFødselsdato = familieGrunnlagRepository.hentAggregat(behandling).getGjeldendeBekreftetVersjon()
            .flatMap(FamilieHendelse::getFødselsdato);
        Optional<LocalDate> origBekreftetFødselsdato = behandling.getOriginalBehandling()
            .flatMap(orig -> familieGrunnlagRepository.hentAggregat(orig).getGjeldendeBekreftetVersjon())
            .flatMap(FamilieHendelse::getFødselsdato);

        if (nyBekreftetFødselsdato.isPresent()) {
            if (!origBekreftetFødselsdato.isPresent()
                || nyBekreftetFødselsdato.get().isBefore(origBekreftetFødselsdato.get())) {

                // Familiehendelse har blitt bekreftet etter original behandling, eller flyttet til tidligere dato
                if (nyBekreftetFødselsdato.get().isBefore(nySkjæringstidspunkt)) {
                    return true;
                }
            }
        }

        if (origSkjæringstidspunkt.isPresent()) {
            return nySkjæringstidspunkt.isBefore(origSkjæringstidspunkt.get());
        }
        return false;
    }

    private boolean erAntallBekreftedeBarnEndret(Long id1, Long id2) {
        FamilieHendelseGrunnlag grunnlag1 = familieGrunnlagRepository.hentFamilieHendelserPåId(id1);
        FamilieHendelseGrunnlag grunnlag2 = familieGrunnlagRepository.hentFamilieHendelserPåId(id2);
        Integer antallBarn1 = grunnlag1.getBekreftetVersjon().map(FamilieHendelse::getAntallBarn).orElse(0);
        Integer antallBarn2 = grunnlag2.getGjeldendeBekreftetVersjon().map(FamilieHendelse::getAntallBarn).orElse(0);

        return !antallBarn1.equals(antallBarn2);
    }
}
