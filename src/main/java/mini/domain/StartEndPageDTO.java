package mini.domain;

import org.apache.ibatis.type.Alias;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Alias("StartEndPageDTO")
public class StartEndPageDTO {
	long startRow;
	long endRow;
	String searchWord;
}
