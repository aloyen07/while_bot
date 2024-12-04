package ru.aloyenz.whilebot.sql.homework.parsing;

import ru.aloyenz.whilebot.exceptions.schema.*;
import ru.aloyenz.whilebot.sql.homework.schema.Branch;
import ru.aloyenz.whilebot.sql.homework.schema.BranchEndpoint;
import ru.aloyenz.whilebot.sql.homework.schema.TreeBranch;
import ru.aloyenz.whilebot.sql.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    public static Pair<String, TreeBranch> createHomeworkFromString(String input) {
        return getMainTreeFromMSG(input);
    }


    private static Pair<String, TreeBranch> getMainTreeFromMSG(String in) {
        String homework = in.replace("+create_homework ", "");
        char[] chars = homework.toCharArray();

        int ordinalLevel = 0;
        int line = 0;

        int opens = 0;
        int closes = 0;

        boolean nameRead = true;
        boolean screened = false;
        StringBuilder temp = new StringBuilder();
        List<TreeBranch> ordinals = new ArrayList<>();

        StringBuilder hwName = new StringBuilder();

        boolean initialized = false;

        int position = -1;
        for (char ch : chars) {
            position += 1;

            if (nameRead) {
                if (ch != '{') {
                    hwName.append(ch);
                } else {
                    // We parsed a homework name
                    nameRead = false;
                    opens += 1;
                    initialized = true;
                    ordinals.add(new TreeBranch(-1, "MAIN TREE", null));
                }
                continue;
            }

            // Main parsing branches
            if (ch == '{' && !screened) {
                try {
                    ordinals.add((TreeBranch) getFromString(temp.toString().strip(), TreeBranch.class));
                } catch (RawIndexParseError e) {
                    throw new IndexParseError(e, line + 1);
                } catch (RawIndexIsNegative e) {
                    throw new IndexIsNegative(e, line + 1);
                }

                temp = new StringBuilder();
                ordinalLevel += 1;
                opens += 1;
            } else if (ch == ';' && !screened) {
                try {
                    ordinals.get(ordinalLevel).addBranch(getFromString(temp.toString().strip(), BranchEndpoint.class));
                } catch (RawIndexParseError e) {
                    throw new IndexParseError(e, line + 1);
                } catch (RawIndexIsNegative e) {
                    throw new IndexIsNegative(e, line + 1);
                }

                temp = new StringBuilder();
            } else if (ch == '\\' && !screened) {
                screened = true;
            } else if (ch == '}' && ordinalLevel != 0 && !screened) {
                // Closing a bracket
                if (!temp.toString().isBlank()) { // Check for correctly closing
                    throw new LineEndRequired(line + 1);
                }

                TreeBranch ordinalBranch = ordinals.get(ordinalLevel);

                if (ordinalBranch.isEmpty()) { // Check to branch non-empty
                    throw new BranchIsEmpty(line + 1, ordinalBranch);
                }

                checkForIndexDuplications(ordinalBranch);

                ordinals.get(ordinalLevel - 1).addBranch(ordinalBranch);
                ordinals.remove(ordinalLevel);
                ordinalLevel -= 1;
                closes += 1;
            } else if (ordinalLevel == 0 && ch == '}' && !screened) {
                if (!temp.toString().isBlank()) {
                    throw new LineEndRequired(line + 1);
                }

                closes += 1;

                // Check to end of string
                position += 1;
                try {
                    if (!in.substring(position).isBlank()) {
                        throw new BracketsMismatch(0, 100);
                    }
                } catch (IndexOutOfBoundsException ignore) {}


                break;
            } else {
                screened = false; // Default letters
                temp.append(ch);
            }

            if (ch == '\n') { // For debugging
                line += 1;
            }
        }

        if (!initialized) {
            throw new NotInitializedException();
        }

        checkForIndexDuplications(ordinals.getFirst());

        if (opens != closes) {
            throw new BracketsMismatch(opens, closes);
        }

        //ordinalLevel -= 1;
        if (ordinalLevel < 0) {
            throw new OrdinalLevelIsNegative(ordinalLevel);
        }
        if (ordinalLevel != 0) { // Checks for valid structure
            throw new OrdinalLevelNonNull(ordinalLevel);
        }



        return new Pair<>(hwName.toString(), ordinals.getFirst());
    }

    private static void checkForIndexDuplications(TreeBranch branchIn) {
        List<Integer> indexes = new ArrayList<>();
        for (Branch branch : branchIn.getBranches()) {

            if (indexes.contains(branch.getIndex())) {
                throw new IndexDuplicationError(branchIn, branch);
            }

            indexes.add(branch.getIndex());
        }
    }


    private static <T extends Branch> Branch getFromString(String argument, Class<T> branchClass) throws RawIndexParseError, RawIndexIsNegative {
        // get index
        argument = argument.replace("^(?!\\\\)\\{", "").replace("^(?!\\\\);", "");
        int index;
        try {
            index = Integer.parseInt(argument.split(" - ")[0].strip());
        } catch (NumberFormatException ignore) {
            throw new RawIndexParseError(argument.split(" - ")[0].strip());
        }

        if (index <= 0) {
            throw new RawIndexIsNegative(index);
        }

        String name = null;
        String description = null;

        if (argument.startsWith(index + " - ")) { // We have additional arguments
            String add = argument.split(" - ", 2)[1].strip();

            if (add.contains("//")) { // We have description, wow!
                name = add.split("//", 2)[0].strip();
                description = add.split("//", 2)[1].strip();
            } else { // We have only name
                name = add;
            }
        }

        if (branchClass.equals(TreeBranch.class)) {
            return new TreeBranch(index, name, description);
        } else if (branchClass.equals(BranchEndpoint.class)) {
            return new BranchEndpoint(index, name, description);
        }

        return null;
    }
}
