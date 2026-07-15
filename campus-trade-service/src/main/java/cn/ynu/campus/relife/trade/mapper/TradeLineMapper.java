package cn.ynu.campus.relife.trade.mapper;

import cn.ynu.campus.relife.trade.domain.TradeLine;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TradeLineMapper {

    @Insert("""
            INSERT INTO trade_line
            (trade_id, trade_no, buyer_id, item_id, seller_id, item_title, item_cover,
             unit_price, quantity, line_amount)
            VALUES (#{line.tradeId}, #{line.tradeNo}, #{line.buyerId}, #{line.itemId}, #{line.sellerId},
                    #{line.itemTitle}, #{line.itemCover}, #{line.unitPrice}, #{line.quantity}, #{line.lineAmount})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "line.id")
    int insert(@Param("line") TradeLine line);

    @Select("SELECT id, trade_id, trade_no, buyer_id, item_id, seller_id, item_title, item_cover, " +
            "unit_price, quantity, line_amount, created_at " +
            "FROM trade_line WHERE trade_id = #{tradeId} AND buyer_id = #{buyerId}")
    List<TradeLine> selectByTradeId(@Param("tradeId") Long tradeId, @Param("buyerId") Long buyerId);
}
