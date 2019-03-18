//package example.fp3d
//
//import scalaz.{EitherT, Monad, MonadError}
//
//trait Foo[F[_]] {
//  def bar(i: Int): F[String]
//}
//
//trait Baz[F[_]] {
//  def h1(s: String): F[Char]
//}
//
//class NotthatBadProgram(foo: Foo[EitherT[Task, Throwable, ?]], baz: Baz[EitherT[Task, Throwable, ?]]) {
//  def doo(i: Int) : EitherT[Task, Throwable, Char] =
//    for {
//      s <- foo.bar(i)
//      ctx <- EitherT.right(…)
//      _ <- if (s.length == 0) FE.raiseError(new RuntimeException("Boom!")) else F.pure(())
//      c <- baz.h1(s)
//    } yield c
//}
//
//class NotthatBadProgram[F[_]](foo: Foo[F], baz: Baz[F])(implicit  FE: MonadError[F, Throwable]) {
//  def doo(i: Int) : F[Char] =
//    for {
//      s <- foo.bar(i)
//      _ <- F.pure(…)
//      _ <- if (s.length == 0) FE.raiseError(new RuntimeException("Boom!")) else F.pure(())
//      c <- baz.h1(s)
//    } yield c
//}
//
//class FooApi(foo: Foo[EitherT[ConnectionIO, Throwable, ?]]) {
//  type Http[A] = Nothing
//  val endpoint : Http[Int => String] = foo.bar(request.body.toInt).fold(errorToHttp, _.toJson).unsafePerformSync
//}
//
//class FooService extends Foo[EitherT[ConnectionIO, Throwable, ?]] {
//  // ConnetionIO
//  def bar(i : Int) : EitherT[ConnectionIO, Throwable, String] = i.toString
//}
//
//class FooClient extends Foo[Task] {
//  // Task[Async[…
//  override def bar(i : Int) : Task[String] = httpClient.post(i).decodeResult
//}
