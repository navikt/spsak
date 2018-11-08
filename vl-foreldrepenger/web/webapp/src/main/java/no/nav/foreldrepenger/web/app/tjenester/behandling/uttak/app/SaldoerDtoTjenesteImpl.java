package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import static no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType.ORDINÆRT_ARBEID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.domene.uttak.saldo.Aktivitet;
import no.nav.foreldrepenger.domene.uttak.saldo.Saldoer;
import no.nav.foreldrepenger.domene.uttak.saldo.StønadskontoSaldoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AktivitetIdentifikatorDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.stønadskonto.dto.AktivitetSaldoDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.stønadskonto.dto.StønadskontoDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.stønadskonto.dto.SaldoerDto;

@Dependent
public class SaldoerDtoTjenesteImpl implements SaldoerDtoTjeneste {

    private StønadskontoSaldoTjeneste stønadskontoSaldoTjeneste;
    private VirksomhetRepository virksomhetRepository;

    public SaldoerDtoTjenesteImpl() {
        //For CDI
    }

    @Inject
    public SaldoerDtoTjenesteImpl(StønadskontoSaldoTjeneste stønadskontoSaldoTjeneste, BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.stønadskontoSaldoTjeneste =stønadskontoSaldoTjeneste;
        this.virksomhetRepository = behandlingRepositoryProvider.getVirksomhetRepository();
    }

    @Override
    public SaldoerDto lagStønadskontoerDto(Behandling behandling) {
        Saldoer saldoer = stønadskontoSaldoTjeneste.finnSaldoer(behandling);
        Map<String, StønadskontoDto> stønadskontoMap = new HashMap<>();
        for (StønadskontoType stønadskonto : saldoer.stønadskontoer()) {
            List<AktivitetSaldoDto> aktivitetSaldoListe = new ArrayList<>();
            for (Aktivitet aktivitet : saldoer.aktiviteterForSøker()) {
                int saldo = saldoer.saldo(stønadskonto, aktivitet);
                aktivitetSaldoListe.add(new AktivitetSaldoDto(mapToDto(aktivitet), saldo));
            }
            stønadskontoMap.put(stønadskonto.getKode(), new StønadskontoDto(stønadskonto.getKode(),saldoer.getMaxDager(stønadskonto), saldoer.saldo(stønadskonto), aktivitetSaldoListe));
        }
        return new SaldoerDto(saldoer.getMaksDatoUttak(), stønadskontoMap);
    }

    private AktivitetIdentifikatorDto mapToDto(Aktivitet aktivitet) {
        return new AktivitetIdentifikatorDto(
            aktivitet.getUttakArbeidType(),
            aktivitet.getArbeidsforholdOrgnr(),
            aktivitet.getArbeidsforholdId(),
            hentVirksomhetNavn(aktivitet.getUttakArbeidType(), aktivitet.getArbeidsforholdOrgnr()).orElse(null));
    }

    private Optional<String> hentVirksomhetNavn(UttakArbeidType uttakArbeidType, String arbeidsforholdOrgNr) {
        if (uttakArbeidType.equals(ORDINÆRT_ARBEID)) {
            Optional<Virksomhet> virksomhet = virksomhetRepository.hent(arbeidsforholdOrgNr);
            return virksomhet.map(Virksomhet::getNavn);
        }
        return Optional.empty();
    }


}
