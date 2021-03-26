package fi.metatavu.metaform.server.metaforms;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import fi.metatavu.metaform.api.spec.model.FieldRule;
import fi.metatavu.metaform.api.spec.model.Reply;
import org.apache.commons.lang3.StringUtils;


/**
 * Field rule evaluator
 * 
 * @author Antti Leppä
 */
public class FieldRuleEvaluator {

  /**
   * Evaluates field rule
   * 
   * @param rule  rule
   * @param reply reply data
   * @return evaluation result
   */
  public boolean evaluate(FieldRule rule, Reply reply) {
    Map<String, Object> data = reply.getData();
    String field = rule.getField();
    Object fieldData = data.get(field);

    boolean result = false;
    String fieldValue = Objects.toString(fieldData, null);
    String ruleEquals = rule.getEquals();
    String ruleNotEquals = rule.getNotEquals();

    List<FieldRule> andRules = rule.getAnd();
    List<FieldRule> orRules = rule.getOr();

    if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(ruleEquals)) {
      result = StringUtils.equals(ruleEquals, fieldValue);
    }

    if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(ruleNotEquals)) {
      result = !StringUtils.equals(ruleNotEquals, fieldValue);
    }

    if (andRules != null) {
      for (int i = 0; i < andRules.size(); i++) {
        if (!evaluate(andRules.get(i), reply)) {
          return false;
        }
      }
    }

    if (orRules != null) {
      for (int i = 0; i < orRules.size(); i++) {
        if (evaluate(orRules.get(i), reply)) {
          return true;
        }
      }
    }

    return result;
  }

}
