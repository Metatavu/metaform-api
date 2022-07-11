package fi.metatavu.metaform.server.metaform

import fi.metatavu.metaform.api.spec.model.FieldRule
import fi.metatavu.metaform.api.spec.model.Reply
import org.apache.commons.lang3.StringUtils
import java.util.*

/**
 * Field rule evaluator
 *
 * @author Antti Leppä
 */
class FieldRuleEvaluator {
    /**
     * Evaluates field rule
     *
     * @param rule rule
     * @param reply reply data
     * @return evaluation result
     */
    fun evaluate(rule: FieldRule, reply: Reply): Boolean {
        val data = reply.data
        val field = rule.field
        val fieldData = data?.get(field)
        var result = false
        val fieldValue = Objects.toString(fieldData, null)
        val ruleEquals = rule.equals
        val ruleNotEquals = rule.notEquals
        if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(ruleEquals)) {
            result = StringUtils.equals(ruleEquals, fieldValue)
        }
        if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(ruleNotEquals)) {
            result = !StringUtils.equals(ruleNotEquals, fieldValue)
        }
        rule.and?.forEach { andRule ->
            if (!evaluate(andRule, reply)) {
                return false
            }
        }

        rule.or?.forEach { orRule ->
            if (evaluate(orRule, reply)) {
                return true
            }
        }
        return result
    }
}
