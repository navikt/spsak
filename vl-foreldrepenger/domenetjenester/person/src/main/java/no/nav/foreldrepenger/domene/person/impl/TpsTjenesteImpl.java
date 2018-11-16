package no.nav.foreldrepenger.domene.person.impl;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;

import no.nav.foreldrepenger.behandlingslager.aktør.GeografiskTilknytning;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.domene.person.RelasjonKriteria;
import no.nav.foreldrepenger.domene.person.TpsAdapter;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

@ApplicationScoped
public class TpsTjenesteImpl implements TpsTjeneste {

    private TpsAdapter tpsAdapter;

    public TpsTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public TpsTjenesteImpl(TpsAdapter tpsAdapter) {
        this.tpsAdapter = tpsAdapter;
    }

    @Override
    public Optional<Personinfo> hentBrukerForFnr(PersonIdent fnr) {
        if (fnr.erFdatNummer()) {
            return Optional.empty();
        }
        Optional<AktørId> aktørId = tpsAdapter.hentAktørIdForPersonIdent(fnr);
        if (!aktørId.isPresent()) {
            return Optional.empty();
        }
        try {
            Personinfo personinfo = tpsAdapter.hentKjerneinformasjon(fnr, aktørId.get());
            return Optional.ofNullable(personinfo);
        } catch (SOAPFaultException e) {
            if (e.getMessage().contains("status: S100008F")) {
                // Her sorterer vi ut dødfødte barn
                return Optional.empty();
            } else {
                throw e;
            }
        }
    }

    @Override
    public PersonIdent hentFnrForAktør(AktørId aktørId) {
        Optional<PersonIdent> funnetFnr;
        funnetFnr = hentFnr(aktørId);
        if (funnetFnr.isPresent()) {
            return funnetFnr.get();
        }
        throw TpsFeilmeldinger.FACTORY.fantIkkePersonForAktørId().toException();
    }

    @Override
    public Optional<AktørId> hentAktørForFnr(PersonIdent fnr) {
        return tpsAdapter.hentAktørIdForPersonIdent(fnr);
    }

    @Override
    public Optional<PersonIdent> hentFnr(AktørId aktørId) {
        return tpsAdapter.hentIdentForAktørId(aktørId);
    }

    @Override
    public Optional<Personinfo> hentBrukerForAktør(AktørId aktørId) {
        Optional<PersonIdent> funnetFnr;
        funnetFnr = hentFnr(aktørId);

        return funnetFnr.map(fnr -> tpsAdapter.hentKjerneinformasjon(fnr, aktørId));
    }

    @Override
    public Optional<String> hentDiskresjonskodeForAktør(PersonIdent fnr) {
        if (fnr.erFdatNummer()) {
            return Optional.empty();
        }
        return Optional.ofNullable(hentGeografiskTilknytning(fnr).getDiskresjonskode());
    }

    @Override
    public List<Personinfo> hentRelatertePersoner(Personinfo personinfo, RelasjonKriteria relasjonKriteria) {
        List<Optional<Personinfo>> listeMedOptionalRelatertePersoninfo = personinfo.getFamilierelasjoner().stream()
            .filter(relasjonKriteria::erOppfyltAv)
            .filter(relasjon -> relasjon.getPersonIdent() != null)
            .map(relasjon -> this.hentBrukerForFnr(relasjon.getPersonIdent())).collect(toList());
        List<Personinfo> relatertePersoner = new ArrayList<>();
        for (Optional<Personinfo> optPers : listeMedOptionalRelatertePersoninfo) {
            optPers.ifPresent(relatertePersoner::add);
        }
        return relatertePersoner;
    }

    @Override
    public GeografiskTilknytning hentGeografiskTilknytning(PersonIdent fnr) {
        return tpsAdapter.hentGeografiskTilknytning(fnr);
    }

    @Override
    public List<GeografiskTilknytning> hentDiskresjonskoderForFamilierelasjoner(PersonIdent fnr) {
        return tpsAdapter.hentDiskresjonskoderForFamilierelasjoner(fnr);
    }

}
