package example.fp3d

import scalaz.{Applicative, EitherT, Monad, MonadError, MonadListen, MonadPlus, MonadReader, MonadState, MonadTell, Nondeterminism, ReaderT, \/, ~>}
import scalaz.concurrent.Task
import scalaz.Scalaz._

object FP3D {

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

    def mapK[G[_]](f : F ~> G) : BarAlgebra[G] = FunctorK[BarAlgebra].mapK(this)(f)
  }

  object BarAlgebra {
    implicit val functorK : FunctorK[BarAlgebra] = new FunctorK[BarAlgebra] {
      def mapK[F[_], G[_]](hf : BarAlgebra[F])(f : F ~> G) : BarAlgebra[G] = new BarAlgebra[G] {
        def foo(a : Int, b : Int) : G[Int] = f(hf.foo(a, b))

        def bar(a : String, b : String) : G[String] = f(hf.bar(a, b))
      }
    }
  }

  class MTL[M](val M: M) extends AnyVal

  object MTL {
    type MonadMTL[F[_]] <: Monad[F]

    implicit def mtl[M](implicit M: M): MTL[M] = new MTL(M)

    private def MonadMTL[M[f[_]] <: Monad[f], F[_]](M: M[F]): MonadMTL[F] =
      M.asInstanceOf[MonadMTL[F]]

    /** An artificial hierarchy for MTL type classes. */
    object Hierarchy extends Hierarchy

    trait Hierarchy extends Hierarchy0

    sealed private[FP3D] trait Hierarchy0 extends Hierarchy1 {
      implicit def mtlPlus[F[_]](implicit mtl: MTL[MonadPlus[F]]): MonadMTL[F] = MonadMTL(mtl.M)
    }

    sealed private[FP3D] trait Hierarchy1 extends Hierarchy2 {
      implicit def mtlError[F[_], E](implicit mtl: MTL[MonadError[F, E]]): MonadMTL[F] = MonadMTL(mtl.M)
    }

    sealed private[FP3D] trait Hierarchy2 extends Hierarchy3 {
      implicit def mtlState[F[_], S](implicit mtl: MTL[MonadState[F, S]]): MonadMTL[F] = MonadMTL(mtl.M)
    }

    sealed private[FP3D] trait Hierarchy3 extends Hierarchy4 {
      implicit def mtlReader[F[_], R](implicit mtl: MTL[MonadReader[F, R]]): MonadMTL[F] = MonadMTL(mtl.M)
    }

    sealed private[FP3D] trait Hierarchy4 extends Hierarchy5 {
      implicit def mtlListen[F[_], W](implicit mtl: MTL[MonadListen[F, W]]): MonadMTL[F] = MonadMTL(mtl.M)
    }

    sealed private[FP3D] trait Hierarchy5 extends Hierarchy6 {
      implicit def mtlTell[F[_], W](implicit mtl: MTL[MonadTell[F, W]]): MonadMTL[F] = MonadMTL(mtl.M)
    }

    sealed private[FP3D] trait Hierarchy6 extends Hierarchy7 {
      implicit def mtlNonDeterminism[F[_]](implicit mtl: MTL[Nondeterminism[F]]): MonadMTL[F] = MonadMTL(mtl.M)
    }

    sealed private[FP3D] trait Hierarchy7 {
      implicit def monadError[F[_], E](implicit mtl: MTL[MonadError[F, E]]): MonadError[F, E] = mtl.M
      implicit def monadState[F[_], S](implicit mtl: MTL[MonadState[F, S]]): MonadState[F, S] = mtl.M
      implicit def monadReader[F[_], R](implicit mtl: MTL[MonadReader[F, R]]): MonadReader[F, R] = mtl.M
      implicit def monadListen[F[_], W](implicit mtl: MTL[MonadListen[F, W]]): MonadListen[F, W] = mtl.M
      implicit def monadTell[F[_], W](implicit mtl: MTL[MonadTell[F, W]]): MonadTell[F, W] = mtl.M
      implicit def nonDeterminism[F[_]](implicit mtl: MTL[Nondeterminism[F]]): Nondeterminism[F] = mtl.M
    }
  }

  trait Program[F[_]] {
    def run(a : Int) : F[Int]
  }

  class ProgramHK[F[_]](fooAlgebra : FooAlgebra[F], barAlgebra : BarAlgebra[F])(implicit F : MTL[MonadReader[F, Int]], E : MTL[MonadError[F, Throwable]]) extends Program[F] {
    import example.fp3d.FP3D.MTL.Hierarchy._

    def run(a : Int) : F[Int] =
      for {
        a1 <- F.M.ask
        b <- barAlgebra.foo(a, a1)
        c <- fooAlgebra.foo(b)
        d <- barAlgebra.bar(c, c)
        e <- fooAlgebra.bar(d)
      } yield e
  }

  class FooService[F[_]](implicit F : Applicative[F]) extends FooAlgebra[F] {
    def foo(a : Int) : F[String] = F.point(a.toString)

    def bar(a : String) : F[Int] = F.point(a.length)
  }

  type ErrorContext[A] = EitherT[Task, Throwable, A]
  type ReaderContext[A] = ReaderT[ErrorContext, Int, A]

  class BarService[F[_]](implicit F : MonadError[F, Throwable]) extends BarAlgebra[F] {
    def foo(a : Int, b : Int) : F[Int] =
      \/.fromTryCatchNonFatal(a / b * 2).fold(
        F.raiseError[Int],
        i => F.point[Int](i)
      )

    //EitherT.fromTryCatchNonFatal[Task, Int](Task.point((a / (b / 2)).toInt))

    def bar(a : String, b : String) : F[String] =
      if (a.startsWith(b))
        F.raiseError(new Exception("Nope"))
      else
        F.point(b)
  }

  val reading = new (ErrorContext ~> ReaderContext) {
    def apply[A](fa : ErrorContext[A]) : ReaderContext[A] = ReaderT(_ => fa)
  }

  val attempting = new (Task ~> ErrorContext) {
    def apply[A](fa : Task[A]) : ErrorContext[A] = EitherT(fa.attempt)
  }

  val barService : BarAlgebra[ReaderContext] = new BarService[ReaderContext]
  val fooService : FooAlgebra[ReaderContext] = new FooService[ReaderContext]

  import MTL._

  val program = new ProgramHK[ReaderContext](fooService, barService)
}
