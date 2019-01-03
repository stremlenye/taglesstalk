package example.fp3d

import scalaz.{EitherT, Monad, MonadError, \/}
import scalaz.concurrent.Task

class FP3D {

  trait FooAlgebra[F[_]] {
    def foo(a : Int) : F[String]

    def bar(a : String) : F[Int]
  }

  trait BarAlgebra[F[_]] {
    def foo(a : Int, b : Int) : F[Int]

    def bar(a : String, b : String) : F[String]
  }

  trait Program[F[_]] {
    def run(a : Int) : F[Int]
  }

  class ProgramAsync(fooAlgebra : FooAlgebra[Task], barAlgebra : BarAlgebra[Task]) extends Program[Task] {
    def run(a : Int) : Task[Int] =
      for {
        b <- barAlgebra.foo(a, a)
        c <- fooAlgebra.foo(b)
        d <- barAlgebra.bar(c, c)
        e <- fooAlgebra(d)
      } yield e
  }

  class ProgramHK[F[_]](fooAlgebra : FooAlgebra[F], barAlgebra : BarAlgebra[F])(implicit F : Monad[F]) extends Program[F] {
    def run(a : Int) : F[Int] =
      for {
        b <- barAlgebra.foo(a, a)
        c <- fooAlgebra.foo(b)
        d <- barAlgebra.bar(c, c)
        e <- fooAlgebra(d)
      } yield e
  }

  class FooService extends FooAlgebra[Task] {
    def foo(a : Int) : Task[String] = Task.point(a.toString)

    def bar(a : String) : Task[Int] = Task.point(a.length)
  }

  type ErrorContext[A] = EitherT[Task, Throwable, A]

  class SafeBarService extends BarAlgebra[ErrorContext] {
    def foo(a : Int, b : Int) : ErrorContext[Int] =
      \/.fromTryCatchNonFatal(a / b * 2).fold(
        MonadError[ErrorContext, Throwable].raiseError,
        Monad[ErrorContext].point
      )

    //EitherT.fromTryCatchNonFatal[Task, Int](Task.point((a / (b / 2)).toInt))

    def bar(a : String, b : String) : ErrorContext[String] =
      if (a.startsWith(b))
        MonadError[ErrorContext, Throwable].raiseError(new Exception("Nope"))
      else
        Monad[ErrorContext].point(b)
  }

  val fooService = new FooService
  val barService = new SafeBarService

  val program = new ProgramHK[???](fooService, barService)
}
