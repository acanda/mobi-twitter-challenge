package ch.mobi.emme.twitter.sparql.sink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

import ch.mobi.emme.twitter.sparql.util.VersionUtil;

public class SparqlSinkConnector extends SinkConnector {

    private SparqlSinkConnectorConfig config;

    @Override
    public void start(final Map<String, String> originals) {
        config = new SparqlSinkConnectorConfig(originals);
    }

    @Override
    public void stop() {
        // nothing to do
    }

    @Override
    public List<Map<String, String>> taskConfigs(final int maxTasks) {
        return IntStream.range(0, maxTasks).mapToObj(i -> new HashMap<>(config.originalsStrings())).collect(Collectors.toList());
    }

    @Override
    public ConfigDef config() {
        return SparqlSinkConnectorConfig.conf();
    }

    @Override
    public Class<? extends Task> taskClass() {
        return SparqlSinkTask.class;
    }

    @Override
    public String version() {
        return VersionUtil.getVersion();
    }

}
