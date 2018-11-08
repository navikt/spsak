package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Engangsstoenad;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Foreldrepenger;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.PaaroerendeSykdom;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Sykepenger;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.meldinger.FinnGrunnlagListeResponse;

public class YtelsesBeregningsgrunnlag {

    private List<YtelseBeregningsgrunnlagForeldrepenger> foreldrepenger = Collections.emptyList();
    private List<YtelseBeregningsgrunnlagEngangstoenad> engangstoenads = Collections.emptyList();
    private List<YtelseBeregningsgrunnlagSykepenger> sykepenger = Collections.emptyList();
    private List<YtelseBeregningsgrunnlagPaaroerendeSykdom> pårørendesykdommer = Collections.emptyList();

    YtelsesBeregningsgrunnlag(FinnGrunnlagListeResponse finnGrunnlagListeResponse, KodeverkRepository kodeverkRepository) {
        if(finnGrunnlagListeResponse == null){
            return;
        }
        lagForeldrePengeListe(finnGrunnlagListeResponse.getForeldrepengerListe(),kodeverkRepository);
        lagEngangsStønadsListe(finnGrunnlagListeResponse.getEngangstoenadListe());
        lagSykepengerListe(finnGrunnlagListeResponse.getSykepengerListe(),kodeverkRepository);
        lagPaaroerendeSykdomListe(finnGrunnlagListeResponse.getPaaroerendeSykdomListe(),kodeverkRepository);
    }

    private void lagForeldrePengeListe(List<Foreldrepenger> foreldrepengerListe, KodeverkRepository kodeverkRepository) {
        ArrayList<YtelseBeregningsgrunnlagForeldrepenger> fps = new ArrayList<>();
        for (Foreldrepenger fp : foreldrepengerListe) {
            fps.add(new YtelseBeregningsgrunnlagForeldrepenger(fp,kodeverkRepository));
        }
        foreldrepenger = Collections.unmodifiableList(fps);
    }

    private void lagEngangsStønadsListe(List<Engangsstoenad> engangstoenadListe) {
        ArrayList<YtelseBeregningsgrunnlagEngangstoenad> ybe = new ArrayList<>();
        for (Engangsstoenad engangsstoenad: engangstoenadListe){
            ybe.add(new YtelseBeregningsgrunnlagEngangstoenad(engangsstoenad));
        }
        engangstoenads = Collections.unmodifiableList(ybe);
    }

    private void lagSykepengerListe(List<Sykepenger> sykepengerListe, KodeverkRepository kodeverkRepository) {
        ArrayList<YtelseBeregningsgrunnlagSykepenger> spl = new ArrayList<>();
        for (Sykepenger sykep: sykepengerListe){
            spl.add(new YtelseBeregningsgrunnlagSykepenger(sykep,kodeverkRepository));
        }
        sykepenger = Collections.unmodifiableList(spl);
    }

    private void lagPaaroerendeSykdomListe(List<PaaroerendeSykdom> paaroerendeSykdomsListe, KodeverkRepository kodeverkRepository) {
        ArrayList<YtelseBeregningsgrunnlagPaaroerendeSykdom> ybe = new ArrayList<>();
        for (PaaroerendeSykdom paaroerendeSykdom: paaroerendeSykdomsListe){
            ybe.add(new YtelseBeregningsgrunnlagPaaroerendeSykdom(paaroerendeSykdom,kodeverkRepository));
        }
        pårørendesykdommer = Collections.unmodifiableList(ybe);
    }

    public List<YtelseBeregningsgrunnlagForeldrepenger> getForeldrePenger(){
        return foreldrepenger;
    }

    public List<YtelseBeregningsgrunnlagEngangstoenad> getEngangstoenads() {
        return engangstoenads;
    }

    public List<YtelseBeregningsgrunnlagSykepenger> getSykepenger() {
        return sykepenger;
    }

    public List<YtelseBeregningsgrunnlagPaaroerendeSykdom> getPårørendesykdommer() {
        return pårørendesykdommer;
    }

    public List<YtelseBeregningsgrunnlagGrunnlag> getAlleGrunnlag() {
        List<YtelseBeregningsgrunnlagGrunnlag> alle = new ArrayList<>();
        alle.addAll(foreldrepenger);
        alle.addAll(engangstoenads);
        alle.addAll(sykepenger);
        alle.addAll(pårørendesykdommer);
        return alle;
    }

    public boolean erTomt(){
        return pårørendesykdommer.isEmpty() && sykepenger.isEmpty() && engangstoenads.isEmpty() && foreldrepenger.isEmpty();
    }

}
