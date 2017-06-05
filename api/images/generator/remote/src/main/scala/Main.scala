import java.util.UUID

import scala.concurrent.Future
import scala.concurrent.Promise

import de.htwg.zeta.persistence.Persistence
import de.htwg.zeta.server.generator.Result
import de.htwg.zeta.server.generator.Success
import de.htwg.zeta.server.generator.Transformer
import models.document.Filter
import models.document.Generator
import models.document.ModelEntity
import models.file.File
import models.modelDefinitions.model.elements.Edge
import models.modelDefinitions.model.elements.Node
import models.remote.Remote
import org.slf4j.LoggerFactory
import rx.lang.scala.Notification.OnCompleted
import rx.lang.scala.Notification.OnError
import rx.lang.scala.Notification.OnNext

/**
 * Main class of remote generator
 */
object Main extends Template[CreateOptions, RemoteOptions] {

  private val logger = LoggerFactory.getLogger(getClass)

  private val repository = Persistence.fullAccessRepository

  override def createTransformer(options: CreateOptions, imageId: UUID)(implicit remote: Remote): Future[Result] = {
    for {
      image <- repository.generatorImages.read(imageId)
      generator <- repository.generators.create(Generator(user, options.name, image.id))
      created <- repository.files.createVersion("demoFile", createFileContent())
    } yield {
      Success()
    }
  }

  case class MyGenerator(generator: String = "") extends Transformer {

    def transformBasicActorNode(node: Node) = {
      val actorName = node.getAttribute[String]("name")
      val filename = s"${actorName}.scala"
      val content =
        s"""
          |class ${actorName}() extends Actor {
          |   def receive = {
          |
        |   }
          | }
        """

      File(UUID.randomUUID, filename, content)
    }

    def transformPersistentActorNode(node: Node) = "transformPersistentActorNode"

    def transformNode(node: Node) = {
      node.`type`.name match {
        case "BasicActor" => transformBasicActorNode(node)
        case "PersistentActor" => transformPersistentActorNode(node)
      }
    }

    def transform(entity: ModelEntity)(implicit remote: Remote): Future[Transformer] = {
      val p = Promise[Transformer]

      val r1 = remote.call[RemoteOptions, File](generator, RemoteOptions("BasicActor", entity.id))
      val r2 = remote.call[RemoteOptions, File](generator, RemoteOptions("PersistentActor", entity.id))

      val merged = r1.merge(r2)

      merged.materialize.subscribe(n => n match {
        case OnNext(file) => logger.info(file.toString)
        case OnCompleted => p.success(this)
        case OnError(err) => p.failure(err)
      })

      p.future
    }

    def exit()(implicit remote: Remote): Future[Result] = {
      val result = Success("The generator finished")
      Future.successful(result)
    }
  }

  def createFileContent(): File = {
    File(UUID.randomUUID, Settings.generatorFile, "This is a demo of the remote capabilities which doesn't require a template to configure.")
  }

  def compiledGenerator(file: File) = Future.successful(MyGenerator(cmd.generator.getOrElse("")))

  /**
   * Initialize the generator
   *
   * @param file The file which was loaded for the generator
   * @return A Generator
   */
  override def getTransformer(file: File, filter: Filter)(implicit remote: Remote): Future[Transformer] = {
    compiledGenerator(file)
  }

  /**
   * Initialize the generator
   *
   * @param file The file which was loaded for the generator
   * @return A Generator
   */
  override def getTransformer(file: File, model: ModelEntity)(implicit remote: Remote): Future[Transformer] = {
    compiledGenerator(file)
  }

  /**
   * Call the generator (called by another generator) to run with options
   *
   * @return The Result of the generator
   */
  override def runGeneratorWithOptions(options: RemoteOptions)(implicit remote: Remote): Future[Result] = {
    logger.info(s"called with options ${options}")
    remote.emit[File](File(UUID.randomUUID, options.nodeType, "Started and sleep now"))
    Thread.sleep(10000)
    val p = Promise[Result]

    repository.modelEntities.read(options.modelId).map { entity =>
      entity.model.elements.values.foreach {
        case node: Node => if (node.`type`.name == options.nodeType) {
          remote.emit[File](File(UUID.randomUUID, options.nodeType, node.`type`.name))
        }
        case edge: Edge => // ignore
      }
      p.success(Success())
    }
    p.future
  }
}
