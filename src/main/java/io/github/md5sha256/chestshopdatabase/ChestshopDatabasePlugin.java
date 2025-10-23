package io.github.md5sha256.chestshopdatabase;

import io.github.md5sha256.chestshopdatabase.database.DatabaseInterface;
import io.github.md5sha256.chestshopdatabase.database.DatabaseSettings;
import io.github.md5sha256.chestshopdatabase.database.MariaChestshopMapper;
import io.github.md5sha256.chestshopdatabase.database.MariaDatabase;
import io.github.md5sha256.chestshopdatabase.listener.ChestShopListener;
import io.github.md5sha256.chestshopdatabase.util.UnsafeChestShopSign;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class ChestshopDatabasePlugin extends JavaPlugin {

    private final ExecutorService databaseExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private ChestShopState shopState;
    private ItemDiscoverer discoverer;

    private DatabaseSettings getDbSettings() {
        return null;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        UnsafeChestShopSign.init();
        getLogger().info("Plugin enabled");
        shopState = new ChestShopState(Duration.ofMinutes(5));
        discoverer = new ItemDiscoverer(50, Duration.ofMinutes(5), 50, getServer());
        getServer().getPluginManager()
                .registerEvents(new ChestShopListener(shopState, discoverer), this);
        SqlSessionFactory sessionFactory = MariaDatabase.buildSessionFactory(getDbSettings());
        scheduleTasks(sessionFactory);
    }

    private void scheduleTasks(@Nonnull SqlSessionFactory sessionFactory) {
        BukkitScheduler scheduler = getServer().getScheduler();
        Logger logger = getLogger();
        scheduler.runTaskTimer(this, () -> {
            Consumer<DatabaseInterface> flushTask = shopState.flushTask();
            CompletableFuture.runAsync(() -> {
                try (SqlSession session = sessionFactory.openSession(false)) {
                    DatabaseInterface databaseInterface = session.getMapper(MariaChestshopMapper.class);
                    flushTask.accept(databaseInterface);
                    session.commit();
                } catch (Exception ex) {
                    logger.severe("Failed to flush shop state to database!");
                    ex.printStackTrace();
                }
            });
        }, 20, 20);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            this.databaseExecutor.shutdownNow();
            this.databaseExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        getLogger().info("Plugin disabled");
    }
}
