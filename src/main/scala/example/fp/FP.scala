package example.fp

object FP {

  type F[A, B] = A => B

  val fa : F[Int, String] = (a : Int) => a.toString
  val bar : F[String, Int] = (a : String) => a.length

  val baz : F[Int, Int] = fa andThen bar
  val zab : F[String, String] = fa compose bar

  baz(1234) // 4
  zab("barfoo") // "6"

  val hofoo : F[F[Int, String], Int => Int] = (f : F[Int, String]) => (a : Int) => f(a).length

  val unhofoo = hofoo(fa)

  unhofoo(1234) // 4
}
