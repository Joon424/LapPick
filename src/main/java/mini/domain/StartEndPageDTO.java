package mini.domain;

import org.apache.ibatis.type.Alias;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Alias("StartEndPageDTO")
public class StartEndPageDTO {
	int startRow;
	int endRow;
	String searchWord;
}
