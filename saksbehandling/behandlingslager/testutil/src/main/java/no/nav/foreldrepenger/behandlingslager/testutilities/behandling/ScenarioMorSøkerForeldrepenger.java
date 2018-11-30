package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import java.time.LocalDate;
import java.util.UUID;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
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
 */
public class ScenarioMorSøkerForeldrepenger extends AbstractTestScenario<ScenarioMorSøkerForeldrepenger> {

    private ScenarioMorSøkerForeldrepenger(boolean medDefaultSøknad) {
        super(NavBrukerKjønn.KVINNE, new AktørId(1999L));
        settDefaltSøknad(medDefaultSøknad);

    }

    private ScenarioMorSøkerForeldrepenger(boolean medDefaultSøknad, AktørId aktørId) {
        super(NavBrukerKjønn.KVINNE, aktørId);
        settDefaltSøknad(medDefaultSøknad);
    }

    public ScenarioMorSøkerForeldrepenger(boolean medDefaultSøknad, NavBruker navBruker) {
        super(navBruker);
        settDefaltSøknad(medDefaultSøknad);
    }

    private void settDefaltSøknad(boolean medDefaultSøknad) {
        if (medDefaultSøknad) {
            // Defaults - antar default alltid minimum med en søknad
            medDefaultOppgittTilknytning();
            medDefaultInntektArbeidYtelse();

            medSøknad()
                .medSøknadReferanse(UUID.randomUUID().toString())
                .medSykemeldinReferanse(UUID.randomUUID().toString())
                .medSøknadsdato(LocalDate.now(FPDateUtil.getOffset()));
        }
    }

    public static ScenarioMorSøkerForeldrepenger forDefaultAktør() {
        return new ScenarioMorSøkerForeldrepenger(true);
    }

    public static ScenarioMorSøkerForeldrepenger forAktør(boolean defaultSøknad, AktørId aktørId) {
        return new ScenarioMorSøkerForeldrepenger(defaultSøknad, aktørId);
    }

    public static ScenarioMorSøkerForeldrepenger forAktør(AktørId aktørId) {
        return new ScenarioMorSøkerForeldrepenger(true, aktørId);
    }

    public static ScenarioMorSøkerForeldrepenger forBruker(NavBruker navBruker) {
        return new ScenarioMorSøkerForeldrepenger(true, navBruker);
    }

    public Beregningsgrunnlag.Builder medBeregningsgrunnlag() {
        BeregningsgrunnlagScenario beregningsgrunnlagScenario = new BeregningsgrunnlagScenario();
        leggTilScenario(beregningsgrunnlagScenario);
        return beregningsgrunnlagScenario.getBeregningsgrunnlagBuilder();
    }

}
