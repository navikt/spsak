package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperFP.mapUtenlandsopphold;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperFelles.erTomListe;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperFelles.mapAnnenForelder;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperFelles.mapRelasjonTilBarnet;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperFelles.mapSøknad;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperFelles.opprettOppholdNorge;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringEngangsstonadDto;
import no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v1.Engangsstønad;
import no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v1.ObjectFactory;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.OppholdNorge;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.SoekersRelasjonTilBarnet;
import no.nav.vedtak.felles.xml.soeknad.v1.OmYtelse;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

public class SøknadMapperES {

    private SøknadMapperES() {
    }

    public static Soeknad mapTilEngangsstønad(ManuellRegistreringEngangsstonadDto registreringDto, NavBruker navBruker, TpsTjeneste tpsTjeneste) {
        Soeknad søknad = mapSøknad(registreringDto, navBruker);
        Engangsstønad engangsstønad = new Engangsstønad();
        SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = mapRelasjonTilBarnet(registreringDto);
        engangsstønad.setSoekersRelasjonTilBarnet(søkersRelasjonTilBarnet);
        engangsstønad.setMedlemskap(mapMedlemskapES(registreringDto));
        engangsstønad.setAnnenForelder(mapAnnenForelder(registreringDto, tpsTjeneste));

        søknad.setOmYtelse(mapOmYtelse(engangsstønad));
        return søknad;
    }

    static OmYtelse mapOmYtelse(Engangsstønad ytelse) {
        OmYtelse omYtelse = new OmYtelse();
        omYtelse.getAny().add(new ObjectFactory().createEngangsstønad(ytelse));
        return omYtelse;
    }


    static no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap mapMedlemskapES(ManuellRegistreringEngangsstonadDto registreringDto) {
        no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap medlemskap = new no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap();
        boolean harFremtidigOppholdUtenlands = registreringDto.getHarFremtidigeOppholdUtenlands();
        boolean harTidligereOppholdUtenlands = registreringDto.getHarTidligereOppholdUtenlands();
        medlemskap.setINorgeVedFoedselstidspunkt(registreringDto.getOppholdINorge());

        List<OppholdNorge> oppholdNorge = opprettOppholdNorge(registreringDto.getMottattDato(), !harFremtidigOppholdUtenlands, !harTidligereOppholdUtenlands);//Ikke utenlandsopphold tolkes som opphold i norge
        medlemskap.getOppholdNorge().addAll(oppholdNorge);

        if (harFremtidigOppholdUtenlands) {
            if (!erTomListe(registreringDto.getFremtidigeOppholdUtenlands())) {
                medlemskap.getOppholdUtlandet().addAll(mapUtenlandsopphold(registreringDto.getFremtidigeOppholdUtenlands()));
            }
        }
        if (harTidligereOppholdUtenlands) {
            if (!erTomListe(registreringDto.getTidligereOppholdUtenlands())) {
                medlemskap.getOppholdUtlandet().addAll(mapUtenlandsopphold(registreringDto.getTidligereOppholdUtenlands()));
            }
        }
        return medlemskap;
    }
}
