package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Default test scenario builder for Far søker Engangsstønad. Kan opprettes for fødsel eller adopsjon og brukes til å
 * opprette standard scenarioer.
 * <p>
 * Oppretter en default behandling, inkludert default grunnlag med søknad + tomt innangsvilkårresultat.
 * <p>
 * Kan bruke settere (evt. legge til) for å tilpasse utgangspunktet.
 * <p>
 * Mer avansert bruk er ikke gitt at kan bruke denne
 * klassen.
 */
public class ScenarioFarSøkerEngangsstønad extends AbstractTestScenario<ScenarioFarSøkerEngangsstønad> {

    private ScenarioFarSøkerEngangsstønad(boolean medDefaultSøknad) {
        super(NavBrukerKjønn.MANN, new AktørId(999L));
        if (medDefaultSøknad) {
            // Defaults - antar default alltid minimum med en søknad
            medDefaultOppgittTilknytning();
            medSøknad()
                .medRelasjonsRolleType(RelasjonsRolleType.FARA)
                .medSøknadsdato(LocalDate.now(FPDateUtil.getOffset()));
        }

    }

    public static ScenarioFarSøkerEngangsstønad forFødselUtenSøknad() {
        return new ScenarioFarSøkerEngangsstønad(false);
    }

    public static ScenarioFarSøkerEngangsstønad forAdopsjonUtenSøknad() {
        return new ScenarioFarSøkerEngangsstønad( false);
    }

    public static ScenarioFarSøkerEngangsstønad forFødsel() {
        return new ScenarioFarSøkerEngangsstønad( true);
    }

    public static ScenarioFarSøkerEngangsstønad forAdopsjon() {
        return new ScenarioFarSøkerEngangsstønad( true);
    }

}
