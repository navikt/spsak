package no.nav.foreldrepenger.domene.personopplysning.impl;

import no.nav.foreldrepenger.domene.typer.AktørId;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.person.TpsAdapter;
import no.nav.foreldrepenger.domene.personopplysning.VergeAksjonpunktDto;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

class VergeOppdatererAksjonspunkt {
    private TpsAdapter tpsAdapter;
    private KodeverkRepository kodeverkRepository;
    private NavBrukerRepository navBrukerRepository;
    private VergeRepository vergeRepository;

    VergeOppdatererAksjonspunkt(BehandlingRepositoryProvider repositoryProvider, TpsAdapter tpsAdapter, NavBrukerRepository navBrukerRepository) {
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.tpsAdapter = tpsAdapter;
        this.navBrukerRepository = navBrukerRepository;
        this.vergeRepository = repositoryProvider.getVergeGrunnlagRepository();
    }

    public void oppdater(Behandling behandling, VergeAksjonpunktDto adapter) {
        PersonIdent fnr = adapter.getFnr();
        Optional<AktørId> optAktorId = tpsAdapter.hentAktørIdForPersonIdent(fnr);

        VergeBuilder vergeBuilder = new VergeBuilder();
        vergeBuilder
            .gyldigPeriode(adapter.getFom(), adapter.getTom())
            .medVergeType(kodeverkRepository.finn(VergeType.class, adapter.getVergeTypeKode()))
            .medBrevMottaker(adapter.getBrevMottaker())
            .medVedtaksdato(adapter.getVedtaksDato())
            .medMandatTekst(adapter.getMandatTekst())
            .medStønadMottaker(adapter.getErSøkerErUnderTvungenForvaltning());
        // Sjekk verge i TPS
        if (optAktorId.isPresent()) {
            vergeBuilder.medBruker(hentEllerOpprettBruker(fnr, optAktorId.get()));
        } else {
            throw OppdatererAksjonspunktFeil.FACTORY.vergeIkkeFunnetITPS().toException();
        }

        vergeRepository.lagreOgFlush(behandling, vergeBuilder);
    }

    private NavBruker hentEllerOpprettBruker(PersonIdent fnr, AktørId aktoerId) {
        Optional<NavBruker> optBruker = navBrukerRepository.hent(aktoerId);

        if (optBruker.isPresent()) {
            return optBruker.get();
        } else {
            Personinfo personinfo = tpsAdapter.hentKjerneinformasjon(fnr, aktoerId);
            return NavBruker.opprettNy(personinfo);
        }
    }
}
