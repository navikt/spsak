package no.nav.foreldrepenger.behandling.revurdering.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.aktør.FødtBarnInfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;

/**
 * Lag historikk innslag ved revurdering.
 */
public class RevurderingHistorikk {
    private HistorikkRepository historikkRepository;

    public RevurderingHistorikk(HistorikkRepository historikkRepository) {
        this.historikkRepository = historikkRepository;
    }

    public  void opprettHistorikkinnslagOmRevurdering(Behandling behandling,BehandlingÅrsakType revurderingÅrsak, boolean manueltOpprettet) {
        HistorikkAktør historikkAktør = manueltOpprettet ? HistorikkAktør.SAKSBEHANDLER : HistorikkAktør.VEDTAKSLØSNINGEN;

        Historikkinnslag revurderingsInnslag = new Historikkinnslag();
        revurderingsInnslag.setBehandling(behandling);
        revurderingsInnslag.setType(HistorikkinnslagType.REVURD_OPPR);
        revurderingsInnslag.setAktør(historikkAktør);
        HistorikkInnslagTekstBuilder historiebygger = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.REVURD_OPPR)
            .medBegrunnelse(revurderingÅrsak);
        historiebygger.build(revurderingsInnslag);

        historikkRepository.lagre(revurderingsInnslag);
    }

    public void opprettHistorikkinnslagForFødsler(Behandling behandling, List<FødtBarnInfo> barnFødtIPeriode) {
        Historikkinnslag fødselInnslag = new Historikkinnslag();
        fødselInnslag.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);
        fødselInnslag.setType(HistorikkinnslagType.NY_INFO_FRA_TPS);
        fødselInnslag.setBehandling(behandling);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String fødselsdatoVerdi;
        if (barnFødtIPeriode.size() > 1) {
            SortedSet<LocalDate> fødselsdatoer = new TreeSet<>(
                barnFødtIPeriode.stream().map(FødtBarnInfo::getFødselsdato).collect(Collectors.toSet()));
            fødselsdatoVerdi = fødselsdatoer.stream().map(dateFormat::format).collect(Collectors.joining(", "));
        } else {
            fødselsdatoVerdi = dateFormat.format(barnFødtIPeriode.get(0).getFødselsdato());
        }
        HistorikkInnslagTekstBuilder historieBuilder = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.NY_INFO_FRA_TPS)
            .medOpplysning(HistorikkOpplysningType.FODSELSDATO, fødselsdatoVerdi)
            .medOpplysning(HistorikkOpplysningType.TPS_ANTALL_BARN, barnFødtIPeriode.size());
        historieBuilder.build(fødselInnslag);
        historikkRepository.lagre(fødselInnslag);

    }
}
