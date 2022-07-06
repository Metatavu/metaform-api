package fi.metatavu.metaform.server.test.functional.unit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.metatavu.metaform.api.spec.model.FieldRule;
import fi.metatavu.metaform.api.spec.model.Reply;

import fi.metatavu.metaform.server.metaform.FieldRuleEvaluator;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for field rule evaluator
 * 
 * @author Antti Leppä
 */
public class FieldRuleEvaluatorTests {

  @Test
  public void testEquals() throws IOException, URISyntaxException {
    FieldRuleEvaluator fieldRuleEvaluator = new FieldRuleEvaluator();

    FieldRule rule = createRule("field1", "true", null);

    assertTrue(fieldRuleEvaluator.evaluate(rule, createSingleFieldReply("field1", "true")));
    assertFalse(fieldRuleEvaluator.evaluate(rule, createSingleFieldReply("field1", "false")));
    assertFalse(fieldRuleEvaluator.evaluate(rule, createSingleFieldReply("field2", "true")));
  }
  
  @Test
  public void testNotEquals() throws IOException, URISyntaxException {
    FieldRuleEvaluator fieldRuleEvaluator = new FieldRuleEvaluator();

    FieldRule rule = createRule("field1", null, "true");

    assertFalse(fieldRuleEvaluator.evaluate(rule, createSingleFieldReply("field1", "true")));
    assertTrue(fieldRuleEvaluator.evaluate(rule, createSingleFieldReply("field1", "false")));
    assertTrue(fieldRuleEvaluator.evaluate(rule, createSingleFieldReply("field2", "true")));
  }
  
  @Test
  public void testAndOrs() throws IOException, URISyntaxException {
    FieldRuleEvaluator fieldRuleEvaluator = new FieldRuleEvaluator();

    FieldRule andRule = createRule("field1", "true", null, Collections.singletonList(createRule("field2", "true", null)), null);
    FieldRule orRule = createRule("field1", "true", null, null, Collections.singletonList(createRule("field2", "true", null)));

    assertTrue(fieldRuleEvaluator.evaluate(andRule, createTwoFieldReply("field1", "true", "field2", "true")));
    assertFalse(fieldRuleEvaluator.evaluate(andRule, createTwoFieldReply("field1", "true", "field2", "false")));
    assertFalse(fieldRuleEvaluator.evaluate(andRule, createTwoFieldReply("field1", "false", "field2", "true")));
    assertFalse(fieldRuleEvaluator.evaluate(andRule, createTwoFieldReply("field1", "false", "field2", "false")));

    assertTrue(fieldRuleEvaluator.evaluate(orRule, createTwoFieldReply("field1", "true", "field2", "true")));
    assertTrue(fieldRuleEvaluator.evaluate(orRule, createTwoFieldReply("field1", "true", "field2", "false")));
    assertTrue(fieldRuleEvaluator.evaluate(orRule, createTwoFieldReply("field1", "false", "field2", "true")));
    assertFalse(fieldRuleEvaluator.evaluate(orRule, createTwoFieldReply("field1", "false", "field2", "false")));
  }
  
  /**
   * Creates reply with single field
   * 
   * @param field field
   * @param value value
   * @return created reply
   */
  private Reply createSingleFieldReply(String field, String value) {
    Reply reply = new Reply();
    Map<String, Object> data = new HashMap<>();
    data.put(field, value);
    return new Reply(
            null,
            null,
            null,
            null,
            null,
            null,
            data
    );
  }

  /**
   * Creates reply with two fields
   * 
   * @param field1 field 1 name
   * @param value1 field 1 value
   * @param field2 field 2 name
   * @param value2 field 2 value
   * @return created reply
   */
  private Reply createTwoFieldReply(String field1, String value1, String field2, String value2) {
    Map<String, Object> data = new HashMap<>();
    data.put(field1, value1);
    data.put(field2, value2);
    return new Reply(
            null,
            null,
            null,
            null,
            null,
            null,
            data
    );
  }

  /**
   * Creates a rule
   * 
   * @param field field name
   * @param equals equals value
   * @param notEquals not equals value
   * @return created rule
   */
  private FieldRule createRule(String field, String equals, String notEquals) {
    return createRule(field, equals, notEquals, null, null);
  }
  
  /**
   * Creates a rule
   * 
   * @param field field name
   * @param equals equals value
   * @param notEquals not equals value
   * @param ands list of ands or null if none defined
   * @param ors list of ors or null if none defined
   * @return created rule
   */
  private FieldRule createRule(String field, String equals, String notEquals, List<FieldRule> ands, List<FieldRule> ors) {
    return new FieldRule(
            field,
            equals,
            notEquals,
            ands,
            ors
    );
  }

  
}
