package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface KompletthetssjekkerInntektsmelding {

    /**
     * Utleder manglende inntektsmeldinger
     *
     * @return Manglende påkrevde inntektsmeldinger som ennå ikke er mottatt
     */
    List<ManglendeVedlegg> utledManglendeInntektsmeldinger(Behandling behandling);

    /**
     * Henter alle påkrevde inntektsmeldinger fra grunnlaget, og filterer ut alle
     * motatte.
     *
     * @return Manglende påkrevde inntektsmeldinger som ennå ikke er motatt
     */
    List<ManglendeVedlegg> utledManglendeInntektsmeldingerFraGrunnlag(Behandling behandling);

}
