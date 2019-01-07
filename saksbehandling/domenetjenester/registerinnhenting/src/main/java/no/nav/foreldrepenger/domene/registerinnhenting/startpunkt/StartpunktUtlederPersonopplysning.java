package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabellRepository;
import no.nav.foreldrepenger.domene.personopplysning.identifiserer.PersonAdresseEndringIdentifiserer;
import no.nav.foreldrepenger.domene.personopplysning.identifiserer.PersonstatusEndringIdentifiserer;
import no.nav.foreldrepenger.domene.personopplysning.identifiserer.SivilstandEndringIdentifiserer;
import no.nav.foreldrepenger.domene.personopplysning.identifiserer.StatsborgerskapEndringIdentifiserer;

@ApplicationScoped
@GrunnlagRef("PersonInformasjon")
class StartpunktUtlederPersonopplysning implements StartpunktUtleder {

    private PersonopplysningRepository personopplysningRepository;
    private KodeverkTabellRepository kodeverkTabellRepository;

    private SivilstandEndringIdentifiserer sivilstandEndringIdentifiserer;
    private PersonstatusEndringIdentifiserer personstatusEndringIdentifiserer;
    private StatsborgerskapEndringIdentifiserer statsborgerskapEndringIdentifiserer;
    private PersonAdresseEndringIdentifiserer adresseEndringIdentifiserer;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;

    StartpunktUtlederPersonopplysning() {
        // For CDI
    }

    @Inject
    StartpunktUtlederPersonopplysning(GrunnlagRepositoryProvider repositoryProvider, // NOSONAR - ingen enkel måte å unngå mange parametere (endringsidentifiserere) her
                                      SivilstandEndringIdentifiserer sivilstandEndringIdentifiserer,
                                      PersonstatusEndringIdentifiserer personstatusEndringIdentifiserer, StatsborgerskapEndringIdentifiserer statsborgerskapEndringIdentifiserer,
                                      PersonAdresseEndringIdentifiserer adresseEndringIdentifiserer, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
        this.kodeverkTabellRepository = repositoryProvider.getKodeverkRepository().getKodeverkTabellRepository();
        this.sivilstandEndringIdentifiserer = sivilstandEndringIdentifiserer;
        this.personstatusEndringIdentifiserer = personstatusEndringIdentifiserer;
        this.statsborgerskapEndringIdentifiserer = statsborgerskapEndringIdentifiserer;
        this.adresseEndringIdentifiserer = adresseEndringIdentifiserer;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public StartpunktType utledStartpunkt(Behandling behandling, Long grunnlagId1, Long grunnlagId2) {
        PersonopplysningGrunnlag grunnlag1 = personopplysningRepository.hentPersonopplysningerPåId(grunnlagId1);
        PersonopplysningGrunnlag grunnlag2 = personopplysningRepository.hentPersonopplysningerPåId(grunnlagId2);
        return utled(behandling, grunnlag1, grunnlag2);
    }

    private StartpunktType utled(Behandling behandling, PersonopplysningGrunnlag grunnlag1, PersonopplysningGrunnlag grunnlag2) {

        return hentAlleStartpunktForPersonopplysninger(behandling, grunnlag1, grunnlag2).stream()
            .map(it -> kodeverkTabellRepository.finnStartpunktType(it.getKode())) // Må oppfriskes
            .min(Comparator.comparing(StartpunktType::getRangering))
            .orElse(StartpunktType.UDEFINERT);
    }

    // Finn endringer per aggregat under grunnlaget og map dem mot startpunkt.
    private List<StartpunktType> hentAlleStartpunktForPersonopplysninger(Behandling behandling1, PersonopplysningGrunnlag grunnlag1, PersonopplysningGrunnlag grunnlag2) {
        List<StartpunktType> startpunkter = new ArrayList<>();
        final LocalDate skjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling1);

        boolean sivilstandEndret = sivilstandEndringIdentifiserer.erEndret(behandling1.getAktørId(), grunnlag1, grunnlag2);
        boolean personstatusEndret = personstatusEndringIdentifiserer.erEndret(grunnlag1, grunnlag2);
        boolean personstatusUnntattDødEndret = personstatusUnntattDødEndret(personstatusEndret);
        boolean statsborgerskapEndret = statsborgerskapEndringIdentifiserer.erEndret(grunnlag1, grunnlag2);
        boolean adresseEndret = adresseEndringIdentifiserer.erEndret(behandling1, grunnlag1, grunnlag2);

        if (sivilstandEndret) {
            FellesStartpunktUtlederLogger.loggEndringSomFørteTilStartpunkt(this.getClass().getSimpleName(), StartpunktType.UTTAKSVILKÅR, "sivilstand og/eller foreldres død", grunnlag1.getId(), grunnlag2.getId());
            startpunkter.add(StartpunktType.UTTAKSVILKÅR);
        }

        if (personstatusUnntattDødEndret || statsborgerskapEndret || adresseEndret) {
            final boolean personstatusEndretFørStp = personstatusEndringIdentifiserer.erEndretFørSkjæringstidspunkt(grunnlag1, grunnlag2, skjæringstidspunkt);
            final boolean adresseEndretFørStp = adresseEndringIdentifiserer.erEndretFørSkjæringstidspunkt(behandling1.getAktørId(), grunnlag1, grunnlag2, skjæringstidspunkt);
            final boolean statsborgerskapEndretFørStp = statsborgerskapEndringIdentifiserer.erEndretFørSkjæringstidspunkt(grunnlag1, grunnlag2, skjæringstidspunkt);

            if (personstatusEndretFørStp || adresseEndretFørStp || statsborgerskapEndretFørStp) {
                FellesStartpunktUtlederLogger.loggEndringSomFørteTilStartpunkt(this.getClass().getSimpleName(), StartpunktType.INNGANGSVILKÅR_MEDLEMSKAP, "adresse, statsborgerskap og/eller personstatus", grunnlag1.getId(), grunnlag2.getId());
                startpunkter.add(StartpunktType.INNGANGSVILKÅR_MEDLEMSKAP);
            } else {
                FellesStartpunktUtlederLogger.loggEndringSomFørteTilStartpunkt(this.getClass().getSimpleName(), StartpunktType.UTTAKSVILKÅR, "adresse, statsborgerskap og/eller personstatus", grunnlag1.getId(), grunnlag2.getId());
                startpunkter.add(StartpunktType.UTTAKSVILKÅR);
            }
        }

        if (startpunkter.isEmpty()) {
            // Har ikke identifisert endringen som trigget utledning av startpunkt - sett default-verdi ved andre endringer
            FellesStartpunktUtlederLogger.loggEndringSomFørteTilStartpunkt(this.getClass().getSimpleName(), StartpunktType.INNGANGSVILKÅR_MEDLEMSKAP, "kunne ikke fastsettes", grunnlag1.getId(), grunnlag2.getId());
            startpunkter.add(StartpunktType.INNGANGSVILKÅR_MEDLEMSKAP);
        }

        return startpunkter;
    }

    private boolean personstatusUnntattDødEndret(boolean personstatusEndret) {
        return personstatusEndret;
    }
}
