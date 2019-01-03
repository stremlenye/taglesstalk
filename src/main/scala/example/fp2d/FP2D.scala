package example.fp2d

import scalaz._
import scalaz.concurrent.Task

object FP2D {
  type K[M[_], A, B] = A => M[B]

  type F[M[_], A, B] = Kleisli[M, A, B] // Kleisli[M[_], A, B](run: A => M[B])

  type M[A] = Task[A]

  val foo : F[Task, Int, String] = Kleisli((a : Int) => Task.point(a.toString))
  val bar : F[Task, String, Int] = Kleisli((a : String) => Task.point(a.length))

  val baz : F[Task, Int, Int] = foo andThen bar
  val zab : F[Task, String, String] = foo compose bar

  baz(1234) // Task(4)
  zab("barfoo") // Task("6")

  val baz2 : F[Task, Int, Int] = (a : Int) => for {
    b <- foo(a)
    c <- bar(b)
  } yield c

  val zab2 : F[Task, String, String] = (a : String) => for {
    b <- bar(a)
    c <- foo(b)
  } yield c

  baz2(1234) // Task(4)
  zab2("barfoo") // Task("6")
}
