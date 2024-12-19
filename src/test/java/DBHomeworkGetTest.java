import org.junit.jupiter.api.Test;
import ru.aloyenz.whilebot.Main;
import ru.aloyenz.whilebot.exceptions.RecordNotFoundException;
import ru.aloyenz.whilebot.sql.homework.Homework;
import ru.aloyenz.whilebot.sql.homework.Lesson;
import ru.aloyenz.whilebot.sql.homework.schema.TreeBranch;

import java.io.IOException;
import java.sql.SQLException;

public class DBHomeworkGetTest {

    @Test
    public void test() throws RecordNotFoundException, SQLException, IOException {
        Main.initSettings();
        Main.initSQL();

        Homework someHomework = Homework.getHomeworksForLesson(Lesson.lessonFor("Ист").getId()).getLast();

        someHomework.getBranchSchemaFromDatabase();
        TreeBranch branch = someHomework.getMainTreeBranch();

        System.out.println(branch);
    }
}
