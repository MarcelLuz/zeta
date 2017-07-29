package experimental

object StartGenerator extends App {

  val files = Generator.generate(PetriNetMetaModelFixture.metaModel, PetriNetModelFixture.modelEntity)

  files.foreach { file =>
    println(file.name)
    println("------------------------------------------------------------------------------------")
    println(file.content)
    println("------------------------------------------------------------------------------------")
    println()
  }

}
