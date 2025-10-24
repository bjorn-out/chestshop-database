package io.github.md5sha256.chestshopdatabase;

import io.github.md5sha256.chestshopdatabase.command.CommandBean;
import io.github.md5sha256.chestshopdatabase.command.FindShopsCommand;
import io.github.md5sha256.chestshopdatabase.database.DatabaseMapper;
import io.github.md5sha256.chestshopdatabase.database.DatabaseSession;
import io.github.md5sha256.chestshopdatabase.database.DatabaseSettings;
import io.github.md5sha256.chestshopdatabase.database.MariaChestshopMapper;
import io.github.md5sha256.chestshopdatabase.database.MariaDatabase;
import io.github.md5sha256.chestshopdatabase.listener.ChestShopListener;
import io.github.md5sha256.chestshopdatabase.util.UnsafeChestShopSign;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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
        cacheItemCodes(sessionFactory);
        registerCommands(sessionFactory);
        scheduleTasks(sessionFactory);
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

    private void registerCommands(@Nonnull SqlSessionFactory sessionFactory) {
        Executor mainThreadExec = getServer().getScheduler().getMainThreadExecutor(this);
        List<CommandBean> commands = List.of(
                new FindShopsCommand(this.shopState, () -> new DatabaseSession(sessionFactory, MariaChestshopMapper.class), this.databaseExecutor, mainThreadExec)
        );
        this.getLifecycleManager().registerEventHandler(
                LifecycleEvents.COMMANDS, event -> {
                    commands.stream()
                            .map(CommandBean::commands)
                            .flatMap(List::stream)
                            .map(literal -> Commands.literal("csdb").then(literal).build())
                            .forEach(event.registrar()::register);
                }
        );
    }

    private void cacheItemCodes(@Nonnull SqlSessionFactory sessionFactory) {
        try (SqlSession session = sessionFactory.openSession()) {
            DatabaseMapper database = session.getMapper(MariaChestshopMapper.class);
            this.shopState.cacheItemCodes(getLogger(), database);
        }
    }

    private void scheduleTasks(@Nonnull SqlSessionFactory sessionFactory) {
        BukkitScheduler scheduler = getServer().getScheduler();
        Logger logger = getLogger();
        long interval = 1;
        scheduler.runTaskTimer(this, () -> {
            Consumer<DatabaseMapper> flushTask = shopState.flushTask();
            if (flushTask == null) {
                return;
            }
            logger.info("Beginning flush task...");
            CompletableFuture.runAsync(() -> {
                try (SqlSession session = sessionFactory.openSession(false)) {
                    DatabaseMapper databaseMapper = session.getMapper(MariaChestshopMapper.class);
                    flushTask.accept(databaseMapper);
                    session.commit();
                } catch (Exception ex) {
                    logger.severe("Failed to flush shop state to database!");
                    ex.printStackTrace();
                }
                logger.info("Flush task complete!");
            });
        }, interval, interval);
        this.discoverer.schedulePollTask(this, scheduler, 20, 5);
    }
}
