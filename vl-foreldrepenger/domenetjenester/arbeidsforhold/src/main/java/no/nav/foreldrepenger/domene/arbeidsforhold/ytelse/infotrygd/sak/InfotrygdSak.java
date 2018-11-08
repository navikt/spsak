package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak;

import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTema.ENSLIG_FORSORGER_TEMA;
import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTema.FORELDREPENGER_TEMA;
import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTema.PÅRØRENDE_SYKDOM_TEMA;
import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTema.SYKEPENGER_TEMA;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.FagsystemUnderkategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseSakstype;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTema;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class InfotrygdSak {
    private FagsystemUnderkategori fagsystemUnderkategori;
    private Saksnummer saksnummer;
    private RelatertYtelseTema tema;
    private TemaUnderkategori temaUnderkategori;
    private RelatertYtelseStatus status;
    private RelatertYtelseSakstype type;
    private RelatertYtelseResultat resultat;
    private String saksbehandlerId;
    private LocalDate registrert;
    private LocalDate vedtatt;
    private LocalDate iverksatt;
    private LocalDate opphoerFomDato;
    private RelatertYtelseType relatertYtelseType;
    private RelatertYtelseTilstand relatertYtelseTilstand;
    private DatoIntervallEntitet periode;
    private static final EnumSet<DayOfWeek> HELG = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    public InfotrygdSak(no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdSak sak, KodeverkRepository kodeverkRepository) {
        fagsystemUnderkategori = FagsystemUnderkategori.INFOTRYGD_SAK;
        konverterInfotrygdSak(sak, kodeverkRepository);
    }

    public InfotrygdSak(no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdVedtak vedtak, KodeverkRepository kodeverkRepository) {
        opphoerFomDato = DateUtil.convertToLocalDate(vedtak.getOpphoerFom());
        fagsystemUnderkategori = FagsystemUnderkategori.INFOTRYGD_VEDTAK;
        konverterInfotrygdSak(vedtak, kodeverkRepository);
        if (RelatertYtelseType.ENGANGSSTØNAD.equals(relatertYtelseType)) {
            periode = DatoIntervallEntitet.fraOgMedTilOgMed(periode.getFomDato(), periode.getFomDato());
        }
    }

    private void konverterInfotrygdSak(no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdSak sak, KodeverkRepository kodeverkRepository) {
        saksnummer = Saksnummer.infotrygd(sak.getSakId());

        registrert = DateUtil.convertToLocalDate(sak.getRegistrert());
        vedtatt = DateUtil.convertToLocalDate(sak.getVedtatt());
        iverksatt = DateUtil.convertToLocalDate(sak.getIverksatt());
        periode = utledPeriode(iverksatt, opphoerFomDato,vedtatt, registrert);
        saksbehandlerId = sak.getSaksbehandlerId();
        if (sak.getTema() != null) {
            tema = kodeverkRepository.finn(RelatertYtelseTema.class, sak.getTema().getValue());
        }
        if (sak.getBehandlingstema() != null && sak.getBehandlingstema().getValue() != null) {
            temaUnderkategori = kodeverkRepository.finnForKodeverkEiersKode(TemaUnderkategori.class, sak.getBehandlingstema().getValue(), TemaUnderkategori.UDEFINERT);
        } else {
            temaUnderkategori = TemaUnderkategori.UDEFINERT;
        }
        if (sak.getType() != null && sak.getType().getValue() != null) {
            type = kodeverkRepository.finnForKodeverkEiersKode(RelatertYtelseSakstype.class, sak.getType().getValue(), RelatertYtelseSakstype.UDEFINERT);
        } else {
            type = RelatertYtelseSakstype.UDEFINERT;
        }
        if (sak.getStatus() != null && sak.getStatus().getValue() != null) {
            status = kodeverkRepository.finnForKodeverkEiersKode(RelatertYtelseStatus.class, sak.getStatus().getValue(), RelatertYtelseStatus.AVSLUTTET_IT);
        }
        if (sak.getResultat() != null && sak.getResultat().getValue() != null) {
            resultat = kodeverkRepository.finnForKodeverkEiersKode(RelatertYtelseResultat.class, sak.getResultat().getValue(), RelatertYtelseResultat.UDEFINERT);
        } else {
            resultat = RelatertYtelseResultat.UDEFINERT;
        }
        relatertYtelseType = utledRelatertYtelseType(tema, getTemaUnderkategoriString());
        relatertYtelseTilstand = getYteleseTilstand();
    }

    private DatoIntervallEntitet utledPeriode(LocalDate iverksatt, LocalDate opphoerFomDato, LocalDate vedtatt,  LocalDate registrert) {
        if (opphoerFomDato != null) {
            LocalDate tomFraOpphørFom = localDateMinus1Virkedag(opphoerFomDato);
            if (tomFraOpphørFom.isAfter(iverksatt)) {
                return DatoIntervallEntitet.fraOgMedTilOgMed(iverksatt, tomFraOpphørFom);
            } else {
                return DatoIntervallEntitet.fraOgMedTilOgMed(iverksatt, iverksatt);
            }
        } else {
            if (iverksatt != null) {
                return DatoIntervallEntitet.fraOgMed(iverksatt);
            } else if (vedtatt != null){
                return DatoIntervallEntitet.fraOgMed(vedtatt);
            }
            return DatoIntervallEntitet.fraOgMed(registrert);
        }
    }

    private LocalDate localDateMinus1Virkedag(LocalDate opphoerFomDato) {
        int antallDager = beregnAntallDagerTilForrigeVirkedag(opphoerFomDato);
        return opphoerFomDato.minusDays(antallDager);
    }

    private int beregnAntallDagerTilForrigeVirkedag(LocalDate opphoerDato) {
        int antallDagerTilbake = 1;
        LocalDate dato = opphoerDato.minusDays(1);
        while (HELG.contains(dato.getDayOfWeek())) {
            if (HELG.contains(dato.getDayOfWeek())) {
                antallDagerTilbake++;
                dato = dato.minusDays(1);
            }
        }
        return antallDagerTilbake;
    }

    public FagsystemUnderkategori getFagsystemUnderkategori() {
        return fagsystemUnderkategori;
    }

    public Saksnummer getSakId() {
        return saksnummer;
    }

    public RelatertYtelseTema getTema() {
        return tema;
    }

    public TemaUnderkategori getTemaUnderkategori() {
        return temaUnderkategori;
    }

    public RelatertYtelseStatus getStatus() {
        return status;
    }

    public LocalDate getRegistrert() {
        return registrert;
    }

    public LocalDate getVedtatt() {
        return vedtatt;
    }

    public LocalDate getIverksatt() {
        return iverksatt;
    }

    public LocalDate getOpphoerFomDato() {
        return opphoerFomDato;
    }

    public RelatertYtelseSakstype getSaksType() {
        return type;
    }

    public RelatertYtelseTilstand getRelatertYtelseTilstand() { return relatertYtelseTilstand; }

    public RelatertYtelseResultat getResultat() {
        return resultat;
    }

    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    public RelatertYtelseType getRelatertYtelseType() {
        return relatertYtelseType;
    }

    private String getTemaUnderkategoriString() {
        if (temaUnderkategori == null) {
            return null;
        } else {
            return temaUnderkategori.getKode();
        }
    }

    private String getStatusString() {
        if (status == null) {
            return null;
        } else {
            return status.getKode();
        }

    }

    public RelatertYtelseType hentRelatertYtelseTypeForSammenstillingMedBeregningsgrunnlag(){
        if (relatertYtelseType != null && (relatertYtelseType.equals(RelatertYtelseType.SVANGERSKAPSPENGER))) {
            return RelatertYtelseType.FORELDREPENGER;
        }
        return relatertYtelseType;
    }

    private RelatertYtelseType utledRelatertYtelseType(RelatertYtelseTema ytelseTema, String behandlingsTema) {
        if (ENSLIG_FORSORGER_TEMA.equals(ytelseTema)) {
            return RelatertYtelseType.ENSLIG_FORSØRGER;
        } else if (FORELDREPENGER_TEMA.equals(ytelseTema)) {
            if (TemaUnderkategori.erGjelderSvangerskapspenger(behandlingsTema)) {
                return RelatertYtelseType.SVANGERSKAPSPENGER;
            } else if (TemaUnderkategori.erGjelderForeldrepenger(behandlingsTema)) {
                return RelatertYtelseType.FORELDREPENGER;
            } else if (TemaUnderkategori.erGjelderEngangsstonad(behandlingsTema)) {
                return RelatertYtelseType.ENGANGSSTØNAD;
            }
        } else if (SYKEPENGER_TEMA.equals(ytelseTema)) {
            return RelatertYtelseType.SYKEPENGER;
        } else if (PÅRØRENDE_SYKDOM_TEMA.equals(ytelseTema)) {
            return RelatertYtelseType.PÅRØRENDESYKDOM;
        }
        return RelatertYtelseType.UDEFINERT;
    }

    public boolean erAvRelatertYtelseType(RelatertYtelseType... ytelseTyper) {
        if (relatertYtelseType == null) return false;
        for (RelatertYtelseType relatertYtelse : ytelseTyper) {
            if (relatertYtelse.equals(relatertYtelseType)) return true;
        }
        return false;
    }

    public boolean erLøpendeVedtak() {
        return FagsystemUnderkategori.INFOTRYGD_VEDTAK.equals(fagsystemUnderkategori) && RelatertYtelseStatus.erLøpendeVedtak(getStatusString());
    }

    public boolean erAvsluttetVedtak() {
        return FagsystemUnderkategori.INFOTRYGD_VEDTAK.equals(fagsystemUnderkategori) && RelatertYtelseTilstand.AVSLUTTET.equals(relatertYtelseTilstand);
    }

    public boolean erVedtak(){
        return FagsystemUnderkategori.INFOTRYGD_VEDTAK.equals(fagsystemUnderkategori);
    }

    public boolean erÅpenSak() {
        return FagsystemUnderkategori.INFOTRYGD_SAK.equals(fagsystemUnderkategori) && RelatertYtelseStatus.erÅpenSakStatus(getStatusString());
    }

    private boolean erIkkeStartet() {
        return RelatertYtelseStatus.erIkkeStartetStatus(getStatusString());
    }

    public String getSaksbehandlerId() {
        return saksbehandlerId;
    }


    private RelatertYtelseTilstand getYteleseTilstand() {
        if (erLøpendeVedtak()) {
            return RelatertYtelseTilstand.LØPENDE;
        } else if (erÅpenSak()) {
            return RelatertYtelseTilstand.ÅPEN;
        } else if (erIkkeStartet()) {
            return RelatertYtelseTilstand.IKKE_STARTET;
        }
        return RelatertYtelseTilstand.AVSLUTTET;
    }

}
