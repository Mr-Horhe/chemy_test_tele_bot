import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.glassfish.grizzly.utils.Pair;

public class DataBaseHandler extends DataBaseConfig {
    Connection dbConnection;

    public Connection getDbConnection() throws ClassNotFoundException, SQLException {
        String connectionString = "jdbc:mysql://" + dbHost + ":"
                + dbPort + "/" + dbName + "?" +
                "autoReconnect=true&useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift" +
                "=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        Class.forName("com.mysql.cj.jdbc.Driver");

        dbConnection = DriverManager.getConnection(connectionString, dbUser, dbPassword);
        return dbConnection;
    }

    public int getRandomQuestionId() {
        String selectRandomQuestion = "SELECT " + DBConsts.QUESTION_ID +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.QUESTION_TABLE +
                " ORDER BY RAND() LIMIT 1";

        Statement statement = null;
        try {
            statement = getDbConnection().createStatement();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        ResultSet resultSet;
        try {
            assert statement != null;
            resultSet = statement.executeQuery(selectRandomQuestion);
            if (resultSet.next()) {
                return Integer.parseInt(resultSet.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getQuestion(int questionId) {
        String selectRandomQuestion = "SELECT " + DBConsts.QUESTION_TEXT +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.QUESTION_TABLE +
                " WHERE " + DBConsts.QUESTION_ID + " = " + questionId;

        Statement statement;
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            assert statement != null;
            resultSet = statement.executeQuery(selectRandomQuestion);
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getAllQuestions() {
        String selectAllQuestionQuery = "SELECT " + DBConsts.QUESTION_ID + ", " + DBConsts.QUESTION_TEXT +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.QUESTION_TABLE;

        Statement statement;
        List<String> allQuestions = new ArrayList<>();
        List<String> questionId = new ArrayList<>();
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(selectAllQuestionQuery);
            while (resultSet.next()) {
                questionId.add(resultSet.getString(1));
                allQuestions.add(resultSet.getString(2));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return allQuestions;
    }

    public HashMap<Integer, Pair<String, Boolean>> getAnswersAtQuestion(int questionId) {
        String selectAnswersAtQuestionQuery = "SELECT " + DBConsts.ANSWER_ID + ", "
                + DBConsts.ANSWER_TEXT + ", "
                + DBConsts.IS_CORRECT +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.ANSWER_TABLE +
                " WHERE " + DBConsts.QUESTION_ID + " = " + questionId;

        HashMap<Integer, Pair<String, Boolean>> answers = new HashMap<>();

        Statement statement = null;
        try {
            statement = getDbConnection().createStatement();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        ResultSet resultSet;
        try {
            assert statement != null;
            resultSet = statement.executeQuery(selectAnswersAtQuestionQuery);
            while (resultSet.next()) {
                if (Objects.equals(resultSet.getString(3), "true")) {
                    answers.put(Integer.valueOf(resultSet.getString(1)),
                            new Pair<>(resultSet.getString(2), true));
                } else {
                    answers.put(Integer.valueOf(resultSet.getString(1)),
                            new Pair<>(resultSet.getString(2), false));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }

    public List<Pair<Integer, String>> getTopics() {
        String selectTopicsQuery = "SELECT " + DBConsts.TOPIC_ID + ", " + DBConsts.TOPIC_NAME +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.TOPIC_TABLE +
                " ORDER BY " + DBConsts.MODULE_ID;

        Statement statement;
        List<Pair<Integer, String>> topics = new ArrayList<>();
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(selectTopicsQuery);
            while (resultSet.next()) {
                topics.add(new Pair<>(Integer.valueOf(resultSet.getString(1)),
                        resultSet.getString(2)));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return topics;
    }

    public HashMap<Integer, String> getTopics(Integer moduleId) {
        String selectTopicsQuery = "SELECT " + DBConsts.TOPIC_ID + ", " + DBConsts.TOPIC_NAME +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.TOPIC_TABLE +
                " WHERE " + DBConsts.MODULE_ID + " = " + moduleId;

        Statement statement;
        HashMap<Integer, String> topics = new HashMap<>();
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(selectTopicsQuery);
            while (resultSet.next()) {
                topics.put(Integer.valueOf(resultSet.getString(1)), resultSet.getString(2));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return topics;
    }

    public HashMap<Integer, String> getModules() {
        String selectModulesQuery = "SELECT " + DBConsts.MODULE_ID + ", " + DBConsts.MODULE_NAME +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.MODULE_TABLE;
        Statement statement;
        HashMap<Integer, String> modules = new HashMap<>();
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(selectModulesQuery);
            while (resultSet.next()) {
                modules.put(Integer.valueOf(resultSet.getString(1)), resultSet.getString(2));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return modules;
    }

    public Pair<Integer, String> getModule(Integer topicId) {
        // Returns Pair<Integer, String> where key is moduleId, value is module name using topicId
        String selectModuleIdAndNameUsingTopicId = "SELECT " + DBConsts.MODULE_ID + ", " + DBConsts.MODULE_NAME +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.MODULE_TABLE +
                " WHERE " + DBConsts.MODULE_ID + " = " +
                " ( SELECT " + DBConsts.MODULE_ID + " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.TOPIC_TABLE +
                " WHERE " + DBConsts.TOPIC_ID + " = " + topicId.toString() + ")";


        Statement statement;
        Pair<Integer, String> module = new Pair<>();
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(selectModuleIdAndNameUsingTopicId);
            while (resultSet.next()) {
                module.setFirst(Integer.valueOf(resultSet.getString(1)));
                module.setSecond(resultSet.getString(2));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return module;
    }

    public List<Question> getQuestions(int topicId) {
        String selectQuestionIdAndText = "SELECT DISTINCT " + DBConsts.QUESTION_ID + ", " + DBConsts.QUESTION_TEXT +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.QUESTION_TABLE +
                " INNER JOIN " + DBConsts.SCHEMA_NAME + "." + DBConsts.QUESTION_TOPIC_MODULE_TABLE +
                " USING (" + DBConsts.QUESTION_ID + ") WHERE " + DBConsts.TOPIC_ID + " = " + topicId;

        Statement statement;
        HashMap<Integer, String> questionIdAndText = new HashMap<>();
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(selectQuestionIdAndText);
            while (resultSet.next()) {
                questionIdAndText.put(Integer.valueOf(resultSet.getString(1)), resultSet.getString(2));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        HashMap<Integer, Pair<String, Boolean>> answers;
        List<Question> questions = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : questionIdAndText.entrySet()) {
            answers = getAnswersAtQuestion(entry.getKey());
            questions.add(new Question(entry.getKey(), topicId, entry.getValue(), answers));
        }
        return questions;
    }

    public String getTopicName(int topicId) {
        String selectTopicsQuery = "SELECT " + DBConsts.TOPIC_NAME +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.TOPIC_TABLE +
                " WHERE " + DBConsts.TOPIC_ID + " = " + topicId;

        Statement statement;
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(selectTopicsQuery);
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addQuestionWithAnswers(String[] moduleIds, String[] topicIds, String questionText, String testNumber,
                                       HashMap<String, Pair<Boolean, Boolean>> textIsCorrectIsHandwrittenAnswer) {
        String questionId = String.valueOf(addQuestion(moduleIds, topicIds, questionText, testNumber));
        addAnswers(questionId, textIsCorrectIsHandwrittenAnswer);
    }
//  Нужно все add сделать boolean, чтобы было понятно, выполнился запрос или нет
    public int addQuestion(String[] moduleIds, String[] topicIds,
                           String questionText, String testNumber) {
        // Add question to database and return question_id of inserted question
        String insertQuestionQuery = "INSERT INTO " + DBConsts.SCHEMA_NAME + "." + DBConsts.QUESTION_TABLE +
                " (" + DBConsts.TEST_NUMBER + ", " + DBConsts.QUESTION_TEXT + ") VALUES (?, ?)";
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(insertQuestionQuery);
            prSt.setString(1, testNumber);
            prSt.setString(2, questionText);
            prSt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        int questionId = -1;
        String getIdQuestionQuery = "SELECT " + DBConsts.QUESTION_ID +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.QUESTION_TABLE +
                " WHERE " + DBConsts.QUESTION_TEXT + " = " + "'" + questionText + "'";
        Statement statement;
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            assert statement != null;
            resultSet = statement.executeQuery(getIdQuestionQuery);
            if (resultSet.next()) {
                questionId = Integer.parseInt(resultSet.getString(1));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        for (String moduleId : moduleIds) {
            for (String topicId : topicIds) {
                String insertQuestionTopicModuleQuery = "INSERT INTO " + DBConsts.SCHEMA_NAME + "." +
                        DBConsts.QUESTION_TOPIC_MODULE_TABLE +
                        " (" + DBConsts.QUESTION_ID + ", " + DBConsts.TOPIC_ID + ", " + DBConsts.MODULE_ID +
                        ") VALUES (?, ?, ?)";
                try {
                    PreparedStatement prSt = getDbConnection().prepareStatement(insertQuestionTopicModuleQuery);
                    prSt.setString(1, String.valueOf(questionId));
                    prSt.setString(2, topicId);
                    prSt.setString(3, moduleId);
                    prSt.executeUpdate();
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return questionId;
    }

    public void addAnswers(String questionId,
                           HashMap<String, Pair<Boolean, Boolean>> textIsCorrectIsHandwrittenAnswer) {
        String insertAnswerQuery = "INSERT INTO " + DBConsts.SCHEMA_NAME + "." + DBConsts.ANSWER_TABLE +
                " (" + DBConsts.QUESTION_ID + ", " + DBConsts.IS_CORRECT + ", " +
                DBConsts.IS_HANDWRITTEN + ", " + DBConsts.ANSWER_TEXT + ") " +
                " VALUES (?, ?, ?, ?)";

        for (Map.Entry<String, Pair<Boolean, Boolean>> entry : textIsCorrectIsHandwrittenAnswer.entrySet()) {
            String answerText = entry.getKey();
            String isCorrect = String.valueOf(entry.getValue().getFirst());
            String isHandwritten = String.valueOf(entry.getValue().getSecond());

            try {
                PreparedStatement prSt = getDbConnection().prepareStatement(insertAnswerQuery);
                prSt.setString(1, questionId);
                prSt.setString(2, isCorrect);
                prSt.setString(3, isHandwritten);
                prSt.setString(4, answerText);
                prSt.executeUpdate();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void addModule(String moduleName) {
        String insertQuestionQuery = "INSERT INTO " + DBConsts.SCHEMA_NAME + "." + DBConsts.MODULE_TABLE +
                " (" + DBConsts.MODULE_NAME + ") VALUES (?)";
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(insertQuestionQuery);
            prSt.setString(1, moduleName);
            prSt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addTopic(String topicName, int moduleId) {
        String insertQuestionQuery = "INSERT INTO " + DBConsts.SCHEMA_NAME + "." + DBConsts.TOPIC_TABLE +
                " (" + DBConsts.MODULE_ID + ", " + DBConsts.TOPIC_NAME + ") VALUES (?, ?)";
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(insertQuestionQuery);
            prSt.setString(1, String.valueOf(moduleId));
            prSt.setString(2, topicName);
            prSt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean isUserRegistered(String chatId) {
        String checkUserRegisteredQuery = "SELECT " + DBConsts.CHAT_ID +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.CHAT_TABLE +
                " WHERE " + DBConsts.CHAT_ID + " = " + chatId;
        Statement statement;
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(checkUserRegisteredQuery);
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addStudent(String studentName, String password) {
        String insertStudentQuery = "INSERT INTO " + DBConsts.SCHEMA_NAME + "." + DBConsts.STUDENT_TABLE +
                " (" + DBConsts.STUDENT_NAME +  ", " + DBConsts.PASSWORD +
                ") VALUES (?, ?)";
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(insertStudentQuery);
            prSt.setString(1, studentName);
            prSt.setString(2, password);
            prSt.executeUpdate();
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addChat(long chatId, String login) {
        String insertStudentQuery = "INSERT INTO " + DBConsts.SCHEMA_NAME + "." + DBConsts.CHAT_TABLE +
                " (" + DBConsts.CHAT_ID + ", " + DBConsts.STUDENT_ID + ") VALUES (?," + " ?)";
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(insertStudentQuery);
            prSt.setString(1, String.valueOf(chatId));
            prSt.setString(2, getStudentId(login));
            prSt.executeUpdate();
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getStudentId(String login) {
        String getStudentIdQuery = "SELECT " + DBConsts.STUDENT_ID +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.STUDENT_TABLE +
                " WHERE " + DBConsts.STUDENT_NAME + " = " + "\"" + login + "\"";

        Statement statement;
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(getStudentIdQuery);
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteStudent(long studentId) {
        String deleteStudentQuery = "DELETE FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.STUDENT_TABLE +
                " WHERE " + DBConsts.STUDENT_ID + " = " + studentId;
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(deleteStudentQuery);
            prSt.executeUpdate();
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;

    }

    public long getStudentId(long chatId){
        String getStudentIdQuery = "SELECT " + DBConsts.STUDENT_ID +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.CHAT_TABLE +
                " WHERE " + DBConsts.CHAT_ID + " = " + chatId;
        Statement statement;

        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(getStudentIdQuery);
            if (resultSet.next()) {
                return Long.parseLong(resultSet.getString(1));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
