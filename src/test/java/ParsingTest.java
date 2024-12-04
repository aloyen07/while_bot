import org.junit.jupiter.api.Test;
import ru.aloyenz.whilebot.sql.homework.parsing.Parser;
import ru.aloyenz.whilebot.sql.homework.schema.TreeBranch;
import ru.aloyenz.whilebot.sql.utils.Pair;

public class ParsingTest {

    // Resources
    public static String testString = """
Сделать пресс 2000 раз {
  1 - Команда А {
    1 - Пресс 0 раз // А ты везучий;
    2 - Пресс 1000 раз;
    3 - Пресс 9999 раз // А ты хорош;
    4 - Имя подпункта // Описание подпункта. Не забываем про точку с запятой;
  }
  2 - Команда Б {
    1 - Затролить Геннадия Дмитриевича;
  }
}
""";
    public static String errorString = """
Сделать пресс 2000 раз {
    1 - Команда А {
      -1 - FFF;
    }
}
""";
    public static String emptyString = "";

    @Test
    public void test() {
        Pair<String, TreeBranch> pair = Parser.createHomeworkFromString(errorString);
        System.out.println(pair);
    }
}
