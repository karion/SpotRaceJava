package pl.net.karion.SpotRacer.support;

import org.testcontainers.containers.PostgreSQLContainer;

public final class PostgresTestContainer extends PostgreSQLContainer<PostgresTestContainer> {

    private static final String IMAGE_VERSION = "postgres:16";
    private static PostgresTestContainer container;

    private PostgresTestContainer() {
        super(IMAGE_VERSION);
        withDatabaseName("myapp");
        withUsername("myapp");
        withPassword("myapp");
    }

    public static synchronized PostgresTestContainer getInstance() {
        if (container == null) {
            container = new PostgresTestContainer();
            container.start();
        }
        return container;
    }
}