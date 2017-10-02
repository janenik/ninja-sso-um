package services.sso.admin;

import models.sso.admin.ApplicationStatisticsEntry;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Application statistics service. Provides statistics about application and controllers usage.
 */
@Singleton
public class ApplicationStatisticsService {

    /**
     * Count attribute name.
     */
    private final static String COUNT_NAME = "Count";

    /**
     * Rate unit attribute name.
     */
    private final static String RATE_UNIT_NAME = "RateUnit";

    /**
     * Duration unit attribute name.
     */
    private final static String DURATION_UNIT_NAME = "DurationUnit";

    /**
     *  Bean name prefix.
     */
    private final static String BEAN_NAME_PREFIX = "name=";

    /**
     * Maps JMX values by name to appropriate method in {@link ApplicationStatisticsEntry}.
     */
    private static final Map<String, BiConsumer<ApplicationStatisticsEntry, Double>> jmxDoubleValuesToMethodMapper;

    static {
        Map<String, BiConsumer<ApplicationStatisticsEntry, Double>> mapper = new HashMap<>();
        mapper.put("Max", ApplicationStatisticsEntry::setMax);
        mapper.put("Min", ApplicationStatisticsEntry::setMin);
        mapper.put("50thPercentile", ApplicationStatisticsEntry::setPercentile50th);
        mapper.put("75thPercentile", ApplicationStatisticsEntry::setPercentile75th);
        mapper.put("95thPercentile", ApplicationStatisticsEntry::setPercentile95th);
        mapper.put("98thPercentile", ApplicationStatisticsEntry::setPercentile98th);
        mapper.put("99thPercentile", ApplicationStatisticsEntry::setPercentile99th);
        mapper.put("999thPercentile", ApplicationStatisticsEntry::setPercentile999th);
        mapper.put("StdDev", ApplicationStatisticsEntry::setStdDev);
        mapper.put("Mean", ApplicationStatisticsEntry::setMean);
        mapper.put("FifteenMinuteRate", ApplicationStatisticsEntry::setFifteenMinuteRate);
        mapper.put("FiveMinuteRate", ApplicationStatisticsEntry::setFiveMinuteRate);
        mapper.put("OneMinuteRate", ApplicationStatisticsEntry::setOneMinuteRate);
        mapper.put("MeanRate", ApplicationStatisticsEntry::setMeanRate);
        jmxDoubleValuesToMethodMapper = Collections.unmodifiableMap(mapper);
    }

    /**
     * Application properties.
     */
    private final NinjaProperties properties;

    /**
     * Logger.
     */
    private final Logger logger;

    /**
     * JMX beans scope for application beans.
     */
    private final String jmxApplicationScope;


    @Inject
    public ApplicationStatisticsService(NinjaProperties properties, Logger logger) {
        this.properties = properties;
        this.logger = logger;
        this.jmxApplicationScope =
                properties.getWithDefault(
                        "application.sso.admin.jmx.scope", "Ninja Web Application");
    }

    public List<ApplicationStatisticsEntry> getStatistics() throws MBeanException {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        List<ApplicationStatisticsEntry> statisticsEntries = new ArrayList<>();
        try {
            Set<ObjectName> objectNames = server.queryNames(new ObjectName(jmxApplicationScope + ":*"), null);
            for (ObjectName objectName : objectNames) {
                readStatisticsEntry(server, objectName).ifPresent(statisticsEntries::add);
            }
        } catch (MalformedObjectNameException | ReflectionException | IntrospectionException
                | InstanceNotFoundException | MBeanException | AttributeNotFoundException e) {
            logger.error("Error while retrieving JMX objects.", e);
            if (e.getClass() == MBeanException.class) {
                throw MBeanException.class.cast(e);
            } else {
                throw new MBeanException(e);
            }
        }
        return statisticsEntries;
    }

    private Optional<ApplicationStatisticsEntry> readStatisticsEntry(
            MBeanServer server,
            ObjectName objectName)
            throws AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException,
            IntrospectionException {
        Object countValue;
        Object rateUnitName;
        Object durationUnitName;
        MBeanInfo info = server.getMBeanInfo(objectName);
        try {
            countValue = server.getAttribute(objectName, COUNT_NAME);
            rateUnitName = server.getAttribute(objectName, RATE_UNIT_NAME);
            durationUnitName = server.getAttribute(objectName, DURATION_UNIT_NAME);
            if (!Long.class.isInstance(countValue)) {
                return Optional.empty();
            }
        } catch (AttributeNotFoundException anfe) {
            // Safely ignore.
            return Optional.empty();
        }
        ApplicationStatisticsEntry statisticsEntry = new ApplicationStatisticsEntry();
        statisticsEntry.setName(objectName.getCanonicalKeyPropertyListString().replace(BEAN_NAME_PREFIX, ""));
        statisticsEntry.setCount((Long) countValue);
        statisticsEntry.setRateUnit((rateUnitName instanceof String) ? (String) rateUnitName : null);
        statisticsEntry.setDurationUnit((durationUnitName instanceof String) ? (String) durationUnitName : null);
        for (MBeanAttributeInfo attributeInfo : info.getAttributes()) {
            Object value = server.getAttribute(objectName, attributeInfo.getName());
            String name = attributeInfo.getName();
            BiConsumer<ApplicationStatisticsEntry, Double> mappingFunction = jmxDoubleValuesToMethodMapper.get(name);
            if (mappingFunction == null) {
                continue;
            }
            if (value instanceof Double) {
                mappingFunction.accept(statisticsEntry, (Double) value);
            } else {
                logger.warn("JMX Bean value {} :: {} is expected to be Double. Value type: {}.",
                        objectName.getCanonicalKeyPropertyListString(), name, value.getClass().getCanonicalName());
            }
        }
        return Optional.of(statisticsEntry);
    }
}
