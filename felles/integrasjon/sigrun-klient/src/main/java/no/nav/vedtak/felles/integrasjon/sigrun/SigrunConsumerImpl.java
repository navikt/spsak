package no.nav.vedtak.felles.integrasjon.sigrun;

import static java.util.Arrays.asList;

import java.net.URI;
import java.time.Year;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.type.TypeReference;

import no.nav.vedtak.felles.integrasjon.rest.JsonMapper;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.FPDateUtil;


@ApplicationScoped
public class SigrunConsumerImpl implements SigrunConsumer {

    private SigrunRestClient sigrunRestClient;

    SigrunConsumerImpl() {
        //CDI
    }

    @Inject
    public SigrunConsumerImpl(SigrunRestClient sigrunRestClient, @KonfigVerdi("SigrunRestBeregnetSkatt.url") URI endpoint) {
        this.sigrunRestClient = sigrunRestClient;
        this.sigrunRestClient.setEndpoint(endpoint);
    }

    @Override
    public SigrunResponse beregnetskatt(Long aktørId) {
        Map<Year, List<BeregnetSkatt>> årTilListeMedSkatt = new HashMap<>();
        ferdiglignedeÅr()
                .stream()
                .collect(Collectors.toMap(år -> år, år -> {
                    String resultat = sigrunRestClient.hentBeregnetSkattForAktørOgÅr(aktørId, år.toString());
                    return resultat != null ? resultat : "";
                }))
                .forEach((resulatÅr, skatt) -> leggTil(årTilListeMedSkatt, resulatÅr, skatt));

        return new SigrunResponse(årTilListeMedSkatt);
    }

    private void leggTil(Map<Year, List<BeregnetSkatt>> årTilListeMedSkatt, Year år, String skatt) {
        årTilListeMedSkatt.put(år, skatt.isEmpty()
                ? Collections.emptyList()
                : JsonMapper.fromJson(skatt, new TypeReference<List<BeregnetSkatt>>() {
        }));
    }

    //TODO(OJR) bestem hvordan vi skal utlede ferdiglignede år
    private List<Year> ferdiglignedeÅr() {
        Year iFjor = Year.now(FPDateUtil.getOffset()).minusYears(1L);
        return asList(iFjor, iFjor.minusYears(1L), iFjor.minusYears(2L));
    }
}
