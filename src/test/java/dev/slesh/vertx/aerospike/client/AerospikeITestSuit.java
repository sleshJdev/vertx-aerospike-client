package dev.slesh.vertx.aerospike.client;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.async.NettyEventLoops;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.query.KeyRecord;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.Statement;
import com.github.dockerjava.api.model.Capability;
import com.playtika.test.aerospike.AerospikeProperties;
import com.playtika.test.aerospike.AerospikeWaitStrategy;
import com.playtika.test.common.properties.CommonContainerProperties;
import com.playtika.test.common.utils.ContainerUtils;
import io.netty.channel.nio.NioEventLoopGroup;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public abstract class AerospikeITestSuit {
    public static final Logger log = LoggerFactory.getLogger(AerospikeITestSuit.class);
    public static final String namespace = "as";
    public static final String set = "vertx";
    static final AerospikeProperties props = aerospikeProps();
    @Container
    public GenericContainer<?> aerospike = aerospike(props);

    private static AerospikeProperties aerospikeProps() {
        final var props = new AerospikeProperties();
        props.setDockerImage("aerospike:ce-5.7.0.12");
        props.setNamespace(namespace);
        props.setWaitTimeoutInSeconds(10);
        return props;
    }

    private static GenericContainer<?> aerospike(AerospikeProperties properties) {
        log.info("Starting aerospike server. Docker image: {}", properties.getDockerImage());
        WaitStrategy waitStrategy = new WaitAllStrategy()
                .withStrategy(new AerospikeWaitStrategy(properties))
                .withStrategy(new HostPortWaitStrategy())
                .withStartupTimeout(properties.getTimeoutDuration());

        GenericContainer<?> aerospike =
                new GenericContainer<>(DockerImageName.parse(properties.getDockerImage()))
                        .withExposedPorts(properties.getPort())
                        // see https://github.com/aerospike/aerospike-server.docker/blob/master/aerospike.template.conf
                        .withEnv("NAMESPACE", properties.getNamespace())
                        .withEnv("SERVICE_PORT", String.valueOf(properties.getPort()))
                        .withEnv("MEM_GB", String.valueOf(1))
                        .withEnv("STORAGE_GB", String.valueOf(1))
                        .waitingFor(waitStrategy);
        String featureKey = properties.getFeatureKey();
        if (featureKey != null) {
            // see https://github.com/aerospike/aerospike-server-enterprise.docker/blob/master/aerospike.template.conf
            aerospike
                    .withEnv("FEATURES", featureKey)
                    .withEnv("FEATURE_KEY_FILE", "env-b64:FEATURES");
        }

        log.info("Starting container with Docker image: {}", props.getDockerImage());
        GenericContainer<?> updatedContainer = aerospike
                .withStartupTimeout(properties.getTimeoutDuration())
                .withReuse(properties.isReuseContainer())
                .withLogConsumer(ContainerUtils.containerLogsConsumer(log))
                .withImagePullPolicy(properties.isUsePullAlwaysPolicy()
                        ? PullPolicy.alwaysPull() : PullPolicy.defaultPolicy())
                .withEnv(properties.getEnv());

        for (CommonContainerProperties.CopyFileProperties fileToCopy : properties.getFilesToInclude()) {
            MountableFile mountableFile = MountableFile.forClasspathResource(fileToCopy.getClasspathResource());
            updatedContainer = updatedContainer.withCopyFileToContainer(mountableFile, fileToCopy.getContainerPath());
        }

        for (CommonContainerProperties.MountVolume mountVolume : properties.getMountVolumes()) {
            updatedContainer.addFileSystemBind(mountVolume.getHostPath(), mountVolume.getContainerPath(), mountVolume.getMode());
        }

        for (Capability capability : properties.getCapabilities()) {
            updatedContainer.withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withCapAdd(capability));
        }

        return properties.getCommand() != null
                ? updatedContainer.withCommand(properties.getCommand())
                : updatedContainer;
    }

    @BeforeEach
    public void setUp() {
        final var eventLoops = new NettyEventLoops(new NioEventLoopGroup());
        try (final AerospikeClient client = createClient(eventLoops)) {
            final var statement = new Statement();
            statement.setNamespace(namespace);
            statement.setSetName(set);
            final RecordSet records = client.query(client.queryPolicyDefault, statement);
            for (final KeyRecord record : records) {
                client.delete(client.writePolicyDefault, record.key);
            }
        }
    }

    @NotNull
    public AerospikeClient createClient(final NettyEventLoops eventLoops) {
        final var clientPolicy = new ClientPolicy();
        clientPolicy.eventLoops = eventLoops;
        return new AerospikeClient(clientPolicy, aerospike.getHost(), aerospike.getFirstMappedPort());
    }
}
