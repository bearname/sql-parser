package com.sqlparser.service;

import com.sqlparser.model.Join;
import com.sqlparser.model.JoinType;
import com.sqlparser.model.Limit;
import com.sqlparser.model.Query;

import java.util.HashMap;
import java.util.Map;

public class SqlAnalyzer {
    public static final char ALL_COLUMNS_CHAR = '*';
    public static final char WHITESPACE = ' ';

    private final static String SELECT = "SELECT ";
    private final static String FROM = "FROM ";
    private static final String WHERE = "WHERE ";
    public static final String LEFT_JOIN = "LEFT JOIN ";
    public static final String RIGHT_JOIN = "RIGHT JOIN ";
    public static final String INNER_JOIN = "INNER JOIN ";
    public static final String FULL_OUTER_JOIN = "FULL OUTER JOIN ";
    private static final String GROUP_BY = "GROUP BY ";
    private static final String ORDER_BY = "ORDER BY ";
    private static final String LIMIT = "LIMIT ";
    private static final String OFFSET = "OFFSET ";
    private static final String BETWEEN = "BETWEEN ";
    public static final String ASC = " ASC";
    public static final String DESC = " DESC";

    public static final char QUERY_END_SYMBOL = ';';
    public static final char STRING_QUOTED_SYMBOL = '\'';
    public static final String OR = "OR ";
    public static final String AND = "AND ";
    private final Map<String, OperatorType> mapComparisonOperator = new HashMap<>();

    private final String sqlQueryInput;
    private final Query query = new Query();
    private int position = 0;
    private final int QUERY_LENGTH;

    public SqlAnalyzer(String sqlQueryInput) {
        this.sqlQueryInput = sqlQueryInput;
        this.QUERY_LENGTH = sqlQueryInput.length();
        this.mapComparisonOperator.put("<", OperatorType.LESS_THAN);
        this.mapComparisonOperator.put("<=", OperatorType.LESS_THAN_OR_EQUAL_TO);
        this.mapComparisonOperator.put(">", OperatorType.GREATER_THAN);
        this.mapComparisonOperator.put(">=", OperatorType.GREATER_THAN_OR_EQUAL_TO);
        this.mapComparisonOperator.put("!=", OperatorType.NOT_EQUAL);
        this.mapComparisonOperator.put("<>", OperatorType.NOT_EQUAL);
        this.mapComparisonOperator.put("=", OperatorType.EQUAL);
    }

    public Query analyze() throws Exception {
        if (sqlQueryInput.isEmpty()) {
            throw new Exception("Query isEmpty");
        }
        if (sqlQueryInput.charAt(QUERY_LENGTH - 1) != ';') {
            throw new Exception("Query must contains ';' at the end. " + sqlQueryInput);
        }

        checkSelectAggregateColumns();
        checkFromTableExpressions();
        checkJoinTable();
        checkWhereTableExpressions();
        //TODO having support,
        // multiple 'OR' and 'AND' operator
        // subquery,
        // function,

        checkGroupBy();
        checkOrderBy();
        checkLimitOffset();

        return query;
    }

    private void checkSelectAggregateColumns() throws Exception {
        char charAt = sqlQueryInput.charAt(position);
        checkKeyWord(charAt, SELECT);
        charAt = getNextToken();
        if (charAt == ALL_COLUMNS_CHAR) {
            query.addColumn(String.valueOf(ALL_COLUMNS_CHAR));
            position++;
            if (sqlQueryInput.charAt(position) != WHITESPACE) {
                throwInvalidToken(charAt, position);
            }
        } else {
            String aggregateColumn = getAggregateColumn();
            query.addColumn(aggregateColumn);
            charAt = getCharIgnoringRedundantWhitespace(position);

            while (charAt == ',') {
                this.position++;
                aggregateColumn = getAggregateColumn();
                query.addColumn(aggregateColumn);
                if (charAt == ' ') {
                    charAt = getCharIgnoringRedundantWhitespace(this.position);
                } else {
                    charAt = getCurrentToken();
                }
            }
        }
    }

    private void checkFromTableExpressions() throws Exception {
        char charAt = sqlQueryInput.charAt(position);
        if (charAt == ' ' && position + 1 < sqlQueryInput.length() - 2) {
            if (sqlQueryInput.charAt(position + 1) == ' ') {
                while (charAt == ' ') {
                    charAt = getNextToken();
                }
            } else {
                charAt = getNextToken();
            }
        }
        final int i = QUERY_LENGTH - FROM.length() - 1 - 1 - 1 - 1;
        if (this.position > i || (charAt != 'F')) {
            throwInvalidToken(charAt, position);
        }

        checkKeyWord(charAt, FROM);
        String sourceTable = getAggregateColumn();
        this.query.addFromSource(sourceTable);
        charAt = getCharIgnoringRedundantWhitespace(position);

        while (charAt == ',') {
            this.position++;
            sourceTable = getAggregateColumn();
            this.query.addFromSource(sourceTable);
            if (charAt == ' ') {
                charAt = getCharIgnoringRedundantWhitespace(this.position);
            } else {
                charAt = getCurrentToken();
            }
        }
    }

    private void checkJoinTable() throws Exception {
        char token = sqlQueryInput.charAt(this.position);
        if (token != QUERY_END_SYMBOL) {
            if (!maybeJoins(token)) {
                return;
            }
            while (true) {
                final boolean b = maybeJoins(token);
                if (!b) {
                    break;
                }
                final JoinType joinType = getJoinType(token);
                if (joinType == null) {
                    return;
                }
                token = getNextTokeSkippingWhiteSpace(token);
                final String joinTable = parseColumnRef(token);
                token = getNextTokeSkippingWhiteSpace(token);
                if (joinTable.isEmpty() || token != 'O' || getToken(1) != 'N' || getToken(2) != ' ') {
                    return;
                }

                shiftPosition(2);
                token = getNextTokeSkippingWhiteSpace(getToken(2));
                final String joinLeftTableKey = parseColumnRef(token);
                token = getNextTokeSkippingWhiteSpace(getCurrentToken());
                if (token != '=') {
                    return;
                }
                token = getNextToken();
                if (token != ' ') {
                    return;
                }
                if (sqlQueryInput.charAt(this.position + 1) == ' ') {
                    token = getCharIgnoringRedundantWhitespace(this.position);
                } else {
                    token = getNextToken();
                }

                final String joinRightTableKey = parseColumnRef(token);
                token = getNextToken();
                if (token == ' ') {
                    token = getNextTokeSkippingWhiteSpace(token);
                }
                query.setJoin(new Join(joinType, joinTable, joinLeftTableKey, joinRightTableKey));
            }
        }
    }

    private boolean maybeJoins(char token) {
        return "LRFI".indexOf(token) >= 0;
    }

    private char getToken(int offset) {
        return sqlQueryInput.charAt(this.position + offset);
    }

    private void shiftPosition(int shift) {
        this.position += shift;
    }

    private char getNextTokeSkippingWhiteSpace(char token) throws Exception {
        char newToken;
        if (token == ' ' && sqlQueryInput.charAt(this.position + 1) == ' ') {
            newToken = getCharIgnoringRedundantWhitespace(this.position);
        } else {
            newToken = getNextToken();
        }

        return newToken;
    }

    private JoinType getJoinType(char token) {
        JoinType joinType = null;
        if (token == 'L') {
            try {
                checkKeyWord(token, LEFT_JOIN);
                joinType = JoinType.LEFT;
            } catch (Exception exception) {
            }
        } else if (token == 'R') {
            try {
                checkKeyWord(token, RIGHT_JOIN);
                joinType = JoinType.RIGHT;
            } catch (Exception exception) {
            }
        } else if (token == 'F') {
            try {
                checkKeyWord(token, FULL_OUTER_JOIN);
                joinType = JoinType.FULL_OUTER;
            } catch (Exception exception) {
            }
        } else if (token == 'I') {
            try {
                checkKeyWord(token, INNER_JOIN);
                joinType = JoinType.INNER;
            } catch (Exception exception) {
            }
        }

        return joinType;
    }

    private void checkWhereTableExpressions() {
        try {
            char token = getCharIgnoringRedundantWhitespace(this.position);
            if (token != QUERY_END_SYMBOL) {
                final int i = QUERY_LENGTH - WHERE.length() - 1 - 1 - 1 - 1;
                if (this.position < i && token == WHERE.charAt(0)) {
                    checkKeyWord(token, WHERE);
                    token = getCurrentToken();
                    if (token == ' ') {
                        token = getCharIgnoringRedundantWhitespace(this.position);
                    } else {
                        token = getNextToken();
                    }

                    final String s = parseExpression(token);
                    query.addWhere(s);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void checkGroupBy() throws Exception {
        if (this.position > QUERY_LENGTH - 1 - GROUP_BY.length() - 1 - ";".length()) {
            return;
        }
        char token = getCharIgnoringRedundantWhitespace(this.position);
        if (token != QUERY_END_SYMBOL) {
            final int i = QUERY_LENGTH - GROUP_BY.length() - 1 - 1 - 1 - 1;
            if (this.position <= i && token == GROUP_BY.charAt(0)) {
                int startPosition = this.position;
                checkKeyWord(token, GROUP_BY);
                token = getCurrentToken();
                if (token == ' ') {
                    token = getCharIgnoringRedundantWhitespace(this.position);
                } else {
                    token = getNextToken();
                }

                final String s = parseColumnRef(token);
                query.addGroupBy(GROUP_BY + startPosition + " " + s);
            }
        }
    }

    private void checkOrderBy() throws Exception {
        if (this.position >= QUERY_LENGTH - 1 - ORDER_BY.length() - 1 - ";".length()) {
            return;
        }
        char token = getCharIgnoringRedundantWhitespace(this.position);
        if (token != QUERY_END_SYMBOL) {
            final int i = QUERY_LENGTH - ORDER_BY.length() - 1 - 1 - 1 - 1;
            if (this.position <= i && token == ORDER_BY.charAt(0)) {
                int startPosition = this.position;
                checkKeyWord(token, ORDER_BY);
                token = sqlQueryInput.charAt(this.position);
                if (token == ' ') {
                    token = getCharIgnoringRedundantWhitespace(this.position);
                } else {
                    token = getNextToken();
                }

                StringBuilder s = new StringBuilder(parseOrderByValue(token));
                token = getCurrentToken();
                while (token == ',') {
                    token = getNextToken();
                    if (token == ' ') {
                        token = getCharIgnoringRedundantWhitespace(this.position);
                    }
                    s.append(", ").append(parseColumnRef(token));
                    token = getCurrentToken();
                    if (token == ' ') {
                        token = getCharIgnoringRedundantWhitespace(this.position);
                    }
                    if (this.position < QUERY_LENGTH - 1) {
                        break;
                    }
                }
                String result = ORDER_BY + startPosition + " " + s;
                token = getCharIgnoringRedundantWhitespace(this.position);
                if (startPosition + ORDER_BY.length() + 1 + 1 + " ASC;".length() < QUERY_LENGTH && token == 'A' &&
                        this.sqlQueryInput.charAt(this.position + 1) == 'S' &&
                        this.sqlQueryInput.charAt(this.position + 2) == 'C'
                ) {
                    this.position += ASC.length();
                    result += ASC;
                } else if (startPosition + ORDER_BY.length() + 1 + 1 + " DESC".length() < QUERY_LENGTH && token == 'D' &&
                        this.sqlQueryInput.charAt(this.position + 1) == 'E' &&
                        this.sqlQueryInput.charAt(this.position + 2) == 'S' &&
                        this.sqlQueryInput.charAt(this.position + 3) == 'C'
                ) {
                    this.position += DESC.length();
                    result += DESC;
                }

                query.addOrderBy(result);
            }
        }
    }

    private void checkLimitOffset() throws Exception {
        if (this.position > QUERY_LENGTH - 1 - ORDER_BY.length() - 1 - ";".length()) {
            return;
        }
        char token = sqlQueryInput.charAt(this.position);
        if (token != QUERY_END_SYMBOL) {
            if (token == ' ') {
                token = getCharIgnoringRedundantWhitespace(this.position);
            } else {
                token = getNextToken();
            }
            final int i = QUERY_LENGTH - LIMIT.length() - 1 - 1 - 1 - 1;
            if (this.position <= i && token == LIMIT.charAt(0)) {
                int startPosition = this.position;
                checkKeyWord(token, LIMIT);
                token = sqlQueryInput.charAt(this.position);
                if (token == ' ' && sqlQueryInput.charAt(this.position + 1) == ' ') {
                    token = getCharIgnoringRedundantWhitespace(this.position);
                } else {
                    token = getNextToken();
                }

                Limit limit = getLimit(token, startPosition);
                query.addLimit(limit);
            }
        }
    }

    private Limit getLimit(char token, int startPosition) throws Exception {
        final String parseDigit = parseDigit(token);
        final int limit = Integer.parseInt(parseDigit);
        final String offset = getOffset();
        if (!offset.isEmpty()) {
            return new Limit(limit, Integer.parseInt(offset), startPosition);
        }
        return new Limit(limit, startPosition);
    }

    private String getOffset() throws Exception {
        char token = getCurrentToken();
        if (token == ' ' && sqlQueryInput.charAt(this.position + 1) == ' ') {
            token = getCharIgnoringRedundantWhitespace(this.position);
        } else {
            token = getNextToken();
        }
        if (token == ',') {
            token = getNextToken();
            if (sqlQueryInput.charAt(this.position + 1) == ' ') {
                token = getCharIgnoringRedundantWhitespace(this.position);
            }
            return parseDigit(token);
        } else if (this.position <= QUERY_LENGTH - OFFSET.length() - 1 - 1 - 1 - 1 && token == OFFSET.charAt(0)) {
            boolean isOffset = true;
            try {
                checkKeyWord(token, OFFSET);
            } catch (Exception e) {
                isOffset = false;
            }
            if (isOffset) {
                if (token == ' ' && sqlQueryInput.charAt(this.position + 1) == ' ') {
                    token = getCharIgnoringRedundantWhitespace(this.position);
                } else {
                    token = getNextToken();
                }

                return parseDigit(token);
            }
        }

        return "";
    }

    private String parseOrderByValue(char token) throws Exception {
        StringBuilder result = new StringBuilder();
        final String value = parseDigit(token);
        if (!value.isEmpty()) {
            result.append(value);
        } else {
            final String columnRef = parseColumnRef(token);
            if (!columnRef.isEmpty()) {
                result.append(columnRef);
            }
        }

        return result.toString();
    }

    private char getCurrentToken() {
        return sqlQueryInput.charAt(this.position);
    }

    private String parseExpression(char token) throws Exception {
        if (this.position >= this.QUERY_LENGTH - 3) {
            throw new Exception("Invalid query");
        }
        StringBuilder result = new StringBuilder();
        String leftCondition = parseAndCondition(token);
        result.append(leftCondition);

        if (this.position + 5 >= QUERY_LENGTH) {
            return result.toString();
        }

        char currentToken = getNextToken();
        if (currentToken == ' ' && sqlQueryInput.charAt(position + 1) == ' ') {
            currentToken = getNextTokeSkippingWhiteSpace(currentToken);
        }
        if (currentToken == 'O' && this.sqlQueryInput.charAt(position + 1) == 'R' &&
                this.sqlQueryInput.charAt(position + 2) == ' '
        ) {
            int startPosition = this.position;
            this.position += 3;
            final char nextToken = getNextToken();
            leftCondition = parseAndCondition(nextToken);
            result.append(" ").append(OR).append(startPosition).append(' ').append(leftCondition);
        }

        return result.toString();
    }

    private String parseAndCondition(final char token) throws Exception {
        if (this.position >= this.QUERY_LENGTH - 3) {
            throw new Exception("Invalid query");
        }
        StringBuilder result = new StringBuilder();
        String leftCondition = parseCondition(token);
        result.append(leftCondition);
        if (this.position >= QUERY_LENGTH - 3) {
            return result.toString();
        }
        char currentToken = getNextToken();
        if (currentToken == ' ' && sqlQueryInput.charAt(position + 1) == ' ') {
            currentToken = getNextTokeSkippingWhiteSpace(currentToken);
        }
        if (currentToken == ' ' && this.sqlQueryInput.charAt(position + 1) == 'A' &&
                this.sqlQueryInput.charAt(position + 2) == 'N' &&
                this.sqlQueryInput.charAt(position + 3) == 'D' &&
                this.sqlQueryInput.charAt(position + 4) == ' '
        ) {
            final int startPosition = this.position;
            this.position += AND.length() + 1;
            currentToken = getNextToken();
            leftCondition = parseCondition(currentToken);
            result.append(" " + AND).append(startPosition).append(' ').append(leftCondition);
        }

        return result.toString();
    }

    private String parseCondition(char token) throws Exception {
        if (this.position >= this.QUERY_LENGTH - 3) {
            throw new Exception("Invalid query");
        }
        StringBuilder result = new StringBuilder();
        if (token == ' ') {
            token = getCharIgnoringRedundantWhitespace(this.position);
        }
        if (token == 'N' && this.sqlQueryInput.charAt(position + 1) == 'O'
                && this.sqlQueryInput.charAt(position + 2) == 'T'
        ) {
            result.append("NOT");
            String value = parseExpression(token);
            result.append(value);
        } else if (token == '(') {
            result.append(token);
            String value = parseExpression(token);
            char currentToken = getNextToken();
            if (currentToken != ')') {
                throwExpectedToken(currentToken, ")");
            }
            result.append(value);
        } else {
            int start = this.position;
            String operandFirst = parseOperand(token);
            result.append(operandFirst).append(' ');
            this.position = start + operandFirst.length();
            char currentToken = getCurrentToken();
            if (currentToken == ' ') {
                currentToken = getCharIgnoringRedundantWhitespace(this.position);
            }
            final String compareOperand = parseCompareCommand(currentToken);
            if (!compareOperand.isEmpty()) {
                currentToken = getCurrentToken();
                String operandSecond = parseOperand(currentToken);
                result.append(compareOperand).append(' ').append(operandSecond);
            } else if (currentToken == 'I') {
                final char nextToken = this.sqlQueryInput.charAt(this.position + 1);
                if (nextToken == 'S') {
                    position += 2;
                    result.append(" IS ");
                    currentToken = getCurrentToken();
                    if (currentToken == ' ') {
                        currentToken = getNextTokeSkippingWhiteSpace(currentToken);
                    }
                    if (currentToken == 'N' && this.sqlQueryInput.charAt(position + 1) == 'O'
                            && this.sqlQueryInput.charAt(position + 2) == 'T'
                            && this.sqlQueryInput.charAt(position + 3) == ' '
                    ) {
                        result.append("NOT ");
                        this.position += 3;
                        currentToken = getNextToken();
                    }
                    if (currentToken == 'N' && this.sqlQueryInput.charAt(position + 1) == 'U'
                            && this.sqlQueryInput.charAt(position + 2) == 'L'
                            && this.sqlQueryInput.charAt(position + 3) == 'L'
                            && (this.sqlQueryInput.charAt(position + 4) == ' ' || this.sqlQueryInput.charAt(position + 4) == ';')
                    ) {
                        result.append("NULL");
                        this.position += "NULL ".length();
                    } else {
                        throw new Exception("Invalid query");
                    }
                } else if (this.sqlQueryInput.charAt(this.position + 1) == 'N' &&
                        this.sqlQueryInput.charAt(this.position + 2) == ' ' &&
                        this.sqlQueryInput.charAt(this.position + 3) == '(') {
                    parseIn(result);
                }
            } else {
                if (currentToken == 'N' && this.sqlQueryInput.charAt(position + 1) == 'O'
                        && this.sqlQueryInput.charAt(position + 2) == 'T' &&
                        this.sqlQueryInput.charAt(position + 3) == ' '
                ) {
                    result.append(" NOT ");
                    this.position += 3;
                    currentToken = getNextToken();
                    if (currentToken == ' ') {
                        currentToken = getNextTokeSkippingWhiteSpace(currentToken);
                    }
                }
                int startPosition = this.position;

                if (currentToken == 'I' && this.sqlQueryInput.charAt(this.position + 1) == 'N' &&
                        this.sqlQueryInput.charAt(this.position + 2) == ' ' &&
                        this.sqlQueryInput.charAt(this.position + 3) == '(') {
                    parseIn(result);
                } else if (currentToken == 'L' && this.sqlQueryInput.charAt(this.position + 1) == 'I' &&
                        this.sqlQueryInput.charAt(this.position + 2) == 'K' &&
                        this.sqlQueryInput.charAt(this.position + 3) == 'E') {
                    this.position += 4;
                    currentToken = getNextToken();
                    final String operand = parseOperand(currentToken);
                    result.append("LIKE ").append(startPosition).append(" ").append(operand);
                } else if (currentToken == 'B' && this.sqlQueryInput.charAt(this.position + 1) == 'E' &&
                        this.sqlQueryInput.charAt(this.position + 2) == 'T' &&
                        this.sqlQueryInput.charAt(this.position + 3) == 'W' &&
                        this.sqlQueryInput.charAt(this.position + 4) == 'E' &&
                        this.sqlQueryInput.charAt(this.position + 5) == 'E' &&
                        this.sqlQueryInput.charAt(this.position + 6) == 'N' &&
                        this.sqlQueryInput.charAt(this.position + 7) == ' '
                ) {
                    this.position += BETWEEN.length();
                    currentToken = getCurrentToken();
                    String operand = parseOperand(currentToken);
                    currentToken = getCurrentToken();
                    if (currentToken == ' ') {
                        currentToken = getNextTokeSkippingWhiteSpace(currentToken);
                    }
                    result.append(BETWEEN).append(startPosition).append(" ").append(operand);
                    if (currentToken == 'A' &&
                            this.sqlQueryInput.charAt(this.position + 1) == 'N' &&
                            this.sqlQueryInput.charAt(this.position + 2) == 'D' &&
                            this.sqlQueryInput.charAt(this.position + 3) == ' '
                    ) {
                        this.position += 4;
                        currentToken = getCurrentToken();
                        operand = parseOperand(currentToken);
                        result.append(' ').append(operand);
                    } else {
                        throwExpectedToken(token, " " + AND);
                    }
                }
            }
        }
        return result.toString();
    }

    private void parseIn(StringBuilder result) throws Exception {
        char currentToken;
        int startPosition = this.position;
        this.position += 4;
        currentToken = getCurrentToken();
        StringBuilder operands = new StringBuilder();
        boolean isFirst =true;
        while (currentToken != ')' && this.position < QUERY_LENGTH - 1) {
            String operand = null;
            int start = this.position;
            try {
                operand = parseOperand(currentToken);

                currentToken = getCurrentToken();

                if (!isFirst) {
                    operands.append(", ");
                }
                if (isFirst) {
                    isFirst = false;
                }
                operands.append(operand);
            } catch (Exception exception) {
                final String message = exception.getMessage();
                if (message.substring(message.length() - 1 - 3).equals("')'")) {
                    throw new Exception(message);
                }
            }

            if (sqlQueryInput.charAt(this.position - 1) == ')') {
                this.position--;
                currentToken = getCurrentToken();
            }
            if (currentToken == ')' ||sqlQueryInput.charAt(start + operand.length() + 1) == ')' || this.position >= QUERY_LENGTH - 1) {
                break;
            }
            currentToken = this.getNextToken();
            if (currentToken == ' ') {
                currentToken = getNextTokeSkippingWhiteSpace(currentToken);
            }
        }

        if (currentToken != ')') {
            throwExpectedToken(currentToken, ")");
        }
        result.append("IN ").append(startPosition).append(" ").append(operands);
        this.position--;
    }

    private String parseCompareCommand(final char token) {
        StringBuilder result = new StringBuilder();
        if (this.position < QUERY_LENGTH - 1 - 1) {
            final int startPosition = this.position;

            OperatorType operatorType = getOperatorType(token);
            if (operatorType != null) {
                result.append(operatorType)
                        .append(' ')
                        .append(startPosition);
            }
        }
        return result.toString();
    }

    private OperatorType getOperatorType(char token) {
        final char nextToken = sqlQueryInput.charAt(this.position + 1);
        if (isCompare(token) && nextToken == ' ') {
            final OperatorType operatorType = mapComparisonOperator.get(String.valueOf(token));
            if (operatorType != null) {
                this.position += 2;
            }
            return operatorType;
        } else if (isCompare(token) && (nextToken == '>' || nextToken == '=')) {
            final char[] chars = new char[2];
            chars[0] = token;
            chars[1] = nextToken;
            final String key = new String(chars);
            if (mapComparisonOperator.containsKey(key)) {
                final OperatorType operatorType = mapComparisonOperator.get(key);
                if (operatorType != null) {
                    this.position += 3;
                }
                return operatorType;
            }
        }
        return null;
    }

    private boolean isCompare(final char token) {
        return "<>=!".indexOf(token) >= 0;
    }

    private String parseOperand(final char token) throws Exception {
        StringBuilder result = new StringBuilder();

        String factor = parseSummOperation(token);

        result.append(factor);
        if (this.position >= QUERY_LENGTH - 4) {
            return result.toString();
        }
        char nextToken = getCurrentToken();

        if (this.position < this.QUERY_LENGTH - 1 - 3) {
            if (nextToken == '|' || sqlQueryInput.charAt(this.position + 1) == '|') {
                result.append("||");
                result.append(parseSummOperation(getNextToken()));
            }
        }

        return result.toString();
    }

    private String parseSummOperation(final char token) throws Exception {
        StringBuilder result = new StringBuilder();

        String factor = parseFactor(token);
        result.append(factor);
        if (this.position >= QUERY_LENGTH - 2) {
            return result.toString();
        }
        char nextToken = getCurrentToken();

        if (nextToken == '+' || nextToken == '-') {
            result.append(nextToken);
            result.append(parseFactor(getNextToken()));
        }

        return result.toString();
    }

    private String parseFactor(final char token) throws Exception {
        StringBuilder result = new StringBuilder();
        String termValue = parseTermValue(token);
        result.append(termValue);
        char nextToken = sqlQueryInput.charAt(this.position++);

        if (nextToken == '*' || nextToken == '/') {
            result.append(nextToken);
            nextToken = getCurrentToken();
            termValue = parseTermValue(nextToken);
            result.append(termValue);
        }

        return result.toString();
    }

    private String parseTermValue(char token) throws Exception {
        StringBuilder result = new StringBuilder();
        final String value = parseValue(token);
        if (!value.isEmpty()) {
            result.append(value);
        } else {
            final String columnRef = parseColumnRef(token);
            if (!columnRef.isEmpty()) {
                result.append(columnRef);
            } else {
                final String rowValueConstructor = rowValueConstructor(token);
                if (!rowValueConstructor.isEmpty()) {
                    result.append(rowValueConstructor);
                } else {
                    if (token == '(') {
                        final String operand = parseOperand(token);
                        final char nextToken = getNextToken();
                        if (nextToken != ')') {
                            throwExpectedToken(token, ")");
                        }
                        result.append(operand);
                    }
                }
            }
        }

        return result.toString();
    }

    private String rowValueConstructor(char token) throws Exception {
        if (token == '(') {
            StringBuilder result = new StringBuilder();
            token = getNextToken();
            result.append(parseValue(token));
            if (token != ',') {
                throwInvalidToken(token, position);
                throwExpectedToken(token, ",");
            }
            result.append(parseValue(token));
            if (token != ')') {
                return throwExpectedToken(token, ")");
            }
            return result.toString();
        }
        return "";
    }

    private String throwExpectedToken(char token, String expectedToken) throws Exception {
        throw new Exception("Invalid token '" + token + "' at " + this.position + " position. Expected '" + expectedToken + "'");
    }

    private String parseColumnRef(char token) throws Exception {
        if (isAlphabetCharacter(token) || token == '`') {
            StringBuilder result = new StringBuilder();
            result.append(parseName(token));
            token = this.getCurrentToken();
            if (token == '.') {
                token = getNextToken();
                final String str = parseName(token);
                if (!str.isEmpty()) {
                    result.append('.');
                    result.append(str);
                }
            }
            return result.toString();
        }

        return "";
    }

    private String parseName(char token) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean isQuoted = false;
        if (token == '`') {
            isQuoted = true;
            token = getNextToken();
        }
        do {
            if (!isQuoted && isKeyword(result.toString())) {
            }
            result.append(token);
            token = getNextToken();
        } while (isAlphabetCharacter(token) || isDigit(token) || token == '_');
        if (isQuoted && token != '`') {
            throwInvalidToken(token, this.position);
        }

        return result.toString();
    }

    private boolean isKeyword(String toString) {
        return false;
    }

    private String parseValue(char token) throws Exception {
        StringBuilder resultValue = new StringBuilder();
        int startPosition = this.position;

        if (token == STRING_QUOTED_SYMBOL) {
            resultValue.append(parseString(token)).append(STRING_QUOTED_SYMBOL);
            this.position++;
        } else if (isDigit(token)) {
            resultValue.append(parseDigit(token));
        } else if (this.position < this.QUERY_LENGTH - 1 - 4 && token == 'T') {
            final char charAt = sqlQueryInput.charAt(this.position + 1);
            if (charAt != 'R' &&
                    sqlQueryInput.charAt(this.position + 2) != 'U' &&
                    sqlQueryInput.charAt(this.position + 3) != 'E'
            ) {
                throwInvalidToken(charAt, position);
            }
            resultValue.append("TRUE");
        } else if (this.position < this.QUERY_LENGTH - 1 - 5 && token == 'F') {
            if (sqlQueryInput.charAt(this.position + 1) != 'A' &&
                    sqlQueryInput.charAt(this.position + 2) != 'L' &&
                    sqlQueryInput.charAt(this.position + 3) != 'S' &&
                    sqlQueryInput.charAt(this.position + 4) != 'E'
            ) {
                throwInvalidToken(token, position);
            }
            resultValue.append("FALSE");
        } else if (this.position < this.QUERY_LENGTH - 1 - 4 && token == 'N') {
            if (sqlQueryInput.charAt(this.position + 1) != 'U' &&
                    sqlQueryInput.charAt(this.position + 2) != 'L' &&
                    sqlQueryInput.charAt(this.position + 3) != 'L'
            ) {
                throwInvalidToken(token, position);
            }
            resultValue.append("NULL ").append(startPosition);
        }

        return resultValue.toString();
    }

    private String parseString(char token) throws Exception {
        StringBuilder result = new StringBuilder();
        int startPosition = this.position;
        do {
            result.append(token);
            token = getNextToken();
        } while (token != STRING_QUOTED_SYMBOL && this.position < this.QUERY_LENGTH - 2);
        if (this.position == this.QUERY_LENGTH) {
            throw new Exception("Unclosed string value started at " + startPosition + " position");
        }
        return result.toString();
    }

    private String parseDigit(char token) throws Exception {
        if (isDigit(token)) {
            StringBuilder result = new StringBuilder();
            do {
                result.append(token);
                token = getNextToken();
            } while (isDigit(token) && this.position < this.QUERY_LENGTH - 2);
            if ((token != ' ') && this.position == this.QUERY_LENGTH - 2) {
                throw new Exception("Invalid digit. Position " + this.position);
            }
            return result.toString();
        }

        return "";
    }


    private void checkKeyWord(char charAt, String from) throws Exception {
        if (charAt == from.charAt(0)) {
            final int startPosition = this.position;
            this.position++;
            for (int i = 1; i < from.length() - 1; position++, i++) {
                if (sqlQueryInput.charAt(position) != from.charAt(i)) {
                    final int invalidPosition = this.position;
                    this.position = startPosition;
                    throwInvalidToken(charAt, invalidPosition);
                }
            }
            charAt = sqlQueryInput.charAt(position);
            if (charAt != ' ') {
                final int invalidPosition = this.position;
                this.position = startPosition;
                throwInvalidToken(charAt, invalidPosition);
            }
        }
    }

    private String getAggregateColumn() throws Exception {
        StringBuilder aggregateColumns = new StringBuilder();
        boolean isQuotedName = false;
        char charAt = getCharIgnoringRedundantWhitespace(position);
        if (charAt == ' ') {
            charAt = getNextToken();
        }
        if (charAt == '`') {
            isQuotedName = true;
            charAt = getNextToken();
            if (charAt == ' ') {
                throwInvalidToken(charAt, position);
            }
        }
        if (isAlphabetCharacter(charAt)) {
            do {
                aggregateColumns.append(charAt);
                charAt = getNextToken();
            } while (isAlphabetCharacter(charAt) || isDigit(charAt) || charAt == '_');
            if (charAt == '.') {
                aggregateColumns.append(charAt);
                charAt = getNextToken();

                do {
                    aggregateColumns.append(charAt);
                    charAt = getNextToken();
                } while (isAlphabetCharacter(charAt) || isDigit(charAt) || charAt == '_');
            }
            if (charAt == '`' && !isQuotedName) {
                throwInvalidToken(charAt, position);
            } else if (isQuotedName && charAt != '`') {
                throwInvalidToken(charAt, position);
            }

            if (isQuotedName) {
                charAt = getNextToken();
            }

            if (charAt == ' ') {
                if (position + 3 < sqlQueryInput.length() - 1) {
                    charAt = getNextToken();
                    charAt = getCharIgnoringRedundantWhitespace(charAt);
                    final char charAt1 = sqlQueryInput.charAt(position + 1);
                    final char charAt2 = sqlQueryInput.charAt(position + 2);

                    if (charAt == 'A' || charAt == 'a' && charAt1 == 'S' || charAt1 == 's' && charAt2 == ' ') {
                        aggregateColumns.append(" AS ");
                        this.position += 3;
                        charAt = getCharIgnoringRedundantWhitespace(this.position);
                        if (isAlphabetCharacter(charAt)) {
                            do {
                                aggregateColumns.append(charAt);
                                charAt = getNextToken();
                            } while (isAlphabetCharacter(charAt) || isDigit(charAt) || charAt == '_');
                        }
                    }
                }
            }
        }

        return aggregateColumns.toString();
    }

    private char getCharIgnoringRedundantWhitespace(char charAt) throws Exception {
        while (charAt == ' ') {
            charAt = getNextToken();
        }
        return charAt;
    }

    private char getCharIgnoringRedundantWhitespace(int position) throws Exception {
        char charAt = sqlQueryInput.charAt(position);
        if (charAt == ' ' && position + 1 < sqlQueryInput.length() - 2) {
            charAt = getNextToken();
            if (sqlQueryInput.charAt(position + 1) == ' ') {
                while (charAt == ' ') {
                    charAt = getNextToken();
                }
            }
        }

        return charAt;
    }

    private char getNextToken() throws Exception {
        if (this.position == this.QUERY_LENGTH - 1) {
            throw new Exception("Invalid query");
        }
        position++;
        return sqlQueryInput.charAt(position);
    }

    private void throwInvalidToken(char charAt, int position) throws Exception {
        throw new Exception("Invalid character '" + charAt + "' at position " + position);
    }

    private boolean isAlphabetCharacter(char character) {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".indexOf(character) >= 0;
    }

    private boolean isDigit(char character) {
        return "0123456789".indexOf(character) >= 0;
    }
}
