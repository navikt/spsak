package no.nav.foreldrepenger.behandlingsprosess.komponenttest.beregningsgrunnlag;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntekt;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Aktoer;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.ArbeidsInntektIdent;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.ArbeidsInntektInformasjon;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.ArbeidsInntektMaaned;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Loennsinntekt;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Organisasjon;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.PersonIdent;
import no.nav.tjeneste.virksomhet.inntekt.v3.meldinger.HentInntektListeBolkRequest;
import no.nav.tjeneste.virksomhet.inntekt.v3.meldinger.HentInntektListeBolkResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.inntekt.InntektConsumer;

class BeregningInntektsConsumerMockProducer {
    static InntektConsumer lagConsumerMock(Behandling behandling, InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste, TpsTjeneste tpsTjeneste) {
        class BeregningInntektConsumerMock implements InntektConsumer {
            @Override
            public HentInntektListeBolkResponse hentInntektListeBolk(HentInntektListeBolkRequest request) {
                Optional<InntektArbeidYtelseGrunnlag> aggregat = inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling);
                List<ArbeidsInntektMaaned> arbeidsInntektMaanedList = new ArrayList<>();
                aggregat.ifPresent(grunnlag ->
                    grunnlag.getAktørInntektForFørStp().forEach(aktørInntekt -> {
                        List<Inntekt> inntektBeregning = aktørInntekt.getInntektBeregningsgrunnlag();
                        inntektBeregning.forEach(inntekt -> {
                            inntekt.getInntektspost().forEach(post -> {
                                ArbeidsInntektInformasjon informasjon = new ArbeidsInntektInformasjon();
                                Loennsinntekt lønn = new Loennsinntekt();
                                lønn.setBeloep(post.getBeløp().getVerdi());
                                YearMonth måned = YearMonth.of(post.getFraOgMed().getYear(), post.getFraOgMed().getMonth());
                                try {
                                    lønn.setUtbetaltIPeriode(DateUtil.convertToXMLGregorianCalendar(måned.atDay(1)));
                                } catch (Exception e) {
                                    throw new IllegalStateException("Klarte ikke opprette testrespons for inntekt", e);
                                }
                                String orgnr = inntekt.getArbeidsgiver().getErVirksomhet() ? inntekt.getArbeidsgiver().getVirksomhet().getOrgnr() : inntekt.getArbeidsgiver().getAktørId().getId();
                                Organisasjon arbeidsplassen = new Organisasjon();
                                arbeidsplassen.setOrgnummer(orgnr);
                                lønn.setArbeidsforholdREF(orgnr);
                                lønn.setVirksomhet(arbeidsplassen);
                                informasjon.getInntektListe().add(lønn);
                                ArbeidsInntektMaaned inntektMaaned = new ArbeidsInntektMaaned();
                                inntektMaaned.setArbeidsInntektInformasjon(informasjon);
                                arbeidsInntektMaanedList.add(inntektMaaned);
                            });
                        });
                    })
                );
                HentInntektListeBolkResponse response = new HentInntektListeBolkResponse();
                Aktoer aktoer = request.getIdentListe().get(0);
                if (aktoer instanceof PersonIdent == false) {
                    return response;
                }
                PersonIdent personIdent = (PersonIdent) aktoer;
                Optional<AktørId> aktørId = tpsTjeneste.hentAktørForFnr(no.nav.foreldrepenger.domene.typer.PersonIdent.fra(personIdent.getPersonIdent()));
                if (!aktørId.isPresent() || !behandling.getAktørId().getId().equals(aktørId.get().getId())) {
                    return response;
                }
                initResponse(response, aktoer);
                List<ArbeidsInntektMaaned> arbeidsInntektMaanedResponse = response.getArbeidsInntektIdentListe().get(0).getArbeidsInntektMaaned();
                arbeidsInntektMaanedResponse.addAll(arbeidsInntektMaanedList);
                return response;
            }
        }
        return new BeregningInntektConsumerMock();
    }

    private static void initResponse(HentInntektListeBolkResponse response, Aktoer aktoer) {
        ArbeidsInntektIdent arbeidsInntektIdent = new ArbeidsInntektIdent();
        arbeidsInntektIdent.setIdent(aktoer);
        response.getArbeidsInntektIdentListe().add(arbeidsInntektIdent);
    }
}
