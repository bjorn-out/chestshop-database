package io.github.md5sha256.chestshopdatabase.database;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

public class DatabaseSession implements Closeable, AutoCloseable {

    private final SqlSession session;
    private DatabaseMapper mapper;

    public DatabaseSession(@NotNull SqlSessionFactory factory, @NotNull Class<? extends DatabaseMapper> mapperClass) {
        this.session = factory.openSession();
        this.mapper = this.session.getMapper(mapperClass);
    }

    public SqlSession session() {
        return this.session;
    }

    public DatabaseMapper mapper() {
        return this.mapper;
    }


    @Override
    public void close() {
        this.mapper = null;
        if (this.session != null) {
            session.close();
        }
    }
}
