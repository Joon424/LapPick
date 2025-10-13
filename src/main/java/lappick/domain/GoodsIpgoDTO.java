package lappick.domain;

import java.util.Date;
import lombok.Data;

@Data
public class GoodsIpgoDTO {
    private Integer ipgoNum;
    private String goodsNum;
    private Integer ipgoQty;
    private Date ipgoDate;
    private String ipgoMemo;
}