package com.sqlparser.service;

import com.sqlparser.model.Join;
import com.sqlparser.model.JoinType;
import com.sqlparser.model.Limit;
import com.sqlparser.model.Query;

public class SqlAnalyzer {
    public static final char ALL_COLUMNS_CHAR = '*';
    public static final char WHITESPACE = ' ';

    private final static String SELECT = "SELECT ";
    private final static String FROM = "FROM ";
    private static final String WHERE = "WHERE ";
    private static final String GROUP_BY = "GROUP BY ";
    private static final String ORDER_BY = "ORDER BY ";
    private static final String LIMIT = "LIMIT ";
    private static final String OFFSET = "OFFSET ";
    public static final String ASC = " ASC";
    public static final String LEFT_JOIN = "LEFT JOIN ";
    public static final String RIGHT_JOIN = "RIGHT JOIN ";
    public static final String INNER_JOIN = "INNER JOIN ";
    public static final String FULL_OUTER_JOIN = "FULL OUTER JOIN ";
    public static final char QUERY_END_SYMBOL = ';';

    private final String sqlQueryInput;
    private final Query query = new Query();
    private int position = 0;
    private final int QUERY_LENGTH;
    public static final String DESC = " DESC";

    public SqlAnalyzer(String sqlQueryInput) {
        this.sqlQueryInput = sqlQueryInput;
        this.QUERY_LENGTH = sqlQueryInput.length();
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
        checkGroupBy();
        checkOrderBy();
        checkLimitOffset();

        return query;
    }

    private void checkJoinTable() throws Exception {
        char token = sqlQueryInput.charAt(this.position);
        if (token != QUERY_END_SYMBOL) {
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
            query.setJoin(new Join(joinType, joinTable, joinLeftTableKey, joinRightTableKey));
        }
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

    private void checkLimitOffset() throws Exception {
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

    private void checkOrderBy() throws Exception {
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

    private void checkGroupBy() throws Exception {
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

    private String parseExpression(char token) throws Exception {
        if (this.position >= this.QUERY_LENGTH - 3) {
            throw new Exception("Invalid query");
        }
        StringBuilder result = new StringBuilder();
        String leftCondition = parseAndCondition(token);
        result.append(leftCondition);

        if (token == ' ' && this.sqlQueryInput.charAt(position + 1) == 'O' &&
                this.sqlQueryInput.charAt(position + 2) == 'R' &&
                this.sqlQueryInput.charAt(position + 3) == ' '
        ) {
            this.position += 4;
            final char nextToken = getNextToken();
            leftCondition = parseAndCondition(nextToken);
            result.append(leftCondition);
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

        if (token == ' ' && this.sqlQueryInput.charAt(position + 1) == 'A' &&
                this.sqlQueryInput.charAt(position + 2) == 'N' &&
                this.sqlQueryInput.charAt(position + 3) == 'D' &&
                this.sqlQueryInput.charAt(position + 4) == ' '
        ) {
            this.position += 5;
            final char nextToken = getNextToken();
            leftCondition = parseCondition(nextToken);
            result.append(leftCondition);
        }

        return result.toString();
    }

    private String parseCondition(final char token) throws Exception {
        if (this.position >= this.QUERY_LENGTH - 3) {
            throw new Exception("Invalid query");
        }
        StringBuilder result = new StringBuilder();

        if (token == 'N' && this.sqlQueryInput.charAt(position + 1) == 'O'
                && this.sqlQueryInput.charAt(position + 2) == 'T'
        ) {
            result.append("NOT");

            String value = parseExpression(token);
            result.append(value);
        } else if (token == '(') {
            result.append(token);
            String value = parseExpression(token);
            final char nextToken = getNextToken();
            if (nextToken != ')') {
                throwExpectedToken(nextToken, ")");
            }
            result.append(value);
        } else {
            String operandFirst = parseOperand(token);
            result.append(operandFirst);
            char nextToken = getNextToken();
            if (nextToken == ' ') {
                nextToken = getCharIgnoringRedundantWhitespace(this.position);
            }
            if (nextToken == 'I') {
                if (this.sqlQueryInput.charAt(this.position + 1) == 'S') {
                    result.append(" IS ");
                    nextToken = getNextToken();
                    if (nextToken == 'N' && this.sqlQueryInput.charAt(position + 1) == 'O'
                            && this.sqlQueryInput.charAt(position + 2) == 'T'
                    ) {
                        result.append("NOT");
                        this.position += 2;
                        nextToken = getNextToken();
                    }
                    if (nextToken == 'N' && this.sqlQueryInput.charAt(position + 1) == 'U'
                            && this.sqlQueryInput.charAt(position + 2) == 'L'
                            && this.sqlQueryInput.charAt(position + 3) == 'L'
                    ) {
                        result.append("NULL");
                    } else {
                        throw new Exception("Invalid query");
                    }
                }
            } else {
                if (nextToken == 'N' && this.sqlQueryInput.charAt(position + 1) == 'O'
                        && this.sqlQueryInput.charAt(position + 2) == 'T'
                ) {
                    result.append("NOT");
                    this.position += 2;
                    nextToken = getNextToken();
                }
                int startPosition = this.position;

                if (nextToken == 'I' && this.sqlQueryInput.charAt(this.position + 1) == 'N' &&
                        this.sqlQueryInput.charAt(this.position + 2) == ' ' &&
                        this.sqlQueryInput.charAt(this.position + 3) == '(') {
                    this.position += 4;
                    nextToken = getNextToken();
                    final String s = parseSummOperation(nextToken);
                    nextToken = this.getCurrentToken();
                    if (nextToken != ')') {
                        throwExpectedToken(nextToken, ")");
                    }
                    result.append("IN ").append(startPosition).append(" ").append(s);

                } else if (nextToken == 'L' && this.sqlQueryInput.charAt(this.position + 1) == 'I' &&
                        this.sqlQueryInput.charAt(this.position + 2) == 'K' &&
                        this.sqlQueryInput.charAt(this.position + 3) == 'E') {
                    this.position += 4;
                    nextToken = getNextToken();
                    final String value = parseOperand(nextToken);
                    result.append("LIKE ").append(startPosition).append(" ").append(value);
                } else if (nextToken == 'B' && this.sqlQueryInput.charAt(this.position + 1) == 'E' &&
                        this.sqlQueryInput.charAt(this.position + 2) == 'T' &&
                        this.sqlQueryInput.charAt(this.position + 3) == 'W' &&
                        this.sqlQueryInput.charAt(this.position + 4) == 'E' &&
                        this.sqlQueryInput.charAt(this.position + 5) == 'E' &&
                        this.sqlQueryInput.charAt(this.position + 6) == 'N' &&
                        this.sqlQueryInput.charAt(this.position + 7) == ' '
                ) {

                    this.position += 8;
                    nextToken = getNextToken();
                    String s = parseOperand(nextToken);
                    nextToken = this.getCurrentToken();
                    result.append("BETWEEN ").append(startPosition).append(" ").append(s);
                    if (nextToken == ' ' && this.sqlQueryInput.charAt(this.position + 1) == 'A' &&
                            this.sqlQueryInput.charAt(this.position + 2) == 'N' &&
                            this.sqlQueryInput.charAt(this.position + 3) == 'D' &&
                            this.sqlQueryInput.charAt(this.position + 4) == ' '
                    ) {
                        this.position += 5;
                        nextToken = getNextToken();
                        s = parseOperand(nextToken);
                        result.append(s);
                    } else {
                        throwExpectedToken(token, " AND ");
                    }
                }
            }


        }
        return result.toString();
    }

    private String parseOperand(final char token) throws Exception {
        StringBuilder result = new StringBuilder();

        String factor = parseSummOperation(token);

        result.append(factor);
        char nextToken = getNextToken();

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
            result.append(token);
            token = getNextToken();
        } while (isAlphabetCharacter(token) || isDigit(token) || token == '_');
        if (isQuoted && token != '`') {
            throwInvalidToken(token, this.position);
        }

        return result.toString();
    }

    private String parseValue(char token) throws Exception {
        StringBuilder resultValue = new StringBuilder();
        int startPosition = this.position;

        if (token == '"') {
            resultValue.append(parseString(token));
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
        } while (token != '"' && this.position < this.QUERY_LENGTH - 2);
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
            if (token != ' ' && this.position == this.QUERY_LENGTH - 2) {
                throw new Exception("Invalid digit. Position " + this.position);
            }
            return result.toString();
        }

        return "";
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

    private void checkKeyWord(char charAt, String from) throws Exception {
        if (charAt == from.charAt(0)) {
            this.position++;
            for (int i = 1; i < from.length() - 1; position++, i++) {
                if (sqlQueryInput.charAt(position) != from.charAt(i)) {
                    throwInvalidToken(charAt, position);
                }
            }
            charAt = sqlQueryInput.charAt(position);
            if (charAt != ' ') {
                throwInvalidToken(charAt, position);
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
            } while (isAlphabetCharacter(charAt) || isDigit(charAt));
            if (charAt == '.') {
                aggregateColumns.append(charAt);
                charAt = getNextToken();

                do {
                    aggregateColumns.append(charAt);
                    charAt = getNextToken();
                } while (isAlphabetCharacter(charAt) || isDigit(charAt));
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
                            } while (isAlphabetCharacter(charAt) || isDigit(charAt));
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
            if (sqlQueryInput.charAt(position + 1) == ' ') {
                charAt = getNextToken();
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
