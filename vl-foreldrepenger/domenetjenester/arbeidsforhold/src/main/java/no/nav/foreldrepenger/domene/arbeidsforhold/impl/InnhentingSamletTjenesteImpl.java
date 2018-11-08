package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import static no.nav.vedtak.feil.LogLevel.WARN;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.domene.arbeidsforhold.InnhentingSamletTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Arbeidsforhold;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.FinnInntektRequest;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.InntektTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.arena.meldekortutbetalingsgrunnlag.MeldekortTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.arena.meldekortutbetalingsgrunnlag.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag.InfotrygdBeregningsgrunnlagTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag.YtelseBeregningsgrunnlagGrunnlag;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag.YtelsesBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.InfotrygdSak;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.InfotrygdSakOgGrunnlag;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.InfotrygdTjeneste;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class InnhentingSamletTjenesteImpl implements InnhentingSamletTjeneste {

    private static final Logger LOGGER = LoggerFactory.getLogger(InnhentingSamletTjenesteImpl.class);
    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    private TpsTjeneste tpsTjeneste;
    private InntektTjeneste inntektTjeneste;
    private InfotrygdTjeneste infotrygdTjeneste;
    private InfotrygdBeregningsgrunnlagTjeneste infotrygdBeregningsgrunnlagTjeneste;
    private MeldekortTjeneste meldekortTjeneste;

    InnhentingSamletTjenesteImpl() {
        //CDI
    }

    @Inject
    public InnhentingSamletTjenesteImpl(ArbeidsforholdTjeneste arbeidsforholdTjeneste,
                                        TpsTjeneste tpsTjeneste, InntektTjeneste inntektTjeneste, InfotrygdTjeneste infotrygdTjeneste,
                                        InfotrygdBeregningsgrunnlagTjeneste infotrygdBeregningsgrunnlagTjeneste,
                                        MeldekortTjeneste meldekortTjeneste) {
        this.arbeidsforholdTjeneste = arbeidsforholdTjeneste;
        this.tpsTjeneste = tpsTjeneste;
        this.inntektTjeneste = inntektTjeneste;
        this.infotrygdBeregningsgrunnlagTjeneste = infotrygdBeregningsgrunnlagTjeneste;
        this.infotrygdTjeneste = infotrygdTjeneste;
        this.meldekortTjeneste = meldekortTjeneste;
    }

    @Override
    public InntektsInformasjon getInntektsInformasjon(AktørId aktørId, Behandling behandling, Interval periode, InntektsKilde kilde) {
        PersonIdent personIdent = getFnrFraAktørId(aktørId);
        FinnInntektRequest request = new FinnInntektRequest(personIdent.getIdent(),
            YearMonth.from(LocalDateTime.ofInstant(periode.getStart(), ZoneId.systemDefault())),
            YearMonth.from(LocalDateTime.ofInstant(periode.getEnd(), ZoneId.systemDefault())),
            behandling.getId(), behandling.getFagsakId());
        return inntektTjeneste.finnInntekt(request, kilde);
    }


    @Override
    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> getArbeidsforhold(AktørId aktørId, Interval opplysningsPeriode) {
        return arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(getFnrFraAktørId(aktørId), opplysningsPeriode);
    }


    private PersonIdent getFnrFraAktørId(AktørId aktørId) {
        return tpsTjeneste.hentFnrForAktør(aktørId);
    }

    @Override
    public List<InfotrygdSakOgGrunnlag> getSammenstiltSakOgGrunnlag(Behandling behandling, AktørId aktørId, Interval opplysningsPeriode, boolean medGrunnlag) {
        final List<InfotrygdSak> infotrygdSakList = filtrerSaker(getInfotrygdSaker(behandling, aktørId, opplysningsPeriode), medGrunnlag);
        LOGGER.info("InfotrygdSak sammentilling antall saker/vedtak: {}", infotrygdSakList.size());
        if (medGrunnlag) {
            final YtelsesBeregningsgrunnlag ytelsesBeregningsgrunnlag = getInfotrygdBeregningsgrunnlag(behandling, aktørId, opplysningsPeriode);
            List<YtelseBeregningsgrunnlagGrunnlag> alleGrunnlag;
            if (ytelsesBeregningsgrunnlag != null) {
                alleGrunnlag = ytelsesBeregningsgrunnlag.getAlleGrunnlag();
            } else {
                alleGrunnlag = new ArrayList<>();
            }
            LOGGER.info("InfotrygdBeregningsgrunnlag antall grunnlag: {}", alleGrunnlag.size());
            return sammenstillSakOgGrunnlag(infotrygdSakList, alleGrunnlag, opplysningsPeriode);
        }

        return infotrygdSakList.stream().map(InfotrygdSakOgGrunnlag::new).collect(Collectors.toList());
    }

    private List<InfotrygdSak> filtrerSaker(List<InfotrygdSak> infotrygdSaker, boolean medGrunnlag) {
        if (medGrunnlag) {
            return infotrygdSaker.stream()
                .filter(this::skalLagresIfbmGrunnlag).collect(Collectors.toList());
        } else {
            return infotrygdSaker.stream()
                .filter(this::skalLagres).collect(Collectors.toList());
        }
    }

    private boolean skalLagres(InfotrygdSak infotrygdSak) {
        return skalLagresBetinget(infotrygdSak, false);
    }

    private boolean skalLagresIfbmGrunnlag(InfotrygdSak infotrygdSak) {
        return skalLagresBetinget(infotrygdSak, true);
    }

    private boolean skalLagresBetinget(InfotrygdSak infotrygdSak, boolean medGrunnlag) {
        if (RelatertYtelseType.ENSLIG_FORSØRGER.equals(infotrygdSak.getRelatertYtelseType())) {
            return false;
        }
        if (infotrygdSak.erLøpendeVedtak() || infotrygdSak.erAvsluttetVedtak()) {
            if (infotrygdSak.erAvRelatertYtelseType(RelatertYtelseType.ENGANGSSTØNAD, RelatertYtelseType.SYKEPENGER,
                RelatertYtelseType.SVANGERSKAPSPENGER, RelatertYtelseType.FORELDREPENGER, RelatertYtelseType.PÅRØRENDESYKDOM)) {
                return true;
            }
        } else if (infotrygdSak.erVedtak()) {
            if (infotrygdSak.erAvRelatertYtelseType(RelatertYtelseType.ENGANGSSTØNAD, RelatertYtelseType.FORELDREPENGER)) {
                return true;
            }
            if (medGrunnlag && infotrygdSak.erAvRelatertYtelseType(RelatertYtelseType.SYKEPENGER,
                RelatertYtelseType.PÅRØRENDESYKDOM, RelatertYtelseType.SVANGERSKAPSPENGER)) {
                return true;
            }
        }
        return false;
    }

    private List<InfotrygdSak> getInfotrygdSaker(Behandling behandling, AktørId aktørId, Interval opplysningsPeriode) {
        return infotrygdTjeneste.finnSakListe(behandling, getFnrFraAktørId(aktørId).getIdent(), LocalDateTime.ofInstant(opplysningsPeriode.getStart(), ZoneId.systemDefault()).toLocalDate());
    }

    private YtelsesBeregningsgrunnlag getInfotrygdBeregningsgrunnlag(Behandling behandling, AktørId aktørId, Interval opplysningsPeriode) {
        return infotrygdBeregningsgrunnlagTjeneste.hentGrunnlagListeFull(behandling, getFnrFraAktørId(aktørId).getIdent(),
            LocalDateTime.ofInstant(opplysningsPeriode.getStart(), ZoneId.systemDefault()).toLocalDate());
    }

    private boolean matcherSakOgGrunnlag(InfotrygdSakOgGrunnlag sak, YtelseBeregningsgrunnlagGrunnlag grunnlag) {
        if (sak.getGrunnlag().isPresent() || sak.getSak().getIverksatt() == null) {
            return false;
        }
        // Samme type (ITrygd tema+behandlingstema) og grunnlag/identdato = sak/iverksattdato
        return grunnlag.getType().equals(sak.getSak().hentRelatertYtelseTypeForSammenstillingMedBeregningsgrunnlag()) && grunnlag.getIdentdato().equals(sak.getSak().getIverksatt());
    }

    private List<InfotrygdSakOgGrunnlag> sammenstillSakOgGrunnlag(List<InfotrygdSak> saker, List<YtelseBeregningsgrunnlagGrunnlag> grunnlagene, Interval opplysningsPeriode) {
        List<InfotrygdSakOgGrunnlag> sammenstilling = saker.stream().map(InfotrygdSakOgGrunnlag::new).collect(Collectors.toList());
        LocalDate periodeFom = LocalDateTime.ofInstant(opplysningsPeriode.getStart(), ZoneId.systemDefault()).toLocalDate();

        for (YtelseBeregningsgrunnlagGrunnlag grunnlag : grunnlagene) {
            Optional<InfotrygdSakOgGrunnlag> funnet = settSammenSakMatchendeGrunnlag(sammenstilling, grunnlag);
            if (!funnet.isPresent() && grunnlag.getTom() != null && !grunnlag.getTom().isBefore(periodeFom)) {
                InnhentingSamletTjenesteImpl.Feilene.FACTORY.manglerInfotrygdSak(grunnlag.getType().toString(), grunnlag.getIdentdato().toString()).log(LOGGER);
            }
        }

        return sammenstilling.stream()
            .filter(isog -> !(erAvsluttet(isog) && isog.getPeriode().getTomDato().isBefore(periodeFom)))
            .filter(isog -> skalTypeLagresUansett(isog) || erEtterIverksatt(isog))
            .collect(Collectors.toList());
    }

    private Optional<InfotrygdSakOgGrunnlag> settSammenSakMatchendeGrunnlag(List<InfotrygdSakOgGrunnlag> sakene, YtelseBeregningsgrunnlagGrunnlag grunnlag) {
        Optional<InfotrygdSakOgGrunnlag> funnet = Optional.empty();
        for (InfotrygdSakOgGrunnlag sak : sakene) {
            if (!funnet.isPresent() && matcherSakOgGrunnlag(sak, grunnlag)) {
                sak.setGrunnlag(grunnlag);
                funnet = Optional.of(sak);
                if (grunnlag.getFom() != null && grunnlag.getFom().isBefore(sak.getPeriode().getFomDato())) {
                    LOGGER.warn("Grunnlag med fom tidligere enn identdato: {}", grunnlag.getFom());
                }
                if (erAvsluttet(sak) && grunnlag.getTom() != null && sak.getPeriode().getTomDato().isAfter(grunnlag.getTom())) {
                    sak.setPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(sak.getPeriode().getFomDato(), grunnlag.getTom()));
                }
            }
        }
        return funnet;
    }

    private boolean erAvsluttet(InfotrygdSakOgGrunnlag infotrygdSakOgGrunnlag) {
        return RelatertYtelseTilstand.AVSLUTTET.equals(infotrygdSakOgGrunnlag.getSak().getRelatertYtelseTilstand());
    }

    private boolean erEtterIverksatt(InfotrygdSakOgGrunnlag infotrygdSakOgGrunnlag) {
        RelatertYtelseTilstand tilstand = infotrygdSakOgGrunnlag.getSak().getRelatertYtelseTilstand();
        return RelatertYtelseTilstand.LØPENDE.equals(tilstand) || RelatertYtelseTilstand.AVSLUTTET.equals(tilstand);
    }

    private boolean skalTypeLagresUansett(InfotrygdSakOgGrunnlag infotrygdSakOgGrunnlag) {
        RelatertYtelseType type = infotrygdSakOgGrunnlag.getSak().getRelatertYtelseType();
        return RelatertYtelseType.ENGANGSSTØNAD.equals(type) || RelatertYtelseType.FORELDREPENGER.equals(type);
    }

    @Override
    public List<MeldekortUtbetalingsgrunnlagSak> hentYtelserTjenester(Behandling behandling, AktørId aktørId, Interval opplysningsPeriode) {
        List<MeldekortUtbetalingsgrunnlagSak> saker = meldekortTjeneste.hentMeldekortListe(aktørId, LocalDateTime.ofInstant(opplysningsPeriode.getStart(), ZoneId.systemDefault()).toLocalDate(),
            LocalDateTime.ofInstant(opplysningsPeriode.getEnd(), ZoneId.systemDefault()).toLocalDate());
        return filtrerYtelserTjenester(saker);
    }

    private List<MeldekortUtbetalingsgrunnlagSak> filtrerYtelserTjenester(List<MeldekortUtbetalingsgrunnlagSak> saker) {
        List<MeldekortUtbetalingsgrunnlagSak> filtrert = new ArrayList<>();
        for (MeldekortUtbetalingsgrunnlagSak sak : saker) {
            if (sak.getKravMottattDato() == null) {
                if (sak.getVedtakStatus() == null) {
                    InnhentingFeil.FACTORY.ignorerArenaSakInfoLogg("vedtak", sak.getSaksnummer()).log(LOGGER);
                } else {
                    InnhentingFeil.FACTORY.ignorerArenaSak("kravMottattDato", sak.getSaksnummer()).log(LOGGER);
                }
            } else if (RelatertYtelseTilstand.ÅPEN.equals(sak.getYtelseTilstand()) && sak.getMeldekortene().isEmpty()) {
                InnhentingFeil.FACTORY.ignorerArenaSakInfoLogg("meldekort", sak.getSaksnummer()).log(LOGGER);
            } else if (sak.getVedtaksPeriodeFom() == null) {
                InnhentingFeil.FACTORY.ignorerArenaSak("vedtaksDato", sak.getSaksnummer()).log(LOGGER);
            } else if (sak.getVedtaksPeriodeTom() != null && sak.getVedtaksPeriodeTom().isBefore(sak.getVedtaksPeriodeFom())) {
                InnhentingFeil.FACTORY.ignorerArenaSakMedVedtakTomFørVedtakFom(sak.getSaksnummer()).log(LOGGER);
            } else {
                filtrert.add(sak);
            }
        }
        return filtrert;
    }

    interface Feilene extends DeklarerteFeil {
        InnhentingSamletTjenesteImpl.Feilene FACTORY = FeilFactory.create(InnhentingSamletTjenesteImpl.Feilene.class);

        @TekniskFeil(feilkode = "FP-074125", feilmelding = "Mangler Infotrygdsak for Infotrygdgrunnlag av type %s identdato %s", logLevel = WARN)
        Feil manglerInfotrygdSak(String type, String dato);
    }

}
