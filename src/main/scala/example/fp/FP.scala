package example.fp

object FP {

  type F[A, B] = A => B

  val foo : F[Int, String] = (a : Int) => a.toString
  val bar : F[String, Int] = (a : String) => a.length

  val baz : F[Int, Int] = foo andThen bar
  val zab : F[String, String] = foo compose bar

  baz(1234) // 4
  zab("barfoo") // "6"

  val hofoo : F[F[Int, String], Int => Int] = (f : F[Int, String]) => (a : Int) => f(a).length

  val unhofoo = hofoo(foo)

  unhofoo(1234) // 4
}
