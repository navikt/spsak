package no.nav.foreldrepenger.domene.familiehendelse.dødsfall;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;

/**
 * Tjenesten sjekker alle EndringIdentifiserere relatert til død for endringer mellom første
 * og nåværende versjon av PersonopplysningGrunnlag.
 */
@Dependent
public class OpplysningerOmDødEndringIdentifiserer {

    private PersonopplysningRepository personopplysningRepository;
    private BarnetsDødsdatoEndringIdentifiserer barnetsDødsdatoEndringIdentifiserer;
    private ForelderErDødEndringIdentifiserer forelderErDødEndringIdentifiserer;
    private Forelder2ErDødEndringIdentifiserer forelder2ErDødEndringIdentifiserer;
    private MorErDødEndringIdentifiserer morErDødEndringIdentifiserer;

    OpplysningerOmDødEndringIdentifiserer() {
        // CDI
    }

    @Inject
    public OpplysningerOmDødEndringIdentifiserer(PersonopplysningRepository personopplysningRepository,
                                                 BarnetsDødsdatoEndringIdentifiserer barnetsDødsdatoEndringIdentifiserer,
                                                 ForelderErDødEndringIdentifiserer forelderErDødEndringIdentifiserer,
                                                 Forelder2ErDødEndringIdentifiserer forelder2ErDødEndringIdentifiserer,
                                                 MorErDødEndringIdentifiserer morErDødEndringIdentifiserer) {
        this.personopplysningRepository = personopplysningRepository;
        this.barnetsDødsdatoEndringIdentifiserer = barnetsDødsdatoEndringIdentifiserer;
        this.forelderErDødEndringIdentifiserer = forelderErDødEndringIdentifiserer;
        this.forelder2ErDødEndringIdentifiserer = forelder2ErDødEndringIdentifiserer;
        this.morErDødEndringIdentifiserer = morErDødEndringIdentifiserer;
    }

    public boolean erEndret(Behandling behandling) {
        PersonopplysningGrunnlag originaltGrunnlag = personopplysningRepository.hentFørsteVersjonAvPersonopplysninger(behandling);
        PersonopplysningGrunnlag nåværendeGrunnlag = personopplysningRepository.hentPersonopplysninger(behandling);

        boolean barnDødt = barnetsDødsdatoEndringIdentifiserer.erEndret(behandling, originaltGrunnlag);
        boolean brukerDød = forelderErDødEndringIdentifiserer.erEndret(behandling.getAktørId(), originaltGrunnlag, nåværendeGrunnlag);
        boolean forelder2Død = forelder2ErDødEndringIdentifiserer.erEndret(behandling, nåværendeGrunnlag, originaltGrunnlag);
        boolean morDød = morErDødEndringIdentifiserer.erEndret(behandling, originaltGrunnlag);

        return barnDødt || brukerDød || forelder2Død || morDød;
    }
}
