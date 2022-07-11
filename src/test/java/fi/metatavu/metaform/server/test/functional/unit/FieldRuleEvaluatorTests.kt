package fi.metatavu.metaform.server.test.functional.unit

import fi.metatavu.metaform.api.spec.model.FieldRule
import fi.metatavu.metaform.api.spec.model.Reply
import fi.metatavu.metaform.server.metaform.FieldRuleEvaluator
import org.junit.Assert
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.URISyntaxException

/**
 * Unit tests for field rule evaluator
 *
 * @author Antti Leppä
 */
class FieldRuleEvaluatorTests {
    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun testEquals() {
        val fieldRuleEvaluator = FieldRuleEvaluator()
        val rule = createRule("field1", "true", null)
        Assert.assertTrue(fieldRuleEvaluator.evaluate(rule, createSingleFieldReply("field1", "true")))
        Assert.assertFalse(fieldRuleEvaluator.evaluate(rule, createSingleFieldReply("field1", "false")))
        Assert.assertFalse(fieldRuleEvaluator.evaluate(rule, createSingleFieldReply("field2", "true")))
    }

    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun testNotEquals() {
        val fieldRuleEvaluator = FieldRuleEvaluator()
        val rule = createRule("field1", null, "true")
        Assert.assertFalse(fieldRuleEvaluator.evaluate(rule, createSingleFieldReply("field1", "true")))
        Assert.assertTrue(fieldRuleEvaluator.evaluate(rule, createSingleFieldReply("field1", "false")))
        Assert.assertTrue(fieldRuleEvaluator.evaluate(rule, createSingleFieldReply("field2", "true")))
    }

    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun testAndOrs() {
        val fieldRuleEvaluator = FieldRuleEvaluator()
        val andRule = createRule("field1", "true", null, listOf(createRule("field2", "true", null)), null)
        val orRule = createRule("field1", "true", null, null, listOf(createRule("field2", "true", null)))
        Assert.assertTrue(fieldRuleEvaluator.evaluate(andRule, createTwoFieldReply("field1", "true", "field2", "true")))
        Assert.assertFalse(fieldRuleEvaluator.evaluate(andRule, createTwoFieldReply("field1", "true", "field2", "false")))
        Assert.assertFalse(fieldRuleEvaluator.evaluate(andRule, createTwoFieldReply("field1", "false", "field2", "true")))
        Assert.assertFalse(fieldRuleEvaluator.evaluate(andRule, createTwoFieldReply("field1", "false", "field2", "false")))
        Assert.assertTrue(fieldRuleEvaluator.evaluate(orRule, createTwoFieldReply("field1", "true", "field2", "true")))
        Assert.assertTrue(fieldRuleEvaluator.evaluate(orRule, createTwoFieldReply("field1", "true", "field2", "false")))
        Assert.assertTrue(fieldRuleEvaluator.evaluate(orRule, createTwoFieldReply("field1", "false", "field2", "true")))
        Assert.assertFalse(fieldRuleEvaluator.evaluate(orRule, createTwoFieldReply("field1", "false", "field2", "false")))
    }

    /**
     * Creates reply with single field
     *
     * @param field field
     * @param value value
     * @return created reply
     */
    private fun createSingleFieldReply(field: String, value: String): Reply {
        val data: MutableMap<String, Any> = HashMap()
        data[field] = value
        return Reply(
                null,
                null,
                null,
                null,
                null,
                null,
                data
        )
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
    private fun createTwoFieldReply(field1: String, value1: String, field2: String, value2: String): Reply {
        val data: MutableMap<String, Any> = HashMap()
        data[field1] = value1
        data[field2] = value2
        return Reply(
                null,
                null,
                null,
                null,
                null,
                null,
                data
        )
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
    private fun createRule(field: String, equals: String?, notEquals: String?, ands: List<FieldRule>? = null, ors: List<FieldRule>? = null): FieldRule {
        return FieldRule(
                field,
                equals,
                notEquals,
                ands,
                ors
        )
    }
}