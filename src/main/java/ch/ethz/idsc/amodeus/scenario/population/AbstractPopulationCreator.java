/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.population;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.Config;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationWriter;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.scenario.readers.CsvReader;
import ch.ethz.idsc.amodeus.util.io.GZHandler;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ abstract class AbstractPopulationCreator {

    private final String fileName = "population.xml";

    protected final CsvReader reader;
    protected final DateTimeFormatter dateFormat;

    private final File populationFile;
    private final File populationFileGz;
    private final Config config;
    private final Network network;
    protected final MatsimAmodeusDatabase db;

    /* package */ AbstractPopulationCreator(File processingDir, Config config, Network network, //
            MatsimAmodeusDatabase db, DateTimeFormatter dateFormat) {
        populationFile = new File(processingDir, fileName);
        populationFileGz = new File(processingDir, fileName + ".gz");
        this.config = config;
        this.network = network;
        this.db = db;
        this.dateFormat = dateFormat;
        reader = new CsvReader(",", dateFormat);
    }

    public void process(File inFile) throws MalformedURLException, Exception {
        System.out.println("INFO start population creation");
        GlobalAssert.that(inFile.isFile());

        // Population init
        Population population = PopulationUtils.createPopulation(config, network);
        PopulationFactory populationFactory = population.getFactory();

        // Population creation (iterate trough all id's)
        reader.read(inFile);
        reader.lines().forEachOrdered(line -> {
            try {
                processLine(line, population, populationFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Validity Check
        GlobalAssert.that(PopulationHelper.checkAllActivitiesInNetwork(population, network));
        GlobalAssert.that(PopulationHelper.checkAllActivitiesInNetwork(population, network));

        // Write new population to xml

        // write the modified population to file
        PopulationWriter populationWriter = new PopulationWriter(population);
        populationWriter.write(populationFileGz.toString());

        // extract the created .gz file
        GZHandler.extract(populationFileGz, populationFile);

        System.out.println("PopulationSize: " + population.getPersons().size());
        if (population.getPersons().size() > 0)
            System.out.println("INFO successfully created population");
        else
            System.err.println("WARN created population is empty");
    }

    public File getPopulation() {
        return populationFile;
    }

    protected static Coord str2coord(String string) throws IOException {
        List<Double> numbers = Arrays.stream(string.split("]")).map(str -> //
        Double.valueOf(str.replaceAll("[^\\.\\-0123456789]", ""))).collect(Collectors.toList());
        if (numbers.size() == 2)
            return new Coord(numbers.get(0), numbers.get(1));
        else if (numbers.size() == 3)
            return new Coord(numbers.get(0), numbers.get(1), numbers.get(2));
        else
            throw new IOException();
    }

    protected static double dateToSeconds(LocalDateTime date) {
        return date.toLocalTime().getHour() * 3600 + date.toLocalTime().getMinute() * 60 + date.toLocalTime().getSecond();
    }

    abstract protected void processLine(String[] line, Population population, PopulationFactory populationFactory) throws Exception;

}