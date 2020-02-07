package ch.mobi.emme.twitter.sparql.sink;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Importance.MEDIUM;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;


public class SparqlSinkConnectorConfig extends AbstractConfig {

    public static final String REPOSITORY_HOST = "repository.host";
    public static final String REPOSITORY_NAME = "repository.name";

    private static final ConfigDef CONFIG_DEF;

    static {
        CONFIG_DEF = new ConfigDef();
        CONFIG_DEF.define(REPOSITORY_HOST, STRING, "http://localhost:7200", MEDIUM, "The base URL of the SPARQL repository.");
        CONFIG_DEF.define(REPOSITORY_NAME, STRING, HIGH, "The name of the SPARQL repository.");
    }

    public SparqlSinkConnectorConfig(final Map<?, ?> originals) {
        super(CONFIG_DEF, originals);
    }

    public static ConfigDef conf() {
        return CONFIG_DEF;
    }

    public String getRepositoryHost() {
        return getString(REPOSITORY_HOST);
    }

    public String getRepositoryName() {
        return getString(REPOSITORY_NAME);
    }

}
