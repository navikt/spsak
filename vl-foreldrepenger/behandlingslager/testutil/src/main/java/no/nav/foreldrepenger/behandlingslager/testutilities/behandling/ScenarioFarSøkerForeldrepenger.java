package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Default test scenario builder for Mor søker Foreldrepenger. Kan opprettes for fødsel og brukes til å
 * opprette standard scenarioer.
 * <p>
 * Oppretter en default behandling, inkludert default grunnlag med søknad + tomt innangsvilkårresultat.
 * <p>
 * Kan bruke settere (evt. legge til) for å tilpasse utgangspunktet.
 * <p>
 * Mer avansert bruk er ikke gitt at kan bruke denne
 * klassen.
 *
 */
public class ScenarioFarSøkerForeldrepenger extends AbstractTestScenario<ScenarioFarSøkerForeldrepenger> {

    private ScenarioFarSøkerForeldrepenger(boolean medDefaultSøknad) {
        super(NavBrukerKjønn.MANN, new AktørId(999L));
        settDefaltSøknad(medDefaultSøknad);

    }

    private ScenarioFarSøkerForeldrepenger(boolean medDefaultSøknad, AktørId aktørId) {
        super(NavBrukerKjønn.MANN, aktørId);
        settDefaltSøknad(medDefaultSøknad);
    }

    private void settDefaltSøknad(boolean medDefaultSøknad) {
        if (medDefaultSøknad) {
            // Defaults - antar default alltid minimum med en søknad
            medDefaultOppgittTilknytning();
            medDefaultInntektArbeidYtelse();
            medSøknad()
                .medSøknadsdato(LocalDate.now(FPDateUtil.getOffset()));
        }
    }

    public static ScenarioFarSøkerForeldrepenger forFødsel() {
        return new ScenarioFarSøkerForeldrepenger( true);
    }

    public static ScenarioFarSøkerForeldrepenger forFødsel(boolean defaultSøknad, AktørId aktørId) {
        return new ScenarioFarSøkerForeldrepenger(defaultSøknad, aktørId);
    }

    public static ScenarioFarSøkerForeldrepenger forFødselMedGittAktørId(AktørId aktørId) {
        return new ScenarioFarSøkerForeldrepenger( true, aktørId);
    }

    public static ScenarioFarSøkerForeldrepenger forAdopsjon() {
        return new ScenarioFarSøkerForeldrepenger( true);
    }

    public Beregningsgrunnlag.Builder medBeregningsgrunnlag() {
        BeregningsgrunnlagScenario beregningsgrunnlagScenario = new BeregningsgrunnlagScenario();
        leggTilScenario(beregningsgrunnlagScenario);
        return beregningsgrunnlagScenario.getBeregningsgrunnlagBuilder();
    }
}
