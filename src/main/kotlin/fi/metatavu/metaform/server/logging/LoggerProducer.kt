package fi.metatavu.pickway.cloud.api.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.enterprise.context.Dependent
import javax.enterprise.inject.Produces
import javax.enterprise.inject.spi.InjectionPoint

/**
 * Producer for Logger object
 *
 * @author Tianxing Wu
 */
@Dependent
class LoggerProducer {

    /**
     * Producer for Logger object
     *
     * @param injectionPoint injection point
     * @return Logger
     */
    @Produces
    fun produceLog(injectionPoint: InjectionPoint): Logger {
        return LoggerFactory.getLogger(injectionPoint.member.declaringClass.name)
    }
}
