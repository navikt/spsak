package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import static java.util.Arrays.asList;

import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import no.nav.vedtak.felles.integrasjon.sigrun.BeregnetSkatt;
import no.nav.vedtak.felles.integrasjon.sigrun.SigrunConsumer;
import no.nav.vedtak.felles.integrasjon.sigrun.SigrunResponse;

@Dependent
@Alternative
@Priority(1)
class SigrunConsumerMock implements SigrunConsumer {

    @Override
    public SigrunResponse beregnetskatt(Long aktørId) {
        // lager et sett med testdata
        Map<Year, List<BeregnetSkatt>> beregnetSkatt = new HashMap<>();

        BeregnetSkatt lønn = new BeregnetSkatt("personinntektLoenn", "300000");
        BeregnetSkatt fiske = new BeregnetSkatt("personinntektFiskeFangstFamiliebarnehage", "100000");
        BeregnetSkatt næring = new BeregnetSkatt("personinntektNaering", "100000");

        beregnetSkatt.put(Year.of(2013), asList(lønn, fiske, næring));
        beregnetSkatt.put(Year.of(2014), asList(lønn, fiske, næring));
        beregnetSkatt.put(Year.of(2015), asList(lønn, fiske, næring));
        beregnetSkatt.put(Year.of(2016), asList(lønn, fiske, næring));
        beregnetSkatt.put(Year.of(2017), asList(lønn, fiske, næring));
        return new SigrunResponse(beregnetSkatt);
    }
}
