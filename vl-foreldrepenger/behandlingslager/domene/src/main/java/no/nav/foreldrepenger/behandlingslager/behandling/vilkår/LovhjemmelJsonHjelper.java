package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

public class LovhjemmelJsonHjelper {

    private LovhjemmelJsonHjelper() {

    }

    public static String findLovhjemmelIJson(FagsakYtelseType fagsakYtelseType, String ekstraData, String kodeverk, String kode) {
        JsonNode ekstraDataJsonNode = getEkstraDataAsJsonNode(ekstraData, kodeverk, kode);
        List<JsonNode> referanser = Collections.emptyList();
        if (fagsakYtelseType.gjelderForeldrepenger()) {
            referanser = ekstraDataJsonNode.findValue("FP").findValues("lovreferanse");
        }
        return referanser.stream()
            .map(JsonNode::asText)
            .flatMap(s -> Arrays.stream(s.split(","))).distinct().collect(Collectors.joining("\n"));
    }

    private static JsonNode getEkstraDataAsJsonNode(String ekstraData, String kodeverk, String kode) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(ekstraData);
        } catch (IOException e) {
            throw new IllegalStateException("Ugyldig format (forventet JSON) for kodeverk=" + kodeverk + ", kode=" + kode //$NON-NLS-1$ //$NON-NLS-2$
                + " " + ekstraData, e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return jsonNode;
    }

}
