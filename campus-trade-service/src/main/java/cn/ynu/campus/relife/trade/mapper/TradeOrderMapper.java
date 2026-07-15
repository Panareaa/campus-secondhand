package cn.ynu.campus.relife.trade.mapper;

import cn.ynu.campus.relife.trade.domain.TradeOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TradeOrderMapper {

    @Insert("""
            INSERT INTO trade_order
            (trade_no, buyer_id, seller_id, total_amount, status, buyer_contact, expired_at)
            VALUES (#{order.tradeNo}, #{order.buyerId}, #{order.sellerId}, #{order.totalAmount},
                    #{order.status}, #{order.buyerContact}, #{order.expiredAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "order.id")
    int insert(@Param("order") TradeOrder order);

    @Select("SELECT id, trade_no, buyer_id, seller_id, total_amount, status, buyer_contact, cancel_reason, " +
            "confirmed_at, completed_at, expired_at, created_at, updated_at " +
            "FROM trade_order WHERE trade_no = #{tradeNo} LIMIT 1")
    TradeOrder selectByTradeNo(@Param("tradeNo") String tradeNo);

    @Select("SELECT id, trade_no, buyer_id, seller_id, total_amount, status, buyer_contact, cancel_reason, " +
            "confirmed_at, completed_at, expired_at, created_at, updated_at " +
            "FROM trade_order WHERE buyer_id = #{buyerId} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<TradeOrder> selectByBuyer(@Param("buyerId") Long buyerId,
                                   @Param("offset") long offset,
                                   @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM trade_order WHERE buyer_id = #{buyerId}")
    long countByBuyer(@Param("buyerId") Long buyerId);

    @Select("SELECT id, trade_no, buyer_id, seller_id, total_amount, status, buyer_contact, cancel_reason, " +
            "confirmed_at, completed_at, expired_at, created_at, updated_at " +
            "FROM trade_order WHERE seller_id = #{sellerId} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<TradeOrder> selectBySeller(@Param("sellerId") Long sellerId,
                                    @Param("offset") long offset,
                                    @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM trade_order WHERE seller_id = #{sellerId}")
    long countBySeller(@Param("sellerId") Long sellerId);

    @Select("""
            SELECT id, trade_no, buyer_id, seller_id, total_amount, status, buyer_contact, cancel_reason,
                   confirmed_at, completed_at, expired_at, created_at, updated_at
            FROM trade_order
            WHERE status = 0 AND expired_at IS NOT NULL AND expired_at <= #{now}
            ORDER BY expired_at ASC
            LIMIT #{limit}
            """)
    List<TradeOrder> selectPendingExpired(@Param("now") java.time.LocalDateTime now,
                                          @Param("limit") int limit);

    @Update("""
            UPDATE trade_order
            SET status = #{order.status},
                cancel_reason = #{order.cancelReason},
                confirmed_at = #{order.confirmedAt},
                completed_at = #{order.completedAt},
                updated_at = NOW()
            WHERE id = #{order.id} AND buyer_id = #{order.buyerId}
            """)
    int updateOrder(@Param("order") TradeOrder order);
}
