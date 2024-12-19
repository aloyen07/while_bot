import org.junit.jupiter.api.Test;
import ru.aloyenz.whilebot.Main;
import ru.aloyenz.whilebot.exceptions.RecordNotFoundException;
import ru.aloyenz.whilebot.sql.homework.parsing.HomeworkArgument;
import ru.aloyenz.whilebot.sql.homework.parsing.HomeworkArgumentParser;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

public class HomeworkArgumentParserTest {

    @Test
    public void test() throws RecordNotFoundException, SQLException, ParseException, IOException {
        Main.initSettings();
        Main.initSQL();
        HomeworkArgument arg = HomeworkArgumentParser.parseString("Name // 1d // История");

        System.out.println(arg.name());
        System.out.println(arg.endsAt() + " " + arg.endsAt().getTime());
        System.out.println(arg.retakeDeadline());
        System.out.println(arg.lessonID());
    }
}
