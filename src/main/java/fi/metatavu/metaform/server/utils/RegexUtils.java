package fi.metatavu.metaform.server.utils;

import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regex utils
 * 
 * @author Antti Leppä
 */
public class RegexUtils {

  /**
   * Replaces every subsequence of the input sequence that matches the pattern with the given replacement function
   * 
   * @param pattern pattern
   * @param input input string
   * @param replacement replacement function
   * @return replaced string
   */
  public static String replaceAll(Pattern pattern, String input, Function<MatchResult, String> replacement) {
    Matcher matcher = pattern.matcher(input);
    
    boolean result = matcher.find();
    
    if (result) {
      StringBuffer sb = new StringBuffer();

      do {
        matcher.appendReplacement(sb, replacement.apply(matcher.toMatchResult()));
        result = matcher.find();
      } while (result);

      matcher.appendTail(sb);
      return sb.toString();
    }

    return input;
  }
  
}
