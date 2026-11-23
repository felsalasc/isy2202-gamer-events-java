package cl.duoc.gamerevents;

import java.sql.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import java.nio.charset.StandardCharsets;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.MountableFile;
import static org.assertj.core.api.Assertions.*;

@Testcontainers(disabledWithoutDocker=true)
class WeekFiveMigrationTests {
    @Container static MySQLContainer mysql=new MySQLContainer("mysql:8.4")
            .withCopyFileToContainer(MountableFile.forHostPath("src/test/resources/week4-schema.sql"),"/docker-entrypoint-initdb.d/01-schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath("db/02-data.sql"),"/docker-entrypoint-initdb.d/02-data.sql");
    @Test void upgradingThePreviousSchemaTwicePreservesDataAndUnknownOwnership() throws Exception {
        try(Connection connection=DriverManager.getConnection(mysql.getJdbcUrl(),mysql.getUsername(),mysql.getPassword());
                Statement statement=connection.createStatement()) {
            statement.executeUpdate("UPDATE gamer_events SET title='Evento histórico desconocido' WHERE id=6");
            for(int repetition=0;repetition<2;repetition++)
                for(String file:new String[]{"db/migrations/2026-11-23-owner.sql","db/03-interactions.sql","db/04-community.sql","db/05-demo-owners.sql"})
                    ScriptUtils.executeSqlScript(connection,new EncodedResource(new FileSystemResource(file),StandardCharsets.UTF_8));
            try(ResultSet result=statement.executeQuery("SELECT COUNT(*),COUNT(owner_id) FROM gamer_events")) {
                result.next();assertThat(result.getInt(1)).isEqualTo(6);assertThat(result.getInt(2)).isEqualTo(5);
            }
            try(ResultSet result=statement.executeQuery("SELECT COUNT(*) FROM gamer_users")) {
                result.next();assertThat(result.getInt(1)).isEqualTo(4);
            }
            try(ResultSet result=statement.executeQuery("SELECT owner_id FROM gamer_events WHERE id=6")) {
                result.next();assertThat(result.getObject(1)).isNull();
            }
        }
    }
}
