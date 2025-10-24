package io.github.md5sha256.chestshopdatabase.database;

import io.github.md5sha256.chestshopdatabase.model.ShopType;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;

import javax.annotation.Nonnull;
import java.util.UUID;

public class MariaDatabaseUtil {

    @Nonnull
    public String selectShopsByItem(@Nonnull ShopType shopType,
                                    @Param("item_code") @Nonnull String itemCode) {
        return new SQL()
                .SELECT("""
                        CAST(world_uuid AS BINARY(16))
                        pos_x AS posX,
                        pos_y AS posY,
                        pos_z AS posZ,
                        item_code AS itemCode,
                        owner_name AS ownerName,
                        buy_price AS buyPrice,
                        sell_price AS sellPrice,
                        quantity,
                        stock
                        """)
                .FROM("Shop")
                .WHERE("item_code = #{item_code}")
                .applyIf(shopType == ShopType.BUY,
                        sql -> sql.WHERE("buy_price IS NOT NULL", "stock > 0"))
                .applyIf(shopType == ShopType.SELL, sql -> sql.WHERE("sell_price IS NOT NULL"))
                .toString();
    }

    @Nonnull
    public String selectShopsByWorldAndItem(@Nonnull ShopType shopType,
                                            @Param("world_uuid") @Nonnull UUID world,
                                            @Param("item_code") @Nonnull String itemCode) {
        return new SQL()
                .SELECT("""
                        CAST(world_uuid AS BINARY(16)) AS worldID,
                        pos_x AS posX,
                        pos_y AS posY,
                        pos_z AS posZ,
                        item_code AS itemCode,
                        owner_name AS ownerName,
                        buy_price AS buyPrice,
                        sell_price AS sellPrice,
                        quantity,
                        stock
                        """)
                .FROM("Shop")
                .WHERE("item_code = #{item_code}",
                        "world_uuid = #{world_uuid, javaType=java.util.UUID, jdbcType=OTHER}")
                .applyIf(shopType == ShopType.BUY, sql -> sql.WHERE("buy_price IS NOT NULL"))
                .applyIf(shopType == ShopType.SELL, sql -> sql.WHERE("sell_price IS NOT NULL"))
                .toString();
    }

    @Nonnull
    public String selectShopsByWorldItemDistance(
            @Nonnull ShopType shopType,
            @Param("world_uuid") @Nonnull UUID world,
            @Param("item_code") @Nonnull String itemCode,
            @Param("x") int x,
            @Param("y") int y,
            @Param("z") int z,
            @Param("distance") double distance
    ) {
        return """
                """ +
                new SQL()
                        .SELECT("""
                                CAST(world_uuid AS BINARY(16)) AS worldID,
                                pos_x AS posX,
                                pos_y AS posY,
                                pos_z AS posZ,
                                item_code AS itemCode,
                                owner_name AS ownerName,
                                buy_price AS buyPrice,
                                sell_price AS sellPrice,
                                quantity,
                                stock,
                                #{distance} * #{distance} AS distanceSquared
                                """)
                        .FROM("Shop")
                        .WHERE("item_code = #{item_code}",
                                "world_uuid = #{world_uuid, javaType=java.util.UUID, jdbcType=OTHER}")
                        .applyIf(shopType == ShopType.BUY,
                                sql -> sql.WHERE("buy_price IS NOT NULL"))
                        .applyIf(shopType == ShopType.SELL,
                                sql -> sql.WHERE("sell_price IS NOT NULL"))
                        .WHERE("pow(pos_x - #{x}, 2) + pow(pos_y - #{y}, 2) + pow(pos_z - #{z}, 2) <= distanceSquared")
                        .toString();
    }


}
