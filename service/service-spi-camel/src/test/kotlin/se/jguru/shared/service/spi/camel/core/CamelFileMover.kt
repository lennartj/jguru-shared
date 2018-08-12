package se.jguru.shared.service.spi.camel.core

import org.apache.camel.builder.RouteBuilder

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class CamelFileMover(resourcePath: String = "camelTestDir",
                     baseDir : String) : RouteBuilder() {

    // Internal state
    val inboxResourcePath: String = "$baseDir/$resourcePath/inbox"
    val outboxResourcePath: String = "$baseDir/$resourcePath/outbox"
    val inboxURI : String get() = toFileURI(inboxResourcePath)
    val outboxURI : String get() = toFileURI(outboxResourcePath)

    private fun toFileURI(resourcePath: String) = "file://$resourcePath"

    override fun configure() {
        from(inboxURI).to(outboxURI);
    }

    /*
    override fun toString(): String {
        return "CamelFileMover(inboxResourcePath='$inboxResourcePath', outboxResourcePath='$outboxResourcePath')"
    }
    */
}