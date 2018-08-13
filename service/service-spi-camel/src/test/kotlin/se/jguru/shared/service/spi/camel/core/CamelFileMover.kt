package se.jguru.shared.service.spi.camel.core

import org.apache.camel.builder.RouteBuilder

const val ROUTEID = "fileMover"

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class CamelFileMover(resourcePath: String = "camelTestDir", baseDir : String) : RouteBuilder() {

    // Internal state
    val inboxResourcePath: String = "$baseDir/$resourcePath/inbox"
    val outboxResourcePath: String = "$baseDir/$resourcePath/outbox"
    val inboxURI : String get() = toFileURI(inboxResourcePath)
    val outboxURI : String get() = toFileURI(outboxResourcePath)

    override fun configure() {
        from(inboxURI).id(ROUTEID).to(outboxURI);
    }

    //
    // Private helpers
    //

    private fun toFileURI(resourcePath: String) = "file://$resourcePath"
}