package com.library.application.search;

import com.library.domain.book.Genre;
import com.library.domain.search.*;

import java.util.Arrays;
import java.util.List;

public final class BookQueryParser {
    private final List<String> tokens;
    private int pos = 0;

    private BookQueryParser(List<String> tokens){
        this.tokens = tokens;
    }

    public static BookExpression parse(String query){
        List<String> tokens = tokenize(query);
        if(tokens.isEmpty()){
            throw new QuerySyntaxException("Upit je prazan");
        }

        var parser = new BookQueryParser(tokens);
        BookExpression expression = parser.parseOr();
        if(parser.pos < tokens.size()){
            throw new QuerySyntaxException("Visak u upitu: \"" + tokens.get(parser.pos));
        }

        return expression;
    }

    private static List<String> tokenize(String query){
        String spaced = query.replace("(", " ( ").replace(")", " ) ").trim();
        if(spaced.isEmpty()){
            return List.of();
        }
        return Arrays.asList(spaced.split("\\s+"));
    }

    private BookExpression parseOr()
    {
        BookExpression left = parseAnd();
        while(accept("OR")){
            left = new Or(left, parseNot());
        }
        return left;
    }

    private BookExpression parseAnd() {
        BookExpression left = parseNot();
        while(accept("AND")){
            left = new And(left, parseNot());
        }
        return left;
    }

    private BookExpression parseNot()
    {
        if(accept("NOT")){
            return new Not(parseNot());
        }

        return parsePrimary();
    }

    private BookExpression parsePrimary() {
        if (accept("(")) {
            BookExpression inner = parseOr();
            if (!accept(")")) {
                throw new QuerySyntaxException("Nedostaje zatvorena zagrada");
            }
            return inner;
        }
        return parseCondition(next());
    }

    private BookExpression parseCondition(String token){
        String lower = token.toLowerCase();

        if (lower.equals("available"))   return new IsAvailable();
        if (lower.startsWith("author:")) return new AuthorContains(value(token, "author:"));
        if (lower.startsWith("title:"))  return new TitleContains(value(token, "title:"));
        if (lower.startsWith("genre:"))  return new GenreIs(genre(value(token, "genre:")));
        if (lower.startsWith("pages>"))  return new PagesGreaterThan(number(value(token, "pages>")));
        if (lower.startsWith("pages<"))  return new PagesLessThan(number(value(token, "pages<")));

        throw new QuerySyntaxException("Nepoznat uslov: \"" + token + "\"");
    }

    private static String value(String token, String prefix){
        String value = token.substring(prefix.length());
        if(value.isEmpty()){
            throw new QuerySyntaxException("Nedostaje vrednost posle \"" + prefix + "\"");
        }
        return value;
    }

    private static Genre genre(String value){
        try {
            return Genre.valueOf(value.toUpperCase());
        }catch (IllegalArgumentException e){
            throw new QuerySyntaxException("Nepoznat žanr: \"" + value + "\". Dozvoljeni: "
                    + Arrays.toString(Genre.values()));
        }
    }

    private static int number(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new QuerySyntaxException("Očekivan broj, a dobijeno: \"" + value + "\"");
        }
    }

    // ---------- pomoćne ----------

    /** Ako je sledeći token očekivana reč, preuzmi ga i vrati true. */
    private boolean accept(String expected) {
        if (pos < tokens.size() && tokens.get(pos).equalsIgnoreCase(expected)) {
            pos++;
            return true;
        }
        return false;
    }

    private String next() {
        if (pos >= tokens.size()) {
            throw new QuerySyntaxException("Upit se neočekivano završio");
        }
        return tokens.get(pos++);
    }
}
