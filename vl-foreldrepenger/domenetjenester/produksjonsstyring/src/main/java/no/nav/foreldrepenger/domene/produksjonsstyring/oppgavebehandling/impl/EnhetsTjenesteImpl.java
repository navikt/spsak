package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.GeografiskTilknytning;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Diskresjonskode;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.arbeidsfordeling.ArbeidsfordelingTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.EnhetsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class EnhetsTjenesteImpl implements EnhetsTjeneste {

    private TpsTjeneste tpsTjeneste;
    private ArbeidsfordelingTjeneste arbeidsfordelingTjeneste;

    private LocalDate sisteInnhenting = LocalDate.MIN;
    // Produksjonsstyring skjer på nivå TEMA - behandlingTema ikke hensyntatt in NORG2
    private OrganisasjonsEnhet enhetKode6;
    private final OrganisasjonsEnhet enhetKlage = new OrganisasjonsEnhet("4205", "NAV Klageinstans Midt-Norge", "AKTIV");
    private List<OrganisasjonsEnhet> alleBehandlendeEnheter;

    public EnhetsTjenesteImpl() {
        // For CDI proxy
    }

    @Inject
    public EnhetsTjenesteImpl(TpsTjeneste tpsTjeneste, ArbeidsfordelingTjeneste arbeidsfordelingTjeneste) {
        this.tpsTjeneste = tpsTjeneste;
        this.arbeidsfordelingTjeneste = arbeidsfordelingTjeneste;
    }


    @Override
    public List<OrganisasjonsEnhet> hentEnhetListe() {
        oppdaterEnhetCache();
        return alleBehandlendeEnheter;
    }
    
    @Override
    public List<OrganisasjonsEnhet> hentEnhetListe(BehandlingTema behandlingTema) {
        oppdaterEnhetCache();
        return alleBehandlendeEnheter;
    }

    @Override
    public OrganisasjonsEnhet hentEnhetSjekkRegistrerteRelasjoner(AktørId aktørId, BehandlingTema behandlingTema) {
        oppdaterEnhetCache();
        PersonIdent fnr = tpsTjeneste.hentFnrForAktør(aktørId);

        GeografiskTilknytning geografiskTilknytning = tpsTjeneste.hentGeografiskTilknytning(fnr);
        String aktivDiskresjonskode = geografiskTilknytning.getDiskresjonskode();

        return arbeidsfordelingTjeneste.finnBehandlendeEnhet(geografiskTilknytning.getTilknytning(), aktivDiskresjonskode, behandlingTema);
    }

    @Override
    public Optional<OrganisasjonsEnhet> oppdaterEnhetSjekkOppgitte(String enhetId, BehandlingTema behandlingTema, List<AktørId> relaterteAktører) {
        oppdaterEnhetCache();
        if (enhetKode6.getEnhetId().equals(enhetId) || enhetKlage.getEnhetId().equals(enhetId)) {
            return Optional.empty();
        }

        return sjekkSpesifiserteRelaterte(relaterteAktører);
    }

    @Override
    public Optional<OrganisasjonsEnhet> oppdaterEnhetSjekkRegistrerteRelasjoner(String enhetId, BehandlingTema behandlingTema, AktørId aktørId, Optional<AktørId> kobletAktørId, List<AktørId> relaterteAktører) {
        oppdaterEnhetCache();
        if (enhetKode6.getEnhetId().equals(enhetId) || enhetKlage.getEnhetId().equals(enhetId)) {
            return Optional.empty();
        }

        OrganisasjonsEnhet enhet = hentEnhetSjekkRegistrerteRelasjoner(aktørId, behandlingTema);
        if (enhetKode6.getEnhetId().equals(enhet.getEnhetId())) {
            return Optional.of(enhetKode6);
        }
        if (kobletAktørId.isPresent()) {
            OrganisasjonsEnhet enhetKoblet = hentEnhetSjekkRegistrerteRelasjoner(kobletAktørId.get(), behandlingTema);
            if (enhetKode6.getEnhetId().equals(enhetKoblet.getEnhetId())) {
                return Optional.of(enhetKode6);
            }
        }
        return sjekkSpesifiserteRelaterte(relaterteAktører);
    }

    private Optional<OrganisasjonsEnhet> sjekkSpesifiserteRelaterte(List<AktørId> relaterteAktører) {
        for (AktørId relatert : relaterteAktører) {
            PersonIdent personIdent = tpsTjeneste.hentFnrForAktør(relatert);
            GeografiskTilknytning geo = tpsTjeneste.hentGeografiskTilknytning(personIdent);
            if (Diskresjonskode.KODE6.getKode().equals(geo.getDiskresjonskode())) {
                return Optional.of(enhetKode6);
            }
        }
        return Optional.empty();
    }

    private void oppdaterEnhetCache() {
        if (sisteInnhenting.isBefore(LocalDate.now(FPDateUtil.getOffset()))) {
            BehandlingTema behandlingTema = BehandlingTema.UDEFINERT;
            
            enhetKode6 = arbeidsfordelingTjeneste.hentEnhetForDiskresjonskode(Diskresjonskode.KODE6.getKode(), behandlingTema);
            alleBehandlendeEnheter = arbeidsfordelingTjeneste.finnAlleBehandlendeEnhetListe(behandlingTema);
            sisteInnhenting = LocalDate.now(FPDateUtil.getOffset());
        }
    }

    @Override
    public OrganisasjonsEnhet enhetsPresedens(OrganisasjonsEnhet enhetSak1, OrganisasjonsEnhet enhetSak2, boolean arverKlage) {
        oppdaterEnhetCache();
        if (arverKlage && enhetKlage.getEnhetId().equals(enhetSak1.getEnhetId())) {
            return enhetSak1;
        }
        if (enhetKode6.getEnhetId().equals(enhetSak1.getEnhetId()) || enhetKode6.getEnhetId().equals(enhetSak2.getEnhetId())) {
            return enhetKode6;
        }
        return enhetSak1;
    }

}
