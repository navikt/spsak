package no.nav.foreldrepenger.behandling.steg.inngangsvilkår.auto.util;

import static no.nav.foreldrepenger.behandling.steg.inngangsvilkår.auto.JsonUtil.INPUT_SUFFIX;
import static no.nav.foreldrepenger.behandling.steg.inngangsvilkår.auto.JsonUtil.OUTPUT_SUFFIX;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import no.nav.foreldrepenger.behandling.steg.inngangsvilkår.auto.JsonUtil;
import no.nav.foreldrepenger.behandling.steg.inngangsvilkår.auto.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;

class TrekkUtInngangsvilkårTestData {

    private static final int MAX_ANTALL = 750;
    private static final int MAX_FORSØK = 2000;
    private static final Logger log = LoggerFactory.getLogger(TrekkUtInngangsvilkårTestData.class);

    private final ObjectMapper mapper = JsonUtil.getObjectMapper();

    public static void main(String[] args) throws IOException {
        final File file = new File("behandlingsprosess/inngangsvilkar/src/test/testscript/vilkår");
        log.info("Henter ut vilkårsresultat");
        Path directory;
        if (!file.exists()) {
            directory = Files.createDirectory(file.toPath());
        } else {
            directory = file.toPath();
        }

        new TrekkUtInngangsvilkårTestData().trekkUt(directory);
        log.info("Ferdig.");
    }

    private static String clobToString(Clob clb) throws SQLException, IOException {
        if (clb == null) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        String strng;
        BufferedReader br = new BufferedReader(clb.getCharacterStream());
        while ((strng = br.readLine()) != null) {
            str.append(strng);
        }

        return str.toString();
    }

    private void trekkUt(Path rootDirectory) {
        final DataSource dataSource = createDataSource();
        Stream.of("FP_VK_21", "FP_VK_23", "FP_VK_2", "FP_VK_1", "FP_VK_11", "FP_VK_4", "FP_VK_16").forEach(vilkårType -> {
            log.info("Starter med uthenting av " + vilkårType);
            hentUtVilkårsData(dataSource, rootDirectory, vilkårType);
        });
    }

    private void hentUtVilkårsData(DataSource dataSource, Path rootDirectory, String vilkårType) {
        Stream.of(VilkårUtfallType.OPPFYLT, VilkårUtfallType.IKKE_OPPFYLT).forEach(utfall -> {
            try {
                hentUtResultatFor(dataSource, rootDirectory, vilkårType, utfall);
            } catch (SQLException e) {
                log.error("Feilet under uthenting av " + vilkårType, e);
            }
        });
    }

    private void hentUtResultatFor(DataSource dataSource, Path rootDirectory, String vilkårType, VilkårUtfallType vilkårUtfall) throws SQLException {
        Map<FileRef, Set<LocalDate>> fileMap = initFiles(rootDirectory, vilkårType);
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                    "SELECT v.id, v.VILKAR_TYPE, v.VILKAR_UTFALL, v.AVSLAG_KODE, v.REGEL_INPUT, o.FOM, o.TOM, o.OPPTJENT_PERIODE, v.OPPRETTET_TID " +
                        "FROM VILKAR v " +
                        "LEFT OUTER JOIN OPPTJENING o ON o.VILKAR_RESULTAT_ID = v.VILKAR_RESULTAT_ID " +
                        "WHERE v.VILKAR_UTFALL = '" + vilkårUtfall.getKode() + "' " +
                        "AND o.AKTIV = 'J'" +
                        "AND v.VILKAR_TYPE = '" + vilkårType + "' " +
                        "AND v.REGEL_INPUT IS NOT NULL " +
                        "ORDER BY v.OPPRETTET_TID DESC")) {
            
            int antallLagret = 0;
            int antallForsøk = 0;
            
            while (resultSet.next()) {
                try {
                    if (MAX_FORSØK == ++antallForsøk) {
                        break;
                    }
                    final Long id = resultSet.getLong(1);
                    final String vilkarType = resultSet.getString(2);
                    final String utfall = resultSet.getString(3);
                    final String avslag = resultSet.getString(4);
                    final Clob regelInput = resultSet.getClob(5);
                    final LocalDate fomOpptjening = convertToLocalDate(resultSet.getDate(6));
                    final LocalDate tomOpptjening = convertToLocalDate(resultSet.getDate(7));
                    final String opptjentTid = resultSet.getString(8);
                    final LocalDate kjøreTidspunkt = convertToLocalDate(resultSet.getDate(9));

                    skrivTilFil(rootDirectory, fileMap,
                        new VilkårResultat(id, kjøreTidspunkt, vilkarType, utfall, avslag, fomOpptjening, tomOpptjening, opptjentTid), regelInput);
                    antallLagret++;
                    if (antallLagret == MAX_ANTALL) {
                        break;
                    }
                } catch (DuplikatTestCaseException e) {
                    if (log.isDebugEnabled()) {
                        log.debug(e.getMessage());
                    }
                } catch (IOException | SQLException e) {
                    log.error("Noe gikk galt : " + e.getMessage(), e);
                }
            }
            log.info("Hentet ut " + antallLagret + " testcases, fant " + (antallForsøk - antallLagret) + " duplikater" +
                " for " + vilkårType + " med utfall " + vilkårUtfall.getKode());
        }
    }

    private Map<FileRef, Set<LocalDate>> initFiles(Path rootDirectory, String vilkårType) {
        final HashMap<FileRef, Set<LocalDate>> setHashMap = new HashMap<>();

        final Path vilkårFolder = Paths.get(rootDirectory.toFile().getPath() + "/" + vilkårType);
        final File file = vilkårFolder.toFile();
        final File[] listFiles = file.listFiles();
        if (listFiles == null) {
            return setHashMap;
        }
        Arrays.stream(listFiles).filter(it -> it.getName().endsWith(INPUT_SUFFIX)).forEach(kandidat -> {
            try {
                final Optional<File> output = Arrays.stream(listFiles)
                    .filter(fil -> fil.getName().equals(kandidat.getName().replace(INPUT_SUFFIX, OUTPUT_SUFFIX))).findAny();
                if (output.isPresent()) {
                    final VilkårResultat vilkårResultat = mapper.readValue(output.get(), VilkårResultat.class);
                    final FileRef key = new FileRef(Files.readAllBytes(kandidat.toPath()), vilkårResultat.getUtfall());
                    final Set<LocalDate> orDefault = setHashMap.getOrDefault(key, new HashSet<>());
                    orDefault.add(vilkårResultat.getKjøreTidspunkt());
                    setHashMap.put(key, orDefault);
                } else {
                    throw new FileNotFoundException("Fant ikke output fil for " + kandidat.getName());
                }

            } catch (IOException e) {
                log.error("Feiler under lesing av " + kandidat.getName());
            }
        });
        return setHashMap;
    }

    private LocalDate convertToLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toLocalDate();
    }

    private void skrivTilFil(Path rootDirectory, Map<FileRef, Set<LocalDate>> fileMap, VilkårResultat vilkårResultat, Clob regelInput)
            throws IOException, SQLException {
        final ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
        final Path vilkårFolder = Paths.get(rootDirectory.toFile().getPath() + "/" + vilkårResultat.getVilkarType());
        final File file = vilkårFolder.toFile();
        if (!file.exists()) {
            file.mkdir(); // IGNORE
        }
        final String filePrefix = createFilePrefix(vilkårResultat);
        final Path input = Paths.get(vilkårFolder.toFile().getPath() + "/" + filePrefix + INPUT_SUFFIX);
        final File inputFile = input.toFile();
        if (!inputFile.exists()) {
            inputFile.createNewFile();
        }

        objectWriter.writeValue(inputFile, mapper.readValue(clobToString(regelInput), Object.class));
        sjekkOmSammeTestcaseEksisterer(file, fileMap, inputFile, vilkårResultat);

        final Path output = Paths.get(vilkårFolder.toFile().getPath() + "/" + filePrefix + OUTPUT_SUFFIX);
        final File outputFile = output.toFile();
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        objectWriter.writeValue(outputFile, vilkårResultat);
    }

    private void sjekkOmSammeTestcaseEksisterer(File file, Map<FileRef, Set<LocalDate>> fileMap, File inputFile, VilkårResultat vilkårResultat) {
        final File[] listFiles = file.listFiles();
        if (listFiles == null) {
            return;
        }

        try {
            final FileRef inputFileBytes = new FileRef(Files.readAllBytes(inputFile.toPath()), vilkårResultat.getUtfall());
            final Set<LocalDate> kjøretidspunkt = fileMap.getOrDefault(inputFileBytes, new HashSet<>());
            if (kjøretidspunkt.isEmpty() || !kjøretidspunkt.contains(vilkårResultat.getKjøreTidspunkt())) {
                kjøretidspunkt.add(vilkårResultat.getKjøreTidspunkt());
                fileMap.put(inputFileBytes, kjøretidspunkt);
            } else {
                inputFile.delete();
                throw new DuplikatTestCaseException("Fant duplikat testcase. Ignorerer" + vilkårResultat);
            }
        } catch (IOException e) {
            log.error("Feil under kandidatsjekk", e);
        }
    }

    private String createFilePrefix(VilkårResultat resultat) {
        return resultat.getId() + "-" + UUID.randomUUID().toString();
    }

    private DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/fpsak");
        config.setUsername("fpsak");
        config.setPassword("fpsak");
        config.setConnectionTestQuery("select 1");
        config.setDriverClassName("org.postgresql.Driver");
        Properties dsProperties = new Properties();
        config.setDataSourceProperties(dsProperties);

        HikariDataSource ds = new HikariDataSource(config);

        try {
            new EnvEntry("jdbc/defaultDS", ds);
            return ds;
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
    }

    private class DuplikatTestCaseException extends RuntimeException {
        DuplikatTestCaseException(String s) {
            super(s);
        }
    }
}
