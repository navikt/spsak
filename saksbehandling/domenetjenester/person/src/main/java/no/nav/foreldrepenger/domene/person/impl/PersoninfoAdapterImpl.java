package no.nav.foreldrepenger.domene.person.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.Personhistorikkinfo;
import no.nav.foreldrepenger.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.domene.person.TpsAdapter;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

@ApplicationScoped
public class PersoninfoAdapterImpl implements PersoninfoAdapter {

    private TpsAdapter tpsAdapter;

    public PersoninfoAdapterImpl() {
        // for CDI proxy
    }

    @Inject
    public PersoninfoAdapterImpl(TpsAdapter tpsAdapter) {
        this.tpsAdapter = tpsAdapter;
    }

    @Override
    public Personinfo innhentSaksopplysningerForSøker(AktørId aktørId) {
        return hentKjerneinformasjon(aktørId);
    }

    @Override
    public Optional<Personinfo> innhentSaksopplysningerForEktefelle(Optional<AktørId> aktørId) {
        return aktørId.map(this::hentKjerneinformasjon);
    }

    @Override
    public Optional<Personinfo> innhentSaksopplysninger(PersonIdent personIdent) {
        Optional<AktørId> aktørId = tpsAdapter.hentAktørIdForPersonIdent(personIdent);

        if(aktørId.isPresent()) {
            return hentKjerneinformasjonForBarn(aktørId.get(), personIdent);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Personhistorikkinfo innhentPersonopplysningerHistorikk(AktørId aktørId, Interval interval) {
        return tpsAdapter.hentPersonhistorikk(aktørId, interval);
    }

    /** Henter PersonInfo for barn, gitt at det ikke er FDAT nummer (sjekkes på format av PersonIdent, evt. ved feilhåndtering fra TPS). Hvis FDAT nummer returneres {@link Optional#empty()} */
    @Override
    public Optional<Personinfo> innhentSaksopplysningerForBarn(PersonIdent personIdent) {
        if(personIdent.erFdatNummer()) {
            return Optional.empty();
        }
        Optional<AktørId> optAktørId = tpsAdapter.hentAktørIdForPersonIdent(personIdent);
        if (optAktørId.isPresent()) {
            return hentKjerneinformasjonForBarn(optAktørId.get(), personIdent);
        }
        return Optional.empty();
    }

    @Override
    public Adresseinfo innhentAdresseopplysningerForDokumentsending(AktørId aktørId) {
        Optional<PersonIdent> optFnr = tpsAdapter.hentIdentForAktørId(aktørId);
        if (optFnr.isPresent()) {
            return tpsAdapter.hentAdresseinformasjon(optFnr.get());
        }
        return null;
    }

    private Optional<Personinfo> hentKjerneinformasjonForBarn(AktørId aktørId, PersonIdent personIdent) {
        if(personIdent.erFdatNummer()) {
            return Optional.empty();
        }
        try {
            return Optional.of(
                hentKjerneinformasjon(aktørId, personIdent)
            );
            // TODO Lag en skikkelig fiks på dette
            //Her sorterer vi ut dødfødte barn
        } catch (SOAPFaultException e) {
            if (e.getCause().getMessage().contains("status: S610006F")) {
                return Optional.empty();
            }
            throw e;
        }
    }

    private Personinfo hentKjerneinformasjon(AktørId aktørId) {
        Optional<PersonIdent> personIdent = tpsAdapter.hentIdentForAktørId(aktørId);
        if (personIdent.isPresent()) {
            return hentKjerneinformasjon(aktørId, personIdent.get());
        }
        //FIXME Humle returner Optional
        return null;
    }

    private Personinfo hentKjerneinformasjon(AktørId aktørId, PersonIdent personIdent) {
        return tpsAdapter.hentKjerneinformasjon(personIdent, aktørId);
    }
}
