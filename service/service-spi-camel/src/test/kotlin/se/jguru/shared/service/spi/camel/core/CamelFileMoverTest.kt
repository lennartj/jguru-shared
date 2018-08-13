package se.jguru.shared.service.spi.camel.core

import org.apache.camel.Exchange
import org.apache.camel.RoutesBuilder
import org.apache.camel.builder.NotifyBuilder
import org.apache.camel.test.junit4.CamelTestSupport
import org.junit.Assert
import org.junit.Test
import java.io.File

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
open class CamelFileMoverTest : CamelTestSupport() {

    // Shared state
    lateinit var baseDir: String
    lateinit var fileMover: CamelFileMover

    override fun setUp() {

        // #1) Find the path to the target directory of this project
        //     ... even if we are running within an IDE.
        //
        val resourcePath = "testdata/pointOfOrigin.txt"
        val url = Thread.currentThread().contextClassLoader.getResource(resourcePath)
        Assert.assertNotNull("Could not find resource path $resourcePath", url)

        val baseDirectory = File(url.path)
            .parentFile // testdata
            .parentFile // test-classes
            .parentFile // target
        Assert.assertTrue(baseDirectory.exists() && baseDirectory.isDirectory)

        baseDir = baseDirectory.absolutePath
        fileMover = CamelFileMover(baseDir = this.baseDir)

        // #2) While the documentation indicates the opposite,
        //     Camel File Producer seems to require that the outboxdirectory exists.
        //
        val outboxDirectory = File(fileMover.outboxResourcePath)
        val inboxDirectory = File(fileMover.inboxResourcePath)

        if(inboxDirectory.exists() && inboxDirectory.isDirectory) {
            deleteDirectory(inboxDirectory)
        } else {
            inboxDirectory.mkdirs()
        }

        if(outboxDirectory.exists() && outboxDirectory.isDirectory) {
            deleteDirectory(outboxDirectory)
        } else {
            outboxDirectory.mkdirs()
        }

        // Delegate
        super.setUp()
    }

    override fun createRouteBuilder(): RoutesBuilder = this.fileMover

    @Test
    fun validateMovingFiles() {

        // Assemble
        val sentBody = "Hello Camel World!"
        val fileName = "hello.txt"

        val notify = NotifyBuilder(context)
            .whenDone(1)
            .create()

        val targetFile = File(fileMover.outboxResourcePath, fileName);

        // Act
        template.sendBodyAndHeader(
            fileMover.inboxURI,
            sentBody,
            Exchange.FILE_NAME,
            fileName);

        // Assert
        Assert.assertTrue(notify.matchesMockWaitTime());
        Assert.assertTrue("Expected that the file should be moved to $targetFile",
            targetFile.exists() && targetFile.isFile);

        val resultFileContent = context.typeConverter.convertTo(String::class.java, targetFile);
        Assert.assertEquals(sentBody, resultFileContent);
    }

    @Test
    open fun validateMovingOtherFiles() {

        // Assemble
        val sentBody = "Hello Camel World 2!"
        val fileName = "helloOther.txt"

        val notify = NotifyBuilder(context)
            .whenDone(1)
            .create()

        val targetFile = File(fileMover.outboxResourcePath, fileName);

        // Act
        template.sendBodyAndHeader(
            fileMover.inboxURI,
            sentBody,
            Exchange.FILE_NAME,
            fileName);

        // Assert
        Assert.assertTrue(notify.matchesMockWaitTime());
        Assert.assertTrue("Expected that the file should be moved to $targetFile",
            targetFile.exists() && targetFile.isFile);

        val resultFileContent = context.typeConverter.convertTo(String::class.java, targetFile);
        Assert.assertEquals(sentBody, resultFileContent);
    }
}