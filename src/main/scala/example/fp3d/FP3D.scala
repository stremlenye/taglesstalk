package example.fp3d

import scalaz.{EitherT, Monad, MonadError, \/, ~>}
import scalaz.concurrent.Task

class FP3D {

  trait FunctorK[H[_[_]]] {
    def mapK[F[_], G[_]](hf : H[F])(f : F ~> G) : H[G]
  }

  object FunctorK {
    def apply[H[_[_]]](implicit H : FunctorK[H]) : FunctorK[H] = H
  }

  trait FooAlgebra[F[_]] {
    def foo(a : Int) : F[String]

    def bar(a : String) : F[Int]

    def mapK[G[_]](f : F ~> G) : FooAlgebra[G] = FunctorK[FooAlgebra].mapK(this)(f)
  }

  object FooAlgebra {
    implicit val functorK : FunctorK[FooAlgebra] = new FunctorK[FooAlgebra] {
      def mapK[F[_], G[_]](hf : FooAlgebra[F])(f : F ~> G) : FooAlgebra[G] = new FooAlgebra[G] {
        def foo(a : Int) : G[String] = f(hf.foo(a))

        def bar(a : String) : G[Int] = f(hf.bar(a))
      }
    }
  }

  trait BarAlgebra[F[_]] {
    def foo(a : Int, b : Int) : F[Int]

    def bar(a : String, b : String) : F[String]
  }

  object BarAlgebra {
    implicit val functorK : FunctorK[BarAlgebra] = new FunctorK[BarAlgebra] {
      def mapK[F[_], G[_]](hf : BarAlgebra[F])(f : F ~> G) : BarAlgebra[G] = new BarAlgebra[G] {
        def foo(a : Int, b : Int) : G[Int] = f(hf.foo(a, b))

        def bar(a : String, b : String) : G[String] = f(hf.bar(a, b))
      }
    }
  }

  trait Program[F[_]] {
    def run(a : Int) : F[Int]
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

  val barService : BarAlgebra[ErrorContext] = new SafeBarService
  val fooService : FooAlgebra[ErrorContext] = (new FooService).mapK(new (Task ~> ErrorContext) {
    def apply[A](fa : Task[A]) : ErrorContext[A] = EitherT(fa.attempt)
  })

  val program = new ProgramHK[ErrorContext](fooService, barService)
}
