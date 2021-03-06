package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import java.time.LocalDate;
import java.util.UUID;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Default test scenario builder for Mor søker Engangsstønad. Kan opprettes for fødsel eller adopsjon og brukes til å
 * opprette standard scenarioer.
 * <p>
 * Oppretter en default behandling, inkludert default grunnlag med søknad + tomt innangsvilkårresultat.
 * <p>
 * Kan bruke settere (evt. legge til) for å tilpasse utgangspunktet.
 * <p>
 * Mer avansert bruk er ikke gitt at kan bruke denne
 * klassen.
 */
public class ScenarioMorSøkerEngangsstønad extends AbstractTestScenario<ScenarioMorSøkerEngangsstønad> {

    private ScenarioMorSøkerEngangsstønad(boolean medDefaultSøknad) {
        super(NavBrukerKjønn.KVINNE, new AktørId(1999L));
        if (medDefaultSøknad) {
            // Defaults - antar default alltid minimum med en søknad
            medDefaultOppgittTilknytning();
            medSøknad()
                .medSøknadReferanse(UUID.randomUUID().toString())
                .medSykemeldinReferanse(UUID.randomUUID().toString())
                .medSøknadsdato(LocalDate.now(FPDateUtil.getOffset()));
        }

    }

    public static ScenarioMorSøkerEngangsstønad forDefaultAktør(boolean medDefaultSøknad) {
        return new ScenarioMorSøkerEngangsstønad(medDefaultSøknad);
    }

    public static ScenarioMorSøkerEngangsstønad forDefaultAktør() {
        return new ScenarioMorSøkerEngangsstønad(true);
    }

    public BehandlingLås taSkriveLåsForBehandling() {
        return mockBehandlingRepository().taSkriveLås(getBehandling());
    }

}
